package org.opentravelmate.commons;

import android.os.Handler;

/**
 * Run code in the UI thread.
 * 
 * @author marc.plouhinec@gmail.com (Marc Plouhinec)
 */
public class UIThreadExecutor {
	
	private static Handler handler = null;
	
	/**
	 * Initialize this class.
	 * 
	 * Note: this method must be called in the UI thread.
	 */
	public static void init() {
		handler = new Handler();
	}
	
	/**
	 * Run the given runnable in the UI thread.
	 * 
	 * @param runnable
	 */
	public static void execute(Runnable runnable) {
		handler.post(runnable);
	}

}
