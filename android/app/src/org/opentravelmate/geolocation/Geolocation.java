package org.opentravelmate.geolocation;

import java.util.Date;

import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;

/**
 * HTML5 Geolocation service implementation.
 * 
 * @see http://dev.w3.org/geo/api/spec-source.html
 * @author Marc Plouhinec
 */
public class Geolocation {
	
	private static final int UPDATE_MIN_TIME = 5000; //Milliseconds
	private static final int UPDATE_MIN_DISTANCE = 10; //Meters
	private static final String TIMEOUT_MESSAGE = "Unable to provide a position on time.";
	private final LocationManager locationManager;
	
	/**
	 * Create a new Geolocation service.
	 * 
	 * @param locationManager
	 */
	public Geolocation(LocationManager locationManager) {
		this.locationManager = locationManager;
	}
	
	/**
	 * Get the current user location according to the given options.
	 * 
	 * @param successCallback
	 * @param errorCallback
	 * @param options
	 */
	public void getCurrentPosition(PositionCallback successCallback, PositionErrorCallback errorCallback, PositionOptions options) {
		
		// First check if the last known location is acceptable
		if (options.maximumAge > 0) {
			Location lastKnownNetLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
			Location lastKnownGpsLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
			
			Location locationToCheck = 
					isBetterLocation(lastKnownNetLocation, lastKnownGpsLocation, options.maximumAge) ? lastKnownNetLocation : lastKnownGpsLocation;
			if (isAcceptableLocation(locationToCheck, options)) {
				successCallback.on(locationToPosition(locationToCheck));
				return;
			}
		}
		
		// Query the location manager
		Handler handler = new Handler(); // Handler used to avoid race conditions
		CurrentPositionQuery currentPositionQuery = new CurrentPositionQuery(successCallback, errorCallback, options);
		CurrentPositionLocationListener listener = new CurrentPositionLocationListener(currentPositionQuery, handler);
		
		if (options.enableHighAccuracy) {
			locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, UPDATE_MIN_TIME, UPDATE_MIN_DISTANCE, listener);
		} else {
			locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, UPDATE_MIN_TIME, UPDATE_MIN_DISTANCE, listener);
		}
		
		// Check timeout
		if (options.timeout > 0) {
			handler.postDelayed(new TimeOutChecker(currentPositionQuery, listener), options.timeout);
		}
	}

	/**
	 * Contains the parameters that are passed to {@link Geolocation#getCurrentPosition}.
	 */
	private static class CurrentPositionQuery {
		private final PositionCallback successCallback;
		private final PositionErrorCallback errorCallback;
		private final PositionOptions options;
		private volatile boolean processed = false;
		
		private CurrentPositionQuery(PositionCallback successCallback, PositionErrorCallback errorCallback, PositionOptions options) {
			this.successCallback = successCallback;
			this.errorCallback = errorCallback;
			this.options = options;
		}
	}
	
	/**
	 * Listener called when a location is available for {@link Geolocation#getCurrentPosition}.
	 */
	private class CurrentPositionLocationListener implements LocationListener {
		private final CurrentPositionQuery currentPositionQuery;
		private final Handler handler;
		
		private CurrentPositionLocationListener(CurrentPositionQuery currentPositionQuery, Handler handler) {
			this.currentPositionQuery = currentPositionQuery;
			this.handler = handler;
		}
		
		@Override public void onLocationChanged(final Location location) {
			handler.post(new Runnable() {
				@Override public void run() {
					if (isAcceptableLocation(location, currentPositionQuery.options)) {
						currentPositionQuery.processed = true;
						currentPositionQuery.successCallback.on(locationToPosition(location));
						locationManager.removeUpdates(CurrentPositionLocationListener.this);
					}
				}
			});
		}
		@Override public void onProviderDisabled(String provider) {}
		@Override public void onProviderEnabled(String provider) {}
		@Override public void onStatusChanged(String provider, int status, Bundle extras) {}
	}
	
	/**
	 * Check {@link Geolocation#getCurrentPosition} timeout.
	 */
	private class TimeOutChecker implements Runnable {
		
		private final CurrentPositionQuery currentPositionQuery;
		private final CurrentPositionLocationListener locationListener;

		private TimeOutChecker(
				CurrentPositionQuery currentPositionQuery,
				CurrentPositionLocationListener locationListener) {
			this.currentPositionQuery = currentPositionQuery;
			this.locationListener = locationListener;
		}

		@Override public void run() {
			if (!currentPositionQuery.processed) {
				currentPositionQuery.errorCallback.on(new PositionError(PositionError.TIMEOUT, TIMEOUT_MESSAGE));
				locationManager.removeUpdates(locationListener);
			}
		}
	}
	
	/**
	 * Check if the given location is acceptable or not.
	 * 
	 * @param location
	 * @param options
	 * @return true if acceptable, false if not.
	 */
	private static boolean isAcceptableLocation(Location location, PositionOptions options) {
		return options.maximumAge >= new Date().getTime() - location.getTime();
	}
	
	/**
	 * Thanks to the Google Android developer page:
	 * @see http://developer.android.com/guide/topics/location/strategies.html
	 * 
	 * @param newLocation
	 * @param currentBestLocation
	 * @return true if the newLocation is better than the current one, or false if not.
	 */
	private static boolean isBetterLocation(Location newLocation, Location currentBestLocation, long maxLocationAge) {
		if (currentBestLocation == null) {
	        // A new location is always better than no location
	        return true;
	    }
		if (newLocation == null) {
			return false;
		}

	    // Check whether the new location fix is newer or older
	    long timeDelta = newLocation.getTime() - currentBestLocation.getTime();
	    boolean isSignificantlyNewer = timeDelta > maxLocationAge;
	    boolean isSignificantlyOlder = timeDelta < -maxLocationAge;
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
	 * Convert a location into a position.
	 * 
	 * @param location
	 * @return position
	 */
	private static Position locationToPosition(Location location) {
		return new Position(new Coordinates(
				location.getLatitude(),
				location.getLongitude(),
				location.getAltitude(),
				location.getAccuracy(),
				location.getAccuracy(),
				location.getBearing(),
				location.getSpeed()), location.getTime());
	}
}
