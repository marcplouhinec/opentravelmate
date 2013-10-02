/**
 * Web implementation of the nativeGeolocation interface.
 *
 * @author marc.plouhinec@gmail.com (Marc Plouhinec)
 */

define(function() {
    'use strict';

    /**
     * @type {Number}
     * @constant
     */
    var POSITION_UNAVAILABLE = 2;

    var nativeGeolocation = {
        /**
         * Get the current device location.
         *
         * @param {String} callbacksId
         *     ID of geolocation success and error callbacks.
         * @param {String} jsonOptions
         *     JSON-serialized PositionOptions.
         */
        'getCurrentPosition': function(callbacksId, jsonOptions) {
            if (!navigator.geolocation) {
                require(['extensions/core/geolocation/geolocation'], function (geolocation) {
                    geolocation.fireCurrentPositionEvent(callbacksId, null, {code: POSITION_UNAVAILABLE, message: 'Geolocation is not supported by this browser.'});
                });
                return;
            }

            navigator.geolocation.getCurrentPosition(function(position) {
                require(['extensions/core/geolocation/geolocation'], function (geolocation) {
                    geolocation.fireCurrentPositionEvent(callbacksId, position, null);
                });
            }, function(positionError) {
                require(['extensions/core/geolocation/geolocation'], function (geolocation) {
                    geolocation.fireCurrentPositionEvent(callbacksId, null, positionError);
                });
            }, JSON.parse(jsonOptions));
        }
    };

    return nativeGeolocation;
});
