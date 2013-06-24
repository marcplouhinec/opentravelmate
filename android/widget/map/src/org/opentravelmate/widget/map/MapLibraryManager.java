package org.opentravelmate.widget.map;

import org.opentravelmate.commons.LibraryManager;
import org.opentravelmate.commons.LibraryRegistrar;
import org.opentravelmate.commons.utils.ExceptionListener;
import org.opentravelmate.widget.map.js.MapJavaObject;

import android.app.Activity;

/**
 * Manage the map library.
 * 
 * @author Marc Plouhinec
 */
public class MapLibraryManager implements LibraryManager {
	
	private MapJavaObject mapJavaObject;

	@Override
	public void initialize(Activity activity, LibraryRegistrar libraryRegistrar, ExceptionListener exceptionListener) {
		mapJavaObject = new MapJavaObject(activity);
		
		libraryRegistrar.registerResources("/js/org/opentravelmate/widget/map/v1", "/org/opentravelmate/widget/map/v1");
		libraryRegistrar.registerJavaObject(
				this.mapJavaObject,
				"org_opentravelmate_widget_map_mapJavaObject",
				"/org/opentravelmate/widget/map/v1/mapJavaObject.js");
	}

	@Override
	public void start() {
	}

}
