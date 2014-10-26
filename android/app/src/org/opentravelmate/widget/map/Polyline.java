package org.opentravelmate.widget.map;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Define a polyline to show on the map.
 *
 * @author Marc Plouhinec
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
	
	/**
	 * Build a list of Polylines from a JSON-serialized representation.
	 * 
	 * @param jsonPolylines
	 * @return List of polylines
	 * @throws JSONException
	 */
	public static List<Polyline> fromJsonPolylines(JSONArray jsonPolylines) throws JSONException {
		List<Polyline> polylines = new ArrayList<Polyline>(jsonPolylines.length());
		
		for (int i = 0; i < jsonPolylines.length(); i++) {
			polylines.add(fromJsonPolyline(jsonPolylines.getJSONObject(i)));
		}
		
		return polylines;
	}
}
