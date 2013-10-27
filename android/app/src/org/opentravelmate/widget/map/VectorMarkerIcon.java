package org.opentravelmate.widget.map;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Define an marker icon defined with an SVG path element.
 *
 * @author marc.plouhinec@gmail.com (Marc Plouhinec)
 */
public class VectorMarkerIcon extends MarkerIcon {

	/**
	 * Shape fill color.
	 */
	public final String fillColor;
	
	/**
	 * Shape fill opacity (number between 0 = transparent and 1 = opaque).
	 */
	public final double fillOpacity;
	
	/**
	 * Shape path.
     *
     * @see http://www.w3.org/TR/SVG/paths.html
	 */
	public final String path;
	
	/**
	 * Rotation angle in degree.
	 */
	public final double rotation;
	
	/**
	 * Scaling factor.
	 */
	public final double scale;
	
	/**
	 * Shape stroke color.
	 */
	public final String strokeColor;
	
	/**
	 * Shape stroke opacity (number between 0 = transparent and 1 = opaque).
	 */
	public final double strokeOpacity;
	
	/**
	 * Shape stroke width.
	 */
	public final double strokeWidth;

	/**
	 * Create a new VectorMarkerIcon.
	 * 
	 * @param anchor
	 * @param size
	 * @param fillColor
	 * @param fillOpacity
	 * @param path
	 * @param rotation
	 * @param scale
	 * @param strokeColor
	 * @param strokeOpacity
	 * @param strokeWidth
	 */
	public VectorMarkerIcon(Point anchor, Dimension size, String fillColor,
			double fillOpacity, String path, double rotation, double scale,
			String strokeColor, double strokeOpacity, double strokeWidth) {
		super(anchor, size);
		this.fillColor = fillColor;
		this.fillOpacity = fillOpacity;
		this.path = path;
		this.rotation = rotation;
		this.scale = scale;
		this.strokeColor = strokeColor;
		this.strokeOpacity = strokeOpacity;
		this.strokeWidth = strokeWidth;
	}
	
	/**
	 * @return JSON-serialized VectorMarkerIcon
	 * @throws JSONException
	 */
	public JSONObject toJson() throws JSONException {
		JSONObject jsonVectorMarkerIcon = new JSONObject();
		jsonVectorMarkerIcon.put("anchor", anchor.toJson());
		jsonVectorMarkerIcon.put("size", size.toJson());
		jsonVectorMarkerIcon.put("fillColor", fillColor);
		jsonVectorMarkerIcon.put("fillOpacity", fillOpacity);
		jsonVectorMarkerIcon.put("path", path);
		jsonVectorMarkerIcon.put("rotation", rotation);
		jsonVectorMarkerIcon.put("scale", scale);
		jsonVectorMarkerIcon.put("strokeColor", strokeColor);
		jsonVectorMarkerIcon.put("strokeOpacity", strokeOpacity);
		jsonVectorMarkerIcon.put("strokeWidth", strokeWidth);
		return jsonVectorMarkerIcon;
	}
	
	public String toSvg() {
		StringBuilder transformations = new StringBuilder();
		if (rotation != 0) {
			transformations.append("rotate(").append(rotation).append(") ");
		}
		if (scale != 1) {
			transformations.append("scale(").append(scale).append(")");
		}
		StringBuilder renderedIcon = new StringBuilder();
		renderedIcon.append("<?xml version=\"1.0\" encoding=\"utf-8\"?>\n");
		renderedIcon.append("<svg xmlns=\"http://www.w3.org/2000/svg\" version=\"1.1\"\n");
		renderedIcon.append("     width=\"").append(size.width).append("\"\n");
		renderedIcon.append("     height=\"").append(size.height).append("\">\n");
		renderedIcon.append("  <path d=\"").append(path).append("\"\n");
		renderedIcon.append("        fill=\"").append(fillColor).append("\"\n");
		renderedIcon.append("        fill-opacity=\"").append(fillOpacity).append("\"\n");
		renderedIcon.append("        stroke=\"").append(strokeColor).append("\"\n");
		renderedIcon.append("        stroke-opacity=\"").append(strokeOpacity).append("\"\n");
		renderedIcon.append("        stroke-width=\"").append(strokeWidth).append("\"\n");
		renderedIcon.append("        transform=\"").append(transformations).append("\"\n");
		renderedIcon.append("  />\n");
		renderedIcon.append("</svg>");
		return renderedIcon.toString();
	}
	
	/**
	 * Build a Point from a JSON-serialized representation.
	 * 
	 * @param jsonVectorMarkerIcon
	 * @return VectorMarkerIcon
	 * @throws JSONException
	 */
	public static VectorMarkerIcon fromJsonVectorMarkerIcon(JSONObject jsonVectorMarkerIcon) throws JSONException {
		return new VectorMarkerIcon(
				Point.fromJsonPoint(jsonVectorMarkerIcon.getJSONObject("anchor")),
				Dimension.fromJsonDimension(jsonVectorMarkerIcon.getJSONObject("size")),
				jsonVectorMarkerIcon.getString("fillColor"),
				jsonVectorMarkerIcon.getDouble("fillOpacity"),
				jsonVectorMarkerIcon.getString("path"),
				jsonVectorMarkerIcon.getDouble("rotation"),
				jsonVectorMarkerIcon.getDouble("scale"),
				jsonVectorMarkerIcon.getString("strokeColor"),
				jsonVectorMarkerIcon.getDouble("strokeOpacity"),
				jsonVectorMarkerIcon.getDouble("strokeWidth"));
	}
}
