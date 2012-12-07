package com.example.ass2note.location;

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

public class FindPositionService extends Service {
	public static final String EXTRA_MESSENGER = "com.example.ass2note.location.EXTRA_MESSENGER";
	private static final int TWO_MINUTES = 1000 * 60 * 2;
	private LocationManager mLocationManager;
	private Bundle extras;
	private Messenger messenger;
	private LocationListener networkListener, gpsListener, bestListener;
	private Location mobileLocation;
	private boolean networkEnabled=false, gpsEnabled=false;
	private Timer timeout;
	private String from = "";
	

	public FindPositionService() {
		super();
	}

	// Binds the service to an activity
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		Log.i("FindPositionService", "oncreate");
	}

	@Override
	public void onDestroy() {
		Log.i("FindPositionService", "destroyed service");
//		boolean networkProviderEnabled = mLocationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
/*		boolean gpsProviderEnabled = mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);*/
//		if (networkProviderEnabled)	mLocationManager.removeUpdates(networkListener);
	/*	if (gpsProviderEnabled)
			mLocationManager.removeUpdates(gpsListener);*/

		super.onDestroy();
		// Code to execute when the service is shutting down
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		// Code to execute when the service is starting up
		Log.i("FindPositionService", "onstart");

		// Acquire a reference to the system Location Manager.
		mLocationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
		
		extras = intent.getExtras();
		messenger = (Messenger) extras.get(EXTRA_MESSENGER);
		networkEnabled = intent.getBooleanExtra("networkEnabled", false);
		gpsEnabled = intent.getBooleanExtra("gpsEnabled", false);
		from = intent.getStringExtra("from");

//			getLastKnownLocation(extras.getBoolean("networkEnabled"), extras.getBoolean("gpsEnabled"));
		/*
		 * if(extras.containsKey("gpsEnabled")){ boolean gpsEnabled =
		 * extras.getBoolean("gpsEnabled"); boolean networkEnabled =
		 * extras.getBoolean("networkEnabled");
		 */
		// TODO: Make so that only googlemapsactivity calls this sequence of codes.
//		if(networkEnabled) findNetworkLocation();
//		else findGpsLocation();
		
		
//		 findGpsLocation();
		
		// The caller only wish to get a rough estimate of the user's location.
		if(from.contains("GoogleMapsActivity"))	findBestLocation();
		
		// The caller wish to get a fine estimate of the user's location:
		else if(from.contains("LocationAlarmService")){
			if(gpsEnabled) findGpsLocation();
		}
		
		return super.onStartCommand(intent, flags, startId);
	}

	
	 public void findNetworkLocation() {
		Log.i("FindPositionService", "findNetworkLocation");

		// Define a listener that responds to location updates.
		// It is called when a new location is found by the network location
		// provider.
		networkListener = new LocationListener() {
			public void onLocationChanged(Location location) {
				Log.i("FindPositionService","networkListener registered position on onLocationChanged");
				timeout.cancel();
				mLocationManager.removeUpdates(this);
				sendLocationMessage(location.getLatitude() * 1000000,
						location.getLongitude() * 1000000);
			}

			public void onStatusChanged(String provider, int status,
					Bundle extras) {
				System.out.println("network status changed");
			}

			public void onProviderEnabled(String provider) {
				System.out.println("network provider enabled");
			}

			public void onProviderDisabled(String provider) {
				System.out.println("network provider disabled");
			}
		};

		mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, networkListener);
		
		timeout=new Timer();
		timeout.schedule(new GetLastLocation(), 10000);
	}

	public void findGpsLocation() {
		Log.i("FindPositionService", "findGPSLocation");

		// Define a listener that responds to location updates.
		// It is called when a new location is found by the network location
		// provider.
		gpsListener = new LocationListener() {
			public void onLocationChanged(Location location) {
				Log.i("FindPositionService", "GpsListener registered position on onLocationChanged");
				timeout.cancel();
				mLocationManager.removeUpdates(this);
				sendLocationMessage(location.getLatitude() * 1000000,
						location.getLongitude() * 1000000);
			}

			public void onStatusChanged(String provider, int status,
					Bundle extras) {
				System.out.println("gps status changed");
			}

			public void onProviderEnabled(String provider) {
				System.out.println("gps provider enabled");
			}

			public void onProviderDisabled(String provider) {
				System.out.println("gps provider disabled");
			}
		};

		// Register the listener with the Location Manager to receive location
		// updates
		mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, gpsListener);

		timeout=new Timer();
		timeout.schedule(new GetLastLocation(), 30000);
	}

	private void findBestLocation() {
		bestListener = new LocationListener() {
			public void onLocationChanged(Location location) {
				Log.i("FindPositionService", "LocationListener registered position on onLocationChanged");
				timeout.cancel();
				mLocationManager.removeUpdates(this);
				sendLocationMessage(location.getLatitude() * 1000000,
						location.getLongitude() * 1000000);
			}

			public void onStatusChanged(String provider, int status,
					Bundle extras) {
				System.out.println("best status changed");
			}

			public void onProviderEnabled(String provider) {
				System.out.println("best provider enabled");
			}

			public void onProviderDisabled(String provider) {
				System.out.println("best provider disabled");
			}
		};

		Criteria c = new Criteria();
		c.setAccuracy(Criteria.ACCURACY_COARSE); // Or whatever criteria you
													// want
		final String PROVIDER = mLocationManager.getBestProvider(c, true);
		mLocationManager.requestLocationUpdates(PROVIDER, 0, 0, bestListener);
		
		/* 
		 * Create a timeout that will run after 9 seconds, so the user don't 
		 * have to wait 
		 */
		timeout=new Timer();
		timeout.schedule(new GetLastLocation(), 9000);
	}

	
	private void sendLocationMessage(double latitude, double longitude) {
		Bundle bundle = new Bundle();
		bundle.putDouble("LATITUDE", latitude);
		bundle.putDouble("LONGITUDE", longitude);

		Message msg = Message.obtain();
		msg.setData(bundle);
		try {
			messenger.send(msg);
		} catch (android.os.RemoteException e1) {
			Log.w(getClass().getName(), "Exception sending message", e1);
		}
	}

	/**
	 * Determines whether one Location reading is better than the current
	 * Location fix
	 * 
	 * @param location
	 *            The new Location that you want to evaluate
	 * @param currentBestLocation
	 *            The current Location fix, to which you want to compare the new
	 *            one
	 */
	protected boolean isBetterLocation(Location location,
			Location currentBestLocation) {
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
		// the new location
		// because the user has likely moved
		if (isSignificantlyNewer) {
			return true;
			// If the new location is more than two minutes older, it must be
			// worse
		} else if (isSignificantlyOlder) {
			return false;
		}

		// Check whether the new location fix is more or less accurate
		int accuracyDelta = (int) (location.getAccuracy() - currentBestLocation
				.getAccuracy());
		boolean isLessAccurate = accuracyDelta > 0;
		boolean isMoreAccurate = accuracyDelta < 0;
		boolean isSignificantlyLessAccurate = accuracyDelta > 200;

		// Check if the old and new location are from the same provider
		boolean isFromSameProvider = isSameProvider(location.getProvider(),
				currentBestLocation.getProvider());

		// Determine location quality using a combination of timeliness and
		// accuracy
		if (isMoreAccurate)
			return true;
		else if (isNewer && !isLessAccurate)
			return true;
		else if (isNewer && !isSignificantlyLessAccurate && isFromSameProvider)
			return true;

		return false;
	}

	/** Checks whether two providers are the same */
	private boolean isSameProvider(String provider1, String provider2) {
		if (provider1 == null) {
			return provider2 == null;
		}
		return provider1.equals(provider2);
	}


	private boolean getLastKnownLocation(){
		if(networkEnabled)
			mobileLocation = mLocationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
		else
			mobileLocation = mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
		
		if(mobileLocation!=null){
			sendLocationMessage(mobileLocation.getLatitude() * 1000000,	mobileLocation.getLongitude() * 1000000);
			return true;
		}
		return false;
	}
	
	class GetLastLocation extends TimerTask {
        @Override
        public void run() {
        	Log.i("FindPositionService", "GetLastLocation running");
        	
        	if(bestListener != null) mLocationManager.removeUpdates(bestListener);
        	
        	if(networkListener!=null) mLocationManager.removeUpdates(networkListener);
        	else if(gpsListener!=null) mLocationManager.removeUpdates(gpsListener);
        	
        	// If no last-known-location was found either, return null.
        	if(!getLastKnownLocation()){
        		Message msg = Message.obtain();
        		try { messenger.send(msg);
        		} catch (android.os.RemoteException e1) {
        			Log.w(getClass().getName(), "Exception sending message", e1);
        		}
        	}
             
        }
    }
	
	
}
