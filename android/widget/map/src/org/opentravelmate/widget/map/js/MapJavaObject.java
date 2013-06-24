package org.opentravelmate.widget.map.js;

import org.opentravelmate.commons.widget.HtmlViewGroup;

import android.app.Activity;
import android.os.Handler;
import android.webkit.JavascriptInterface;
import android.widget.Button;

/**
 * Implementation of the mapJavaObject interface.
 * 
 * @author Marc Plouhinec
 */
public class MapJavaObject {
	
	private final Handler handler = new Handler();
	private final Activity activity;

	public MapJavaObject(Activity activity) {
		this.activity = activity;
	}

	/**
	 * Create a new map at the location specified by the given element.
	 * 
	 * @param htmlElementId Place holder for the map.
	 */
	@JavascriptInterface
	public void createMap(final String htmlElementId) {
		handler.post(new Runnable() {
			@Override public void run() {
				HtmlViewGroup htmlViewGroup = (HtmlViewGroup) activity.findViewById(HtmlViewGroup.VIEW_ID);
				Button button = new Button(activity);
				button.setText("Test");
				htmlViewGroup.addView(button, htmlElementId);
			}
		});
	}

}
