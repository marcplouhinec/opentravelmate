package org.opentravelmate.widget.map;

import android.widget.RelativeLayout;

import com.google.android.gms.maps.GoogleMap;

/**
 * Handle map buttons.
 * 
 * @author Marc Plouhinec
 */
public class MapButtonController {
	
	private final GoogleMap googleMap;
	private final RelativeLayout mapLayout;
	private final String baseUrl;
	
	/**
	 * Create a new MapButtonController.
	 * 
	 * @param googleMap
	 * @param mapLayout
	 * @param baseUrl
	 */
	public MapButtonController(GoogleMap googleMap, RelativeLayout mapLayout, String baseUrl) {
		this.googleMap = googleMap;
		this.mapLayout = mapLayout;
		this.baseUrl = baseUrl;
	}

	/**
	 * Add the given button to the map.
	 * 
	 * @param mapButton
	 */
	public void addButton(MapButton mapButton) {
		// TODO
	}
}
