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
         * @type {Object.<String, Number>}
         * @private
         */
        '_watchIdByCallbacksId': {},

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
        },

        /**
         * Watch the device location.
         *
         * @param {String} callbacksId
         *     ID of geolocation success and error callbacks.
         * @param {String} jsonOptions
         *     JSON-serialized PositionOptions.
         */
        'watchPosition': function(callbacksId, jsonOptions) {
            if (!navigator.geolocation) {
                require(['extensions/core/geolocation/geolocation'], function (geolocation) {
                    geolocation.fireWatchPositionEvent(callbacksId, null, {code: POSITION_UNAVAILABLE, message: 'Geolocation is not supported by this browser.'});
                });
                return;
            }

            var watchId = navigator.geolocation.watchPosition(function(position) {
                require(['extensions/core/geolocation/geolocation'], function (geolocation) {
                    geolocation.fireWatchPositionEvent(callbacksId, position, null);
                });
            }, function(positionError) {
                require(['extensions/core/geolocation/geolocation'], function (geolocation) {
                    geolocation.fireWatchPositionEvent(callbacksId, null, positionError);
                });
            }, JSON.parse(jsonOptions));

            this._watchIdByCallbacksId[callbacksId] = watchId;
        },

        /**
         * Stop watching the device position.
         *
         * @param {String} callbacksId
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
