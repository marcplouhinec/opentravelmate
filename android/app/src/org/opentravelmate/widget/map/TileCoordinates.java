package org.opentravelmate.widget.map;

import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Tile coordinates.
 * 
 * @author marc.plouhinec@gmail.com (Marc Plouhinec)
 */
public class TileCoordinates {
	
	public final int zoom;
	public final int x;
	public final int y;
	
	public TileCoordinates(int zoom, int x, int y) {
		this.zoom = zoom;
		this.x = x;
		this.y = y;
	}
	
	/**
	 * @return JSON-serialized TileCoordinates
	 * @throws JSONException
	 */
	public JSONObject toJson() throws JSONException {
		JSONObject jsonTileCoordinates = new JSONObject();
		jsonTileCoordinates.put("zoom", zoom);
		jsonTileCoordinates.put("x", x);
		jsonTileCoordinates.put("y", y);
		return jsonTileCoordinates;
	}
	
	/**
	 * Convert the given list into a JSON array.
	 * 
	 * @param listOfTileCoordinates
	 * @return JSON array
	 * @throws JSONException
	 */
	public static JSONArray toJson(List<TileCoordinates> listOfTileCoordinates) throws JSONException {
		JSONArray jsonListOfTileCoordinates = new JSONArray();
		for (TileCoordinates tileCoordinates : listOfTileCoordinates) {
			jsonListOfTileCoordinates.put(tileCoordinates.toJson());
		}
		return jsonListOfTileCoordinates;
	}
}
