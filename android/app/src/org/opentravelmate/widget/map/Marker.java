package org.opentravelmate.widget.map;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Define a map marker.
 *
 * @author marc.plouhinec@gmail.com (Marc Plouhinec)
 */
public class Marker {
	
	/**
	 * Marker ID.
	 */
	public final int id;
	
	/**
	 * Marker position on the map.
	 */
	public final LatLng position;
	
	/**
	 * Marker title.
	 */
	public final String title;
	
	/**
	 * Marker icon.
	 */
	public final MarkerIcon icon;
	
	/**
	 * Create a new Marker.
	 * 
	 * @param id
	 * @param position
	 * @param title
	 * @param icon
	 */
	public Marker(int id, LatLng position, String title, MarkerIcon icon) {
		this.id = id;
		this.position = position;
		this.title = title;
		this.icon = icon;
	}
	
	/**
	 * @return JSON-serialized Marker
	 * @throws JSONException
	 */
	public JSONObject toJson() throws JSONException {
		JSONObject jsonMarker = new JSONObject();
		jsonMarker.put("id", id);
		jsonMarker.put("position", position.toJson());
		jsonMarker.put("title", title);
		if (icon instanceof UrlMarkerIcon) {
			jsonMarker.put("icon", ((UrlMarkerIcon)icon).toJson());
		}
		return jsonMarker;
	}
	
	/**
	 * Build a Marker from a JSON-serialized representation.
	 * 
	 * @param jsonMarker
	 * @return Marker
	 * @throws JSONException
	 */
	public static Marker fromJsonMarker(JSONObject jsonMarker) throws JSONException {
		return new Marker(
				jsonMarker.getInt("id"),
				LatLng.fromJsonLatLng(jsonMarker.getJSONObject("position")),
				jsonMarker.getString("title"),
				jsonMarker.has("icon") && !jsonMarker.isNull("icon") ?
						UrlMarkerIcon.fromJsonUrlMarkerIcon(jsonMarker.getJSONObject("icon")) : null);
	}
	
	/**
	 * Build a Marker array from a JSON-serialized representation.
	 * 
	 * @param jsonMarkers
	 * @return List of markers
	 * @throws JSONException
	 */
	public static List<Marker> fromJsonMarkers(JSONArray jsonMarkers) throws JSONException {
		List<Marker> markers = new ArrayList<Marker>(jsonMarkers.length());
		for (int i = 0; i < jsonMarkers.length(); i++) {
			markers.add(fromJsonMarker(jsonMarkers.getJSONObject(i)));
		}
		return markers;
	}
}
