package org.opentravelmate.geolocation;

import org.opentravelmate.commons.ImmutableList;

import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;

/**
 * Wrapper around the Android LocationManager in order to provide geolocations according to different strategies.
 * 
 * @author Marc Plouhinec
 */
public class UserLocationProvider {

	private static final int UPDATE_MIN_TIME = 5000; //Milliseconds
	private static final int UPDATE_MIN_DISTANCE = 10; //Meters
	public static final int MAX_LOCATION_AGE = 1000 * 60 * 2; //Milliseconds
	public static final int ACCEPTABLE_ACCURACY = 100; //Meters
	
	private final static UserLocationProvider INSTANCE = new UserLocationProvider();
	private LocationManager locationManager = null;
	private LocationListener locationListener = null;
	private volatile ImmutableList<Listener> currentPositionListeners = ImmutableList.<Listener>Builder().build();
	private volatile ImmutableList<Listener> followUserListeners = ImmutableList.<Listener>Builder().build();
	private volatile Location lastGoodLocation = null;
	private final Handler handler = new Handler();
	
	private UserLocationProvider() {}
	
	public static UserLocationProvider getInstance() {
		return INSTANCE;
	}
	
	/**
	 * Initialize the UserLocationProvider.
	 * 
	 * @param locationManager
	 */
	public void init(LocationManager locationManager) {
		this.locationManager = locationManager;
		this.lastGoodLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
	}
	
	/**
	 * Stop the UserLocationProvider.
	 */
	public void stop() {
		currentPositionListeners = ImmutableList.<Listener>Builder().build();
		followUserListeners = ImmutableList.<Listener>Builder().build();
		
		if (locationListener != null) {
			locationManager.removeUpdates(locationListener);
			locationListener = null;
		}
	}
	
	/**
	 * @return true if the GPS location is possible, false if not.
	 */
	public boolean isGPSLocationPossible() {
		return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
	}
	
	/**
	 * Add a location listener.
	 * 
	 * @param strategy
	 * @param listener
	 */
	public void addListener(Strategy strategy, final Listener listener) {
		//Register an internal location listener
		handler.post(new Runnable() {
			@Override public void run() {
				registerToLocationManagerIfNecessary();
			}
		});
		
		//Add the listener to the map
		switch (strategy) {
		case CURRENT_POSITION:
			currentPositionListeners = ImmutableList.<Listener>Builder()
				.addAll(currentPositionListeners)
				.add(listener)
				.build();
			break;

		case FOLLOW_USER:
			followUserListeners = ImmutableList.<Listener>Builder()
				.addAll(followUserListeners)
				.add(listener)
				.build();
			break;
		}
		
		//Send the last known location
		handler.post(new Runnable() {
			@Override public void run() {
				Location lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
				Location locationNetwork = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
				if (isBetterLocation(locationNetwork, lastKnownLocation)) {
					lastGoodLocation = locationNetwork;
				}
				Location currentLastGoodLocation = lastGoodLocation;
				if (currentLastGoodLocation != null) {
					listener.onLocationprovided(currentLastGoodLocation);
				}
			}
		});
	}
	
	/**
	 * Remove a location listener.
	 * 
	 * @param listener
	 */
	public void removeListener(Listener listener) {
		if (currentPositionListeners.contains(listener)) {
			currentPositionListeners = ImmutableList.<Listener>Builder()
					.addAllBut(currentPositionListeners, listener)
					.build();
		}
		if (followUserListeners.contains(listener)) {
			followUserListeners = ImmutableList.<Listener>Builder()
					.addAllBut(followUserListeners, listener)
					.build();
		}
	}
	
