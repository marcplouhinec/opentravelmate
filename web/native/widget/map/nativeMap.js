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
    'native/widget/map/CustomInfoWindow'
], function($, stringUtils, google, InfoBox, TileObserver, MarkerRTree, projectionUtils, CustomInfoWindow) {
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
                'mapTypeId': google.maps.MapTypeId.ROADMAP
            });
            gmapByPlaceHolderId[layoutParams.id] = gmap;
            markerRTreeByPlaceHolderId[layoutParams.id] = new MarkerRTree();
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
         * Add a marker on the map.
         *
         * @param {String} id
         *     Map place holder ID.
         * @param {String} jsonMarker
         *     JSON serialized Marker.
         */
        'addMarker': function(id, jsonMarker) {
            var marker = JSON.parse(jsonMarker);
            var gmap = gmapByPlaceHolderId[id];

            // Prepare a new Google Maps Marker
            var markerOptions = {
                position: new google.maps.LatLng(marker.position.lat, marker.position.lng),
                title: marker.title,
                clickable: false
            };

            // Handle UrlMarkerIcon if necessary
            if (marker.icon && marker.icon.url) {
                var urlMarkerIcon = marker.icon;
                markerOptions.icon = {
                    url: urlMarkerIcon.url,
                    scaledSize: new google.maps.Size(urlMarkerIcon.size.width, urlMarkerIcon.size.height),
                    anchor: new google.maps.Point(urlMarkerIcon.anchor.x, urlMarkerIcon.anchor.y)
                };
            }

            // Handle SvgPathMarkerIcon if necessary
            // TODO

            // Create and register the Google Maps Marker
            var gmarker = new google.maps.Marker(markerOptions);
            gmarkerById[marker.id] = gmarker;
            gmarker.setMap(gmap);

            markerRTreeByPlaceHolderId[id].addMarker(marker);
        },

        /**
         * Remove a marker from the map.
         *
         * @param {String} id
         *     Map place holder ID.
         * @param {String} jsonMarker
         *     JSON serialized Marker.
         */
        'removeMarker': function(id, jsonMarker) {
            var marker = JSON.parse(jsonMarker);
            var gmarker = gmarkerById[marker.id];
            delete gmarkerById[marker.id];

            markerRTreeByPlaceHolderId[id].removeMarker(marker);

            gmarker.setMap(null);
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
         */
        'showInfoWindow': function(id, jsonMarker, content) {
            var marker = JSON.parse(jsonMarker);
            var gmap = gmapByPlaceHolderId[id];
            var gmarker = gmarkerById[marker.id];

            // Close the current displayed info window if any
            var infoWindow = infoWindowByPlaceHolderId[id];
            if (infoWindow) {
                infoWindow.close();
            }

            // Create a new info window
            var infoWindow = new CustomInfoWindow(content);
            infoWindow.open(gmap, gmarker);
            infoWindow.onClick(function() {
                require(['extensions/core/widget/Widget'], function (Widget) {
                    var map = /** @type {Map} */ Widget.findById(id);
                    map.fireInfoWindowClickEvent(marker);
                });
            });
            infoWindowByPlaceHolderId[id] = infoWindow;
        }
    };

    return nativeMap;
});
