/**
 * Handle map buttons.
 *
 * @author marc.plouhinec@gmail.com (Marc Plouhinec)
 */

define([
    'jquery',
    'native/widget/map/google'
], function($, google) {
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
     * @constant
     * @type {String}
     */
    var NORMAL_BUTTON_COLOR_DARK = 'rgba(0, 0, 0, 0.25)';

    /**
     * @constant
     * @type {String}
     */
    var PRESSED_BUTTON_COLOR_DARK = 'rgba(119, 119, 119, 0.25)';

    /**
     * @constant
     * @type {String}
     */
    var NORMAL_BUTTON_COLOR_LIGHT = 'rgba(204, 204, 204, 0.7)';

    /**
     * @constant
     * @type {String}
     */
    var PRESSED_BUTTON_COLOR_LIGHT = 'rgba(170, 170, 170, 0.7)';

    /**
     * Create a new MapButtonController.
     *
     * @param {Object} gmap Google map
     * @param {String} mapPlaceHolderId
     * @param {String} baseUrl
     * @constructor
     */
    function MapButtonController(gmap, mapPlaceHolderId, baseUrl) {
        var self = this;

        /**
         * @type {Array.<MapButton>}
         * @private
         */
        this._mapButtons = [];

        /**
         * @type {Array.<HTMLButtonElement>}
         * @private
         */
        this._mapButtonElements = [];


        /**
         * @type {Array.<HTMLImageElement>}
         * @private
         */
        this._mapButtonImgElements = [];

        /**
         * @type {String}
         * @private
         */
        this._baseUrl = baseUrl;

        /**
         * @private
         */
        this._$mapPlaceHolder = $('#' + mapPlaceHolderId);

        /**
         * @type {Array.<function(mapButton: MapButton)>}
         * @private
         */
        this._clickListeners = [];

        /**
         * @type {String}
         * @private
         */
        this._currentNormalButtonColor = NORMAL_BUTTON_COLOR_DARK;

        /**
         * @type {String}
         * @private
         */
        this._currentPressedButtonColor = PRESSED_BUTTON_COLOR_DARK;

        // Change the buttons color according to the map type
        google.maps.event.addListener(gmap, 'maptypeid_changed', function() {
            self._setButtonsDark(gmap.getMapTypeId() === google.maps.MapTypeId.ROADMAP);
        });

        // Move the button when the window is resized
        $(window).resize(function handleWindowResize() {
            self.resetButtonsPosition();
        });
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
        this._mapButtonElements.push(mapButtonElement);
        mapButtonElement.id = 'map-button-' + mapButton.id;
        mapButtonElement.className = 'otm-button otm-button-middle';
        mapButtonElement.style.position = 'absolute';
        mapButtonElement.style.left = (mapOffset.left + mapWidth - MAP_BUTTON_MARGIN_RIGHT - MAP_BUTTON_WIDTH) + 'px';
        mapButtonElement.style.top = (mapOffset.top + MAP_BUTTON_TOTAL_HEIGHT * nbPrecedingButtons + MAP_BUTTON_MARGIN_TOP) + 'px';
        mapButtonElement.style.width = MAP_BUTTON_WIDTH + 'px';
        mapButtonElement.style.height = MAP_BUTTON_HEIGHT + 'px';
        mapButtonElement.style.backgroundColor = this._currentNormalButtonColor;
        mapButtonElement.title = mapButton.tooltip;

        var mapButtonImg = /** @type {HTMLImageElement} */ document.createElement('img');
        this._mapButtonImgElements.push(mapButtonImg);
        mapButtonImg.src = this._baseUrl + '/' + mapButton.iconUrl;
        mapButtonImg.alt = mapButton.tooltip;
        mapButtonElement.appendChild(mapButtonImg);

        $(mapButtonElement).on('mousedown touchstart', function(event) {
            event.stopPropagation();
            event.preventDefault();

            $(this).css('background-color', self._currentPressedButtonColor);
        }).on('mouseup touchend', function(event) {
            event.stopPropagation();
            event.preventDefault();

            $(this).css('background-color', self._currentNormalButtonColor);
            for (var i = 0; i < self._clickListeners.length; i += 1) {
                self._clickListeners[i](mapButton);
            }
        });

        document.body.appendChild(mapButtonElement);
    };

    /**
     * Update the given button on the map.
     *
     * @param {MapButton} mapButton
     */
    MapButtonController.prototype.updateButton = function(mapButton) {
        // Update the map button in the inner collection
        for (var index = 0 ; index < this._mapButtons.length; index += 1) {
            if (this._mapButtons[index].id === mapButton.id) {
                this._mapButtons[index] = mapButton;
                var mapButtonElement = this._mapButtonElements[index];
                mapButtonElement.title = mapButton.tooltip;
                var mapButtonImg = this._mapButtonImgElements[index];
                mapButtonImg.src = this._baseUrl + '/' + mapButton.iconUrl;
                mapButtonImg.alt = mapButton.tooltip;
                break;
            }
        }
    };

    /**
     * Remove the given button from the map.
     *
     * @param {MapButton} mapButton
     */
    MapButtonController.prototype.removeButton = function(mapButton) {
        for (var index = 0 ; index < this._mapButtons.length; index += 1) {
            if (this._mapButtons[index].id === mapButton.id) {
                var mapButtonElement = this._mapButtonElements[index];
                mapButtonElement.parentNode.removeChild(mapButtonElement);
                this._mapButtonElements.splice(index, 1);
                this._mapButtonImgElements.splice(index, 1);
                this._mapButtons.splice(index, 1);
                break;
            }
        }
        this.resetButtonsPosition();
    };

    /**
     * Register a listener for a button click event.
     *
     * @param {function(mapButton: MapButton)} listener
     */
    MapButtonController.prototype.onButtonClick = function(listener) {
        this._clickListeners.push(listener);
    };

    /**
     * Move the buttons to the correct position.
     */
    MapButtonController.prototype.resetButtonsPosition = function() {
        var mapOffset = this._$mapPlaceHolder.offset();
        var mapWidth = this._$mapPlaceHolder.width();

        for (var i = 0; i < this._mapButtonElements.length; i += 1) {
            var mapButtonElement = this._mapButtonElements[i];

            mapButtonElement.style.left = (mapOffset.left + mapWidth - MAP_BUTTON_MARGIN_RIGHT - MAP_BUTTON_WIDTH) + 'px';
            mapButtonElement.style.top = (mapOffset.top + MAP_BUTTON_TOTAL_HEIGHT * i + MAP_BUTTON_MARGIN_TOP) + 'px';
        }
    };

    /**
     * Show or hide the buttons.
     *
     * @param {boolean} visible
     */
    MapButtonController.prototype.setButtonsVisible = function(visible) {
        for (var i = 0; i < this._mapButtonElements.length; i += 1) {
            var mapButtonElement = this._mapButtonElements[i];

            mapButtonElement.style.display = visible ? 'block' : 'none';
        }
    };

    /**
     * Set the buttons color to dark or light.
     *
     * @param {Boolean} darkColor
     *     true = dark color, false = light color
     * @private
     */
    MapButtonController.prototype._setButtonsDark = function(darkColor) {
        this._currentNormalButtonColor = darkColor ? NORMAL_BUTTON_COLOR_DARK : NORMAL_BUTTON_COLOR_LIGHT;
        this._currentPressedButtonColor = darkColor ? PRESSED_BUTTON_COLOR_DARK : PRESSED_BUTTON_COLOR_LIGHT;

        for (var i = 0; i < this._mapButtonElements.length; i += 1) {
            var mapButtonElement = this._mapButtonElements[i];
            mapButtonElement.style.backgroundColor = this._currentNormalButtonColor;
        }
    };

    return MapButtonController;
});