	/**
	 * Register a LocationListener to the LocationManager if necessary.
	 */
	private void registerToLocationManagerIfNecessary() {
		if (locationListener != null) {
			return;
		}
		
		locationListener = new LocationListener() {
			@Override public void onStatusChanged(String provider, int status, Bundle extras) { }
			@Override public void onProviderEnabled(String provider) { }
			@Override public void onProviderDisabled(String provider) { }
			@Override public void onLocationChanged(final Location location) {
				handleNewLocation(location);
			}
		};
		locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, UPDATE_MIN_TIME, UPDATE_MIN_DISTANCE, locationListener);
		locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, UPDATE_MIN_TIME, UPDATE_MIN_DISTANCE, locationListener);
	}
	
	/**
	 * Handle a new location from the the LocationManager.
	 * 
	 * @param location
	 */
	private void handleNewLocation(Location location) {
		//Return if the new location is not better
		if (!isBetterLocation(location, lastGoodLocation)) {
			return;
		}
		
		//Save the new location and forward it to the listeners
		lastGoodLocation = location;
		for (Listener listener : followUserListeners) {
			listener.onLocationprovided(location);
		}
		for (Listener listener : currentPositionListeners) {
			listener.onLocationprovided(location);
		}
		
		//Check if we need a LocationManager listener
		if (ACCEPTABLE_ACCURACY >= location.getAccuracy()) {
			//Remove all the listenerCurrentPosition listeners
			currentPositionListeners = ImmutableList.<Listener>Builder().build();
			
			//Remove the LocationManager listener if there is no listenerFollowUser
			if (followUserListeners.isEmpty()) {
				handler.post(new Runnable() {
					@Override public void run() {
						locationManager.removeUpdates(locationListener);
						locationListener = null;
					}
				});
			}
		}
	}
	
	/**
	 * Thanks to the Google Android developer page:
	 * @see http://developer.android.com/guide/topics/location/strategies.html
	 * 
	 * @param newLocation
	 * @param currentBestLocation
	 * @return true if the newLocation is better than the current one, or false if not.
	 */
	private static boolean isBetterLocation(Location newLocation, Location currentBestLocation) {
		if (currentBestLocation == null) {
	        // A new location is always better than no location
	        return true;
	    }
		if (newLocation == null) {
			return false;
		}

	    // Check whether the new location fix is newer or older
	    long timeDelta = newLocation.getTime() - currentBestLocation.getTime();
	    boolean isSignificantlyNewer = timeDelta > MAX_LOCATION_AGE;
	    boolean isSignificantlyOlder = timeDelta < -MAX_LOCATION_AGE;
	    boolean isNewer = timeDelta > 0;

	    // If it's been more than two minutes since the current location, use the new location
	    // because the user has likely moved
	    if (isSignificantlyNewer) {
	        return true;
	    // If the new location is more than two minutes older, it must be worse
	    } else if (isSignificantlyOlder) {
	        return false;
	    }

	    // Check whether the new location fix is more or less accurate
	    int accuracyDelta = (int) (newLocation.getAccuracy() - currentBestLocation.getAccuracy());
	    boolean isLessAccurate = accuracyDelta > 0;
	    boolean isMoreAccurate = accuracyDelta < 0;
	    boolean isSignificantlyLessAccurate = accuracyDelta > 200;

	    // Check if the old and new location are from the same provider
	    boolean isFromSameProvider = isSameProvider(newLocation.getProvider(), currentBestLocation.getProvider());

	    // Determine location quality using a combination of timeliness and accuracy
	    if (isMoreAccurate) {
	        return true;
	    } else if (isNewer && !isLessAccurate) {
	        return true;
	    } else if (isNewer && !isSignificantlyLessAccurate && isFromSameProvider) {
	        return true;
	    }
	    return false;
	}
	
	/**
	 * Checks whether two providers are the same
	 */
	private static boolean isSameProvider(String provider1, String provider2) {
	    if (provider1 == null) {
	    	return provider2 == null;
	    }
	    return provider1.equals(provider2);
	}
	
	/**
	 * Location provider strategy.
	 */
	public static enum Strategy {
		CURRENT_POSITION, //Send the user position several times until the accuracy is good enough
		FOLLOW_USER //Send the user position at regular intervals
	}
	
	/**
	 * Listener called when the user location is considered as usable.
	 */
	public static interface Listener {
		public void onLocationprovided(Location location);
	}

}
