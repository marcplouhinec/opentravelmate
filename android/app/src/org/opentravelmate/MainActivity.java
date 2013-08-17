package org.opentravelmate;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.http.protocol.HttpRequestHandler;
import org.opentravelmate.R;
import org.opentravelmate.commons.ExceptionListener;
import org.opentravelmate.commons.I18nException;
import org.opentravelmate.commons.UIThreadExecutor;
import org.opentravelmate.httpserver.ExtensionRequestHandler;
import org.opentravelmate.httpserver.HttpServer;
import org.opentravelmate.httpserver.NativeRequestHandler;
import org.opentravelmate.widget.HtmlLayout;
import org.opentravelmate.widget.HtmlLayoutParams;
import org.opentravelmate.widget.map.NativeMap;
import org.opentravelmate.widget.menu.NativeMenu;
import org.opentravelmate.widget.webview.NativeWebView;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;

/**
 * Main activity of the Open Travel Mate application.
 * 
 * @author marc.plouhinec@gmail.com (Marc Plouhinec)
 */
public class MainActivity extends FragmentActivity {
	
	private static final String LOG_TAG = MainActivity.class.getSimpleName();
	private HttpServer httpServer;

	@SuppressLint("NewApi")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		UIThreadExecutor.init();
		I18nException.setContext(this);
		
		// Hide the action bar
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			getActionBar().hide();
		}
		
		// Start the HTTP server
		ExceptionListener exceptionListener = new DefaultExceptionListener();
		Map<String, HttpRequestHandler> requestHandlerByPattern = new LinkedHashMap<String, HttpRequestHandler>();
		final NativeRequestHandler nativeRequestHandler = new NativeRequestHandler();
		nativeRequestHandler.registerInjectedJavaObject(NativeWebView.SCRIPT_URL, NativeWebView.GLOBAL_OBJECT_NAME);
		nativeRequestHandler.registerInjectedJavaObject(NativeMenu.SCRIPT_URL, NativeMenu.GLOBAL_OBJECT_NAME);
		nativeRequestHandler.registerInjectedJavaObject(NativeMap.SCRIPT_URL, NativeMap.GLOBAL_OBJECT_NAME);
		requestHandlerByPattern.put("/native/*", nativeRequestHandler);
		ExtensionRequestHandler extensionRequestHandler = new ExtensionRequestHandler(getAssets());
		requestHandlerByPattern.put("/extensions/*", extensionRequestHandler);
		httpServer = new HttpServer(requestHandlerByPattern, exceptionListener);
		try {
			httpServer.start();
		} catch (IOException e) {
			exceptionListener.onException(true, new I18nException(
					R.string.httpserver_error_unable_to_create_httpserver, e));
			return;
		}
		
		// Initialize native objects to inject in the web views
		String baseUrl = "http://localhost:" + httpServer.getPort() + "/";
		HtmlLayout htmlLayout = new HtmlLayout(this);
		this.setContentView(htmlLayout);
		NativeMenu nativeMenu = new NativeMenu(exceptionListener, htmlLayout, baseUrl);
		NativeMap nativeMap = new NativeMap(exceptionListener, htmlLayout, this.getSupportFragmentManager());
		NativeWebView nativeWebView = new NativeWebView(exceptionListener, htmlLayout, baseUrl, nativeMenu, nativeMap);
		
		// Initialize the root web view
		HtmlLayoutParams layoutParams = new HtmlLayoutParams(HtmlLayout.MAIN_WEBVIEW_ID, 0, 0, 1, 1, true, new HashMap<String, String>(){
			private static final long serialVersionUID = -2001726600946643058L;
		{
			put("url", "extensions/core/mainwebview/mainwebview.html");
			put("entrypoint", "extensions/core/mainwebview/mainwebview");
		}});
		nativeWebView.buildView(layoutParams);
	}
	
	/**
	 * Quit the application when this activity is destroyed.
	 */
	@Override
	protected void onDestroy() {
		httpServer.stop();
		android.os.Process.killProcess(android.os.Process.myPid());
		
		super.onDestroy();
	}
	
	/**
	 * When the user pushes the back button, show a confirm message before closing the application.
	 */
	@Override
	public void onBackPressed() {
		//Confirm if the user wants to close the application
		new AlertDialog.Builder(this)
			.setIcon(android.R.drawable.ic_dialog_alert)
			.setTitle(R.string.exit_confirm_dialog_title)
			.setMessage(R.string.exit_confirm_dialog_message)
			.setPositiveButton(R.string.exit_confirm_dialog_yes, new DialogInterface.OnClickListener() {
				@Override public void onClick(DialogInterface dialog, int which) {
					MainActivity.this.finish();
				}
			})
			.setNegativeButton(R.string.exit_confirm_dialog_no, null)
			.show();
	}

	/**
	 * Handle all expected exceptions.
	 */
	private class DefaultExceptionListener implements ExceptionListener {
		@Override
		public void onException(boolean isUnrecoverable, Exception e) {
			Log.e(LOG_TAG, e.getMessage(), e);
			UIThreadExecutor.execute(new DefaultExceptionListenerInActivityThread(isUnrecoverable, e));
		}
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
			final AlertDialog alertDialog = new AlertDialog.Builder(MainActivity.this)
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
				// Quit the application
				MainActivity.this.finish();
			}
		}
	}
}
