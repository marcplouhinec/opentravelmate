package org.opentravelmate.geolocation;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Represent geolocation coordinates.
 * 
 * @author Marc Plouhinec
 */
public class Coordinates {
	
	public final double latitude;
	public final double longitude;
	public final double altitude;
	public final double accuracy;
	public final double altitudeAccuracy;
	public final double heading;
	public final double speed;
	
	/**
	 * Create new Coordinates.
	 */
	public Coordinates(double latitude, double longitude, double altitude,
			double accuracy, double altitudeAccuracy, double heading,
			double speed) {
		this.latitude = latitude;
		this.longitude = longitude;
		this.altitude = altitude;
		this.accuracy = accuracy;
		this.altitudeAccuracy = altitudeAccuracy;
		this.heading = heading;
		this.speed = speed;
	}
	
	/**
	 * @return JSON-serialized Coordinates
	 * @throws JSONException
	 */
	public JSONObject toJson() throws JSONException {
		JSONObject jsonCoordinates = new JSONObject();
		jsonCoordinates.put("latitude", latitude);
		jsonCoordinates.put("longitude", longitude);
		jsonCoordinates.put("altitude", altitude);
		jsonCoordinates.put("accuracy", accuracy);
		jsonCoordinates.put("altitudeAccuracy", altitudeAccuracy);
		jsonCoordinates.put("heading", heading);
		jsonCoordinates.put("speed", speed);
		return jsonCoordinates;
	}
}
