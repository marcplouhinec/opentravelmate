package org.opentravelmate.widget.webview.js;

import android.webkit.JavascriptInterface;

/**
 * Implementation of the webViewJavaObject interface.
 * 
 * @author Marc Plouhinec
 */
public class WebViewJavaObject {
	
	/**
	 * Get the root web view ID.
	 * 
	 * @return web view ID.
	 */
	@JavascriptInterface
	public String getRootWebViewId() {
		return "FAKE-ID";
	}

}
