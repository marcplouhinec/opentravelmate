/**
 * Web implementation of the nativeMenu interface.
 *
 * @author marc.plouhinec@gmail.com (Marc Plouhinec)
 */

define(['jquery'], function($) {
    'use strict';

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

				// Add the 'More' button
				self.addMenuItem(layoutParams.id, JSON.stringify({
					title: 'More',
					tooltip: 'More',
					iconUrl: baseUrl + 'native/widget/menu/image/ic_btn_more.png'
				}));
			});
        },

        /**
         * Add the menu item to the native widget.
         * 
         * @param {String} menuPlaceHolderId Place-holder ID of the menu.
         * @param {String} jsonMenuItem JSON-serialized MenuItem
         */
        'addMenuItem': function(menuPlaceHolderId, jsonMenuItem) {
			var item = JSON.parse(jsonMenuItem),
				$menuContainer = $('#' + this._getMenuContainerId(menuPlaceHolderId)),
				$buttonPanel = $menuContainer.find('.otm-menu-button-panel');

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
