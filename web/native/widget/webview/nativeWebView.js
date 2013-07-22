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
			var iframe = /** @type {HTMLIFrameElement} */document.createElement('iframe');
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
				var script = /** @type {HTMLScriptElement} */ iframe.contentDocument.createElement('script');
				script.src = baseUrl + 'extensions/core/lib/require.min.js';
				script.setAttribute('data-main', baseUrl + 'extensions/core/widget/webview/startupScript');
                iframe.contentDocument.body.appendChild(script);
			};
			
			document.body.appendChild(iframe);
        },

        /**
         * Update the native view object for the current widget.
         *
         * @param {String} jsonLayoutParams JSON-serialized LayoutParams
         */
        'updateView': function(jsonLayoutParams) {
            var layoutParams = JSON.parse(jsonLayoutParams);

            var iframe = document.getElementById('webview-' + layoutParams.id);
            iframe.style.left = layoutParams.x + 'px';
            iframe.style.top = layoutParams.y + 'px';
            iframe.style.width = layoutParams.width + 'px';
            iframe.style.height = layoutParams.height + 'px';
            iframe.style.visibility = layoutParams.visible ? 'visible' : 'hidden';
        },

        /**
         * Remove the native view object for the current widget.
         *
         * @param {String} id Place holder ID
         */
        'removeView': function(id) {
            $('#webview-' + id).remove();
        },

        /**
         * Fire an event to a listener that is outside of the WebView.
         *
         * @param {String} webViewPlaceHolderId
         * @param {String} eventName
         * @param {String} jsonPayload
         */
        'fireExternalEvent': function(webViewPlaceHolderId, eventName, jsonPayload) {
            window.parent.require(['core/widget/Widget'], function (Widget) {
                var webView = /** @type {WebView} */ Widget.findById(webViewPlaceHolderId);
                var payload = JSON.parse(jsonPayload);
                webView.fireEvent(eventName, payload);
            });
        },

        /**
         * Fire an event to a listener that is inside the WebView.
         *
         * @param {String} webViewPlaceHolderId
         * @param {String} eventName
         * @param {String} jsonPayload
         */
        'fireInternalEvent': function(webViewPlaceHolderId, eventName, jsonPayload) {
            var iframe = /** @type{HTMLIFrameElement} */ document.getElementById('webview-' + webViewPlaceHolderId);
            if (iframe && iframe.contentWindow && iframe.contentWindow.require) {
                iframe.contentWindow.require(['core/widget/webview/WebView'], function (WebView) {
                    var payload = JSON.parse(jsonPayload);
                    WebView.getCurrent().fireEvent(eventName, payload);
                });
            }
        }
    };

    return nativeWebView;
});
