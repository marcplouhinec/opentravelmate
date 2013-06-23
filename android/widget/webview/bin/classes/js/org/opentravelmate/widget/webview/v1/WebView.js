/**
 * WebView implementation for Android.
 *
 * @author marc.plouhinec@opentravelmate.org (Marc Plouhinec)
 */


define([
  'org/opentravelmate/widget/webview/v1/webViewJavaObject',
  'jquery'
], function(webViewJavaObject, $) {
  'use strict';

  /**
   * Create a new web view.
   *
   * @param {string} webViewId ID provided by the Java object.
   * @constructor
   */
  function WebView(webViewId) {
    /**
     * Web View ID.
     *
     * @type {string}
     * @private
     */
    this.webViewId_ = webViewId;
  }


  /**
   * Find the root web view.
   *
   * @return {WebView} Root web view.
   */
  WebView.getRootWebView = function() {
    var webViewId = webViewJavaObject.getRootWebViewId();
    return new WebView(webViewId);
  };

  /**
   * Get the web view ID.
   *
   * @return {string} Web view ID.
   */
  WebView.prototype.getId = function() {
    return this.webViewId_;
  };

  /**
   * Get the layout information of the place holders defined by the given IDs.
   * Note: this function returns the result by calling webViewJavaObject.updatePlaceHolderLayoutParams().
   *
   * @param {Array.<string>} htmlElementIds IDs of the place holders.
   */
  WebView.getPlaceHolderLayoutParams = function(htmlElementIds) {
    var i, $window, $placeholder, offset, layoutParams = [];

    for (i = 0; i < htmlElementIds.length; i += 1) {
      $placeholder = $('#' + htmlElementIds[i]);

      if ($placeholder) {
        offset = $placeholder.offset();

        layoutParams.push({
          'width': $placeholder.width(),
          'height': $placeholder.height(),
          'x': offset.left,
          'y': offset.top,
          'visible': $placeholder.is(":visible")
        });
      }
    }

    $window = $(window);
    webViewJavaObject.updatePlaceHolderLayoutParams($window.width(), $window.height(), layoutParams);
  };

  // Register this class to the Java side
  webViewJavaObject.registerWebViewClass(WebView);

  return WebView;
});
