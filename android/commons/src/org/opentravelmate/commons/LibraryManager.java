package org.opentravelmate.commons;

import org.opentravelmate.commons.utils.ExceptionListener;

import android.app.Activity;

/**
 * All the libraries must override this interface. It allows the launcher to
 * initialize all libraries before executing the extensions.
 * 
 * @author Marc Plouhinec
 */
public interface LibraryManager {
	
	/**
	 * Initialize the library.
	 * 
	 * @param activity
	 *   Main Activity when the library is active.
	 * @param libraryRegistrar
	 *   Place to register all the JavaScript libraries to inject in web views.
	 * @param exceptionListener
	 *   Handle the exceptions that need to be show to the user.
	 */
	void initialize(Activity activity, LibraryRegistrar libraryRegistrar, ExceptionListener exceptionListener);
	
	/**
	 * Start the library.
	 */
	void start();

}
