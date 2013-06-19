/**
 * Main menu implementation for Android.
 *
 * @author marc.plouhinec@opentravelmate.org (Marc Plouhinec)
 */


define([
  'org/opentravelmate/widget/mainmenu/v1/mainMenuJavaObject'
], function(mainMenuJavaObject) {
  'use strict';

  /**
   * Create a new main menu.
   *
   * @param {string} viewId ID provided by the Java object.
   * @constructor
   */
  function MainMenu(viewId) {
    /**
     * View ID.
     *
     * @type {string}
     * @private
     */
    this.viewId_ = viewId;
  }
  
  
  /**
   * Create a new main menu at the location specified by the given element.
   *
   * @param {string} elementId Place holder for the main menu.
   */
  MainMenu.createMainMenu = function(elementId) {
	mainMenuJavaObject.createMainMenu(elementId);
  };
  

  /**
   * Get the view ID.
   *
   * @return {string} View ID.
   */
  MainMenu.prototype.getId = function() {
    return this.viewId_;
  };

  return MainMenu;
});
