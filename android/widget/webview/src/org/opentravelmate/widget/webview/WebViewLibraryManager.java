package org.opentravelmate.widget.webview;

import org.opentravelmate.commons.LibraryManager;
import org.opentravelmate.commons.LibraryRegistrar;
import org.opentravelmate.commons.utils.ExceptionListener;
import org.opentravelmate.commons.utils.InternationalizedException;
import org.opentravelmate.widget.webview.js.WebViewJavaObject;
import org.opentravelmate.widget.webview.view.HtmlLayout;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.webkit.ConsoleMessage;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;

/**
 * Manage the WebView library.
 * 
 * @author Marc Plouhinec
 */
public class WebViewLibraryManager implements LibraryManager {

	private final WebViewJavaObject webViewJavaObject = new WebViewJavaObject();
	private Activity activity;
	private WebViewLibraryRegistrar webViewLibraryRegistrar;
	private ExceptionListener exceptionListener;
	private String httpServerUrl;
	private ViewRepository viewRepository = new ViewRepository();

	@Override
	public void initialize(Activity activity, LibraryRegistrar libraryRegistrar, ExceptionListener exceptionListener) {
		this.activity = activity;
		this.webViewLibraryRegistrar = (WebViewLibraryRegistrar)libraryRegistrar;
		this.exceptionListener = exceptionListener;
		
		libraryRegistrar.registerResources("/js/org/opentravelmate/widget/webview/v1", "/org/opentravelmate/widget/webview/v1");
		libraryRegistrar.registerJavaObject(
				this.webViewJavaObject,
				"org_opentravelmate_widget_webview_webViewJavaObject",
				"/org/opentravelmate/widget/webview/v1/webViewJavaObject.js");
	}
	
	/**
	 * Set the base URL of the server.
	 * 
	 * @param httpServerUrl
	 *   URL in the form "http://host:port"
	 */
	public void setHttpServerUrl(String httpServerUrl) {
		this.httpServerUrl = httpServerUrl;
	}

	@Override
	public void start() {
		// Create the root web view
		HtmlLayout htmlLayout = new HtmlLayout(activity);
		htmlLayout.setId(123456789);
		WebView rootWebView = createWebView("rootWebView");
		htmlLayout.addView(rootWebView);
		activity.setContentView(htmlLayout);
		
		// Display the root document in the root web view
		rootWebView.loadUrl(httpServerUrl + "/index.html");
	}
	
	/**
	 * Create and initialize a web view.
	 * 
	 * @param viewId
	 * @return web view
	 */
	@SuppressLint("SetJavaScriptEnabled")
	private WebView createWebView(final String viewId) {
		// Create the web view
		WebView webView = new WebView(activity);
		webView.setId(viewRepository.put(viewId, webView));
		webView.getSettings().setJavaScriptEnabled(true);
		
		// Register error handler
		webView.setWebViewClient(new WebViewClient() {
			@Override public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
				exceptionListener.onException(false, new InternationalizedException(
						R.string.webview_received_error, null, viewId, description));
			}
		});
		webView.setWebChromeClient(new WebChromeClient() {
			@Override public boolean onConsoleMessage(ConsoleMessage cm) {
				exceptionListener.onException(false, new InternationalizedException(
						R.string.webview_javascript_console_msg, null, viewId, cm.message(), cm.lineNumber(), cm.sourceId()));
		    return true;
		  }
		});
		
		// Inject java objects
		webViewLibraryRegistrar.injectJavaObjects(webView);
		
		return webView;
	}

}
