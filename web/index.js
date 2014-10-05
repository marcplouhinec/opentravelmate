/**
 * Application startup script.
 *
 * @author Marc Plouhinec
 */

// Compute the baseUrl
var baseUrl = String(document.URL);
var indexOfSlash = baseUrl.lastIndexOf('/');
if (indexOfSlash !== baseUrl.length - 1) {
    baseUrl = baseUrl.substring(0, indexOfSlash + 1);
}

// Set global variables
window.org_opentravelmate_widget_webview_webviewId = 'mainWebViewWrapper';
window.org_opentravelmate_widget_webview_webviewUrl = baseUrl;
window.org_opentravelmate_widget_webview_webviewEntrypoint = './entryPoint';
window.org_opentravelmate_widget_webview_webviewBaseUrl = baseUrl;

// Load the core startup script
require(['./extensions/org/opentravelmate/controller/widget/webview/startupScript']);



