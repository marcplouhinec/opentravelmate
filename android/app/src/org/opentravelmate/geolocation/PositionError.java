package org.opentravelmate.geolocation;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Define a position error.
 *
 * @author Marc Plouhinec
 */
public class PositionError {
	public static final int PERMISSION_DENIED = 1;
	public static final int POSITION_UNAVAILABLE = 1;
	public static final int TIMEOUT = 1;
	
	public final int code;
	public final String message;
	
	/**
	 * Create a new PositionError.
	 * 
	 * @param code
	 * @param message
	 */
	public PositionError(int code, String message) {
		this.code = code;
		this.message = message;
	}

	/**
	 * @return JSON-serialized Position
	 * @throws JSONException
	 */
	public JSONObject toJson() throws JSONException {
		JSONObject jsonPositionError = new JSONObject();
		jsonPositionError.put("code", code);
		jsonPositionError.put("message", message);
		return jsonPositionError;
	}
}
