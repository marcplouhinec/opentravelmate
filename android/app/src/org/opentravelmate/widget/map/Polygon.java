package org.opentravelmate.widget.map;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Define a polygon to show on the map.
 *
 * @author marc.plouhinec@gmail.com (Marc Plouhinec)
 */
public class Polygon {
	
	/**
	 * Generated polygon ID.
	 */
	public final int id;
	
	/**
	 * Polygon points on the map.
	 */
	public final List<LatLng> path;
	
	/**
	 * Color in the format 0xOORRGGBB where
     * OO is the opacity (FF = opaque, 00 = transparent),
     * RR, GG, BB are the red, green and blue colors.
	 */
	public final int fillColor;
	
	/**
	 * Color in the format 0xOORRGGBB where
     * OO is the opacity (FF = opaque, 00 = transparent),
     * RR, GG, BB are the red, green and blue colors.
	 */
	public final int strokeColor;
	
	/**
	 * Width of the stroke in pixels.
	 */
	public final int strokeWidth;

	/**
	 * Create a new polygon.
	 * 
	 * @param id
	 * @param path
	 * @param fillColor
	 * @param strokeColor
	 * @param strokeWidth
	 */
	public Polygon(int id, List<LatLng> path, int fillColor, int strokeColor, int strokeWidth) {
		this.id = id;
		this.path = path;
		this.fillColor = fillColor;
		this.strokeColor = strokeColor;
		this.strokeWidth = strokeWidth;
	}

	/**
	 * Build a Polygon from a JSON-serialized representation.
	 * 
	 * @param jsonPolygon
	 * @return Polygon
	 * @throws JSONException
	 */
	public static Polygon fromJsonPolygon(JSONObject jsonPolygon) throws JSONException {
		return new Polygon(
				jsonPolygon.getInt("id"),
				LatLng.fromJsonLatLngArray(jsonPolygon.getJSONArray("path")),
				jsonPolygon.getInt("fillColor"),
				jsonPolygon.getInt("strokeColor"),
				jsonPolygon.getInt("strokeWidth"));
	}
	
	/**
	 * Build an array of Polygons from a JSON-serialized representation.
	 * 
	 * @param jsonPolygons
	 * @return Array of Polygons
	 * @throws JSONException
	 */
	public static List<Polygon> fromJsonPolygonArray(JSONArray jsonPolygons) throws JSONException {
		List<Polygon> polygons = new ArrayList<Polygon>(jsonPolygons.length());
		for (int i = 0; i < jsonPolygons.length(); i++) {
			polygons.add(fromJsonPolygon(jsonPolygons.getJSONObject(i)));
		}
		return polygons;
	}
}
