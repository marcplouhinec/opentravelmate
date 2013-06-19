/**
 * Main Web View that contains all the root widgets.
 *
 * @interface
 * @author marc.plouhinec@gmail.com (Marc Plouhinec)
 */

var WebView = require('./WebView');



/**
 * Root web view.
 *
 * @type {WebView}
 */
var rootWebView = new WebView(undefined);


/** Export the object as a module */
module.exports = rootWebView;
