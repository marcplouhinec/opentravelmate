/**
 * Interface implemented by a Java Object
 *
 * @author marc.plouhinec@opentravelmate.org (Marc Plouhinec)
 */


define([], function() {

    var webViewJavaObject = {};

  /**
   * Get the root web view ID.
   *
   * @return {string} web view ID.
   */
  webViewJavaObject.getRootWebViewId = function() {
    return '';
  };

  /**
   * Update the place-holders layout parameters.
   *
   * @param {number} windowWidth Window width.
   * @param {number} windowHeight Window height.
   * @param {object} placeholderLayoutParams Place-holders layout parameters.
   */
  webViewJavaObject.updatePlaceHolderLayoutParams = function(windowWidth, windowHeight, placeholderLayoutParams) {
  };

  return webViewJavaObject;
});
