package org.opentravelmate.widget.map;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Define an URL map marker icon.
 *
 * @author marc.plouhinec@gmail.com (Marc Plouhinec)
 */
public class UrlMarkerIcon extends MarkerIcon {

	/**
	 * Marker icon URL.
	 */
	public final String url;

	/**
	 * Create a new UrlMarkerIcon.
	 * 
	 * @param anchor
	 * @param size
	 * @param url
	 */
	public UrlMarkerIcon(Point anchor, Dimension size, String url) {
		super(anchor, size);
		this.url = url;
	}
	
	/**
	 * @return JSON-serialized UrlMarkerIcon
	 * @throws JSONException
	 */
	public JSONObject toJson() throws JSONException {
		JSONObject jsonUrlMarkerIcon = new JSONObject();
		jsonUrlMarkerIcon.put("anchor", anchor.toJson());
		jsonUrlMarkerIcon.put("size", size.toJson());
		jsonUrlMarkerIcon.put("url", url);
		return jsonUrlMarkerIcon;
	}
	
	/**
	 * Build a Point from a JSON-serialized representation.
	 * 
	 * @param jsonUrlMarkerIcon
	 * @return UrlMarkerIcon
	 * @throws JSONException
	 */
	public static UrlMarkerIcon fromJsonUrlMarkerIcon(JSONObject jsonUrlMarkerIcon) throws JSONException {
		return new UrlMarkerIcon(
				Point.fromJsonPoint(jsonUrlMarkerIcon.getJSONObject("anchor")),
				Dimension.fromJsonDimension(jsonUrlMarkerIcon.getJSONObject("size")),
				jsonUrlMarkerIcon.getString("url"));
	}
}
