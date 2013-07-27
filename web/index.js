/**
 * Application startup script.
 *
 * @author marc.plouhinec@gmail.com (Marc Plouhinec)
 */

// Set the baseUrl
/** @type {String} */
var baseUrl = String(document.URL);
var indexOfSlash = baseUrl.lastIndexOf('/');
if (indexOfSlash !== baseUrl.length - 1) {
	baseUrl = baseUrl.substring(0, indexOfSlash + 1);
}

// Define AMD incompatible libraries
requirejs.config({
    baseUrl: baseUrl + 'extensions/',
    paths: {
        'jquery': 'core/lib/jquery.min',
        'underscore': 'core/lib/underscore.min',
        'async': 'core/lib/async',
        'nativeWebView': baseUrl + 'native/widget/webview/nativeWebView',
        'nativeMenu': baseUrl + 'native/widget/menu/nativeMenu',
        'nativeMap': baseUrl + 'native/widget/map/nativeMap'
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
    'core/widget/webview/webview'
], function(webview) {
    'use strict';

    $(document).ready(function() {
        // Create manually the current WebView
        webview.id = 'mainWebView';
        webview.baseUrl = baseUrl;

        // Layout the main view
        webview.layout();
    });
});
