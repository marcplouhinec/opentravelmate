package org.opentravelmate.widget.map;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Define an overlay that will be displayed on top of the map.
 *
 * @author marc.plouhinec@gmail.com (Marc Plouhinec)
 */
public class TileOverlay {
	
	/**
	 * TileOverlay ID.
	 */
	public final int id;
	
	/**
	 * TileOverlay Z-index.
	 */
	public final float zIndex;
	
	/**
	 * true = visible, false = invisible.
	 */
	public final boolean isVisible;
	
	/**
	 * URL to a given tile with the ${zoom}, ${x} and ${y} place-holders.
	 */
	public final String tileUrlPattern;

	/**
	 * Create a new TileOverlay.
	 * 
	 * @param id
	 * @param zIndex
	 * @param isVisible
	 * @param tileUrlPattern
	 */
	public TileOverlay(int id, float zIndex, boolean isVisible, String tileUrlPattern) {
		this.id = id;
		this.zIndex = zIndex;
		this.isVisible = isVisible;
		this.tileUrlPattern = tileUrlPattern;
	}
	
	/**
	 * Build a TileOverlay from a JSON-serialized representation.
	 * 
	 * @param jsonTileOverlay
	 * @return TileOverlay
	 * @throws JSONException
	 */
	public static TileOverlay fromJsonTileOverlay(JSONObject jsonTileOverlay) throws JSONException {
		return new TileOverlay(
				jsonTileOverlay.getInt("id"),
				(float)jsonTileOverlay.getDouble("zIndex"),
				jsonTileOverlay.getBoolean("isVisible"),
				jsonTileOverlay.getString("tileUrlPattern"));
	}
}
