package org.opentravelmate.launcher.window.controller;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.http.protocol.HttpRequestHandler;
import org.opentravelmate.commons.LibraryManager;
import org.opentravelmate.commons.LibraryRegistrar;
import org.opentravelmate.commons.utils.ExceptionListener;
import org.opentravelmate.commons.utils.InternationalizedException;
import org.opentravelmate.launcher.R;
import org.opentravelmate.launcher.httpserver.HttpServer;
import org.opentravelmate.launcher.httpserver.JavascriptRequestHandler;
import org.opentravelmate.launcher.httpserver.RootRequestHandler;
import org.opentravelmate.launcher.window.WindowActivity;
import org.opentravelmate.launcher.window.WindowExceptionListener;
import org.opentravelmate.widget.mainmenu.MainMenuLibraryManager;
import org.opentravelmate.widget.webview.WebViewLibraryManager;
import org.opentravelmate.widget.webview.WebViewLibraryRegistrar;

import android.app.AlertDialog;
import android.content.DialogInterface;

/**
 * Controll the main window.
 * 
 * @author Marc Plouhinec
 */
public class MainWindowController implements WindowController {
	
	private final HttpServer httpServer;
	private final WindowActivity windowActivity;
	
	/**
	 * Create a main window controller.
	 * 
	 * @param windowActivity
	 */
	public MainWindowController(WindowActivity windowActivity) {
		this.windowActivity = windowActivity;
		
		// Initialize the InternationalizedException class in order to allow its instances to get translations
		InternationalizedException.setContext(windowActivity);
		
		// Start the HTTP server
		ExceptionListener exceptionListener = new WindowExceptionListener(windowActivity);
		Map<String, HttpRequestHandler> requestHandlerByPattern = new LinkedHashMap<String, HttpRequestHandler>();
		RootRequestHandler rootRequestHandler = new RootRequestHandler(windowActivity.getAssets());
		requestHandlerByPattern.put("/index.css", rootRequestHandler);
		requestHandlerByPattern.put("/index.html", rootRequestHandler);
		requestHandlerByPattern.put("/main.js", rootRequestHandler);
		requestHandlerByPattern.put("/require.js", rootRequestHandler);
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
		webViewLibraryManager.initialize(windowActivity, libraryRegistrar, exceptionListener);
		webViewLibraryManager.setHttpServerUrl("http://127.0.0.1:" + httpServer.getPort());
		LibraryManager mainMenulibraryManager = new MainMenuLibraryManager();
		mainMenulibraryManager.initialize(windowActivity, libraryRegistrar, exceptionListener);
		
		// Execute the extensions
		// TODO
	}

	/**
	 * Stop the application.
	 */
	@Override
	public void onDestroy() {
		httpServer.stop();
		android.os.Process.killProcess(android.os.Process.myPid());
	}

	/**
	 * When the user pushes the back button, show a confirm message before closing the application.
	 */
	@Override
	public void onBackPressed() {
		//Confirm if the user wants to close the application
		new AlertDialog.Builder(this.windowActivity)
			.setIcon(android.R.drawable.ic_dialog_alert)
			.setTitle(R.string.exit_confirm_dialog_title)
			.setMessage(R.string.exit_confirm_dialog_message)
			.setPositiveButton(R.string.exit_confirm_dialog_yes, new DialogInterface.OnClickListener() {
				@Override public void onClick(DialogInterface dialog, int which) {
					windowActivity.finish();
				}
			})
			.setNegativeButton(R.string.exit_confirm_dialog_no, null)
			.show();
	}

}
