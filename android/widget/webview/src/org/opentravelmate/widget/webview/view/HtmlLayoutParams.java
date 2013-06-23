package org.opentravelmate.widget.webview.view;

import android.view.ViewGroup;

/**
 * Layout parameters of a child view of an HtmlLayout.
 * 
 * @author Marc Plouhinec
 */
public class HtmlLayoutParams extends ViewGroup.LayoutParams {

	/**
	 * ID of the place-holder element in the HTML document.
	 */
	public final String htmlElementId;
	
	/**
	 * Abscissa of the widget (min = 0, max = 1).
	 */
	public final double x;
	
	/**
	 * Ordinate of the widget (min = 0, max = 1).
	 */
	public final double y;
	
	/**
	 * Width of the widget (min = 0, max = 1).
	 */
	public final double width;
	
	/**
	 * Height of the widget (min = 0, max = 1).
	 */
	public final double height;
	
	/**
	 * if true, the widget is visible, if false, the widget is not visible.
	 */
	public final boolean visible;
	
	/**
	 * Create a new HtmlLayoutParams object.
	 * 
	 * @param htmlElementId
	 * @param x
	 * @param y
	 * @param width
	 * @param height
	 * @param visible
	 */
	public HtmlLayoutParams(String htmlElementId, double x, double y, double width, double height, boolean visible) {
		super(0, 0);
		this.htmlElementId = htmlElementId;
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
		this.visible = visible;
	}
}
