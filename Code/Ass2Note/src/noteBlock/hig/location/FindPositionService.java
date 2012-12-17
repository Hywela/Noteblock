package noteBlock.hig.location;

import java.util.Timer;
import java.util.TimerTask;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.util.Log;

/**
 * This is a service that finds the user's location based on either network
 * or gps.
 * @author Kristoffer Benum , and Solveig Sørheim
 */
public class FindPositionService extends Service {
	public static final String EXTRA_MESSENGER = "com.example.ass2note.location.EXTRA_MESSENGER";
	private static final int TWO_MINUTES = 1000 * 60 * 2;

	private Messenger messenger;
	private LocationManager mLocationManager;
	private Location lastKnownLocation, networkLocation, gpsLocation;
	private LocationListener networkListener, gpsListener, bestListener;
	
	private boolean networkEnabled = false, gpsEnabled = false;
	private boolean networkChecked = false, gpsChecked = false;
	private Timer timeout, gpsTimeout, networkTimeout;

	
	public FindPositionService() {
		super();
	}

	// Binds the service to an activity
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		// Acquire a reference to the system Location Manager.
		mLocationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

		Bundle extras = intent.getExtras();
		messenger = (Messenger) extras.get(EXTRA_MESSENGER);
		networkEnabled = intent.getBooleanExtra("networkEnabled", false);
		gpsEnabled = intent.getBooleanExtra("gpsEnabled", false);
		String from = intent.getStringExtra("from");

		// The caller only wish to get a rough estimate of the user's location.
		if (from.contains("GoogleMapsActivity")) findBestLocation();

		// The caller wish to get a fine estimate of the user's location:
		else if (from.contains("LocationAlarmService")) {
			if (gpsEnabled)		findGpsLocation();
			else				gpsChecked = true;
			
			if (networkEnabled)	findNetworkLocation();
			else				networkChecked = true;
		}

