/**
 * Define a customized version of the InfoWindow based on an InfoBox.
 *
 * @author marc.plouhinec@gmail.com (Marc Plouhinec)
 */

define(['./InfoBox', '../../../extensions/core/utils/stringUtils', './google'], function(InfoBox, stringUtils, google) {
    'use strict';

    /**
     * @constant
     * @type {String}
     */
    var INFO_WINDOW_FONT_FAMILY = 'Roboto, sans-serif';

    /**
     * @constant
     * @type {String}
     */
    var INFO_WINDOW_FONT_SIZE = '13px';

    /**
     * @constant
     * @type {String}
     */
    var INFO_WINDOW_FONT_WEIGHT = 'bold';

    /**
     * @constant
     * @type {Number}
     */
    var INFO_WINDOW_PADDING = 6;

    /**
     * Create a new CustomInfoWindow.
     *
     * @param {string} content
     * @constructor
     */
    function CustomInfoWindow(content) {
        var self = this;

        /**
         * @type {Array.<Function>}
         * @private
         */
        this._clickListeners = [];

        // Create the HTML elements that will paint the info window
        var infoBoxFont = INFO_WINDOW_FONT_SIZE + '_' + INFO_WINDOW_FONT_WEIGHT + ' ' + INFO_WINDOW_FONT_FAMILY;
        var infoBoxTextWidth = Math.round(stringUtils.computeWidth(content, infoBoxFont) * 1.05);
        var infoBoxWidth = infoBoxTextWidth + INFO_WINDOW_PADDING * 2 + 2;

        var divWrapperElement = /** @type {HTMLDivElement} */document.createElement('div');
        divWrapperElement.style.cursor = 'pointer';

        var divElement = /** @type {HTMLDivElement} */document.createElement('div');
        divElement.style.backgroundColor = 'white';
        divElement.style.border = '1px solid #959595';
        divElement.style.padding = INFO_WINDOW_PADDING + 'px';
        divElement.style.fontSize = INFO_WINDOW_FONT_SIZE;
        divElement.style.fontFamily = INFO_WINDOW_FONT_FAMILY;
        divElement.style.fontWeight = INFO_WINDOW_FONT_WEIGHT;
        divElement.style.textAlign = 'center';
        divElement.style.width = infoBoxTextWidth + 'px';
        divElement.onclick = function () {
            for (var i = 0; i < self._clickListeners.length; i += 1) {
                self._clickListeners[i]();
            }
            return false;
        };
        divElement.textContent = content;
        divWrapperElement.appendChild(divElement);

        var divTriangleElementShadow = /** @type {HTMLDivElement} */document.createElement('div');
        divTriangleElementShadow.style.width = 0;
        divTriangleElementShadow.style.height = 0;
        divTriangleElementShadow.style.borderLeft = '8px solid transparent';
        divTriangleElementShadow.style.borderRight = '8px solid transparent';
        divTriangleElementShadow.style.borderTop = '16px solid #959595';
        divTriangleElementShadow.style.marginTop = '-1px';
        divTriangleElementShadow.style.marginLeft = (infoBoxWidth / 2 - 8) + 'px';
        divWrapperElement.appendChild(divTriangleElementShadow);

        var divTriangleElement = /** @type {HTMLDivElement} */document.createElement('div');
        divTriangleElement.style.width = 0;
        divTriangleElement.style.height = 0;
        divTriangleElement.style.borderLeft = '7px solid transparent';
        divTriangleElement.style.borderRight = '7px solid transparent';
        divTriangleElement.style.borderTop = '14px solid white';
        divTriangleElement.style.marginTop = '-16px';
        divTriangleElement.style.marginLeft = (infoBoxWidth / 2 - 7) + 'px';
        divWrapperElement.appendChild(divTriangleElement);

        divWrapperElement.onmouseover = function() {
            divElement.style.backgroundColor = '#0099cc';
            divTriangleElement.style.borderTop = '14px solid #0099cc';
        };
        divWrapperElement.onmousedown = function() {
            divElement.style.backgroundColor = '#0099cc';
            divTriangleElement.style.borderTop = '14px solid #0099cc';
        };
        divWrapperElement.onmouseout = function() {
            divElement.style.backgroundColor = 'white';
            divTriangleElement.style.borderTop = '14px solid white';
        };
        divWrapperElement.onmouseup = function() {
            divElement.style.backgroundColor = 'white';
            divTriangleElement.style.borderTop = '14px solid white';
        };

        // Create the info box
        this._infoBox = new InfoBox({
            content: divWrapperElement,
            closeBoxURL: '',
            pixelOffset: new google.maps.Size(-infoBoxWidth / 2, -46),
            maxWidth: 0
        });
    }

    /**
     * Open the info window.
     *
     * @param gmap
     * @param gmarker
     */
    CustomInfoWindow.prototype.open = function(gmap, gmarker) {
        this._infoBox.open(gmap, gmarker);
    };

    /**
     * Close the info window.
     */
    CustomInfoWindow.prototype.close = function() {
        this._infoBox.close();
    };

    /**
     * Register a listener for the CLICK event.
     *
     * @param {Function} listener
     */
    CustomInfoWindow.prototype.onClick = function(listener) {
        this._clickListeners.push(listener);
    };

    return CustomInfoWindow;
});