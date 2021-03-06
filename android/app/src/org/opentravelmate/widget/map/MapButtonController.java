package org.opentravelmate.widget.map;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.opentravelmate.R;
import org.opentravelmate.commons.ExceptionListener;
import org.opentravelmate.commons.ImageLoader;
import org.opentravelmate.widget.HtmlLayout;
import org.opentravelmate.widget.HtmlLayoutParams;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;

/**
 * Handle map buttons.
 * 
 * @author Marc Plouhinec
 */
public class MapButtonController {
	
	private final static double MAP_BUTTON_MARGIN_RIGHT = 5;
	private final static double MAP_BUTTON_MARGIN_TOP = 5;
	private final static double MAP_BUTTON_WIDTH = 48;
	private final static double MAP_BUTTON_HEIGHT = 48;
	private final static double MAP_BUTTON_TOTAL_HEIGHT = MAP_BUTTON_MARGIN_TOP + MAP_BUTTON_HEIGHT;
	
	private final HtmlLayout htmlLayout;
	private HtmlLayoutParams mapLayoutParams;
	private final String baseUrl;
	private final ExceptionListener exceptionListener;
	private final LayoutInflater layoutInflater;
	private final List<ClickListener> clickListeners = new LinkedList<ClickListener>();
	private final List<MapButton> mapButtons = new ArrayList<MapButton>();
	private final List<ImageButton> imageButtons = new ArrayList<ImageButton>();
	
	/**
	 * Create a new MapButtonController.
	 */
	public MapButtonController(HtmlLayout htmlLayout, HtmlLayoutParams mapLayoutParams, String baseUrl, ExceptionListener exceptionListener) {
		this.htmlLayout = htmlLayout;
		this.mapLayoutParams = mapLayoutParams;
		this.baseUrl = baseUrl;
		this.exceptionListener = exceptionListener;
		this.layoutInflater = (LayoutInflater) htmlLayout.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	/**
	 * Add the given button to the map.
	 * 
	 * @param mapButton
	 */
	public void addButton(final MapButton mapButton) {
		mapButtons.add(mapButton);
		int nbPrecedingButtons = mapButtons.size() - 1;
		
		ImageButton button = (ImageButton)layoutInflater.inflate(R.layout.map_button_layout, htmlLayout, false);
		imageButtons.add(button);
		button.setBackgroundResource(R.drawable.mapbutton_selector);
		button.setLayoutParams(getButtonLayoutParams(mapButton, button, nbPrecedingButtons));
		htmlLayout.addView(button);
		
		ImageLoader.loadImageForImageView(button, baseUrl + mapButton.iconUrl, exceptionListener);
		
		// Call the click listeners when the button is clicked
		button.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				for(ClickListener listener : clickListeners) {
					listener.onClick(mapButton);
				}
			}
		});
	}
	
	/**
	 * Update the given button on the map.
	 * 
	 * @param mapButton
	 */
	public void updateButton(final MapButton mapButton) {
		// Update the map button in the inner collection
		for (int index = 0; index < mapButtons.size(); index++) {
			if (mapButtons.get(index).id == mapButton.id) {
				ImageButton button = imageButtons.get(index);
				ImageLoader.loadImageForImageView(button, baseUrl + mapButton.iconUrl, exceptionListener);
				break;
			}
		}
	}
	
	/**
	 * Remove the given button from the map.
	 * 
	 * @param mapButton
	 */
	public void removeButton(final MapButton mapButton) {
		int buttonIndex = -1;
		for (int index = 0; index < mapButtons.size(); index++) {
			if (mapButtons.get(index).id == mapButton.id) {
				buttonIndex = index;
				break;
			}
		}
		
		if (buttonIndex > 0) {
			htmlLayout.removeView(imageButtons.get(buttonIndex));
			mapButtons.remove(buttonIndex);
			imageButtons.remove(buttonIndex);
		}
	}
	
	/**
	 * Register a listener for a button click event.
	 * 
	 * @param listener
	 */
	public void onButtonClick(ClickListener listener) {
		clickListeners.add(listener);
	}
	
	/**
	 * Tell the displayed map type.
	 * 
	 * @param mapType SATELLITE or ROADMAP
	 */
	public void setMapType(String mapType) {
		int backgroundResource = "ROADMAP".equals(mapType) ? R.drawable.mapbutton_selector : R.drawable.mapbutton_selector_light;
		for (ImageButton button : imageButtons) {
			button.setBackgroundResource(backgroundResource);
		}
	}
	
	/**
	 * Function called when the screen is resized or the orientation is changed.
	 */
	public void onWindowResize(HtmlLayoutParams mapLayoutParams) {
		this.mapLayoutParams = mapLayoutParams;
		
		for (int i = 0; i < imageButtons.size(); i++) {
			ImageButton button = imageButtons.get(i);
			MapButton mapButton = mapButtons.get(i);
			
			button.setLayoutParams(getButtonLayoutParams(mapButton, button, i));
		}
	}
	
	/**
	 * Compute the Map Button widget LayoutParams.
	 * 
	 * @param mapButton
	 * @param button
	 * @param nbPrecedingButtons
	 * @return LayoutParams
	 */
	private HtmlLayoutParams getButtonLayoutParams(MapButton mapButton, ImageButton button, int nbPrecedingButtons) {
		double x = mapLayoutParams.x + mapLayoutParams.width - (MAP_BUTTON_MARGIN_RIGHT + MAP_BUTTON_WIDTH) / mapLayoutParams.windowWidth;
		double y = mapLayoutParams.y + (MAP_BUTTON_TOTAL_HEIGHT * nbPrecedingButtons + MAP_BUTTON_MARGIN_TOP) / mapLayoutParams.windowHeight;
		double width = MAP_BUTTON_WIDTH / mapLayoutParams.windowWidth;
		double height = MAP_BUTTON_HEIGHT / mapLayoutParams.windowHeight;
		
		HtmlLayoutParams layoutParams = new HtmlLayoutParams(
				"button-" + mapButton.id,
				x, y, width, height, true,
				Collections.<String, String>emptyMap(),
				mapLayoutParams.windowWidth,
				mapLayoutParams.windowHeight);
		return layoutParams;
	}
	
	/**
	 * Listen for the map button click events.
	 */
	public interface ClickListener {
		void onClick(MapButton mapButton);
	}
}
