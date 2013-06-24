package org.opentravelmate.widget.mainmenu.js;

import org.opentravelmate.widget.mainmenu.view.MainMenuController;

import android.app.Activity;
import android.os.Handler;
import android.webkit.JavascriptInterface;

/**
 * Implementation of the mainMenuJavaObject interface.
 * 
 * @author Marc Plouhinec
 */
public class MainMenuJavaObject {
	
	private final Handler handler = new Handler();
	private final Activity activity;

	public MainMenuJavaObject(Activity activity) {
		this.activity = activity;
	}

	/**
	 * Create a new main menu at the location specified by the given element.
	 * 
	 * @param htmlElementId Place holder for the main menu.
	 */
	@JavascriptInterface
	public void createMainMenu(final String htmlElementId) {
		handler.post(new Runnable() {
			@Override public void run() {
				MainMenuController mainMenuController = new MainMenuController(activity);
				mainMenuController.onCreate(htmlElementId);
			}
		});
	}

}
