/**
 * Define the main view wrapper entry point.
 *
 * @author marc.plouhinec@gmail.com (Marc Plouhinec)
 */

define([
    'core/widget/webview/webview'
], function(webview) {
    'use strict';

    /**
     * Main view wrapper entry point.
     */
    return function main() {
        webview.layout();
    };
});