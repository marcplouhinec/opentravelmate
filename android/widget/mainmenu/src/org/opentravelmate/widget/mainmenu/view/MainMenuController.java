package org.opentravelmate.widget.mainmenu.view;

import org.opentravelmate.commons.widget.HtmlViewGroup;
import org.opentravelmate.widget.mainmenu.R;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


/**
 * Handle the layout and handle user events.
 * 
 * @author Marc Plouhinec
 */
public class MainMenuController {
	
	private final Activity activity;
	
	public MainMenuController(Activity activity) {
		this.activity = activity;
	}

	/**
	 * Initialize the view and register event listeners.
	 * 
	 * @param htmlElementId  Place-holder for the main menu.
	 */
	public void onCreate(final String htmlElementId) {
		HtmlViewGroup htmlViewGroup = (HtmlViewGroup) activity.findViewById(HtmlViewGroup.VIEW_ID);
		
		LayoutInflater inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View view = inflater.inflate(R.layout.mainmenu_layout, (ViewGroup)htmlViewGroup, false);
		htmlViewGroup.addView(view, htmlElementId);
	}


}
