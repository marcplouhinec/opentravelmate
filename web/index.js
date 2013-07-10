/**
 * Application startup script.
 *
 * @author marc.plouhinec@gmail.com (Marc Plouhinec)
 */

// Define AMD incompatible libraries
requirejs.config({
    paths: {
        'jquery': 'lib/jquery.min',
        'underscore': 'lib/underscore.min',
        'async': 'lib/async'
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
    'org/opentravelmate/widget/webview/WebView'
], function(WebView) {
    'use strict';
    
    /** @type {String} */
	var baseUrl = String(document.URL);
	var indexOfSlash = baseUrl.lastIndexOf('/');
	if (indexOfSlash !== baseUrl.length - 1) {
		baseUrl = baseUrl.substring(0, indexOfSlash + 1);
	}

	// Create manually the current WebView
    WebView.setCurrent(new WebView({
		id: 'root',
		url: String(document.URL),
		entrypoint: '',
		baseUrl: baseUrl
	}));

	// Layout the main view
	WebView.getCurrent().layout();
});
