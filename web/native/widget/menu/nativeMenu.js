/**
 * Web implementation of the nativeMenu interface.
 *
 * @author marc.plouhinec@gmail.com (Marc Plouhinec)
 */

define(['jquery', 'extensions/core/utils/FunctionDam'], function($, FunctionDam) {
    'use strict';

    var menuReadyDam = new FunctionDam();

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
                var $head = $('head');
				var menuLink = document.createElement('link');
				menuLink.id = 'otm-menu-stylesheet';
				menuLink.setAttribute('rel', 'stylesheet');
				menuLink.setAttribute('href', baseUrl + 'native/widget/menu/menu.css');
				menuLink.setAttribute('type', 'text/css');
				menuLink.setAttribute('media', 'screen');
                $head.append(menuLink);

                var commonLink = document.createElement('link');
                commonLink.setAttribute('rel', 'stylesheet');
                commonLink.setAttribute('href', baseUrl + 'extensions/core/style/common.css');
                commonLink.setAttribute('type', 'text/css');
                commonLink.setAttribute('media', 'screen');
                $head.append(commonLink);
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
				$(menuContainer).find('img.otm-menu-logo-img').attr('src', baseUrl + 'extensions/org/opentravelmate/controller/widget/menu/image/ic_logo.png');

                // Add the menu items that have been potentially added when the menu was not initialized yet
                menuReadyDam.setOpened(true);
			});

            // Add the 'More' button
            this.addMenuItem(layoutParams.id, JSON.stringify({
                id: -1,
                title: 'More',
                tooltip: 'More',
                iconUrl: baseUrl + 'extensions/org/opentravelmate/controller/widget/menu/image/ic_btn_more.png'
            }));
        },

        /**
         * Update the native view object for the current widget.
         *
         * @param {String} jsonLayoutParams JSON-serialized LayoutParams
         */
        'updateView': function(jsonLayoutParams) {
            var layoutParams = JSON.parse(jsonLayoutParams);

            // Resize the container
            var menuContainer = document.getElementById(this._getMenuContainerId(layoutParams.id));
            menuContainer.style.left = layoutParams.x + 'px';
            menuContainer.style.top = layoutParams.y + 'px';
            menuContainer.style.width = layoutParams.width + 'px';
            menuContainer.style.height = layoutParams.height + 'px';
            menuContainer.style.visibility = layoutParams.visible ? 'visible' : 'hidden';

            // Resize the buttons
            var $buttonPanel = $(menuContainer).find('.otm-menu-button-panel');
            var buttonSize = $buttonPanel.height();
            var $buttons = $buttonPanel.children('button.otm-menu-button');
            $buttons.each(function(i, button) {
                button.style.width = buttonSize + 'px';
                button.style.height = buttonSize + 'px';
            });
        },

        /**
         * Remove the native view object for the current widget.
         *
         * @param {String} id Place holder ID
         */
        'removeView': function(id) {
            $(this._getMenuContainerId(id)).remove();
        },

        /**
         * Add the menu item to the native widget.
         * 
         * @param {String} menuPlaceHolderId Place-holder ID of the menu.
         * @param {String} jsonMenuItem JSON-serialized MenuItem
         */
        'addMenuItem': function(menuPlaceHolderId, jsonMenuItem) {
            var self = this;

            // if the menu is not initialized yet, execute this function later
            if (!menuReadyDam.isOpened()) {
                menuReadyDam.executeWhenOpen(function() {
                    self.addMenuItem(menuPlaceHolderId, jsonMenuItem);
                });
                return;
            }

            /** @type {{id: Number, title: String, tooltip: String, iconUrl: String}} */
			var item = JSON.parse(jsonMenuItem);
			var $menuContainer = $('#' + this._getMenuContainerId(menuPlaceHolderId));
			var $buttonPanel = $menuContainer.find('.otm-menu-button-panel');

            // Create a button and add it to the panel
			var button = document.createElement('button');
            button.setAttribute('class', 'otm-button otm-menu-button');
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
            $(button).bind('touchstart click', function(event) {
                event.stopPropagation();
                event.preventDefault();

                require(['extensions/org/opentravelmate/controller/widget/Widget'], function (Widget) {
                    var menu = /** @type {Menu} */ Widget.findById(menuPlaceHolderId);
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
