/**
 * Main menu item.
 *
 * @author marc.plouhinec@gmail.com (Marc Plouhinec)
 */



/**
 * Create a menu item.
 *
 * @param {!string} title
 *   Menu item title.
 * @param {?string} tooltip
 *   Text displayed when the user hovers the mouse pointer on top of this
 *   menu item.
 * @param {!string} iconUrl
 *   URL to the menu item icon.
 * @constructor
 */
function MenuItem(title, tooltip, iconUrl) {
  'use strict';

  /**
   * Menu item title.
   *
   * @type {!string}
   */
  this.title = title;

  /**
   * Menu item tooltip.
   *
   * @type {?string}
   */
  this.tooltip = tooltip;

  /**
   * Menu item icon URL.
   *
   * @type {!string}
   */
  this.iconUrl = iconUrl;

  /**
   * Registered click listeners.
   *
   * @private
   * @type {Array.<function>}
   */
  this.clickListeners_ = [];
}


/**
 * Add a click listener.
 *
 * @param {!function(this: MenuItem)} clickListener
 *   Function called when the user clicks on the menu item.
 *   Note: the value of 'this' in the function is the current MenuItem.
 */
MenuItem.prototype.onClick = function(clickListener) {
  'use strict';

  this.clickListeners_.push(clickListener);
};


/**
 * Fire a click event to the registered listeners.
 */
MenuItem.prototype.fireOnClickEvent = function() {
  'use strict';

  var /** @type {number} */ i;
  for (i = 0; i < this.clickListeners_.length; i += 1) {
    this.clickListeners_[i].apply(this);
  }
};


/** Export the class as a module */
module.exports = MenuItem;
