package org.opentravelmate.widget.map.view;

import org.opentravelmate.commons.widget.HtmlViewGroup;
import org.opentravelmate.widget.map.R;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;

import android.app.Activity;
import android.content.Context;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Handle the layout and handle user events.
 * 
 * @author Marc Plouhinec
 */
public class MapController {
	
	private static final float DEFAULT_ZOOM = 13;
	private static final double DEFAULT_LATITUDE = 49.6;
	private static final double DEFAULT_LONGITUDE = 6.135;
	
	private final Activity activity;

	public MapController(Activity activity) {
		this.activity = activity;
	}
	
	/**
	 * Initialize the view and register event listeners.
	 * 
	 * @param htmlElementId  Place-holder for the map.
	 */
	public void onCreate(final String htmlElementId) {
		HtmlViewGroup htmlViewGroup = (HtmlViewGroup) activity.findViewById(HtmlViewGroup.VIEW_ID);
		
		LayoutInflater inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View view = inflater.inflate(R.layout.map_layout, (ViewGroup)htmlViewGroup, false);
		htmlViewGroup.addView(view, htmlElementId);
		
		GoogleMap map = ((SupportMapFragment) ((FragmentActivity)activity).getSupportFragmentManager().findFragmentById(R.id.map)).getMap();
		CameraPosition cameraPosition = new CameraPosition.Builder()
			.target(new LatLng(DEFAULT_LATITUDE, DEFAULT_LONGITUDE))
			.zoom(DEFAULT_ZOOM)
			.build();
		map.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
	}

}
