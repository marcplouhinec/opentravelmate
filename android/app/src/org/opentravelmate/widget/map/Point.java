package org.opentravelmate.widget.map;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Define a position with x and y coordinates.
 *
 * @author marc.plouhinec@gmail.com (Marc Plouhinec)
 */
public class Point {
	
	/**
	 * x coordinate.
	 */
	public final double x;
	
	/**
	 * y coordinate.
	 */
	public final double y;
	
	/**
	 * Create a new point.
	 * 
	 * @param x
	 * @param y
	 */
	public Point(double x, double y) {
		this.x = x;
		this.y = y;
	}
	
	/**
	 * Build a Point from a JSON-serialized representation.
	 * 
	 * @param jsonPoint
	 * @return Point
	 * @throws JSONException
	 */
	public static Point fromJsonPoint(JSONObject jsonPoint) throws JSONException {
		return new Point(jsonPoint.getDouble("x"), jsonPoint.getDouble("y"));
	}
}
