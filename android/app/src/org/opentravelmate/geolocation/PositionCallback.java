package org.opentravelmate.geolocation;

/**
 * Function called when a position is available.
 * 
 * @author Marc Plouhinec
 */
public interface PositionCallback {
	void on(Position position);
}
