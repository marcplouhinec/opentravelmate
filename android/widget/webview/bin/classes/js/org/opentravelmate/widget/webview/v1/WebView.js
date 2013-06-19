/**
 * WebView implementation for Android.
 *
 * @author marc.plouhinec@opentravelmate.org (Marc Plouhinec)
 */


define([
  'org/opentravelmate/widget/webview/v1/webViewJavaObject'
], function(webViewJavaObject) {
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

  return WebView;
});
