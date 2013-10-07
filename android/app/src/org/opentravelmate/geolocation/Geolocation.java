package org.opentravelmate.geolocation;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

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
	public static final int DEFAULT_MAX_LOCATION_AGE = 1000 * 60 * 2; //Milliseconds
	private final LocationManager locationManager;
	private final Map<Long, WatchPositionLocationListener> locationListenerByWatchId = new HashMap<Long, WatchPositionLocationListener>();
	private long nextWatchId = 42;
	
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
		if (sendLastKnownLocation(successCallback, options) != null) {
			return;
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
			handler.postDelayed(new CurrentPositionTimeOutChecker(currentPositionQuery, listener), options.timeout);
		}
	}
	
	/**
	 * Watch the device location.
	 * 
	 * @param successCallback
	 * @param errorCallback
	 * @param options
	 * @return watchId
	 */
	public Long watchPosition(PositionCallback successCallback, PositionErrorCallback errorCallback, PositionOptions options) {
		// Send the last known location
		Location lastKnownLocation = sendLastKnownLocation(successCallback, options);
		
		// Query the location manager
		Handler handler = new Handler(); // Handler used to avoid race conditions
		WatchPositionQuery watchPositionQuery = new WatchPositionQuery(successCallback, errorCallback, options);
		WatchPositionTimeOutChecker timeOutChecker = new WatchPositionTimeOutChecker(errorCallback);
		WatchPositionLocationListener listener = new WatchPositionLocationListener(watchPositionQuery, handler, lastKnownLocation, timeOutChecker);

		locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, UPDATE_MIN_TIME, UPDATE_MIN_DISTANCE, listener);
		if (options.enableHighAccuracy) {
			locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, UPDATE_MIN_TIME, UPDATE_MIN_DISTANCE, listener);
		}
		
		// Check timeout
		if (options.timeout > 0) {
			handler.postDelayed(timeOutChecker, options.timeout);
		}
		
		// Store the locationListener with a newly generated watch id
		Long watchId = nextWatchId++;
		locationListenerByWatchId.put(watchId, listener);
		return watchId;
	}
	
	/**
	 * Stop watching the device position.
	 * 
	 * @param watchId
	 */
	public void clearWatch(Long watchId) {
		WatchPositionLocationListener locationListener = locationListenerByWatchId.get(watchId);
		if (locationListener != null) {
			locationManager.removeUpdates(locationListener);
		}
	}
	
	/**
	 * Send the last known location if acceptable.
	 * 
	 * @param successCallback
	 * @param options
	 * @return sent location or null if no acceptable location is available.
	 */
	private Location sendLastKnownLocation(PositionCallback successCallback, PositionOptions options) {
		if (options.maximumAge > 0) {
			Location lastKnownNetLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
			Location lastKnownGpsLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
			
			Location locationToCheck = 
					isBetterLocation(lastKnownNetLocation, lastKnownGpsLocation, options.maximumAge) ? lastKnownNetLocation : lastKnownGpsLocation;
			if (isAcceptableLocation(locationToCheck, options)) {
				successCallback.on(locationToPosition(locationToCheck));
				return locationToCheck;
			}
		}
		return null;
	}
	
	/**
	 * Contains the parameters that are passed to {@link Geolocation#watchPosition}.
	 */
	private static class WatchPositionQuery {
		final PositionCallback successCallback;
		final PositionErrorCallback errorCallback;
		final PositionOptions options;
		
		private WatchPositionQuery(PositionCallback successCallback, PositionErrorCallback errorCallback, PositionOptions options) {
			this.successCallback = successCallback;
			this.errorCallback = errorCallback;
			this.options = options;
		}
	}

	/**
	 * Contains the parameters that are passed to {@link Geolocation#getCurrentPosition}.
	 */
	private static class CurrentPositionQuery extends WatchPositionQuery {
		private volatile boolean processed = false;
		
		private CurrentPositionQuery(PositionCallback successCallback, PositionErrorCallback errorCallback, PositionOptions options) {
			super(successCallback, errorCallback, options);
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
	 * Listener called when a location is available for {@link Geolocation#watchPosition}.
	 */
	private class WatchPositionLocationListener implements LocationListener {
		private final WatchPositionQuery watchPositionQuery;
		private final Handler handler;
		private final WatchPositionTimeOutChecker timeOutChecker;
		private volatile Location currentBestLocation;
		
		private WatchPositionLocationListener(
				WatchPositionQuery watchPositionQuery,
				Handler handler,
				Location currentBestLocation,
				WatchPositionTimeOutChecker timeOutChecker) {
			this.watchPositionQuery = watchPositionQuery;
			this.handler = handler;
			this.currentBestLocation = currentBestLocation;
			this.timeOutChecker = timeOutChecker;
		}
		
		@Override public void onLocationChanged(final Location location) {
			handler.post(new Runnable() {
				@Override public void run() {
					PositionOptions options = watchPositionQuery.options;
					if (isAcceptableLocation(location, options) &&
						isBetterLocation(location, currentBestLocation, options.maximumAge > 0 ? options.maximumAge : DEFAULT_MAX_LOCATION_AGE)) {
						currentBestLocation = location;
						watchPositionQuery.successCallback.on(locationToPosition(location));
					}
					
					// Check timeout
					if (options.timeout > 0) {
						handler.removeCallbacks(timeOutChecker);
						handler.postDelayed(timeOutChecker, options.timeout);
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
	private class CurrentPositionTimeOutChecker implements Runnable {
		
		private final CurrentPositionQuery currentPositionQuery;
		private final CurrentPositionLocationListener locationListener;

		private CurrentPositionTimeOutChecker(
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
	 * Check {@link Geolocation#watchPosition} timeout.
	 */
	private class WatchPositionTimeOutChecker implements Runnable {
		
		private final PositionErrorCallback errorCallback;

		private WatchPositionTimeOutChecker(PositionErrorCallback errorCallback) {
			this.errorCallback = errorCallback;
		}

		@Override public void run() {
			errorCallback.on(new PositionError(PositionError.TIMEOUT, TIMEOUT_MESSAGE));
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
