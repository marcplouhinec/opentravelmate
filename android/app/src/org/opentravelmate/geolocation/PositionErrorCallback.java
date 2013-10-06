package org.opentravelmate.geolocation;

/**
 * Function called when an error occured.
 * 
 * @author Marc Plouhinec
 */
public interface PositionErrorCallback {
	void on(PositionError positionError);
}
