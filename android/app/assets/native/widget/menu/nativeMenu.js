/**
 * Stub for the native implementation of this object.
 *
 * @author marc.plouhinec@gmail.com (Marc Plouhinec)
 */

define(function() {
    'use strict';

    var nativeMenu = {
        /**
	 * Build the native view object for the current widget.
	 * 
	 * @param {String} jsonLayoutParams JSON-serialized LayoutParams
	 */
        'buildView': function(jsonLayoutParams) {
        },

        /**
	 * Add the menu item to the native widget.
	 * 
	 * @param {String} menuPlaceHolderId Place-holder ID of the menu.
	 * @param {String} jsonMenuItem JSON-serialized MenuItem
	 */
        'addMenuItem': function(menuPlaceHolderId, jsonMenuItem) {
        }
    };

    return nativeMenu;
});
