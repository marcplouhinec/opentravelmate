/**
 * Main menu widget.
 *
 * @interface
 * @author marc.plouhinec@gmail.com (Marc Plouhinec)
 */

//JSLint assumptions
/*global module: false, require: false*/

var MenuItem = require('./MenuItem');

var mainMenu = {

  /**
   * Register a menu item to the main menu.
   *
   * @param {MenuItem} menuItem Menu item to register.
   */
  'registerItem': function(menuItem) {}

};


/** Export the object as a module */
module.exports = mainMenu;
