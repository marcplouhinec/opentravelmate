package org.opentravelmate.widget.webview.js;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.opentravelmate.commons.utils.ExceptionListener;
import org.opentravelmate.widget.webview.view.HtmlLayout;
import org.opentravelmate.widget.webview.view.HtmlLayoutParams;

import android.webkit.JavascriptInterface;

/**
 * Implementation of the webViewJavaObject interface.
 * 
 * @author Marc Plouhinec
 */
public class WebViewJavaObject {
	
	private HtmlLayout htmlLayout;
	private ExceptionListener exceptionListener;
	
	public void setHtmlLayout(HtmlLayout htmlLayout) {
		this.htmlLayout = htmlLayout;
	}

	public void setExceptionListener(ExceptionListener exceptionListener) {
		this.exceptionListener = exceptionListener;
	}

	/**
	 * Get the root web view ID.
	 * 
	 * @return web view ID.
	 */
	@JavascriptInterface
	public String getRootWebViewId() {
		return "FAKE-ID";
	}
	
	/**
	 * Update the place-holders layout parameters.
	 * 
	 * @param windowWidth
	 * @param windowHeight
	 * @param jsonPlaceholderLayoutParams
	 */
	public void updatePlaceHolderLayoutParams(int windowWidth, int windowHeight, String jsonPlaceholderLayoutParams) {
		try {
			JSONArray jsonPlaceholderLayoutParamsArray = new JSONArray(jsonPlaceholderLayoutParams);
			List<HtmlLayoutParams> layoutParams = new ArrayList<HtmlLayoutParams>(jsonPlaceholderLayoutParamsArray.length());
			
			for (int i = 0; i < jsonPlaceholderLayoutParamsArray.length(); i++) {
				JSONObject jsonLayoutParam = jsonPlaceholderLayoutParamsArray.getJSONObject(i);
				String htmlElementId = jsonLayoutParam.getString("htmlElementId");
				double x = jsonLayoutParam.getDouble("x") / windowWidth;
				double y = jsonLayoutParam.getDouble("y") / windowHeight;
				double width = jsonLayoutParam.getDouble("width") / windowWidth;
				double height = jsonLayoutParam.getDouble("height") / windowHeight;
				boolean visible = jsonLayoutParam.getBoolean("visible");
				layoutParams.add(new HtmlLayoutParams(htmlElementId, x, y, width, height, visible));
			}
			
			this.htmlLayout.updateViewsWithLayoutParams(layoutParams);
			
		} catch (JSONException e) {
			exceptionListener.onException(false, e);
		}
	}
}
