package org.opentravelmate.commons.utils;

import android.content.Context;

/**
 * Create an exception with an internationalized exception message.
 * 
 * @author Marc Plouhinec
 */
public class InternationalizedException extends Exception {

	private static final long serialVersionUID = 4454817477718685774L;
	
	private static Context context;
	
	/**
	 * Set the context that contains the i18n strings.
	 * 
	 * @param context
	 */
	public static void setContext(Context context) {
		InternationalizedException.context = context;
	}
	
	/**
	 * Create an exception.
	 * 
	 * @param resId
	 * @param cause
	 * @param formatArgs
	 */
	public InternationalizedException(int resId, Throwable cause, Object... formatArgs) {
		super(context.getString(resId, formatArgs), cause);
	}

}
