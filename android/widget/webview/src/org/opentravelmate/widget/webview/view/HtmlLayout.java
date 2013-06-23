package org.opentravelmate.widget.webview.view;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.opentravelmate.commons.widget.HtmlViewGroup;

import android.content.Context;
import android.os.Handler;
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
	private Handler handler = new Handler();

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
		
		// Compute the size of the child views with the previous layout parameters
		for (int i = 0; i < this.getChildCount(); i++) {
			View childView = this.getChildAt(i);
			
			if (childView == this.rootWebView) {
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
		int width = right - left;
		int height = bottom - top;
		
		for (int i = 0; i < this.getChildCount(); i++) {
			View childView = this.getChildAt(i);
			
			if (childView == this.rootWebView) {
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
	 * Add a child view with the given HTML element ID.
	 * 
	 * @param view
	 * @param htmlElementId
	 */
	public void addView(View view, String htmlElementId) {
		viewByHtmlElementId.put(htmlElementId, view);
		view.setLayoutParams(new HtmlLayoutParams(htmlElementId, 0, 0, 0, 0, false));
		this.addView(view);
		
		// Query the HTML document to get the views layout parameters
		JSONArray jsonViewHtmlElementIds = new JSONArray();
		for(int i = 0; i < getChildCount(); i++) {
			View childView = getChildAt(i);
			
			if (childView.getLayoutParams() instanceof HtmlLayoutParams) {
				HtmlLayoutParams layoutParams = (HtmlLayoutParams)childView.getLayoutParams();
				jsonViewHtmlElementIds.put(layoutParams.htmlElementId);
			}
		}
		rootWebView.loadUrl(
				"javascript:if(window.org_opentravelmate_widget_WebView){" +
				"  window.org_opentravelmate_widget_WebView.getPlaceHolderLayoutParams(" + jsonViewHtmlElementIds.toString() + ");" +
				"}");
	}
	
	/**
	 * Update the HtmlLayoutParams of the child views.
	 * 
	 * @param layoutParams
	 */
	public void updateViewsWithLayoutParams(final List<HtmlLayoutParams> layoutParams) {
		handler.post(new Runnable() {
			@Override public void run() {
				for (HtmlLayoutParams layoutParam : layoutParams) {
					View view = viewByHtmlElementId.get(layoutParam.htmlElementId);
					if (view == null) {
						continue;
					}
					
					view.setLayoutParams(layoutParam);
				}
				
				HtmlLayout.this.invalidate();
			}
		});
	}
}
