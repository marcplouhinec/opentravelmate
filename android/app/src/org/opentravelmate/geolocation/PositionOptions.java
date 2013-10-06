package org.opentravelmate.geolocation;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Define position options.
 *
 * @author marc.plouhinec@gmail.com (Marc Plouhinec)
 */
public class PositionOptions {
	public final boolean enableHighAccuracy;
	public final long timeout;
	public final long maximumAge;
	
	/**
	 * Create PositionOptions.
	 * 
	 * @param enableHighAccuracy
	 * @param timeout
	 * @param maximumAge
	 */
	public PositionOptions(boolean enableHighAccuracy, long timeout, long maximumAge) {
		this.enableHighAccuracy = enableHighAccuracy;
		this.timeout = timeout;
		this.maximumAge = maximumAge;
	}
	
	/**
	 * Build a PositionOptions from a JSON-serialized representation.
	 * 
	 * @param jsonPositionOptions
	 * @return PositionOptions
	 * @throws JSONException
	 */
	public static PositionOptions fromJson(JSONObject jsonPositionOptions) throws JSONException {
		return new PositionOptions(
				jsonPositionOptions.getBoolean("enableHighAccuracy"),
				jsonPositionOptions.getLong("timeout"),
				jsonPositionOptions.getLong("maximumAge"));
	}
}
