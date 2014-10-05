/**
 * Define the main view wrapper entry point.
 *
 * @author Marc Plouhinec
 */

define([
    './extensions/org/opentravelmate/controller/widget/webview/webview'
], function(webview) {
    'use strict';

    /**
     * Main view wrapper entry point.
     */
    return function main() {
        webview.layout();
    };
});