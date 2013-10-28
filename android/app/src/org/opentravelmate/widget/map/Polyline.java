package org.opentravelmate.widget.map;

import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Define a polyline to show on the map.
 *
 * @author marc.plouhinec@gmail.com (Marc Plouhinec)
 */
public class Polyline {
	
	/**
	 * Generated polyline ID.
	 */
	public final int id;
	
	/**
	 * Polyline points on the map.
	 */
	public final List<LatLng> path;
	
	/**
	 * Color in the format 0xOORRGGBB where
     * OO is the opacity (FF = opaque, 00 = transparent),
     * RR, GG, BB are the red, green and blue colors.
	 */
	public final int color;
	
	/**
	 * Width of the line in pixels.
	 */
	public final int width;
	
	/**
	 * Create a new polyline.
	 * 
	 * @param id
	 * @param path
	 * @param color
	 * @param width
	 */
	public Polyline(int id, List<LatLng> path, int color, int width) {
		this.id = id;
		this.path = path;
		this.color = color;
		this.width = width;
	}

	/**
	 * Build a Polyline from a JSON-serialized representation.
	 * 
	 * @param jsonPolyline
	 * @return Polyline
	 * @throws JSONException
	 */
	public static Polyline fromJsonPolyline(JSONObject jsonPolyline) throws JSONException {
		return new Polyline(
				jsonPolyline.getInt("id"),
				LatLng.fromJsonLatLngArray(jsonPolyline.getJSONArray("path")),
				jsonPolyline.getInt("color"),
				jsonPolyline.getInt("width"));
	}
}
