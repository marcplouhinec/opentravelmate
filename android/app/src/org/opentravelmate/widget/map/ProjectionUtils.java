package org.opentravelmate.widget.map;

/**
 * Compute the Mercator projection between the world coordinates and the screen coordinates.
 * 
 * @author Marc Plouhinec
 */
public class ProjectionUtils {
	
	/**
	 * Get the X coordinate of a waypoint (the reference is 1 = tile size).
	 * 
	 * @param zoom Map zoom level
	 * @param longitude
	 * @return X coordinate
	 */
	public static double lngToTileX(float zoom, double longitude) {
		return Math.pow(2, zoom) * (longitude + 180) / 360;
	}
	
	/**
	 * Get the Y coordinate of a waypoint (the reference is 1 = tile size).
	 * 
	 * @param zoom Map zoom level
	 * @param latitude
	 * @return Y coordinate
	 */
	public static double latToTileY(float zoom, double latitude) {
		return Math.pow(2, zoom - 1) * (1 - Math.log(Math.tan(Math.PI / 4 + latitude * Math.PI / 360)) / Math.PI);
	}
	
	/**
	 * Get the longitude of a waypoint from its X coordinate.
	 * 
	 * @param zoom Map zoom level
	 * @param x
	 * @return longitude
	 */
	public static double tileXToLng(float zoom, double x) {
		return x * 360 / Math.pow(2, zoom) - 180;
	}
	
	/**
	 * Get the latitude of a waypoint from its Y coordinate.
	 * 
	 * @param zoom Map zoom level
	 * @param y
	 * @return latitude
	 */
	public static double tileYToLat(float zoom, double y) {
		return Math.atan(Math.exp(Math.PI * (1 - (2 * y / Math.pow(2, zoom))))) * 360 / Math.PI - Math.PI * 360 / (4 * Math.PI);
	}

}
