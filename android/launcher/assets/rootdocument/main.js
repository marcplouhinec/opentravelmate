/**
 * Main script executed in the root web view.
 *
 * @author marc.plouhinec@opentravelmate.org (Marc Plouhinec)
 */

require(['org/opentravelmate/widget/webview/v1/WebView', 'org/opentravelmate/widget/mainmenu/v1/MainMenu'], function(WebView, MainMenu) {
	var rootWebView = WebView.getRootWebView();
	document.getElementById('test').innerHTML = 'rootWebView --> ' + rootWebView.getId();
	MainMenu.createMainMenu('test2');
});