package org.opentravelmate.launcher.window;

import org.opentravelmate.commons.utils.ExceptionListener;
import org.opentravelmate.launcher.R;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Handler;
import android.util.Log;

/**
 * Handle the exceptions of a window.
 * 
 * @author Marc Plouhinec
 */
public class WindowExceptionListener implements ExceptionListener {
	
	private static final String LOG_TAG = "DefaultExceptionListener";
	private final Handler handler = new Handler();
	private final WindowActivity windowActivity;
	
	/**
	 * Create the default exception listener.
	 * 
	 * @param windowActivity
	 */
	public WindowExceptionListener(WindowActivity windowActivity) {
		this.windowActivity = windowActivity;
	}
	
	@Override
	public void onException(boolean isUnrecoverable, Exception e) {
		Log.e(LOG_TAG, e.getMessage(), e);
		handler.post(new DefaultExceptionListenerInActivityThread(isUnrecoverable, e));
	}
	
	/**
	 * Show exceptions to the user.
	 */
	private class DefaultExceptionListenerInActivityThread implements Runnable, DialogInterface.OnClickListener {
		private final boolean isUnrecoverable;
		private final Exception e;
		
		/**
		 * Create a DefaultExceptionListener that is executed in the activity thread.
		 * 
		 * @param isUnrecoverable
		 * @param e
		 */
		public DefaultExceptionListenerInActivityThread(boolean isUnrecoverable, Exception e) {
			this.isUnrecoverable = isUnrecoverable;
			this.e = e;
		}

		@Override
		public void run() {
			// Show the error to the user
			final AlertDialog alertDialog = new AlertDialog.Builder(windowActivity)
				.setMessage(e.getMessage())
				.setTitle(R.string.error_dialog_title)
				.setNeutralButton(android.R.string.ok, this)
				.create();
			alertDialog.show();
		}

		@Override
		public void onClick(DialogInterface dialog, int which) {
			dialog.dismiss();
			
			if (isUnrecoverable) {
				// Close the window
				windowActivity.finish();
			}
		}
	}
}