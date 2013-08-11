/**
 * Web implementation of the nativeMap interface.
 *
 * @author marc.plouhinec@gmail.com (Marc Plouhinec)
 */

define(['jquery', '../native/widget/map/google'], function($, google) {
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
     * @type {Object.<String, google.maps.Map>}
     */
    var gmapByPlaceHolderId = {};

    /**
     * @type {Object.<Number, google.maps.Marker>}
     */
    var gmarkerById = {};


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
        }
    };

    return nativeMap;
});
