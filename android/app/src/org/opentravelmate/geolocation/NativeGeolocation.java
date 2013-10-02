package org.opentravelmate.geolocation;

import java.util.Date;

import org.json.JSONException;
import org.opentravelmate.commons.ExceptionListener;
import org.opentravelmate.commons.UIThreadExecutor;
import org.opentravelmate.widget.HtmlLayout;

import android.webkit.JavascriptInterface;
import android.webkit.WebView;

/**
 * Injected object.
 * 
 * @author marc.plouhinec@gmail.com (Marc Plouhinec)
 */
public class NativeGeolocation {
	
	public static final String GLOBAL_OBJECT_NAME = "org_opentravelmate_native_geolocation_nativeGeolocation";
	public static final String SCRIPT_URL = "/native/geolocation/nativeGeolocation.js";
	
	private final ExceptionListener exceptionListener;
	private final HtmlLayout htmlLayout;

	/**
	 * Create a NativeGeolocation.
	 */
	public NativeGeolocation(ExceptionListener exceptionListener, HtmlLayout htmlLayout) {
		this.exceptionListener = exceptionListener;
		this.htmlLayout = htmlLayout;
	}

	/**
	 * Get the current device location.
	 * 
	 * @param callbacksId
     *     ID of geolocation success and error callbacks.
	 * @param jsonOptions
	 *     JSON-serialized PositionOptions.
	 */
	@JavascriptInterface
	public void getCurrentPosition(final String callbacksId, final String jsonOptions) {
		UIThreadExecutor.execute(new Runnable() {
			@Override public void run() {
				// TODO
				Position position = new Position(new Coordinates(49.611621, 6.1319349, 0, 140000, 0, 0, 0), new Date().getTime());
				WebView mainWebView = (WebView)htmlLayout.findViewByPlaceHolderId(HtmlLayout.MAIN_WEBVIEW_ID);
				try {
					mainWebView.loadUrl("javascript:(function(){" +
							"    require(['extensions/core/geolocation/geolocation'], function (geolocation) {" +
							"        geolocation.fireCurrentPositionEvent(\"" + callbacksId + "\", " + position.toJson().toString(2) + ", null);" +
							"    });" +
							"})();");
				} catch (JSONException e) {
					exceptionListener.onException(false, e);
				}
			}
		});
	}
}
