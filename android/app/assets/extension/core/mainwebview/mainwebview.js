/**
 * Define the main view entry point.
 *
 * @author marc.plouhinec@gmail.com (Marc Plouhinec)
 */

define([
    'org/opentravelmate/widget/webview/WebView'
], function(WebView) {
    'use strict';

    /**
     * Extension entry point.
     */
    return function main() {
        // Layout the widgets
        WebView.getCurrent().layout();
    };
});
