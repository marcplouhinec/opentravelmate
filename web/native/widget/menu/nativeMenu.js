/**
 * Web implementation of the nativeMenu interface.
 *
 * @author marc.plouhinec@gmail.com (Marc Plouhinec)
 */

define(['jquery'], function($) {
    'use strict';

    /**
     * Array of menu items to add when the menu is completely initialized.
     *
     * @type {Object.<String, Array.<{id: Number, title: String, tooltip: String, iconUrl: String}>>}
     */
    var menuItemsToAddByPlaceHolderId = {};

    var nativeMenu = {
        /**
         * Build the native view object for the current widget.
         * 
         * @param {String} jsonLayoutParams JSON-serialized LayoutParams
         */
        'buildView': function(jsonLayoutParams) {
			var self = this,
			    layoutParams = JSON.parse(jsonLayoutParams),
				baseUrl = layoutParams.additionalParameters['baseUrl'];

			// Load the menu CSS if necessary
			if ($('#otm-menu-stylesheet').length === 0) {
				var menuLink = document.createElement('link');
				menuLink.id = 'otm-menu-stylesheet';
				menuLink.setAttribute('rel', 'stylesheet');
				menuLink.setAttribute('href', baseUrl + 'native/widget/menu/menu.css');
				menuLink.setAttribute('type', 'text/css');
				menuLink.setAttribute('media', 'screen');
				$('head').append(menuLink);
			}

			// Create a container for the menu
			var menuContainer = document.createElement('div');
			menuContainer.id = this._getMenuContainerId(layoutParams.id);
			menuContainer.style.position = 'absolute';
			menuContainer.style.left = layoutParams.x + 'px';
			menuContainer.style.top = layoutParams.y + 'px';
			menuContainer.style.width = layoutParams.width + 'px';
			menuContainer.style.height = layoutParams.height + 'px';
			menuContainer.style.visibility = layoutParams.visible ? 'visible' : 'hidden';
			document.body.appendChild(menuContainer);

			// Paint some HTMl content in the canvas
			$.get(baseUrl + 'native/widget/menu/menu.html', function(data, textStatus, jqXHR) {
				$(menuContainer).append(jqXHR.responseText);
				
				// Set the logo
				$(menuContainer).find('img.otm-menu-logo-img').attr('src', baseUrl + 'extensions/core/widget/menu/image/ic_logo.png');

                // Add the menu items that have been potentially added when the menu was not initialized yet
                var menuItemsToAdd = menuItemsToAddByPlaceHolderId[layoutParams.id];
                if (_.isArray(menuItemsToAdd)) {
                    _.each(menuItemsToAdd, function addMenuItem(menuItemToAdd) {
                        self.addMenuItem(layoutParams.id, JSON.stringify(menuItemToAdd));
                    });
                }
			});

            // Add the 'More' button
            this.addMenuItem(layoutParams.id, JSON.stringify({
                id: -1,
                title: 'More',
                tooltip: 'More',
                iconUrl: baseUrl + 'extensions/core/widget/menu/image/ic_btn_more.png'
            }));
        },

        /**
         * Add the menu item to the native widget.
         * 
         * @param {String} menuPlaceHolderId Place-holder ID of the menu.
         * @param {String} jsonMenuItem JSON-serialized MenuItem
         */
        'addMenuItem': function(menuPlaceHolderId, jsonMenuItem) {
            /** @type {{id: Number, title: String, tooltip: String, iconUrl: String}} */
			var item = JSON.parse(jsonMenuItem);
			var $menuContainer = $('#' + this._getMenuContainerId(menuPlaceHolderId));
			var $buttonPanel = $menuContainer.find('.otm-menu-button-panel');

            // if the menu is not initialized yet, add the item in a todo-list
            if ($buttonPanel.length === 0) {
                var menuItemsToAdd = menuItemsToAddByPlaceHolderId[menuPlaceHolderId];
                if (!_.isArray(menuItemsToAdd)) {
                    menuItemsToAdd = [];
                    menuItemsToAddByPlaceHolderId[menuPlaceHolderId] = menuItemsToAdd;
                }
                menuItemsToAdd.push(item);
                return;
            }

            // Create a button and add it to the panel
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

            // Handle the click event
            $(button).click(function handleMenuItemClick() {
                require(['core/widget/Widget'], function (Widget) {
                    /** @type {Menu} */
                    var menu = Widget.findById(menuPlaceHolderId);
                    menu.fireClickEvent(item.id);
                });
            });
        },
        
        /**
         * @private
         * @param {String} menuPlaceHolderId Place-holder ID of the menu.
         * @return {String} Menu container element ID.
         */
        '_getMenuContainerId': function(menuPlaceHolderId) {
			return menuPlaceHolderId + '-container';
		}
    };

    return nativeMenu;
});
