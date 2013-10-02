package org.opentravelmate.widget.menu;

import org.json.JSONException;
import org.json.JSONObject;
import org.opentravelmate.R;
import org.opentravelmate.commons.ExceptionListener;
import org.opentravelmate.commons.ImageLoader;
import org.opentravelmate.commons.UIThreadExecutor;
import org.opentravelmate.widget.HtmlLayout;
import org.opentravelmate.widget.HtmlLayoutParams;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.LinearLayout;

/**
 * Injected object.
 * 
 * @author marc.plouhinec@gmail.com (Marc Plouhinec)
 */
public class NativeMenu {
	
	public static final String GLOBAL_OBJECT_NAME = "org_opentravelmate_native_widget_menu_nativeMenu";
	public static final String SCRIPT_URL = "/native/widget/menu/nativeMenu.js";
	
	private final ExceptionListener exceptionListener;
	private final HtmlLayout htmlLayout;
	private final LayoutInflater layoutInflater;
	private final String baseUrl;
	
	/**
	 * Create a native menu.
	 */
	public NativeMenu(ExceptionListener exceptionListener, HtmlLayout htmlLayout, String baseUrl) {
		this.exceptionListener = exceptionListener;
		this.htmlLayout = htmlLayout;
		this.layoutInflater = (LayoutInflater) htmlLayout.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		this.baseUrl = baseUrl;
	}
	
	/**
	 * Build the native view object for the current widget.
	 * 
	 * @param jsonLayoutParams
	 */
	@JavascriptInterface
	public void buildView(final String jsonLayoutParams) {
		UIThreadExecutor.execute(new Runnable() {
			@Override public void run() {
				try {
					buildView(HtmlLayoutParams.fromJsonLayoutParams(new JSONObject(jsonLayoutParams)));
				} catch (JSONException e) {
					exceptionListener.onException(false, e);
				}
			}
		});
	}
	
	/**
	 * Update the native view object for the current widget.
	 * 
	 * @param jsonLayoutParams
	 */
	@JavascriptInterface
	public void updateView(final String jsonLayoutParams) {
		UIThreadExecutor.execute(new Runnable() {
			@Override public void run() {
				try {
					HtmlLayoutParams layoutParams = HtmlLayoutParams.fromJsonLayoutParams(new JSONObject(jsonLayoutParams));
					View view = htmlLayout.findViewByPlaceHolderId(layoutParams.id);
					if (view != null) {
						view.setLayoutParams(layoutParams);
					}
				} catch (JSONException e) {
					exceptionListener.onException(false, e);
				}
				
			}
		});
	}
	
	/**
	 * Remove the native view object for the current widget.
	 * 
	 * @param id Place holder ID
	 */
	@JavascriptInterface
	public void removeView(final String id) {
		UIThreadExecutor.execute(new Runnable() {
			@Override public void run() {
				View view = htmlLayout.findViewByPlaceHolderId(id);
				if (view != null) {
					htmlLayout.removeView(view);
				}
			}
		});
	}
	
	/**
	 * Build the native view object for the current widget.
	 * 
	 * @param layoutParams
	 */
	public void buildView(HtmlLayoutParams layoutParams) {
		View view = layoutInflater.inflate(R.layout.menu_layout, htmlLayout, false);
		view.setLayoutParams(layoutParams);
		htmlLayout.addView(view);
		
		// Set the logo
		ImageView imageViewMenuLogo = (ImageView)view.findViewById(R.id.imageViewMenuLogo);
		imageViewMenuLogo.setImageBitmap(null);
		ImageLoader.loadImageForImageView(imageViewMenuLogo, this.baseUrl + "extensions/core/widget/menu/image/ic_logo.png", exceptionListener);
		
		// Add the 'more' button
		this.addMenuItem(layoutParams.id, new MenuItem(-1, "More", "More", this.baseUrl + "extensions/core/widget/menu/image/ic_btn_more.png"));
	}
	
	/**
	 * Add an item to the menu.
	 * 
	 * @param menuPlaceHolderId
	 * @param jsonMenuItem
	 */
	@JavascriptInterface
	public void addMenuItem(final String menuPlaceHolderId, final String jsonMenuItem) {
		UIThreadExecutor.execute(new Runnable() {
			@Override public void run() {
				try {
					addMenuItem(menuPlaceHolderId, MenuItem.fromJsonMenuItem(new JSONObject(jsonMenuItem)));
				} catch (JSONException e) {
					exceptionListener.onException(false, e);
				}
			}
		});
	}
	
	/**
	 * Add an item to the menu.
	 * 
	 * @param menuPlaceHolderId
	 * @param menuItem
	 */
	public void addMenuItem(final String menuPlaceHolderId, final MenuItem menuItem) {
		View menuView = htmlLayout.findViewByPlaceHolderId(menuPlaceHolderId);
		if (menuView == null) {
			exceptionListener.onException(false, new IllegalStateException("Unknown menu: " + menuPlaceHolderId));
			return;
		}
		
		LinearLayout buttonPanel = (LinearLayout)menuView.findViewById(R.id.linearLayoutMainMenuButtonPanel);
		final MenuItemView menuItemButton = (MenuItemView)layoutInflater.inflate(R.layout.menu_item_layout, htmlLayout, false);
		buttonPanel.addView(menuItemButton, 0, new LinearLayout.LayoutParams(50, 50){{
			setMargins(0, menuItemButton.getMarginTop(), menuItemButton.getMarginRight(), menuItemButton.getMarginBottom());
		}});
		menuItemButton.setContentDescription(menuItem.title);
		menuItemButton.setImageBitmap(null);
		ImageLoader.loadImageForImageView(menuItemButton, menuItem.iconUrl, exceptionListener);
		
		// Handle the click event
		menuItemButton.setOnClickListener(new View.OnClickListener() {
			@Override public void onClick(View v) {
				WebView webView = (WebView)htmlLayout.findViewByPlaceHolderId(HtmlLayout.MAIN_WEBVIEW_ID);
				webView.loadUrl("javascript:" +
						"(function handleMenuItemClick() {" +
						"    require(['extensions/core/widget/Widget'], function (Widget) {" +
						"        var menu = Widget.findById('" + menuPlaceHolderId + "');" +
						"        menu.fireClickEvent(" + menuItem.id + ");" +
						"    });" +
						"})();");
			}
		});
	}
}
