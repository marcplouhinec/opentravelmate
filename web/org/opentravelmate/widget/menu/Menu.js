/**
 * Define the menu widget.
 *
 * @author marc.plouhinec@gmail.com (Marc Plouhinec)
 */

define([
    'jquery',
    'org/opentravelmate/widget/Widget',
    'org/opentravelmate/widget/LayoutParams',
    'org/opentravelmate/widget/menu/MenuItem'
], function($, Widget, LayoutParams, MenuItem) {
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
        var self = this;

        // Load the menu CSS if necessary
        if ($('#otm-menu-stylesheet').length === 0) {
            var menuLink = document.createElement('link');
            menuLink.id = 'otm-menu-stylesheet';
            menuLink.setAttribute('rel', 'stylesheet');
            menuLink.setAttribute('href', this.baseUrl + 'org/opentravelmate/widget/menu/menu.css');
            menuLink.setAttribute('type', 'text/css');
            menuLink.setAttribute('media', 'screen');
            $('head').append(menuLink);
        }

        // Create a container for the menu
        this._menuContainer = document.createElement('div');
        this._menuContainer.id = layoutParams.id + '-container';
        this._menuContainer.style.position = 'absolute';
        this._menuContainer.style.left = layoutParams.x + 'px';
        this._menuContainer.style.top = layoutParams.y + 'px';
        this._menuContainer.style.width = layoutParams.width + 'px';
        this._menuContainer.style.height = layoutParams.height + 'px';
        this._menuContainer.style.visibility = layoutParams.visible ? 'visible' : 'hidden';
        document.body.appendChild(this._menuContainer);

        // Paint some HTMl content in the canvas
        $.get(this.baseUrl + 'org/opentravelmate/widget/menu/menu.html', function(htmlContent) {
            $(self._menuContainer).append(htmlContent);

            // Add the 'More' button
            self.addMenuItem(new MenuItem({
                title: 'More',
                tooltip: 'More',
                iconUrl: self.baseUrl + 'org/opentravelmate/widget/menu/image/ic_btn_more.png'
            }));
        });
    };

    /**
     * Add an item to the menu.
     *
     * @param {MenuItem} item
     */
    Menu.prototype.addMenuItem = function(item) {
        var $buttonPanel = $(this._menuContainer).find('.otm-menu-button-panel');

        var button = document.createElement('button');
        button.setAttribute('title', item.tooltip);
        var buttonImage = document.createElement('img');
        buttonImage.src = item.iconUrl;
        buttonImage.setAttribute('alt', item.title);
        button.appendChild(buttonImage);
        var buttonSize = $buttonPanel.height();
        button.style.width = buttonSize + 'px';
        button.style.height = buttonSize + 'px';

        $buttonPanel.append(button);
    };

    return Menu;
});