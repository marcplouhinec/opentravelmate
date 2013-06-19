package org.opentravelmate.widget.webview;

import java.util.LinkedHashMap;
import java.util.Map;

import org.opentravelmate.commons.LibraryRegistrar;

import android.webkit.WebView;

/**
 * Inject the libraries into the web views.
 * 
 * @author Marc Plouhinec
 */
public class WebViewLibraryRegistrar implements LibraryRegistrar {
	
	private final RegisteredResourcesListener registeredResourcesListener;
	private Map<String, Object> objectsToInjectByGlobalName = new LinkedHashMap<String, Object>();
	
	/**
	 * Create a WebViewLibraryRegistrar.
	 * 
	 * @param registeredResourcesListener
	 */
	public WebViewLibraryRegistrar(RegisteredResourcesListener registeredResourcesListener) {
		this.registeredResourcesListener = registeredResourcesListener;
	}
	
	/**
	 * Inject all the registered java objects into the given web view.
	 * 
	 * @param webView
	 */
	public void injectJavaObjects(WebView webView) {
		for (Map.Entry<String, Object> entry : objectsToInjectByGlobalName.entrySet()) {
			webView.addJavascriptInterface(entry.getValue(), entry.getKey());
		}
	}

	@Override
	public void registerJavaObject(Object javaObject, String globalName, String requirejsUrl) {
		objectsToInjectByGlobalName.put(globalName, javaObject);
		registeredResourcesListener.onJavaObjectInjected(globalName, requirejsUrl);
	}

	@Override
	public void registerResources(String folderClassPath, String urlPrefix) {
		registeredResourcesListener.onResourcesRegistered(folderClassPath, urlPrefix);
	}
	
	/**
	 * Listener for resources registration.
	 */
	public static interface RegisteredResourcesListener {
		void onResourcesRegistered(String folderClassPath, String urlPrefix);
		void onJavaObjectInjected(String globalName, String requirejsUrl);
	}

}
