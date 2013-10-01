package org.opentravelmate.widget.map;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Define a button that will be displayed on the map.
 * 
 * @author marc.plouhinec@gmail.com (Marc Plouhinec)
 */
public class MapButton {
	
	public final int id;
	public final String tooltip;
	public final String iconUrl;
	
	/**
	 * Create a new MapButton.
	 * 
	 * @param id
	 * @param tooltip
	 * @param iconUrl
	 */
	public MapButton(int id, String tooltip, String iconUrl) {
		this.id = id;
		this.tooltip = tooltip;
		this.iconUrl = iconUrl;
	}
	
	/**
	 * Build a LatLng from a JSON-serialized representation.
	 * 
	 * @param jsonLatLng
	 * @return LatLng
	 * @throws JSONException
	 */
	public static MapButton fromJsonMapButton(JSONObject jsonMapButton) throws JSONException {
		return new MapButton(jsonMapButton.getInt("id"), jsonMapButton.getString("tooltip"), jsonMapButton.getString("iconUrl"));
	}
}
