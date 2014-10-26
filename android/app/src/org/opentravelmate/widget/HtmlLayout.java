package org.opentravelmate.widget;

import java.util.HashMap;
import java.util.Map;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;

/**
 * Display the widgets according to their location on the HTML document.
 * 
 * @author Marc Plouhinec
 */
@SuppressLint("ViewConstructor")
public class HtmlLayout extends ViewGroup {
	
	public static final String MAIN_WEBVIEW_ID = "mainWebView";
	
	private WebView cachedMainWebView = null;
	private Map<String, Integer> viewIdByPlaceHolderId = new HashMap<String, Integer>();
	private int nextViewId = Integer.MAX_VALUE / 42;

	/**
	 * Create a HtmlLayout.
	 * 
	 * @param context
	 * @param rootWebView
	 */
	public HtmlLayout(Context context) {
		super(context);
	}

	@Override
	public void addView(View child) {
		if (child != null && child.getLayoutParams() instanceof HtmlLayoutParams) {
			HtmlLayoutParams layoutParams = (HtmlLayoutParams)child.getLayoutParams();
			int generatedViewId = nextViewId++;
			String placeHolderId = layoutParams.id;
			viewIdByPlaceHolderId.put(placeHolderId, generatedViewId);
			child.setId(generatedViewId);
		}
		
		super.addView(child);
	}
	
	/**
	 * Find a view by its place holder ID.
	 * 
	 * @param placeHolderId
	 * @return view or null if not found
	 */
	public View findViewByPlaceHolderId(String placeHolderId) {
		if (viewIdByPlaceHolderId.containsKey(placeHolderId)) {
			int viewId = viewIdByPlaceHolderId.get(placeHolderId);
			return this.findViewById(viewId);
		}
		return null;
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
		if (getMainWebView() == null) {
			return;
		}
		int childWidthMeasureSpec = MeasureSpec.makeMeasureSpec(getMeasuredWidth(), MeasureSpec.EXACTLY);
	    int childHeightMeasureSpec = MeasureSpec.makeMeasureSpec(getMeasuredHeight(), MeasureSpec.EXACTLY);
	    getMainWebView().measure(childWidthMeasureSpec, childHeightMeasureSpec);
		
		// Compute the size of the child views with the previous layout parameters
		for (int i = 0; i < this.getChildCount(); i++) {
			View childView = this.getChildAt(i);
			
			if (childView == getMainWebView()) {
				continue;
			}
			
			if (!(childView.getLayoutParams() instanceof HtmlLayoutParams)) {
				// Unknown layout parameter
				int measureSpec = MeasureSpec.makeMeasureSpec(0, MeasureSpec.EXACTLY);
				childView.measure(measureSpec, measureSpec);
				continue;
			}
			HtmlLayoutParams layoutParams = (HtmlLayoutParams)childView.getLayoutParams();
			
			childWidthMeasureSpec = MeasureSpec.makeMeasureSpec((int)Math.round(layoutParams.width * getMeasuredWidth()), MeasureSpec.EXACTLY);
			childHeightMeasureSpec = MeasureSpec.makeMeasureSpec((int)Math.round(layoutParams.height * getMeasuredHeight()), MeasureSpec.EXACTLY);
			childView.measure(childWidthMeasureSpec, childHeightMeasureSpec);
		}
	}

	@Override
	protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
		if (getMainWebView() == null) {
			return;
		}
		
		int width = right - left;
		int height = bottom - top;
		
		for (int i = 0; i < this.getChildCount(); i++) {
			View childView = this.getChildAt(i);
			
			if (childView == getMainWebView()) {
				childView.layout(left, top, right, bottom);
				continue;
			}
			
			if (!(childView.getLayoutParams() instanceof HtmlLayoutParams)) {
				// Unknown layout parameter
				childView.layout(0, 0, 0, 0);
				continue;
			}
			HtmlLayoutParams layoutParams = (HtmlLayoutParams)childView.getLayoutParams();
			
			if (!layoutParams.visible) {
				childView.layout(0, 0, 0, 0);
				continue;
			}
			
			childView.layout(
					(int)Math.round(layoutParams.x * width),
					(int)Math.round(layoutParams.y * height),
					(int)Math.round((layoutParams.x + layoutParams.width) * width),
					(int)Math.round((layoutParams.y + layoutParams.height) * height));
		}
	}
	
	/**
	 * @return main web view
	 */
	private WebView getMainWebView() {
		if (this.cachedMainWebView != null) {
			return this.cachedMainWebView;
		}
		
		for (int i = 0; i < this.getChildCount(); i++) {
			View childView = this.getChildAt(i);
			
			if (childView instanceof WebView && childView.getLayoutParams() instanceof HtmlLayoutParams) {
				WebView webView = (WebView) childView;
				HtmlLayoutParams layoutParams = (HtmlLayoutParams)childView.getLayoutParams();
				if (MAIN_WEBVIEW_ID.equals(layoutParams.id)) {
					this.cachedMainWebView = webView;
					return webView;
				}
			}
		}
		return null;
	}
}
