package org.opentravelmate.widget.mainmenu;

import org.opentravelmate.commons.LibraryManager;
import org.opentravelmate.commons.LibraryRegistrar;
import org.opentravelmate.commons.utils.ExceptionListener;
import org.opentravelmate.widget.mainmenu.js.MainMenuJavaObject;

import android.app.Activity;

/**
 * Manage the main menu library.
 * 
 * @author Marc Plouhinec
 */
public class MainMenuLibraryManager implements LibraryManager {
	
	private MainMenuJavaObject mainMenuJavaObject;

	@Override
	public void initialize(Activity activity, LibraryRegistrar libraryRegistrar, ExceptionListener exceptionListener) {
		mainMenuJavaObject = new MainMenuJavaObject(activity);
		
		libraryRegistrar.registerResources("/js/org/opentravelmate/widget/mainmenu/v1", "/org/opentravelmate/widget/mainmenu/v1");
		libraryRegistrar.registerJavaObject(
				this.mainMenuJavaObject,
				"org_opentravelmate_widget_mainmenu_mainMenuJavaObject",
				"/org/opentravelmate/widget/mainmenu/v1/mainMenuJavaObject.js");
	}

	@Override
	public void start() {
	}

}
