package org.opentravelmate.widget;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import android.view.ViewGroup;

/**
 * Define a set of HTML layout parameters.
 *
 * @author marc.plouhinec@gmail.com (Marc Plouhinec)
 */
public class HtmlLayoutParams extends ViewGroup.LayoutParams {
	
	/**
	 * ID of the place-holder element in the HTML document.
	 */
	public final String id;
	
	/**
	 * Abscissa of the widget (min = 0, max = 1).
	 */
	public final double x;
	
	/**
	 * Ordinate of the widget (min = 0, max = 1).
	 */
	public final double y;
	
	/**
	 * Width of the widget (min = 0, max = 1).
	 */
	public final double width;
	
	/**
	 * Height of the widget (min = 0, max = 1).
	 */
	public final double height;
	
	/**
	 * if true, the widget is visible, if false, the widget is not visible.
	 */
	public final boolean visible;
	
	/**
	 * Additional parameters defined with place-holder attributes with a name starting from "data-otm-".
	 */
	public final Map<String, String> additionalParameters;
	
	/**
	 * HTML Window width in pixels (according to the default Webview scale ratio).
	 */
	public final int windowWidth;
	
	/**
	 * HTML Window height in pixels (according to the default Webview scale ratio).
	 */
	public final int windowHeight;
	
	/**
	 * Create a new LayoutParams object.
	 */
	public HtmlLayoutParams(
			String id,
			double x,
			double y,
			double width,
			double height,
			boolean visible,
			Map<String, String> additionalParameters,
			int windowWidth,
			int windowHeight) {
		super(0, 0);
		this.id = id;
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
		this.visible = visible;
		this.additionalParameters = additionalParameters;
		this.windowWidth = windowWidth;
		this.windowHeight = windowHeight;
	}
	
	/**
	 * Build a HtmlLayoutParams from a JSON-serialized LayoutParams.
	 * 
	 * @param jsonLayoutParams
	 * @return HtmlLayoutParams
	 * @throws JSONException
	 */
	public static HtmlLayoutParams fromJsonLayoutParams(JSONObject jsonLayoutParams) throws JSONException {
		int windowWidth = jsonLayoutParams.getInt("windowWidth");
		int windowHeight = jsonLayoutParams.getInt("windowHeight");
		String id = jsonLayoutParams.getString("id");
		double x = jsonLayoutParams.getDouble("x") / windowWidth;
		double y = jsonLayoutParams.getDouble("y") / windowHeight;
		double width = jsonLayoutParams.getDouble("width") / windowWidth;
		double height = jsonLayoutParams.getDouble("height") / windowHeight;
		boolean visible = jsonLayoutParams.getBoolean("visible");
		Map<String, String> additionalParameters = new HashMap<String, String>();
		JSONObject jsonAdditionalParameters = jsonLayoutParams.getJSONObject("additionalParameters");
		Iterator<?> keys = jsonAdditionalParameters.keys();
		while (keys.hasNext()) {
			String key = String.valueOf(keys.next());
			additionalParameters.put(key, jsonAdditionalParameters.getString(key));
		}
		return new HtmlLayoutParams(id, x, y, width, height, visible, additionalParameters, windowWidth, windowHeight);
	}

	@Override
	public String toString() {
		return "HtmlLayoutParams [id=" + id + ", x=" + x + ", y=" + y
				+ ", width=" + width + ", height=" + height + ", visible="
				+ visible + ", additionalParameters=" + additionalParameters
				+ "]";
	}
	
	/**
	 * @return JSON-serialized additionalParameters
	 */
	public JSONObject getAdditionalParametersAsJson() {
		JSONObject jsonObject = new JSONObject();
		try {
			for (Map.Entry<String, String> entry : additionalParameters.entrySet()) {
				jsonObject.put(entry.getKey(), entry.getValue());
			}
			return jsonObject;
		} catch (JSONException e) {
			return jsonObject;
		}
	}
}
