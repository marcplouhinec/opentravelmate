package org.opentravelmate.widget.webview.view;

import java.util.HashMap;
import java.util.Map;

import org.opentravelmate.commons.widget.HtmlViewGroup;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;

/**
 * Display the widgets according to their location on the HTML document.
 * 
 * @author Marc Plouhinec
 */
public class HtmlLayout extends ViewGroup implements HtmlViewGroup {
	
	private final WebView rootWebView;
	private final Map<String, View> viewByHtmlElementId = new HashMap<String, View>();

	/**
	 * 
	 * @param context
	 * @param rootWebView
	 */
	public HtmlLayout(Context context, WebView rootWebView) {
		super(context);
		
		this.rootWebView = rootWebView;
		this.addView(rootWebView);
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		// Set the size of the layout view
		setMeasuredDimension(this.getWidth(), this.getHeight());
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		
		//
		// Set the size of the children
		//
		
		// Set the rootWebView size to 100% of the screen
		int childWidthMeasureSpec = MeasureSpec.makeMeasureSpec(getMeasuredWidth(), MeasureSpec.EXACTLY);
	    int childHeightMeasureSpec = MeasureSpec.makeMeasureSpec(getMeasuredHeight(), MeasureSpec.EXACTLY);
		this.rootWebView.measure(childWidthMeasureSpec, childHeightMeasureSpec);
		
		// Query the HTML document to get the views layout parameters
		//rootWebView.loadUrl("javascript:window.webViewHelper.getPlaceHolderLayoutParams();");
		
		// Compute the size of the child views with the previous layout parameters
		for(int i = 0; i < getChildCount(); i++) {
			View childView = getChildAt(i);
			
			if (childView == rootWebView) {
				break;
			}
			
			if (!(childView.getLayoutParams() instanceof HtmlLayoutParams)) {
				// Unknown layout parameter
				int measureSpec = MeasureSpec.makeMeasureSpec(0, MeasureSpec.EXACTLY);
				childView.measure(measureSpec, measureSpec);
				break;
			}
			HtmlLayoutParams layoutParams = (HtmlLayoutParams)childView.getLayoutParams();
			
			childWidthMeasureSpec = MeasureSpec.makeMeasureSpec(layoutParams.width, MeasureSpec.EXACTLY);
			childHeightMeasureSpec = MeasureSpec.makeMeasureSpec(layoutParams.height, MeasureSpec.EXACTLY);
			childView.measure(childWidthMeasureSpec, childHeightMeasureSpec);
		}
	}

	@Override
	protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
		for(int i = 0; i < getChildCount(); i++) {
			View childView = getChildAt(i);
			
			if (childView == rootWebView) {
				childView.layout(left, top, right, bottom);
				break;
			}
			
			if (!(childView.getLayoutParams() instanceof HtmlLayoutParams) && childView != rootWebView) {
				// Unknown layout parameter
				childView.layout(0, 0, 0, 0);
				break;
			}
			HtmlLayoutParams layoutParams = (HtmlLayoutParams)childView.getLayoutParams();
			
			childView.layout(layoutParams.x, layoutParams.y, layoutParams.x + layoutParams.width, layoutParams.y + layoutParams.height);
		}
	}

	/**
	 * Add a child view with the given HTML element ID.
	 * 
	 * @param view
	 * @param htmlElementId
	 */
	public void addView(View view, String htmlElementId) {
		viewByHtmlElementId.put(htmlElementId, view);
	}
}
