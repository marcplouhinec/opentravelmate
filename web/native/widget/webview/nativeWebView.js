/**
 * Web implementation of the nativeWebView interface.
 *
 * @author marc.plouhinec@gmail.com (Marc Plouhinec)
 */

define(function() {
    'use strict';

    var nativeWebView = {
        /**
         * Build the native view object for the current widget.
         * 
         * @param {String} jsonLayoutParams JSON-serialized LayoutParams
         */
        'buildView': function(jsonLayoutParams) {
			var layoutParams = JSON.parse(jsonLayoutParams),
				baseUrl = layoutParams.additionalParameters['baseUrl'];
		
			// Create an iframe
			var iframe = document.createElement('iframe');
			iframe.id = 'webview-' + layoutParams.id;
			iframe.src = baseUrl + layoutParams.additionalParameters['url'];
			iframe.style.position = 'absolute';
			iframe.style.left = layoutParams.x + 'px';
			iframe.style.top = layoutParams.y + 'px';
			iframe.style.width = layoutParams.width + 'px';
			iframe.style.height = layoutParams.height + 'px';
			iframe.style.border = 'none';
			iframe.style.visibility = layoutParams.visible ? 'visible' : 'hidden';
            iframe.style.overflow = 'auto';
			
			iframe.onload = function onLoad() {
				// Inject the startup script
				iframe.contentWindow.org_opentravelmate_widget_webview_webviewId = layoutParams.id;
				iframe.contentWindow.org_opentravelmate_widget_webview_webviewUrl = layoutParams.additionalParameters['url'];
				iframe.contentWindow.org_opentravelmate_widget_webview_webviewEntrypoint = layoutParams.additionalParameters['entrypoint'];
				iframe.contentWindow.org_opentravelmate_widget_webview_webviewBaseUrl = baseUrl;
				var script = iframe.contentDocument.createElement('script');
				script.src = baseUrl + 'extensions/core/lib/require.min.js';
				script.setAttribute('data-main', baseUrl + 'extensions/core/widget/webview/startupScript');
				iframe.contentDocument.body.appendChild(script);
			};
			
			document.body.appendChild(iframe);
        }
    };

    return nativeWebView;
});
