/**
 * Define the extension entry point.
 *
 * @author marc.plouhinec@gmail.com (Marc Plouhinec)
 */

define([
    'jquery',
    'org/opentravelmate/widget/webview/WebView'
], function($, WebView) {
    'use strict';

    /**
     * Extension entry point.
     */
    return function startup() {
        // Add the main menu place-holder
        $('body').append('<div ' +
            'id="main-menu" ' +
            'data-widget="WebView" ' +
            'data-url="extension/org/opentravelmate/mainmenu/mainmenu.html"' +
            'style="position: absolute; left: 0px; top: 0px; right: 0px; height: 80px;">' +
            '</div>');

        // Add the map place-holder
        // TODO

        // Layout the widgets
        WebView.getCurrent().layout();
    };
});
