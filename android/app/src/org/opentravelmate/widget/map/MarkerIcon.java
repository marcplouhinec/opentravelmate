package org.opentravelmate.widget.map;

/**
 * Define a map marker icon.
 *
 * @author Marc Plouhinec
 */
public abstract class MarkerIcon {
	
	/**
	 * Marker icon anchor point.
	 */
	public final Point anchor;
	
	/**
	 * Marker icon size.
	 */
	public final Dimension size;

	/**
	 * Create a new MarkerIcon.
	 * 
	 * @param anchor
	 * @param size
	 */
	public MarkerIcon(Point anchor, Dimension size) {
		this.anchor = anchor;
		this.size = size;
	}
}
