/**
 * Define the Map widget.
 *
 * @author marc.plouhinec@gmail.com (Marc Plouhinec)
 */

define([
    'jquery',
    'underscore',
    'org/opentravelmate/widget/Widget',
    'org/opentravelmate/widget/LayoutParams',
    'org/opentravelmate/widget/map/google'
], function($, _, Widget, LayoutParams, google) {
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
     * Create a Map.
     *
     * @param {{id: String}} options
     * @constructor
     */
    function Map(options) {
        Widget.call(this, options);
    }

    Map.prototype = new Widget();
    Map.prototype.constructor = Map;

	/**
     * Build the native view object for the current widget.
     * 
     * @param {LayoutParams} layoutParams
     */
    Map.prototype.buildView = function(layoutParams) {

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
		new google.maps.Map(mapCanvas, {
			zoom: DEFAULT_ZOOM,
			center: new google.maps.LatLng(DEFAULT_LATITUDE, DEFAULT_LONGITUDE),
			mapTypeId: google.maps.MapTypeId.ROADMAP
		});
	};

    return Map;
});
