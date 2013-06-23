/**
 * Main script executed in the root web view.
 *
 * @author marc.plouhinec@opentravelmate.org (Marc Plouhinec)
 */

requirejs.config({
    shim: {
        'jquery': {
            exports: '$'
        }
    }
});

require(['org/opentravelmate/widget/webview/v1/WebView', 'org/opentravelmate/widget/mainmenu/v1/MainMenu', 'jquery'], function(WebView, MainMenu, $) {
	var rootWebView = WebView.getRootWebView();
	document.getElementById('test').innerHTML = 'rootWebView --> ' + rootWebView.getId() + ' width = ' + $(window).width();
	MainMenu.createMainMenu('test2');
});
