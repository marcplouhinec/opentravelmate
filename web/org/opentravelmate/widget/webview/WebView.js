/**
 * Define the WebView widget.
 *
 * @author marc.plouhinec@gmail.com (Marc Plouhinec)
 */

define([
    'jquery',
    'underscore',
    'org/opentravelmate/widget/Widget',
    'org/opentravelmate/widget/LayoutParams'
], function($, _, Widget, LayoutParams) {
    'use strict';

    /**
     * Create a WebView.
     *
     * @param {{id: String, url: String, entrypoint: String, baseUrl: String}} options
     * @constructor
     */
    function WebView(options) {
        Widget.call(this, options);

        /** @type {String} */
        this.url = options.url;
        /** @type {String} */
        this.entrypoint = options.entrypoint;
        /** @type {String} */
        this.baseUrl = options.baseUrl;
    }

    WebView.prototype = new Widget();
    WebView.prototype.constructor = WebView;

    /**
     * @return {WebView} WebView where this code is running.
     */
    WebView.getCurrent = function() {
        return window.currentWebView;
    };

    /**
     * Set the current web view.
     *
     * @param {WebView} webView
     */
    WebView.setCurrent = function(webView) {
        window.currentWebView = webView;
    };

    /**
     * Set the widgets size and position based on the position of their
     * place-holders.
     */
    WebView.prototype.layout = function() {
        var self = this;

        // Scan each place-holder
        /** @type {Array.<LayoutParams>} */
        var layoutParamsList = [];
        $(document.body).children('*[data-otm-widget]').each(function() {
            var $placeholder = $(this);
            /** @type {{left: Number, top: Number}} */
            var offset = $placeholder.offset();
            /** @type {Object.<String, String>} */
            var additionalParameters = {};
            var attributes = $placeholder.get(0).attributes;
            for (var i = 0; i < attributes.length; i += 1) {
                var attr = attributes.item(i);
                if (attr.nodeName.indexOf('data-otm-') === 0) {
                    additionalParameters[attr.nodeName.substring('data-otm-'.length)] = attr.nodeValue;
                }
            }

            layoutParamsList.push(new LayoutParams({
                'id': $placeholder.attr('id'),
                'width': $placeholder.width(),
                'height': $placeholder.height(),
                'x': offset.left,
                'y': offset.top,
                'visible': $placeholder.is(':visible'),
                'additionalParameters': additionalParameters
            }));
        });

        // Update the widgets
        _.each(layoutParamsList, function(layoutParams) {
            // Get the widget if it already exists
            var widget = Widget.findById(layoutParams.id);

            if (!widget) {
                self._createChildWidget(layoutParams);
            } else {
                self._updateChildWidget(layoutParams, widget);
            }
        });

        // Remove widgets that don't have place-holder anymore
        // TODO
    };

    /**
     * Create a widget.
     *
     * @param {LayoutParams} layoutParams
     * @private
     */
    WebView.prototype._createChildWidget = function(layoutParams) {
        switch (layoutParams.additionalParameters['widget']) {
            case 'WebView':
				// Create a WebView
				var childWebView = new WebView({
					id: layoutParams.id,
					url: layoutParams.additionalParameters['url'],
					entrypoint: layoutParams.additionalParameters['entrypoint'],
					baseUrl: this.baseUrl
				});
				childWebView.buildView(layoutParams);
                break;
            case 'Map':
				// TODO support more widgets
				break;
        }
    };

    /**
     * Update a widget.
     *
     * @param {LayoutParams} layoutParams
     * @param {Widget} widget
     * @private
     */
    WebView.prototype._updateChildWidget = function(layoutParams, widget) {
        // TODO
    };

	/**
     * Build the native view object for the current widget.
     * 
     * @param {LayoutParams} layoutParams
     */
    Widget.prototype.buildView = function(layoutParams) {
		var self = this;
		
		// Create an iframe
		var iframe = document.createElement('iframe');
		iframe.id = 'webview-' + layoutParams.id;
		iframe.src = self.baseUrl + layoutParams.additionalParameters['url'];
		iframe.style.position = 'absolute';
		iframe.style.left = layoutParams.x + 'px';
		iframe.style.top = layoutParams.y + 'px';
		iframe.style.width = layoutParams.width + 'px';
		iframe.style.height = layoutParams.height + 'px';
		iframe.style.border = 'none';
		iframe.style.visibility = layoutParams.visible ? 'visible' : 'hidden';
		
		iframe.onload = function onLoad() {
			// Inject the startup script
			iframe.contentWindow.webviewId = layoutParams.id;
			iframe.contentWindow.webviewUrl = layoutParams.additionalParameters['url'];
			iframe.contentWindow.webviewEntrypoint = layoutParams.additionalParameters['entrypoint'];
			iframe.contentWindow.webviewBaseUrl = self.baseUrl;
			var script = iframe.contentDocument.createElement('script');
			script.src = self.baseUrl + 'lib/require.min.js';
			script.setAttribute('data-main', self.baseUrl + 'org/opentravelmate/widget/webview/startupScript');
			iframe.contentDocument.body.appendChild(script);
		};
		
		document.body.appendChild(iframe);
	};

    return WebView;
});
