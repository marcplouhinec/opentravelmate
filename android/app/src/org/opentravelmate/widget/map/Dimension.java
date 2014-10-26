package org.opentravelmate.widget.map;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Define a rectangle dimension with width and height.
 *
 * @author Marc Plouhinec
 */
public class Dimension {
	
	public final int width;
	public final int height;
	
	public Dimension(int width, int height) {
		this.width = width;
		this.height = height;
	}
	
	/**
	 * @return JSON-serialized Dimension
	 * @throws JSONException
	 */
	public JSONObject toJson() throws JSONException {
		JSONObject jsonDimension = new JSONObject();
		jsonDimension.put("width", width);
		jsonDimension.put("height", height);
		return jsonDimension;
	}

	/**
	 * Build a Dimension from a JSON-serialized representation.
	 * 
	 * @param jsonDimension
	 * @return Dimension
	 * @throws JSONException
	 */
	public static Dimension fromJsonDimension(JSONObject jsonDimension) throws JSONException {
		return new Dimension(jsonDimension.getInt("width"), jsonDimension.getInt("height"));
	}
}
