package org.opentravelmate.geolocation;

import org.json.JSONException;
import org.json.JSONObject;
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
	private final Geolocation geolocation;

	/**
	 * Create a NativeGeolocation.
	 */
	public NativeGeolocation(ExceptionListener exceptionListener, HtmlLayout htmlLayout, Geolocation geolocation) {
		this.exceptionListener = exceptionListener;
		this.htmlLayout = htmlLayout;
		this.geolocation = geolocation;
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
				try {
					PositionOptions positionOptions = PositionOptions.fromJson(new JSONObject(jsonOptions));
					getCurrentLocation(callbacksId, positionOptions);
				} catch (JSONException e) {
					exceptionListener.onException(false, e);
				}
			}
		});
	}
	
	/**
	 * Get the current device location.
	 * Note: this function must be called in the UI thread.
	 * 
	 * @param callbacksId
	 */
	private void getCurrentLocation(final String callbacksId, PositionOptions positionOptions) {
		geolocation.getCurrentPosition(new PositionCallback() {
			@Override public void on(Position position) {
				try {
					WebView mainWebView = (WebView)htmlLayout.findViewByPlaceHolderId(HtmlLayout.MAIN_WEBVIEW_ID);
					mainWebView.loadUrl("javascript:(function(){" +
							"    require(['extensions/core/geolocation/geolocation'], function (geolocation) {" +
							"        geolocation.fireCurrentPositionEvent(\"" + callbacksId + "\", " + position.toJson().toString(2) + ", null);" +
							"    });" +
							"})();");
				} catch (JSONException e) {
					exceptionListener.onException(false, e);
				}
			}
		}, new PositionErrorCallback() {
			@Override public void on(PositionError positionError) {
				try {
					WebView mainWebView = (WebView)htmlLayout.findViewByPlaceHolderId(HtmlLayout.MAIN_WEBVIEW_ID);
					mainWebView.loadUrl("javascript:(function(){" +
							"    require(['extensions/core/geolocation/geolocation'], function (geolocation) {" +
							"        geolocation.fireCurrentPositionEvent(\"" + callbacksId + "\", null, " + positionError.toJson().toString(2) + ");" +
							"    });" +
							"})();");
				} catch (JSONException e) {
					exceptionListener.onException(false, e);
				}
			}
		}, positionOptions);
	}
	
	/**
	 * Watch the device location.
	 * 
	 * @param callbacksId
     *     ID of geolocation success and error callbacks.
	 * @param jsonOptions
	 *     JSON-serialized PositionOptions.
	 */
	@JavascriptInterface
	public void watchPosition(final String callbacksId, final String jsonOptions) {
		// TODO
	}
	
	/**
     * Stop watching the device position.
     *
     * @param {String} callbacksId
     *     ID of geolocation success and error callbacks.
     */
	@JavascriptInterface
	public void clearWatch(final String callbacksId) {
		// TODO
	}
}
