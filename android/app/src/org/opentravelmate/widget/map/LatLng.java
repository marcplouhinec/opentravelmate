package org.opentravelmate.widget.map;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
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
	 * @return JSON-serialized LatLng
	 * @throws JSONException
	 */
	public JSONObject toJson() throws JSONException {
		JSONObject jsonLatLng = new JSONObject();
		jsonLatLng.put("lat", lat);
		jsonLatLng.put("lng", lng);
		return jsonLatLng;
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

	/**
	 * Build a list of LatLng from a JSON-serialized representation.
	 * 
	 * @param jsonLatLngArray
	 * @return List of LatLng
	 * @throws JSONException
	 */
	public static List<LatLng> fromJsonLatLngArray(JSONArray jsonLatLngArray) throws JSONException {
		List<LatLng> latLngList = new ArrayList<LatLng>(jsonLatLngArray.length());
		
		for (int i = 0; i < jsonLatLngArray.length(); i++) {
			latLngList.add(fromJsonLatLng(jsonLatLngArray.getJSONObject(i)));
		}
		
		return latLngList;
	}
}
