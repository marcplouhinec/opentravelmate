/**
 * Webview startup script.
 *
 * @author marc.plouhinec@gmail.com (Marc Plouhinec)
 */

// Define AMD incompatible libraries
requirejs.config({
	baseUrl: window.org_opentravelmate_widget_webview_webviewBaseUrl + 'extension/',
    paths: {
        'jquery': 'core/lib/jquery.min',
        'underscore': 'core/lib/underscore.min',
        'async': 'core/lib/async',
        'nativeWebView': window.org_opentravelmate_widget_webview_webviewBaseUrl + 'native/widget/webview/nativeWebView',
        'nativeMenu': window.org_opentravelmate_widget_webview_webviewBaseUrl + 'native/widget/menu/nativeMenu'
    },
    shim: {
        'jquery': {
            exports: '$'
        },
        'underscore': {
            exports: '_'
        }
    }
});

require([
	'core/widget/webview/WebView',
	window.org_opentravelmate_widget_webview_webviewEntrypoint],
function(WebView, entrypoint) {
    'use strict';
    
    // Create the current WebView
    WebView.setCurrent(new WebView({
        id: window.org_opentravelmate_widget_webview_webviewId,
        url: window.org_opentravelmate_widget_webview_webviewUrl,
        entrypoint: window.org_opentravelmate_widget_webview_webviewEntrypoint,
        baseUrl: org_opentravelmate_widget_webview_webviewBaseUrl
    }));
    
    // Call the entry point
    entrypoint();
});
