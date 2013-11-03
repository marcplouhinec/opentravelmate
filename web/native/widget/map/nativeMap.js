/**
 * Web implementation of the nativeMap interface.
 *
 * @author marc.plouhinec@gmail.com (Marc Plouhinec)
 */

define([
    'jquery',
    'extensions/core/utils/stringUtils',
    'native/widget/map/google',
    'native/widget/map/InfoBox',
    'native/widget/map/TileObserver',
    'native/widget/map/MarkerRTree',
    'extensions/core/widget/map/projectionUtils',
    'native/widget/map/CustomInfoWindow',
    'native/widget/map/MapButtonController'
], function($, stringUtils, google, InfoBox, TileObserver, MarkerRTree, projectionUtils, CustomInfoWindow, MapButtonController) {
    'use strict';
    
    /**
	 * @constant
	 * @type {Number}
	 */
	var DEFAULT_ZOOM = 13;
	/**
	 * @constant
	 * @type {Number}
	 */
	var DEFAULT_LATITUDE = 49.61;
	/**
	 * @constant
	 * @type {Number}
	 */
	var DEFAULT_LONGITUDE = 6.131;

    /**
     * @constant
     * @type {Number}
     */
    var TILE_EVENT_MAPTYPE_INDEX = 42;

    /**
     * @constant
     * @type {number}
     */
    var MAX_MOUSE_MARKER_DISTANCE = 17;

    /**
     * @constant
     * @type {{width: number, height: number}}
     */
    var DEFAULT_MARKER_SIZE = {
        width: 22,
        height: 40
    };

    /**
     * @constant
     * @type {{x: number, y: number}}
     */
    var DEFAULT_MARKER_ANCHOR = {
        x: 11,
        y: 40
    };

    /**
     * @type {Object.<String, google.maps.Map>}
     */
    var gmapByPlaceHolderId = {};

    /**
     * @type {Object.<Number, google.maps.Marker>}
     */
    var gmarkerById = {};

    /**
     * @type {Object.<String, TileObserver>}
     */
    var tileObserverByPlaceHolderId = {};

    /**
     * @type {Object.<String, MarkerRTree>}
     */
    var markerRTreeByPlaceHolderId = {};

    /**
     * @type {Object.<String, Boolean>}
     */
    var markerObservingStateByPlaceHolderId = {};

    /**
     * @type {Object.<String, Object>}
     */
    var markerUnderMouseByPlaceHolderId = {};

    /**
     * @type {Object.<String, CustomInfoWindow>}
     */
    var infoWindowByPlaceHolderId = {};

    /**
     * @type {Object.<String, MapButtonController>}
     */
    var mapButtonControllerByPlaceHolderId = {};

    /**
     * Google Polylines by Polyline IDs.
     *
     * @type {Array}
     */
    var gpolylineById = [];

    /**
     * Google Polygon by Polygon IDs.
     *
     * @type {Array}
     */
    var gpolygonById = [];


    var nativeMap = {
        /**
         * Build the native view object for the current widget.
         * 
         * @param {String} jsonLayoutParams JSON-serialized LayoutParams
         */
        'buildView': function(jsonLayoutParams) {
			var layoutParams = JSON.parse(jsonLayoutParams),
                baseUrl = layoutParams.additionalParameters['baseUrl'];
			
			// Create the div that will contain the map
			var mapCanvas = document.createElement('div');
			mapCanvas.id = layoutParams.id + '-canvas';
			mapCanvas.style.position = 'absolute';
			mapCanvas.style.left = layoutParams.x + 'px';
			mapCanvas.style.top = layoutParams.y + 'px';
			mapCanvas.style.width = layoutParams.width + 'px';
			mapCanvas.style.height = layoutParams.height + 'px';
			mapCanvas.style.visibility = layoutParams.visible ? 'visible' : 'hidden';
			document.body.appendChild(mapCanvas);
			
			// Initialize the map
            var gmap = new google.maps.Map(mapCanvas, {
                'zoom': DEFAULT_ZOOM,
                'center': new google.maps.LatLng(DEFAULT_LATITUDE, DEFAULT_LONGITUDE),
                'mapTypeId': google.maps.MapTypeId.ROADMAP,
                'mapTypeControl': false
            });
            gmapByPlaceHolderId[layoutParams.id] = gmap;
            markerRTreeByPlaceHolderId[layoutParams.id] = new MarkerRTree(DEFAULT_MARKER_SIZE, DEFAULT_MARKER_ANCHOR);
            mapButtonControllerByPlaceHolderId[layoutParams.id] = new MapButtonController(gmap, layoutParams.id, baseUrl);
            mapButtonControllerByPlaceHolderId[layoutParams.id].onButtonClick(function(mapButton) {
                require(['extensions/core/widget/Widget'], function (Widget) {
                    var map = /** @type {Map} */ Widget.findById(layoutParams.id);
                    map.fireMapButtonClickEvent(mapButton.id);
                });
            });
        },

        /**
         * Update the native view object for the current widget.
         *
         * @param {String} jsonLayoutParams JSON-serialized LayoutParams
         */
        'updateView': function(jsonLayoutParams) {
            var layoutParams = JSON.parse(jsonLayoutParams);

            var mapCanvas = document.getElementById(layoutParams.id + '-canvas');
            mapCanvas.style.left = layoutParams.x + 'px';
            mapCanvas.style.top = layoutParams.y + 'px';
            mapCanvas.style.width = layoutParams.width + 'px';
            mapCanvas.style.height = layoutParams.height + 'px';
            mapCanvas.style.visibility = layoutParams.visible ? 'visible' : 'hidden';
        },

        /**
         * Remove the native view object for the current widget.
         *
         * @param {String} id Place holder ID
         */
        'removeView': function(id) {
            $(id + '-canvas').remove();

            delete markerRTreeByPlaceHolderId[id];
        },

        /**
         * Add an overlay to the map.
         *
         * @param {String} id
         *     Map place holder ID.
         * @param {String} jsonTileOverlay
         *     JSON serialized TileOverlay.
         */
        'addTileOverlay': function(id, jsonTileOverlay) {
            var tileOverlay = JSON.parse(jsonTileOverlay);
            var gmap = gmapByPlaceHolderId[id];

            gmap.overlayMapTypes.insertAt(tileOverlay.zIndex, {
                tileSize: new google.maps.Size(256, 256),

                /**
                 * This object implements the interface google.maps.MapType in order to determine which waypoints to loads.
                 *
                 * @see https://developers.google.com/maps/documentation/javascript/maptypes
                 *
                 * @param {{x: Number, y: Number}} coord
                 * @param {Number} zoom
                 * @param {HTMLDocument} ownerDocument
                 * @return {HTMLElement}
                 */
                getTile: function (coord, zoom, ownerDocument) {
                    var tileUrl = tileOverlay.tileUrlPattern
                        .replace('${zoom}', zoom)
                        .replace('${x}', coord.x)
                        .replace('${y}', coord.y);

                    // Create a div block with the tile picture as a background picture (the web browser will load it automatically)
                    var divTile = /** @type {HTMLElement} */ ownerDocument.createElement('DIV');
                    divTile.setAttribute('id', zoom + '_' + coord.x + '_' + coord.y);
                    divTile.style.width = '256px';
                    divTile.style.height = '256px';
                    divTile.style.backgroundImage = 'url(' + tileUrl + ')';
                    divTile.style.backgroundSize = '256px 256px';
                    // divTile.style.border = 'solid red 1px';
                    // divTile.appendChild(document.createTextNode('(' + zoom + ', ' + coord.x + ', ' + coord.y +')'));
                    return divTile;
                }
            });
        },

        /**
         * Move the map center to the given location.
         *
         * @param {String} id
         *     Map place holder ID.
         * @param {String} jsonCenter
         *     JSON serialized LatLng.
         */
        'panTo': function(id, jsonCenter) {
            var center = JSON.parse(jsonCenter);

            var gmap = gmapByPlaceHolderId[id];
            gmap.panTo(new google.maps.LatLng(center.lat, center.lng));
        },

        /**
         * Get the map bounds (South-West and North-East points).
         *
         * @param {String} id
         *     Map place holder ID.
         * @return {String} jsonBounds
         *     JSON serialized {sw: LatLng, ne: LatLng}.
         */
        'getBounds': function(id) {
            var gmap = gmapByPlaceHolderId[id];
            var gBounds = gmap.getBounds();
            return JSON.stringify({sw: {
                lat: gBounds.getSouthWest().lat(),
                lng: gBounds.getSouthWest().lng()
            }, ne: {
                lat: gBounds.getNorthEast().lat(),
                lng: gBounds.getNorthEast().lng()
            }});
        },

        /**
         * Add markers on the map.
         *
         * @param {String} id
         *     Map place holder ID.
         * @param {String} jsonMarkers
         *     JSON serialized array of markers.
         */
        'addMarkers': function(id, jsonMarkers) {
            var markers = /** @type {Array} */ JSON.parse(jsonMarkers);
            var gmap = gmapByPlaceHolderId[id];

            for (var i = 0; i < markers.length; i += 1) {
                var marker = markers[i];

                // Prepare a new Google Maps Marker
                var markerOptions = {
                    position: new google.maps.LatLng(marker.position.lat, marker.position.lng),
                    title: marker.title,
                    clickable: false
                };

                // Handle the marker icon if applicable
                if (marker.icon) {
                    if (marker.icon.url) {
                        // UrlMarkerIcon
                        var urlMarkerIcon = marker.icon;
                        markerOptions.icon = {
                            url: urlMarkerIcon.url,
                            scaledSize: new google.maps.Size(urlMarkerIcon.size.width, urlMarkerIcon.size.height),
                            anchor: new google.maps.Point(urlMarkerIcon.anchor.x, urlMarkerIcon.anchor.y)
                        };
                    } else if (marker.icon.path) {
                        // VectorMarkerIcon
                        markerOptions.icon = {
                            fillColor: marker.icon.fillColor,
                            fillOpacity: marker.icon.fillOpacity,
                            path: marker.icon.path,
                            rotation: marker.icon.rotation,
                            scale: marker.icon.scale,
                            strokeColor: marker.icon.strokeColor,
                            strokeOpacity: marker.icon.strokeOpacity,
                            strokeWeight: marker.icon.strokeWidth,
                            anchor: new google.maps.Point(marker.icon.anchor.x, marker.icon.anchor.y)
                        };
                    }
                }

                // Create and register the Google Maps Marker
                var gmarker = new google.maps.Marker(markerOptions);
                gmarkerById[marker.id] = gmarker;
                gmarker.setMap(gmap);

                markerRTreeByPlaceHolderId[id].addMarker(marker);
            }
        },

        /**
         * Remove markers from the map.
         *
         * @param {String} id
         *     Map place holder ID.
         * @param {String} jsonMarkers
         *     JSON serialized array of markers.
         */
        'removeMarkers': function(id, jsonMarkers) {
            var markers = /** @type {Array} */ JSON.parse(jsonMarkers);

            for (var i = 0; i < markers.length; i += 1) {
                var marker = markers[i];
                var gmarker = gmarkerById[marker.id];
                if (gmarker) {
                    delete gmarkerById[marker.id];

                    markerRTreeByPlaceHolderId[id].removeMarker(marker);

                    gmarker.setMap(null);
                }
            }
        },

        /**
         * Add a button on the map top-right corner.
         *
         * @param {String} id
         *     Map place holder ID.
         * @param {String} jsonMapButton
         *     JSON serialized MapButton.
         */
        'addMapButton': function(id, jsonMapButton) {
            mapButtonControllerByPlaceHolderId[id].addButton(JSON.parse(jsonMapButton));
        },

        /**
         * Update a button on the map.
         *
         * @param {String} id
         *     Map place holder ID.
         * @param {String} jsonMapButton
         *     JSON serialized MapButton.
         */
        'updateMapButton': function(id, jsonMapButton) {
            mapButtonControllerByPlaceHolderId[id].updateButton(JSON.parse(jsonMapButton));
        },

        /**
         * Start observing tiles and forward the TILES_DISPLAYED and TILES_RELEASED events to the
         * map defined by the given place-holder ID.
         * Note: this function does nothing if the tiles are already observed.
         *
         * @param {String} id
         *     Map place holder ID.
         */
        'observeTiles': function(id) {
            var tileObserver = tileObserverByPlaceHolderId[id];

            // Create the TileObserver if necessary
            if (!tileObserver) {
                var gmap = gmapByPlaceHolderId[id];
                tileObserver = new TileObserver(gmap);
                tileObserverByPlaceHolderId[id] = tileObserver;

                require(['extensions/core/widget/Widget'], function (Widget) {
                    var map = /** @type {Map} */ Widget.findById(id);

                    tileObserver.onTilesDisplayed(function(tileCoordinates) {
                        map.fireTileEvent('TILES_DISPLAYED', tileCoordinates);
                    });

                    tileObserver.onTilesReleased(function(tileCoordinates) {
                        map.fireTileEvent('TILES_RELEASED', tileCoordinates);
                    });
                });
            }
        },

        /**
         * Get all the visible tile coordinates.
         * Note: the function observeTiles() must be called before executing this one.
         *
         * @param {String} id
         *     Map place holder ID.
         * @return {String} JSON-serialized Array.<{zoom: Number, x: Number, y: Number}>
         */
        'getDisplayedTileCoordinates': function(id) {
            var tileObserver = tileObserverByPlaceHolderId[id];
            return JSON.stringify(tileObserver ? tileObserver.getDisplayedTileCoordinates() : null);
        },

        /**
         * Start observing markers and forward the CLICK, MOUSE_ENTER and MOUSE_LEAVE events to the
         * map defined by the given place-holder ID.
         * Note: this function does nothing if the markers are already observed.
         *
         * @param {String} id
         *     Map place holder ID.
         */
        'observeMarkers': function (id) {
            var self = this;

            if (!markerObservingStateByPlaceHolderId[id]) {
                markerObservingStateByPlaceHolderId[id] = true;
                var gmap = gmapByPlaceHolderId[id];

                google.maps.event.addListener(gmap, 'click', function (event) {
                    self._handleMouseClick(id, gmap, event);
                });
                google.maps.event.addListener(gmap, 'mousemove', function (event) {
                    self._handleMouseMove(id, gmap, event);
                });
            }
        },

        /**
         * Handle a mouse click event.
         *
         * @param {String} id
         *     Map place holder ID.
         * @param gmap
         *     Google Map object.
         * @param event
         *     Google Maps event.
         */
        '_handleMouseClick': function(id, gmap, event) {
            var zoom = gmap.getZoom();
            var x = projectionUtils.lngToTileX(zoom, event.latLng.lng());
            var y = projectionUtils.latToTileY(zoom, event.latLng.lat());
            var markerWithDistance = markerRTreeByPlaceHolderId[id].getNearestMarkerWithDistance(zoom, x, y);

            // Call the click listeners if the distance is low enough
            if (markerWithDistance && markerWithDistance.distance <= MAX_MOUSE_MARKER_DISTANCE) {
                require(['extensions/core/widget/Widget'], function (Widget) {
                    var map = /** @type {Map} */ Widget.findById(id);
                    map.fireMarkerEvent('CLICK', markerWithDistance.marker);
                });
            }

            // Close the current displayed info window if any
            var infoWindow = infoWindowByPlaceHolderId[id];
            if (infoWindow) {
                infoWindow.close();
            }
        },

        /**
         * Handle a mouse move event.
         *
         * @param {String} id
         *     Map place holder ID.
         * @param gmap
         *     Google Map object.
         * @param event
         *     Google Maps event.
         */
        '_handleMouseMove': function(id, gmap, event) {
            var zoom = gmap.getZoom();
            var x = projectionUtils.lngToTileX(zoom, event.latLng.lng());
            var y = projectionUtils.latToTileY(zoom, event.latLng.lat());
            var markerWithDistance = markerRTreeByPlaceHolderId[id].getNearestMarkerWithDistance(zoom, x, y);
            var markerUnderMouse = markerUnderMouseByPlaceHolderId[id];

            if (markerWithDistance && markerWithDistance.distance <= MAX_MOUSE_MARKER_DISTANCE) {
                // Fire a MOUSE_LEAVE event if necessary
                if (markerUnderMouse && markerUnderMouse.id !== markerWithDistance.marker.id) {
                    require(['extensions/core/widget/Widget'], function (Widget) {
                        var map = /** @type {Map} */ Widget.findById(id);
                        map.fireMarkerEvent('MOUSE_LEAVE', markerUnderMouse);
                    });
                }

                // Fire a MOUSE_ENTER event if necessary
                if (!markerUnderMouse || markerUnderMouse.id !== markerWithDistance.marker.id) {
                    markerUnderMouse = markerWithDistance.marker;
                    markerUnderMouseByPlaceHolderId[id] = markerUnderMouse;
                    require(['extensions/core/widget/Widget'], function (Widget) {
                        var map = /** @type {Map} */ Widget.findById(id);
                        map.fireMarkerEvent('MOUSE_ENTER', markerUnderMouse);
                    });
                }

                // Set the mouse cursor to 'pointer'
                gmap.setOptions({draggableCursor: 'pointer'});
            } else {
                // Send a MOUSE_LEAVE event if necessary
                if (markerUnderMouse) {
                    delete markerUnderMouseByPlaceHolderId[id];

                    require(['extensions/core/widget/Widget'], function (Widget) {
                        var map = /** @type {Map} */ Widget.findById(id);
                        map.fireMarkerEvent('MOUSE_LEAVE', markerUnderMouse);
                    });
                }

                // Set the mouse cursor to 'default'
                gmap.setOptions({draggableCursor: null});
            }
        },

        /**
         * Show the given text in an Info Window on top of the given marker.
         *
         * @param {String} id
         *     Map place holder ID.
         * @param {String} jsonMarker
         *     JSON-serialized marker where to set the Info Window anchor.
         * @param content
         *     Text displayed in the Info Window.
         * @param {String} jsonAnchor
         *     JSON-serialized position of the the InfoWindow-base compared to the marker position.
         *     Examples:
         *       - (0,0) is the marker position.
         *       - (0,1) is on the under of the marker position.
         *       - (-1,0) is on the left of the marker position.
         */
        'showInfoWindow': function(id, jsonMarker, content, jsonAnchor) {
            var marker = JSON.parse(jsonMarker);
            var anchor = /** @type {{x: Number, y: Number}|null} */ JSON.parse(jsonAnchor);
            var gmap = gmapByPlaceHolderId[id];
            var gmarker = gmarkerById[marker.id];

            // Close the current displayed info window if any
            this.closeInfoWindow(id);

            // Compute the default anchor
            if (!anchor) {
                var markerSize = marker.icon ? marker.icon.size : DEFAULT_MARKER_SIZE;
                var markerAnchor = marker.icon ? marker.icon.anchor : DEFAULT_MARKER_ANCHOR;
                anchor = {
                    x: markerSize.width / 2 - markerAnchor.x,
                    y: -markerAnchor.y
                };
            }

            // Create a new info window
            var infoWindow = new CustomInfoWindow(content, anchor);
            infoWindow.open(gmap, gmarker);
            infoWindow.onClick(function() {
                require(['extensions/core/widget/Widget'], function (Widget) {
                    var map = /** @type {Map} */ Widget.findById(id);
                    map.fireInfoWindowClickEvent(marker);
                });
            });
            infoWindowByPlaceHolderId[id] = infoWindow;
        },

        /**
         * Close the Info Window if any.
         *
         * @param {String} id
         *     Map place holder ID.
         */
        'closeInfoWindow': function(id) {
            var infoWindow = infoWindowByPlaceHolderId[id];
            if (infoWindow) {
                infoWindow.close();
                delete infoWindowByPlaceHolderId[id];
            }
        },

        /**
         * Set the map type.
         *
         * @param {String} id
         *     Map place holder ID.
         * @param {String} mapType
         *     'ROADMAP', 'HYBRID' or 'SATELLITE'.
         */
        'setMapType': function(id, mapType) {
            var gmap = gmapByPlaceHolderId[id];
            var gmapType;
            switch (mapType) {
                case 'ROADMAP':
                    gmapType = google.maps.MapTypeId.ROADMAP;
                    break;
                case 'HYBRID':
                    gmapType = google.maps.MapTypeId.HYBRID;
                    break;
                default:
                    gmapType = google.maps.MapTypeId.SATELLITE;
                    break;
            }
            gmap.setOptions({
                mapTypeId: gmapType
            });
        },

        /**
         * Add the given polyline on the map.
         *
         * @param {String} id
         *     Map place holder ID.
         * @param {String} jsonPolyline
         *     Polyline to add.
         */
        'addPolyline': function(id, jsonPolyline) {
            var gmap = gmapByPlaceHolderId[id];
            var polyline = JSON.parse(jsonPolyline);

            // Convert the path
            var gpath = [];
            for (var i = 0; i < polyline.path.length; i++) {
                var latLng = polyline.path[i];
                gpath.push(new google.maps.LatLng(latLng.lat, latLng.lng));
            }

            // Convert the color
            var decomposedColor = this._decomposeColor(polyline.color);
            var strokeOpacity = decomposedColor.opacity / 0xFF;
            var strokeColor = 'rgb(' + decomposedColor.red + ', ' + decomposedColor.green + ', ' + decomposedColor.blue + ')';

            // Create and show the poly line
            var gpolyline = new google.maps.Polyline({
                path: gpath,
                strokeColor: strokeColor,
                strokeOpacity: strokeOpacity,
                strokeWeight: polyline.width,
                clickable: false
            });
            gpolyline.setMap(gmap);
            gpolylineById[polyline.id] = gpolyline;
        },

        /**
         * Remove the given polyline from the map.
         *
         * @param {String} id
         *     Map place holder ID.
         * @param {String} jsonPolyline
         *     Polyline to remove.
         */
        'removePolyline': function(id, jsonPolyline) {
            var polyline = JSON.parse(jsonPolyline);

            var gpolyline = gpolylineById[polyline.id];
            if (gpolyline) {
                gpolyline.setMap(null);
                delete gpolylineById[polyline.id];
            }
        },

        /**
         * Add the given polygons on the map.
         *
         * @param {String} id
         *     Map place holder ID.
         * @param {String} jsonPolygons
         *     Polygons to add.
         */
        'addPolygons': function(id, jsonPolygons) {
            var gmap = gmapByPlaceHolderId[id];
            var polygons = JSON.parse(jsonPolygons);

            for (var p = 0; p < polygons.length; p++) {
                var polygon = polygons[p];

                // Convert the path
                var gpath = [];
                for (var i = 0; i < polygon.path.length; i++) {
                    var latLng = polygon.path[i];
                    gpath.push(new google.maps.LatLng(latLng.lat, latLng.lng));
                }

                // Convert the fill and stroke color
                var decomposedColor = this._decomposeColor(polygon.strokeColor);
                var strokeOpacity = decomposedColor.opacity / 0xFF;
                var strokeColor = 'rgb(' + decomposedColor.red + ', ' + decomposedColor.green + ', ' + decomposedColor.blue + ')';
                decomposedColor = this._decomposeColor(polygon.fillColor);
                var fillOpacity = decomposedColor.opacity / 0xFF;
                var fillColor = 'rgb(' + decomposedColor.red + ', ' + decomposedColor.green + ', ' + decomposedColor.blue + ')';

                // Create and show the polygon
                var gpolygon = new google.maps.Polygon({
                    path: gpath,
                    fillColor: fillColor,
                    fillOpacity: fillOpacity,
                    strokeColor: strokeColor,
                    strokeOpacity: strokeOpacity,
                    strokeWeight: polygon.strokeWidth,
                    clickable: false
                });
                gpolygon.setMap(gmap);
                gpolygonById[polygon.id] = gpolygon;
            }
        },

        /**
         * Remove the given polygons from the map.
         *
         * @param {String} id
         *     Map place holder ID.
         * @param {String} jsonPolygons
         *     Polygons to remove.
         */
        'removePolygons': function(id, jsonPolygons) {
            var polygons = JSON.parse(jsonPolygons);

            for (var p = 0; p < polygons.length; p++) {
                var polygon = polygons[p];
                var gpolygon = gpolygonById[polygon.id];
                if (gpolygon) {
                    gpolygon.setMap(null);
                    delete gpolygonById[polygon.id];
                }
            }
        },

        /**
         * Decompose a color in the format 0xOORRGGBB where
         * OO is the opacity (FF = opaque, 00 = transparent),
         * RR, GG, BB are the red, green and blue colors.
         *
         * @private
         * @param {Number} color
         * @return {{
         *     opacity: Number,
         *     red: Number,
         *     green: Number,
         *     blue: Number
         * }} decomposed color (0 <= opacity <= 1, 0 <= red | green | blue <= 255)
         */
        '_decomposeColor': function(color) {
            return {
                opacity: Math.round( color / 0x00FFFFFF),
                red:     Math.round((color & 0x00FF0000) / 0xFFFF),
                green:   Math.round((color & 0x0000FF00) / 0xFF),
                blue:    Math.round( color & 0x000000FF)
            };
        }
    };

    return nativeMap;
});
