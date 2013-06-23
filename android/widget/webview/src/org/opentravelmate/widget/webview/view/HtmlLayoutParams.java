package org.opentravelmate.widget.webview.view;

import android.view.ViewGroup;

/**
 * Layout parameters of a child view of an HtmlLayout.
 * 
 * @author Marc Plouhinec
 */
public class HtmlLayoutParams extends ViewGroup.LayoutParams {

	public final String htmlElementId;
	public final int x;
	public final int y;
	public final boolean visible;
	
	public HtmlLayoutParams(String htmlElementId, int x, int y, int width, int height, boolean visible) {
		super(width, height);
		this.htmlElementId = htmlElementId;
		this.x = x;
		this.y = y;
		this.visible = visible;
	}
}
