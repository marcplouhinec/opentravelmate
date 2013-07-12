package org.opentravelmate.commons;

import android.content.Context;

/**
 * Create an exception with an internationalized exception message.
 * 
 * @author marc.plouhinec@gmail.com (Marc Plouhinec)
 */
public class I18nException extends Exception {

	private static final long serialVersionUID = 4454817477718685774L;
	
	private static Context context;
	
	/**
	 * Set the context that contains the i18n strings.
	 * 
	 * @param context
	 */
	public static void setContext(Context context) {
		I18nException.context = context;
	}
	
	/**
	 * Create an exception.
	 * 
	 * @param resId
	 * @param cause
	 * @param formatArgs
	 */
	public I18nException(int resId, Throwable cause, Object... formatArgs) {
		super(context.getString(resId, formatArgs), cause);
	}

}
