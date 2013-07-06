/**
 * Application startup script.
 *
 * @author marc.plouhinec@gmail.com (Marc Plouhinec)
 */

// Define AMD incompatible libraries
requirejs.config({
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
    'jquery',
    'org/opentravelmate/widget/webview/WebView',
    'extension/org/opentravelmate/startup/startup'
], function($, WebView, startup) {
    'use strict';

    // Create the root WebView
    var rootWebView = new WebView({
        id: 'root',
        url: String(document.URL),
        mainFunctionUrl: 'main'
    });
    WebView.setCurrent(rootWebView);

    // Launch the startup extension
    startup();
});
