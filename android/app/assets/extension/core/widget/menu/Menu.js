/**
 * Define the menu widget.
 *
 * @author marc.plouhinec@gmail.com (Marc Plouhinec)
 */

define([
    'core/widget/Widget',
    'core/widget/LayoutParams',
    'core/widget/menu/MenuItem',
    'nativeMenu'
], function(Widget, LayoutParams, MenuItem, nativeMenu) {
    'use strict';

    /**
     * Create a menu.
     *
     * @param {{id: String, baseUrl: String}} options
     * @constructor
     */
    function Menu(options) {
        Widget.call(this, options);

        /** @type {String} */
        this.baseUrl = options.baseUrl;

        /**
         * @type {HTMLElement|undefined}
         * @private
         */
        this._menuContainer = undefined;
    }

    Menu.prototype = new Widget();
    Menu.prototype.constructor = Menu;

    /**
     * Build the native view object for the current widget.
     *
     * @param {LayoutParams} layoutParams
     */
    Menu.prototype.buildView = function(layoutParams) {
        nativeMenu.buildView(JSON.stringify(layoutParams));
    };

    /**
     * Add an item to the menu.
     *
     * @param {MenuItem} item
     */
    Menu.prototype.addMenuItem = function(item) {
        nativeMenu.addMenuItem(this.id, JSON.stringify(item));
    };

    return Menu;
});
