package org.opentravelmate.geolocation;

import org.json.JSONException;
import org.opentravelmate.commons.ExceptionListener;
import org.opentravelmate.commons.UIThreadExecutor;
import org.opentravelmate.geolocation.UserLocationProvider.Strategy;
import org.opentravelmate.widget.HtmlLayout;

import android.location.Location;
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
				// TODO take care of jsonOptions
				getCurrentLocation(callbacksId);
			}
		});
	}
	
	/**
	 * Get the current device location.
	 * Note: this function must be called in the UI thread.
	 * 
	 * @param callbacksId
	 */
	private void getCurrentLocation(final String callbacksId) {
		UserLocationProvider.getInstance().addListener(Strategy.CURRENT_POSITION, new UserLocationProvider.Listener() {
			@Override public void onLocationprovided(Location location) {
				// Check if the user location is good enough
				if (UserLocationProvider.ACCEPTABLE_ACCURACY >= location.getAccuracy()) {
					Position position = new Position(new Coordinates(
							location.getLatitude(),
							location.getLongitude(),
							location.getAltitude(),
							location.getAccuracy(),
							location.getAccuracy(),
							0,
							location.getSpeed()), location.getTime());
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
			}
		});
	}
}
