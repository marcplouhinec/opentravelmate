package org.opentravelmate.widget.webview;

import java.util.LinkedHashMap;
import java.util.Map;

import android.view.View;

/**
 * Contains all the views.
 * 
 * @author Marc Plouhinec
 */
public class ViewRepository {
	
	private LinkedHashMap<String, View> viewById = new LinkedHashMap<String, View>();
	
	/**
	 * Save a view with its ID.
	 * 
	 * @param viewId String ID
	 * @param view
	 * @return view index (internal widget ID)
	 */
	public int put(String viewId, View view) {
		viewById.put(viewId, view);
		return viewById.size() - 1;
	}
	
	/**
	 * Find a view by its String ID
	 * 
	 * @param viewId
	 * @return view
	 */
	public View getViewById(String viewId) {
		return viewById.get(viewId);
	}
	
	/**
	 * Get the index of a view.
	 * 
	 * @param view
	 * @return view index (internal widget ID)
	 */
	public int getViewIndex(View view) {
		int index = 0;
		for (Map.Entry<String, View> entry : viewById.entrySet()) {
			if (entry.getValue().equals(view)) {
				return index;
			}
			index++;
		}
		return -1;
	}

}
