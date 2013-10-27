package org.opentravelmate.widget.map;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Define an marker icon defined with an SVG path element.
 *
 * @author marc.plouhinec@gmail.com (Marc Plouhinec)
 */
public class SvgPathMarkerIcon extends MarkerIcon {

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
	public final double strokeWeight;

	/**
	 * Create a new SvgPathMarkerIcon.
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
	 * @param strokeWeight
	 */
	public SvgPathMarkerIcon(Point anchor, Dimension size, String fillColor,
			double fillOpacity, String path, double rotation, double scale,
			String strokeColor, double strokeOpacity, double strokeWeight) {
		super(anchor, size);
		this.fillColor = fillColor;
		this.fillOpacity = fillOpacity;
		this.path = path;
		this.rotation = rotation;
		this.scale = scale;
		this.strokeColor = strokeColor;
		this.strokeOpacity = strokeOpacity;
		this.strokeWeight = strokeWeight;
	}
	
	/**
	 * @return JSON-serialized SvgPathMarkerIcon
	 * @throws JSONException
	 */
	public JSONObject toJson() throws JSONException {
		JSONObject jsonSvgPathMarkerIcon = new JSONObject();
		jsonSvgPathMarkerIcon.put("anchor", anchor.toJson());
		jsonSvgPathMarkerIcon.put("size", size.toJson());
		jsonSvgPathMarkerIcon.put("fillColor", fillColor);
		jsonSvgPathMarkerIcon.put("fillOpacity", fillOpacity);
		jsonSvgPathMarkerIcon.put("path", path);
		jsonSvgPathMarkerIcon.put("rotation", rotation);
		jsonSvgPathMarkerIcon.put("scale", scale);
		jsonSvgPathMarkerIcon.put("strokeColor", strokeColor);
		jsonSvgPathMarkerIcon.put("strokeOpacity", strokeOpacity);
		jsonSvgPathMarkerIcon.put("strokeWeight", strokeWeight);
		return jsonSvgPathMarkerIcon;
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
		renderedIcon.append("        stroke-width=\"").append(strokeWeight).append("\"\n");
		renderedIcon.append("        transform=\"").append(transformations).append("\"\n");
		renderedIcon.append("  />\n");
		renderedIcon.append("</svg>");
		return renderedIcon.toString();
	}
	
	/**
	 * Build a Point from a JSON-serialized representation.
	 * 
	 * @param jsonSvgPathMarkerIcon
	 * @return SvgPathMarkerIcon
	 * @throws JSONException
	 */
	public static SvgPathMarkerIcon fromJsonSvgPathMarkerIcon(JSONObject jsonSvgPathMarkerIcon) throws JSONException {
		return new SvgPathMarkerIcon(
				Point.fromJsonPoint(jsonSvgPathMarkerIcon.getJSONObject("anchor")),
				Dimension.fromJsonDimension(jsonSvgPathMarkerIcon.getJSONObject("size")),
				jsonSvgPathMarkerIcon.getString("fillColor"),
				jsonSvgPathMarkerIcon.getDouble("fillOpacity"),
				jsonSvgPathMarkerIcon.getString("path"),
				jsonSvgPathMarkerIcon.getDouble("rotation"),
				jsonSvgPathMarkerIcon.getDouble("scale"),
				jsonSvgPathMarkerIcon.getString("strokeColor"),
				jsonSvgPathMarkerIcon.getDouble("strokeOpacity"),
				jsonSvgPathMarkerIcon.getDouble("strokeWeight"));
	}
}
