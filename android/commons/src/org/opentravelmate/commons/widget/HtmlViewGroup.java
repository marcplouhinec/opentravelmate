package org.opentravelmate.commons.widget;

import android.content.Context;
import android.view.View;

/**
 * View group where to put the views that are linked to place holders in the HTML document.
 * 
 * @author Marc Plouhinec
 */
public interface HtmlViewGroup {
	
	public static final int VIEW_ID = Integer.MAX_VALUE - 42;
	
	/**
	 * @return View context
	 */
	public Context getContext();
	
	/**
	 * Add a child view with the given HTML element ID.
	 * 
	 * @param view
	 * @param htmlElementId
	 */
	public void addView(View view, String htmlElementId);

}
