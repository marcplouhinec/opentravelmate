package org.opentravelmate.launcher.window.controller;

/**
 * Control a window.
 * 
 * @author Marc Plouhinec
 */
public interface WindowController {
	
	/**
	 * Function called when a window is closed.
	 */
	void onDestroy();
	
	/**
	 * Function called when the back button is pressed.
	 */
	void onBackPressed();

}
