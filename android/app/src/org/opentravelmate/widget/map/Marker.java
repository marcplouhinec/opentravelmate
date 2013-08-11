package org.opentravelmate.widget.map;

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
	 * Relative position of the marker icon.
	 */
	public final Point anchorPoint;
	
	public Marker(int id, LatLng position, String title, Point anchorPoint) {
		this.id = id;
		this.position = position;
		this.title = title;
		this.anchorPoint = anchorPoint;
	}
	
	/**
	 * Build a Marker from a JSON-serialized representation.
	 * 
	 * @param jsonMarker
	 * @return Marker
	 * @throws JSONException
	 */
	public static Marker fromJsonMarker(JSONObject jsonMarker) throws JSONException {
		JSONObject jsonAnchorPoint = jsonMarker.get("anchorPoint") == null ? jsonMarker.getJSONObject("anchorPoint") : null;
		return new Marker(
				jsonMarker.getInt("id"),
				LatLng.fromJsonLatLng(jsonMarker.getJSONObject("position")),
				jsonMarker.getString("title"),
				jsonAnchorPoint == null ? null : Point.fromJsonPoint(jsonAnchorPoint));
	}
}
