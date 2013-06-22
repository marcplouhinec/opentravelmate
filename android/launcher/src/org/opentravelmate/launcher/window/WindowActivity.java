package org.opentravelmate.launcher.window;

import org.opentravelmate.launcher.window.controller.DefaultWindowController;
import org.opentravelmate.launcher.window.controller.DefaultWindowController.WindowOptions;
import org.opentravelmate.launcher.window.controller.MainWindowController;
import org.opentravelmate.launcher.window.controller.WindowController;
import org.opentravelmate.widget.webview.view.HtmlLayout;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
import android.webkit.WebView;

/**
 * On the Android platform, a Window is implemented via an Activity that contains:
 *  - A WebView in the background
 *  - A HtmlLayout on top of the WebView
 *  
 * The HtmlLayout contains the child widgets that are displayed on top to place-holder
 * located in the HTML document of the WebView.
 * 
 * @author Marc Plouhinec
 */
public class WindowActivity extends Activity {
	
	private static final int HTML_LAYOUT_ID = Integer.MAX_VALUE;
	private static final int WEB_VIEW_ID = Integer.MAX_VALUE - 1;
	
	private WindowController windowController = null;

	@SuppressLint("NewApi")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		// Hide the action bar
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			getActionBar().hide();
		}
		
		// Create the WebView and the HtmlLayout
		WebView webView = new WebView(this);
		webView.setId(WEB_VIEW_ID);
		HtmlLayout htmlLayout = new HtmlLayout(this);
		htmlLayout.setId(HTML_LAYOUT_ID);
		htmlLayout.addView(rootWebView);
		this.setContentView(htmlLayout);
		
		// Check if this window is the main one or a normal one
		boolean isMainWindow = this.getIntent() == null;
		if (isMainWindow) {
			windowController = new MainWindowController(this);
		} else {
			String jsonWindowOptions = this.getIntent().getExtras().getString("jsonWindowOptions");
			WindowOptions windowOptions;
			if (jsonWindowOptions == null) {
				windowOptions = new WindowOptions();
			} else {
				windowOptions = WindowOptions.fromJson(jsonWindowOptions);
			}
			windowController = new DefaultWindowController(this, windowOptions);
		}
	}

	@Override
	protected void onDestroy() {
		windowController.onDestroy();
		super.onDestroy();
	}
	
	@Override
	public void onBackPressed() {
		windowController.onBackPressed();
		super.onBackPressed();
	}

}
