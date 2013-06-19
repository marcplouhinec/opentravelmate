/**
 * Display a HTML document.
 *
 * @interface
 * @author marc.plouhinec@gmail.com (Marc Plouhinec)
 */



/**
 * Create a new web view.
 *
 * @param {HTMLDocument} document
 * @constructor
 */
function WebView(document) {
  'use strict';

  /**
   * HTML document.
   *
   * @type {HTMLDocument}
   * @private
   */
  this.document_ = document;
}


/**
 * Get the web view document.
 *
 * @return {HTMLDocument} HTML document.
 */
WebView.prototype.getDocument = function() {
  'use strict';
  return this.document_;
};


/** Export the class as a module */
module.exports = WebView;
