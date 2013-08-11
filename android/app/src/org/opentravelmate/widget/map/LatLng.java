package org.opentravelmate.widget.map;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Define a geographic location with latitude and longitude coordinates.
 *
 * @author marc.plouhinec@gmail.com (Marc Plouhinec)
 */
public class LatLng {
	
	/**
	 * Latitude.
	 */
	public final double lat;
	
	/**
	 * Longitude.
	 */
	public final double lng;
	
	/**
	 * Create a new LatLng.
	 * 
	 * @param lat
	 * @param lng
	 */
	public LatLng(double lat, double lng) {
		this.lat = lat;
		this.lng = lng;
	}
	
	/**
	 * Build a LatLng from a JSON-serialized representation.
	 * 
	 * @param jsonLatLng
	 * @return LatLng
	 * @throws JSONException
	 */
	public static LatLng fromJsonLatLng(JSONObject jsonLatLng) throws JSONException {
		return new LatLng(jsonLatLng.getDouble("lat"), jsonLatLng.getDouble("lng"));
	}

}