		return super.onStartCommand(intent, flags, startId);
	}

	/**
	 * Method for initiating a listener that finds the user's location based
	 * on criteria. It uses both network and GPS to find the location.
	 */
	private void findBestLocation() {
		bestListener = new LocationListener() {
			public void onLocationChanged(Location location) {
				timeout.cancel();
				mLocationManager.removeUpdates(this);
				sendLocationMessage(location.getLatitude() * 1000000,
						location.getLongitude() * 1000000);
			}
			
			public void onStatusChanged(String provider, int status, Bundle extras) {}
			public void onProviderEnabled(String provider) {}
			public void onProviderDisabled(String provider) {}
		};

		Criteria c = new Criteria();
		c.setAccuracy(Criteria.ACCURACY_COARSE);
		
		final String PROVIDER = mLocationManager.getBestProvider(c, true);
		mLocationManager.requestLocationUpdates(PROVIDER, 0, 0, bestListener);

		// Create a timeout that will run after 9 seconds:
		timeout = new Timer();
		timeout.schedule(new bestTimeout(), 9000);
	}

	/**
	 * Method for initiating a listener that will use the network to find
	 * the user's current location. If the location is not found within
	 * ten seconds, a timer will be called.
	 */
	public void findNetworkLocation() {
		networkListener = new LocationListener() {
			public void onLocationChanged(Location location) {
				networkTimeout.cancel();
				mLocationManager.removeUpdates(this);
				networkLocation = location;
				networkChecked = true;

				// If the GPS-listener already found the user's location.
				if (gpsChecked)	prepareMessage();
				
			}
			public void onStatusChanged(String provider, int status, Bundle extras) {}
			public void onProviderEnabled(String provider) {}
			public void onProviderDisabled(String provider) {}
		};

		mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, networkListener);

		// Create a timeout that will run after 10 seconds:
		networkTimeout = new Timer();
		networkTimeout.schedule(new networkTimeout(), 10000);
	}

	/**
	 * Method for initiating a listener that will use the gps to find
	 * the user's current location. If the location is not found within
	 * 40 seconds, a timer will be called.
	 */
	public void findGpsLocation() {
		gpsListener = new LocationListener() {
			public void onLocationChanged(Location location) {
				gpsTimeout.cancel();
				mLocationManager.removeUpdates(this);
				gpsLocation = location;
				gpsChecked = true;

				/* If the network-Listener already found the user's location */
				if (networkChecked) {
					prepareMessage();
				}
			}

			public void onStatusChanged(String provider, int status, Bundle extras) {}
			public void onProviderEnabled(String provider) {}
			public void onProviderDisabled(String provider) {}
		};

		mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, gpsListener);

		// Create a timeout that will run after 40 seconds:
		gpsTimeout = new Timer();
		gpsTimeout.schedule(new gpsTimeout(), 40000);
	}
	
	private void prepareMessage(){
		if(networkLocation!=null && gpsLocation!=null){
			boolean networkIsBest = isBetterLocation(networkLocation, gpsLocation);
			
			if (networkIsBest)	sendMessage(networkLocation);
			else				sendMessage(gpsLocation);
		}else if(networkLocation!=null) sendMessage(networkLocation);
		else 							sendMessage(gpsLocation);
	}

	private void sendMessage(Location location){
		sendLocationMessage(location.getLatitude() * 100000, 
							location.getLongitude() * 100000);
	}
	
	private void sendLocationMessage(double latitude, double longitude) {
		Bundle bundle = new Bundle();
		bundle.putDouble("LATITUDE", latitude);
		bundle.putDouble("LONGITUDE", longitude);

		Message msg = Message.obtain();
		msg.setData(bundle);
		try { messenger.send(msg); }
		catch (android.os.RemoteException e1) {
			Log.w(getClass().getName(), "Exception sending message", e1);
		}
	}

	/**
	 * Determines whether one Location reading is better than the current Location fix
	 * @param location The new Location that you want to evaluate
	 * @param currentBestLocation The current Location fix, to which you want to compare the new one
	 */
	protected boolean isBetterLocation(Location location, Location currentBestLocation) {
		if (currentBestLocation == null) {
			// A new location is always better than no location
			return true;
		}

		// Check whether the new location fix is newer or older
		long timeDelta = location.getTime() - currentBestLocation.getTime();
		boolean isSignificantlyNewer = timeDelta > TWO_MINUTES;
		boolean isSignificantlyOlder = timeDelta < -TWO_MINUTES;
		boolean isNewer = timeDelta > 0;

		// If it's been more than two minutes since the current location, use
		// the new location because the user has likely moved
		if (isSignificantlyNewer) {
			return true;
		// If the new location is more than two minutes older, it must be worse
		} else if (isSignificantlyOlder) {
			return false;
		}

		// Check whether the new location fix is more or less accurate
		int accuracyDelta = (int) (location.getAccuracy() - currentBestLocation.getAccuracy());
		boolean isLessAccurate = accuracyDelta > 0;
		boolean isMoreAccurate = accuracyDelta < 0;
		boolean isSignificantlyLessAccurate = accuracyDelta > 200;

		// Check if the old and new location are from the same provider
		boolean isFromSameProvider = isSameProvider(location.getProvider(),
				currentBestLocation.getProvider());

		// Determine location quality using a combination of timeliness and accuracy
		if (isMoreAccurate)						return true;
		else if (isNewer && !isLessAccurate)	return true;
		else if (isNewer && !isSignificantlyLessAccurate && isFromSameProvider)	return true;

		return false;
	}

	/** Checks whether two providers are the same */
	private boolean isSameProvider(String provider1, String provider2) {
		if (provider1 == null) return provider2 == null;
		
		return provider1.equals(provider2);
	}

	private boolean getLastKnownLocation() {
		if (networkEnabled)
			lastKnownLocation = mLocationManager
					.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
		else
			lastKnownLocation = mLocationManager
					.getLastKnownLocation(LocationManager.GPS_PROVIDER);

		if (lastKnownLocation != null) {
			sendLocationMessage(lastKnownLocation.getLatitude() * 1000000,
					lastKnownLocation.getLongitude() * 1000000);
			return true;
		}
		return false;
	}
	
	class networkTimeout extends TimerTask {
		@Override
		public void run() {
			// The user's location was not found by networkListener:
			mLocationManager.removeUpdates(networkListener);
			networkChecked = true;

			// If the gpsListener could not find the user's location either
			if (gpsChecked && gpsLocation == null) returnEmptyMessage();

			// If the gpsListener found the user's location:
			else if (gpsChecked && gpsLocation != null) prepareMessage();
		}
	}
	
	class gpsTimeout extends TimerTask {
		@Override
		public void run() {
			// If the gpsListener couldn't find the user's location:
			mLocationManager.removeUpdates(gpsListener);
			gpsChecked = true;

			// If the networkListener could not find the user's location either.
			if (networkChecked && networkLocation == null)		returnEmptyMessage();
			
			// If the networkListener found the user's location:
			else if (networkChecked && networkLocation != null) prepareMessage();		
		}
	}

	class bestTimeout extends TimerTask {
		@Override
		public void run() {
			mLocationManager.removeUpdates(bestListener);

			// If no last-known-location was found either, return null.
			if (!getLastKnownLocation()) returnEmptyMessage();
		}
	}


	public void returnEmptyMessage() {
		Message msg = Message.obtain();
		try { messenger.send(msg); } 
		catch (android.os.RemoteException e1) {
			Log.w(getClass().getName(), "Exception sending message", e1);
		}
	}

}
