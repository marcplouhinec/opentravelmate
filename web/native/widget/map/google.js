/**
 * Handy object used to easily manipulate Google Maps objects with require.js modules.
 * 
 * @author Marc Plouhinec
 */

define(['async!http://maps.googleapis.com/maps/api/js?libraries=places&sensor=true!callback'], function() {
	'use strict';
	return window.google;
});
