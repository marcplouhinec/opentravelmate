/**
 * Define a set of HTML layout parameters.
 *
 * @author marc.plouhinec@gmail.com (Marc Plouhinec)
 */

define(function() {
    'use strict';

    /**
     * Create a set of layout parameters.
     *
     * @param {{id: String, width: Number, height: Number, x: Number, y: Number, visible: Boolean, additionalParameters: Object.<String, String>}} options
     * @constructor
     */
    function Layoutparams(options) {
        /** @type {String} */
        this.id = options.id;
        /** @type {Number} */
        this.width = options.width;
        /** @type {Number} */
        this.height = options.height;
        /** @type {Number} */
        this.x = options.x;
        /** @type {Number} */
        this.y = options.y;
        /** @type {Boolean} */
        this.visible = options.visible;
        /** @type {Object.<String, String>} */
        this.additionalParameters = options.additionalParameters;
    }

    return Layoutparams;
});
