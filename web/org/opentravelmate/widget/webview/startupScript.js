/**
 * Webview startup script.
 *
 * @author marc.plouhinec@gmail.com (Marc Plouhinec)
 */

// Define AMD incompatible libraries
requirejs.config({
	baseUrl: window.webviewBaseUrl,
    paths: {
        'jquery': 'lib/jquery.min',
        'underscore': 'lib/underscore.min'
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
	'org/opentravelmate/widget/webview/WebView',
	window.webviewEntrypoint],
function(WebView, entrypoint) {
    'use strict';
    
    // Create the current WebView
    WebView.setCurrent(new WebView({
		id: window.webviewId,
		url: window.webviewUrl,
		entrypoint: window.webviewEntrypoint,
		baseUrl: webviewBaseUrl
	}));
    
    // Call the entry point
	entrypoint();
});
