package org.opentravelmate.commons;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Run code in a background thread.
 * 
 * @author marc.plouhinec@gmail.com (Marc Plouhinec)
 */
public class BgThreadExecutor {
	
	private static final ExecutorService EXECUTOR_SERVICE = Executors.newFixedThreadPool(4);

	/**
	 * Run the given runnable in a background thread.
	 * 
	 * @param runnable
	 */
	public static void execute(Runnable runnable) {
		EXECUTOR_SERVICE.execute(runnable);
	}
}
