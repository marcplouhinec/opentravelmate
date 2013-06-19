package org.opentravelmate.widget.mainmenu.js;

import android.app.Activity;
import android.os.Handler;
import android.view.ViewGroup;
import android.webkit.JavascriptInterface;
import android.widget.Button;

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
	 * @param elementId Place holder for the main menu.
	 */
	@JavascriptInterface
	public void createMainMenu(String elementId) {
		handler.post(new Runnable() {
			@Override public void run() {
				ViewGroup rootLayout = (ViewGroup) activity.findViewById(123456789);
				
				Button button = new Button(rootLayout.getContext());
				button.setText("Coucou");
				rootLayout.addView(button);
			}
		});
	}

}
