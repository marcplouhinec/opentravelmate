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
	
	public void updatePlaceHolderLayoutParams(int windowWidth, int windowHeight, Object placeholderLayoutParams) {
		System.out.println("windowWidth = " + windowWidth);
		System.out.println("windowHeight = " + windowHeight);
		System.out.println("placeholderLayoutParams = " + placeholderLayoutParams);
	}

	public void registerWebViewClass(Object webViewClass) {
		System.out.println("webViewClass = " + webViewClass);
	}
}
