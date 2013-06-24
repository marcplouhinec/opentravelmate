package org.opentravelmate.launcher;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.http.protocol.HttpRequestHandler;
import org.opentravelmate.commons.LibraryManager;
import org.opentravelmate.commons.LibraryRegistrar;
import org.opentravelmate.commons.utils.ExceptionListener;
import org.opentravelmate.commons.utils.InternationalizedException;
import org.opentravelmate.launcher.httpserver.HttpServer;
import org.opentravelmate.launcher.httpserver.JavascriptRequestHandler;
import org.opentravelmate.launcher.httpserver.RootRequestHandler;
import org.opentravelmate.widget.mainmenu.MainMenuLibraryManager;
import org.opentravelmate.widget.map.MapLibraryManager;
import org.opentravelmate.widget.webview.WebViewLibraryManager;
import org.opentravelmate.widget.webview.WebViewLibraryRegistrar;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

public class MainActivity extends Activity {
	
	private static final String LOG_TAG = "MainActivity";
	private Handler handler;
	private HttpServer httpServer;

	@SuppressLint({ "NewApi", "SetJavaScriptEnabled" })
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		handler = new Handler();
		InternationalizedException.setContext(this);
		
		// Hide the action bar
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			getActionBar().hide();
		}
		
		// Start the HTTP server
		ExceptionListener exceptionListener = new DefaultExceptionListener();
		Map<String, HttpRequestHandler> requestHandlerByPattern = new LinkedHashMap<String, HttpRequestHandler>();
		RootRequestHandler rootRequestHandler = new RootRequestHandler(getAssets());
		requestHandlerByPattern.put("/index.css", rootRequestHandler);
		requestHandlerByPattern.put("/index.html", rootRequestHandler);
		requestHandlerByPattern.put("/main.js", rootRequestHandler);
		requestHandlerByPattern.put("/require.js", rootRequestHandler);
		requestHandlerByPattern.put("/jquery.js", rootRequestHandler);
		final JavascriptRequestHandler javascriptRequestHandler = new JavascriptRequestHandler();
		requestHandlerByPattern.put("*", javascriptRequestHandler);
		httpServer = new HttpServer(requestHandlerByPattern, exceptionListener);
		try {
			httpServer.start();
		} catch (IOException e) {
			exceptionListener.onException(true, new InternationalizedException(
					R.string.httpserver_error_unable_to_create_httpserver, e));
			return;
		}
		
		// Initialize the libraries
		LibraryRegistrar libraryRegistrar = new WebViewLibraryRegistrar(new WebViewLibraryRegistrar.RegisteredResourcesListener() {
			@Override public void onResourcesRegistered(String folderClassPath, String urlPrefix) {
				javascriptRequestHandler.registerResources(folderClassPath, urlPrefix);
			}
			@Override public void onJavaObjectInjected(String globalName, String requirejsUrl) {
				javascriptRequestHandler.registerInjectedJavaObject(requirejsUrl, globalName);
			}
		});
		WebViewLibraryManager webViewLibraryManager = new WebViewLibraryManager();
		webViewLibraryManager.initialize(this, libraryRegistrar, exceptionListener);
		webViewLibraryManager.setHttpServerUrl("http://127.0.0.1:" + httpServer.getPort());
		LibraryManager mainMenuLibraryManager = new MainMenuLibraryManager();
		mainMenuLibraryManager.initialize(this, libraryRegistrar, exceptionListener);
		LibraryManager mapLibraryManager = new MapLibraryManager();
		mapLibraryManager.initialize(this, libraryRegistrar, exceptionListener);
		
		
		// Start the libraries
		mainMenuLibraryManager.start();
		mapLibraryManager.start();
		webViewLibraryManager.start();
		
		// Execute the extensions
		// TODO
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
			handler.post(new DefaultExceptionListenerInActivityThread(isUnrecoverable, e));
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
