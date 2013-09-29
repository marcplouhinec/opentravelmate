/**
 * Handle map buttons.
 *
 * @author marc.plouhinec@gmail.com (Marc Plouhinec)
 */

define([
    'jquery'
], function($) {
    'use strict';

    /**
     * @constant
     * @type {number}
     */
    var MAP_BUTTON_MARGIN_RIGHT = 5;

    /**
     * @constant
     * @type {number}
     */
    var MAP_BUTTON_MARGIN_TOP = 5;

    /**
     * @constant
     * @type {number}
     */
    var MAP_BUTTON_WIDTH = 48;

    /**
     * @constant
     * @type {number}
     */
    var MAP_BUTTON_HEIGHT = 48;

    /**
     * @constant
     * @type {number}
     */
    var MAP_BUTTON_TOTAL_HEIGHT = MAP_BUTTON_MARGIN_TOP + MAP_BUTTON_HEIGHT;

    /**
     * Create a new MapButtonController.
     *
     * @param {String} mapPlaceHolderId
     * @param {String} baseUrl
     * @constructor
     */
    function MapButtonController(mapPlaceHolderId, baseUrl) {
        /**
         * @type {Array.<MapButton>}
         * @private
         */
        this._mapButtons = [];

        /**
         * @type {String}
         * @private
         */
        this._baseUrl = baseUrl;

        this._$mapPlaceHolder = $('#' + mapPlaceHolderId);

        /**
         * @type {Array.<function(mapButton: MapButton)>}
         * @private
         */
        this._clickListeners = [];
    }

    /**
     * Add the given button to the map.
     *
     * @param {MapButton} mapButton
     */
    MapButtonController.prototype.addButton = function(mapButton) {
        var self = this;

        this._mapButtons.push(mapButton);
        var nbPrecedingButtons = this._mapButtons.length - 1;
        var mapOffset = this._$mapPlaceHolder.offset();
        var mapWidth = this._$mapPlaceHolder.width();

        var mapButtonElement = /** @type {HTMLButtonElement} */ document.createElement('button');
        mapButtonElement.id = 'map-button-' + mapButton.id;
        mapButtonElement.className = 'otm-button otm-button-middle';
        mapButtonElement.style.position = 'absolute';
        mapButtonElement.style.left = (mapOffset.left + mapWidth - MAP_BUTTON_MARGIN_RIGHT - MAP_BUTTON_WIDTH) + 'px';
        mapButtonElement.style.top = (mapOffset.top + MAP_BUTTON_TOTAL_HEIGHT * nbPrecedingButtons + MAP_BUTTON_MARGIN_TOP) + 'px';
        mapButtonElement.style.width = MAP_BUTTON_WIDTH + 'px';
        mapButtonElement.style.height = MAP_BUTTON_HEIGHT + 'px';
        mapButtonElement.style.backgroundColor = 'rgba(0, 0, 0, 0.25)';
        mapButtonElement.title = mapButton.tooltip;

        var mapButtonImg = /** @type {HTMLImageElement} */ document.createElement('img');
        mapButtonImg.src = this._baseUrl + '/' + mapButton.iconUrl;
        mapButtonImg.alt = mapButton.tooltip;
        mapButtonElement.appendChild(mapButtonImg);

        $(mapButtonElement).on('mousedown touchstart', function() {
            $(this).css('background-color', 'rgba(119, 119, 119, 0.25)');
        }).on('mouseup touchend', function() {
            $(this).css('background-color', 'rgba(0, 0, 0, 0.25)');
            for (var i = 0; i < self._clickListeners.length; i += 1) {
                self._clickListeners[i](mapButton);
            }
        });

        document.body.appendChild(mapButtonElement);
    }

    /**
     * Register a listener for a button click event.
     *
     * @param {function(mapButton: MapButton)} listener
     */
    MapButtonController.prototype.onButtonClick = function(listener) {
        this._clickListeners.push(listener);
    };

    return MapButtonController;
});