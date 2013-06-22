package org.opentravelmate.widget.webview.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;

/**
 * Display the widgets according to their location on the HTML document.
 * 
 * @author Marc Plouhinec
 */
public class HtmlLayout extends ViewGroup {

	/**
	 * 
	 * @param context
	 * @param baseWebView
	 */
	public HtmlLayout(Context context, WebView baseWebView) {
		super(context);
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		// Set the size of the layout view
		setMeasuredDimension(this.getWidth(), this.getHeight());
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		
		// Set the size of the children
		int wspec = MeasureSpec.makeMeasureSpec(getMeasuredWidth()/getChildCount(), MeasureSpec.EXACTLY);
	    int hspec = MeasureSpec.makeMeasureSpec(getMeasuredHeight(), MeasureSpec.EXACTLY);
	    for(int i=0; i<getChildCount(); i++){
	       View v = getChildAt(i);
	       v.measure(wspec, hspec);
	    }
	}

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		int itemWidth = (r-l)/getChildCount();
		for(int i=0; i< this.getChildCount(); i++){
			View v = getChildAt(i);
			v.layout(itemWidth*i, t, (i+1)*itemWidth, b);
		}
	}

}
