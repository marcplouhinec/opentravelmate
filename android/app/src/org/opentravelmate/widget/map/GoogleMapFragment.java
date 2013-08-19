package org.opentravelmate.widget.map;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMapOptions;
import com.google.android.gms.maps.SupportMapFragment;

/**
 * Customized SupportMapFragment in order to have a finer control over the GoogleMap initialization.
 * 
 * Thanks to {@link http://stackoverflow.com/a/16329503/1844128}.
 */
public class GoogleMapFragment extends SupportMapFragment {

    private static final String SUPPORT_MAP_BUNDLE_KEY = "MapOptions";
    private OnGoogleMapFragmentListener listener = null;
    private boolean isInitialized = false;

    /**
     * Create a GoogleMapFragment with the given options.
     * 
     * @param options
     * @return GoogleMapFragment
     */
    public static GoogleMapFragment newInstance(GoogleMapOptions options) {
        Bundle arguments = new Bundle();
        arguments.putParcelable(SUPPORT_MAP_BUNDLE_KEY, options);

        GoogleMapFragment fragment = new GoogleMapFragment();
        fragment.setArguments(arguments);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);
        view.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        synchronized (this) {
        	if (listener != null) {
            	listener.onMapReady(getMap());
            }
            isInitialized = true;
		}
        return view;
    }
    
    /**
     * Register a listener for the MAP_READY event.
     * 
     * @param listener
     */
    public void setOnGoogleMapFragmentListener(OnGoogleMapFragmentListener listener) {
    	synchronized (this) {
    		if (isInitialized) {
        		listener.onMapReady(getMap());
        	} else {
        		this.listener = listener;
        	}
		}
    }
    
    /**
     * Listener called when the map is initialized.
     */
    public static interface OnGoogleMapFragmentListener {
        void onMapReady(GoogleMap map);
    }
}