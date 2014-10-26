package org.opentravelmate.geolocation;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Represent geolocation position.
 *
 * @author Marc Plouhinec
 */
public class Position {
	
	private final Coordinates coords;
	private final long timestamp;
	
	/**
	 * Create a Position.
	 */
	public Position(Coordinates coords, long timestamp) {
		this.coords = coords;
		this.timestamp = timestamp;
	}

	/**
	 * @return JSON-serialized Position
	 * @throws JSONException
	 */
	public JSONObject toJson() throws JSONException {
		JSONObject jsonPosition = new JSONObject();
		jsonPosition.put("coords", coords.toJson());
		jsonPosition.put("timestamp", timestamp);
		return jsonPosition;
	}
}
