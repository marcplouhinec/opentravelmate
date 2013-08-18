/**
 * Web implementation of the nativeMap interface.
 *
 * @author marc.plouhinec@gmail.com (Marc Plouhinec)
 */

define([
    'jquery', 'native/widget/map/google', 'native/widget/map/TileObserver'
], function($, google, TileObserver) {
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

            var gmarker = new google.maps.Marker({
                position: new google.maps.LatLng(marker.position.lat, marker.position.lng),
                title: marker.title
            });
            if (marker.anchorPoint) {
                gmarker.setOptions({
                    anchorPoint: new google.maps.Point(marker.anchorPoint.x, marker.anchorPoint.y)
                });
            }

            gmarkerById[marker.id] = gmarker;
            gmarker.setMap(gmap);
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
         * @return {Array.<{zoom: Number, x: Number, y: Number}>}
         */
        'getDisplayedTileCoordinates': function(id) {
            var tileObserver = tileObserverByPlaceHolderId[id];
            return tileObserver ? tileObserver.getDisplayedTileCoordinates() : null;
        }
    };

    return nativeMap;
});
