package org.opentravelmate.widget.map;

import java.util.HashMap;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;
import org.opentravelmate.R;
import org.opentravelmate.commons.ExceptionListener;
import org.opentravelmate.commons.UIThreadExecutor;
import org.opentravelmate.widget.HtmlLayout;
import org.opentravelmate.widget.HtmlLayoutParams;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;

import com.google.android.gms.maps.model.MarkerOptions;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.webkit.JavascriptInterface;

/**
 * Injected object.
 * 
 * @author marc.plouhinec@gmail.com (Marc Plouhinec)
 */
public class NativeMap {
	
	public static final String GLOBAL_OBJECT_NAME = "org_opentravelmate_native_widget_map_nativeMap";
	public static final String SCRIPT_URL = "/native/widget/map/nativeMap.js";
	private static final float DEFAULT_ZOOM = 13;
	private static final double DEFAULT_LATITUDE = 49.6;
	private static final double DEFAULT_LONGITUDE = 6.135;
	
	private final ExceptionListener exceptionListener;
	private final HtmlLayout htmlLayout;
	private final LayoutInflater layoutInflater;
	private final FragmentManager fragmentManager;
	private final Map<Integer, com.google.android.gms.maps.model.Marker> gmarkerById =
			new HashMap<Integer, com.google.android.gms.maps.model.Marker>();
	
	/**
	 * Create a NativeMap object.
	 */
	public NativeMap(ExceptionListener exceptionListener, HtmlLayout htmlLayout, FragmentManager fragmentManager) {
		this.exceptionListener = exceptionListener;
		this.htmlLayout = htmlLayout;
		this.layoutInflater = (LayoutInflater) htmlLayout.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		this.fragmentManager = fragmentManager;
	}

	/**
	 * Build the native view object for the current widget.
	 * 
	 * @param jsonLayoutParams
	 */
	@JavascriptInterface
	public void buildView(final String jsonLayoutParams) {
		UIThreadExecutor.execute(new Runnable() {
			@Override public void run() {
				try {
					buildView(HtmlLayoutParams.fromJsonLayoutParams(new JSONObject(jsonLayoutParams)));
				} catch (JSONException e) {
					exceptionListener.onException(false, e);
				}
			}
		});
	}
	
	/**
	 * Update the native view object for the current widget.
	 * 
	 * @param jsonLayoutParams
	 */
	@JavascriptInterface
	public void updateView(final String jsonLayoutParams) {
		UIThreadExecutor.execute(new Runnable() {
			@Override public void run() {
				try {
					HtmlLayoutParams layoutParams = HtmlLayoutParams.fromJsonLayoutParams(new JSONObject(jsonLayoutParams));
					View view = htmlLayout.findViewByPlaceHolderId(layoutParams.id);
					if (view != null) {
						view.setLayoutParams(layoutParams);
					}
				} catch (JSONException e) {
					exceptionListener.onException(false, e);
				}
			}
		});
	}
	
	/**
	 * Remove the native view object for the current widget.
	 * 
	 * @param id Place holder ID
	 */
	@JavascriptInterface
	public void removeView(final String id) {
		UIThreadExecutor.execute(new Runnable() {
			@Override public void run() {
				View view = htmlLayout.findViewByPlaceHolderId(id);
				if (view != null) {
					htmlLayout.removeView(view);
				}
			}
		});
	}
	
	/**
	 * Build the native view object for the current widget.
	 * 
	 * @param layoutParams
	 */
	@SuppressLint("SetJavaScriptEnabled")
	public void buildView(final HtmlLayoutParams layoutParams) {
		View view = layoutInflater.inflate(R.layout.map_layout, htmlLayout, false);
		view.setLayoutParams(layoutParams);
		htmlLayout.addView(view);
		
		GoogleMap map = ((SupportMapFragment) fragmentManager.findFragmentById(R.id.map)).getMap();
		CameraPosition cameraPosition = new CameraPosition.Builder()
			.target(new com.google.android.gms.maps.model.LatLng(DEFAULT_LATITUDE, DEFAULT_LONGITUDE))
			.zoom(DEFAULT_ZOOM)
			.build();
		map.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
	}
	
	/**
	 * Move the map center to the given location.
	 * 
	 * @param id
	 *     Map place holder ID.
	 * @param jsonCenter
	 *     JSON serialized LatLng.
	 */
	@JavascriptInterface
	public void panTo(final String id, final String jsonCenter) {
		UIThreadExecutor.execute(new Runnable() {
			@Override public void run() {
				try {
					LatLng center = LatLng.fromJsonLatLng(new JSONObject(jsonCenter));
					GoogleMap map = ((SupportMapFragment) fragmentManager.findFragmentById(R.id.map)).getMap();
					CameraPosition cameraPosition = new CameraPosition.Builder()
						.target(new com.google.android.gms.maps.model.LatLng(center.lat, center.lng))
						.zoom(map.getCameraPosition().zoom)
						.build();
					map.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
				} catch (JSONException e) {
					exceptionListener.onException(false, e);
				}
			}
		});
	}
	
	/**
	 * Add a marker on the map.
	 * 
	 * @param id
	 *     Map place holder ID.
	 * @param jsonMarker
     *     JSON serialized Marker.
	 */
	@JavascriptInterface
	public void addMarker(final String id, final String jsonMarker) {
		UIThreadExecutor.execute(new Runnable() {
			@Override public void run() {
				try {
					Marker marker = Marker.fromJsonMarker(new JSONObject(jsonMarker));
					GoogleMap map = ((SupportMapFragment) fragmentManager.findFragmentById(R.id.map)).getMap();
					
					MarkerOptions markerOptions = new MarkerOptions()
						.position(new com.google.android.gms.maps.model.LatLng(marker.position.lat, marker.position.lng))
						.title(marker.title); 
					if (marker.anchorPoint != null) {
						markerOptions.anchor((float)marker.anchorPoint.x, (float)marker.anchorPoint.y);
					}
					
					gmarkerById.put(marker.id, map.addMarker(markerOptions));
				} catch (JSONException e) {
					exceptionListener.onException(false, e);
				}
			}
		});
	}
	
	/**
	 * Remove a marker from the map.
	 * 
	 * @param id
	 *     Map place holder ID.
	 * @param jsonMarker
     *     JSON serialized Marker.
	 */
	@JavascriptInterface
	public void removeMarker(final String id, final String jsonMarker) {
		UIThreadExecutor.execute(new Runnable() {
			@Override public void run() {
				try {
					Marker marker = Marker.fromJsonMarker(new JSONObject(jsonMarker));
					gmarkerById.get(marker.id).remove();
					gmarkerById.remove(marker.id);
				} catch (JSONException e) {
					exceptionListener.onException(false, e);
				}
			}
		});
	}
}
