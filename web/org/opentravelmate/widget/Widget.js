/**
 * Define the Widget class.
 *
 * @author marc.plouhinec@gmail.com (Marc Plouhinec)
 */

define([
    'org/opentravelmate/commons/I18nError',
    'org/opentravelmate/commons/ErrorCode'], function(I18nError, ErrorCode) {
    'use strict';

    /**
     * Create a widget.
     *
     * @param {{id: String}} options
     * @constructor
     */
    function Widget(options) {
        if (!options || !options.id) {
            return;
        }

        /** @type {String} */
        this.id = options.id;

        /**
         * Registered listeners.
         *
         * @type {{create: Array.<Function>, destroy: Array.<Function>}}
         * @private
         */
        this._listeners = {
            create: [],
            destroy: []
        };

        // Register this widget
        Widget._widgetById[this.id] = this;
    }

    /**
     * Contains all instances of widgets.
     *
     * @type {Object.<String, Widget>}
     * @private
     */
    Widget._widgetById = {};

    /**
     * Find a widget by its ID.
     *
     * @param {String} id
     * @return {Widget|undefined} Found widget.
     */
    Widget.findById = function(id) {
        return Widget._widgetById[id];
    };

    /**
     * Register a listener for the 'create' event of the widget.
     *
     * @param {Function} listener
     *  Function called just after the widget is created.
     */
    Widget.prototype.onCreate = function(listener) {
        this._listeners.create.push(listener);
    };

    /**
     * Register a listener for the 'destroy' event of the widget.
     *
     * @param {Function} listener
     *  Function called just before the widget is destroyed.
     */
    Widget.prototype.onDestroy = function(listener) {
        this._listeners.destroy.push(listener);
    };

    return Widget;
});
