/**
 * Web implementation of the nativeGeolocation interface.
 *
 * @author Marc Plouhinec
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
         * @type {Object.<string, Number>}
         * @private
         */
        '_watchIdByCallbacksId': {},

        /**
         * Get the current device location.
         *
         * @param {string} callbacksId
         *     ID of geolocation success and error callbacks.
         * @param {string} jsonOptions
         *     JSON-serialized PositionOptions.
         */
        'getCurrentPosition': function(callbacksId, jsonOptions) {
            if (!navigator.geolocation) {
                require(['extensions/org/opentravelmate/service/geolocationService'], function (geolocationService) {
                    geolocationService._fireCurrentPositionEvent(callbacksId, null, {code: POSITION_UNAVAILABLE, message: 'Geolocation is not supported by this browser.'});
                });
                return;
            }

            navigator.geolocation.getCurrentPosition(function(position) {
                require(['extensions/org/opentravelmate/service/geolocationService'], function (geolocationService) {
                    geolocationService._fireCurrentPositionEvent(callbacksId, position, null);
                });
            }, function(positionError) {
                require(['extensions/org/opentravelmate/service/geolocationService'], function (geolocationService) {
                    geolocationService._fireCurrentPositionEvent(callbacksId, null, positionError);
                });
            }, JSON.parse(jsonOptions));
        },

        /**
         * Watch the device location.
         *
         * @param {string} callbacksId
         *     ID of geolocation success and error callbacks.
         * @param {string} jsonOptions
         *     JSON-serialized PositionOptions.
         */
        'watchPosition': function(callbacksId, jsonOptions) {
            if (!navigator.geolocation) {
                require(['extensions/org/opentravelmate/service/geolocationService'], function (geolocationService) {
                    geolocationService._fireWatchPositionEvent(callbacksId, null, {code: POSITION_UNAVAILABLE, message: 'Geolocation is not supported by this browser.'});
                });
                return;
            }

            var watchId = navigator.geolocation.watchPosition(function(position) {
                require(['extensions/org/opentravelmate/service/geolocationService'], function (geolocationService) {
                    geolocationService._fireWatchPositionEvent(callbacksId, position, null);
                });
            }, function(positionError) {
                require(['extensions/org/opentravelmate/service/geolocationService'], function (geolocationService) {
                    geolocationService._fireWatchPositionEvent(callbacksId, null, positionError);
                });
            }, JSON.parse(jsonOptions));

            this._watchIdByCallbacksId[callbacksId] = watchId;
        },

        /**
         * Stop watching the device position.
         *
         * @param {string} callbacksId
         *     ID of geolocation success and error callbacks.
         */
        'clearWatch': function(callbacksId) {
            var watchId = this._watchIdByCallbacksId[callbacksId];
            if (watchId != undefined) {
                delete this._watchIdByCallbacksId[callbacksId];;
                navigator.geolocation.clearWatch(watchId);
            }
        }
    };

    return nativeGeolocation;
});
