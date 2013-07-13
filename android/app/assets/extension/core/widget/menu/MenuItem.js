/**
 * Define a menu item.
 *
 * @author marc.plouhinec@gmail.com (Marc Plouhinec)
 */

define(['underscore'], function(_) {
    /**
     * Create a menu item.
     *
     * @param {{title: String, tooltip: String, iconUrl: String }} options
     * @constructor
     */
    function MenuItem(options) {
        /** @type {String} */
        this.title = options.title;
        /** @type {String} */
        this.tooltip = options.tooltip;
        /** @type {String} */
        this.iconUrl = options.iconUrl;

        /**
         * @type {Array.<Function>}
         * @private
         */
        this._listeners = [];
    }

    /**
     * Register a listener for the click event.
     *
     * @param {Function} listener
     */
    MenuItem.prototype.onClick = function(listener) {
        this._listeners.push(listener);
    };

    /**
     * Fire a click event to the registered listeners.
     */
    MenuItem.prototype.fireClickEvent = function() {
        _.each(this._listeners, function(listener) {
            listener();
        });
    };

    return MenuItem;
});