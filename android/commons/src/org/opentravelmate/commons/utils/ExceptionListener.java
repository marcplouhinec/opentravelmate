package org.opentravelmate.commons.utils;

/**
 * Listener called when an exception occurs.
 * 
 * @author Marc Plouhinec
 */
public interface ExceptionListener {
	
	/**
	 * @param isUnrecoverable
	 *   If true, the application must stop.
	 * @param e
	 *   Thrown exception.
	 */
	void onException(boolean isUnrecoverable, Exception e);
}