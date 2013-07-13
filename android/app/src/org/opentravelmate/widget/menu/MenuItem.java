package org.opentravelmate.widget.menu;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Define a menu item.
 *
 * @author marc.plouhinec@gmail.com (Marc Plouhinec)
 */
public class MenuItem {
	/**
	 * Item title.
	 */
	public final String title;
	
	/**
	 * Item tool tip (displayed when the user put the mouse over the item).
	 */
	public final String tooltip;
	
	/**
	 * Item icon URL.
	 */
	public final String iconUrl;
	
	/**
	 * Create a menu item.
	 * 
	 * @param title
	 * @param tooltip
	 * @param iconUrl
	 */
	public MenuItem(String title, String tooltip, String iconUrl) {
		this.title = title;
		this.tooltip = tooltip;
		this.iconUrl = iconUrl;
	}
	
	/**
	 * Build a MenuItem from a JSON-serialized representation.
	 * 
	 * @param jsonMenuItem
	 * @return MenuItem
	 * @throws JSONException
	 */
	public static MenuItem fromJsonMenuItem(JSONObject jsonMenuItem) throws JSONException {
		String title = jsonMenuItem.getString("title");
		String tooltip = jsonMenuItem.getString("tooltip");
		String iconUrl = jsonMenuItem.getString("iconUrl");
		return new MenuItem(title, tooltip, iconUrl);
	}

	@Override
	public String toString() {
		return "MenuItem [title=" + title + ", tooltip=" + tooltip
				+ ", iconUrl=" + iconUrl + "]";
	}
}
