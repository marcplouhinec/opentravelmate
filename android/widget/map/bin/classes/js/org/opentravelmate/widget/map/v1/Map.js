/**
 * Map implementation for Android.
 *
 * @author marc.plouhinec@opentravelmate.org (Marc Plouhinec)
 */


define([
  'org/opentravelmate/widget/map/v1/mapJavaObject'
], function(mapJavaObject) {
  'use strict';

  /**
   * Create a new map.
   *
   * @param {string} viewId ID provided by the Java object.
   * @constructor
   */
  function Map(viewId) {
    /**
     * View ID.
     *
     * @type {string}
     * @private
     */
    this.viewId_ = viewId;
  }
  
  
  /**
   * Create a new map at the location specified by the given element.
   *
   * @param {string} elementId Place holder for the map.
   */
  Map.createMap = function(elementId) {
	mapJavaObject.createMap(elementId);
  };
  

  /**
   * Get the view ID.
   *
   * @return {string} View ID.
   */
  Map.prototype.getId = function() {
    return this.viewId_;
  };

  return Map;
});
