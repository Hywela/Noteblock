package com.example.ass2note.location;

import android.app.AlertDialog;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.provider.Settings;
import android.util.Log;


/*
 * Adjusting the model to save battery and data exchange
 *  As you test your application, you might find that your model for providing 
 *  good location and good performance needs some adjustment. Here are some things you might change to find a good balance between the two.
 * Reduce the size of the window
 * A smaller window in which you listen for location updates means less 
 *  interaction with GPS and network location services, thus, preserving 
 *  battery life. But it also allows for fewer locations from which to choose
 *  a best estimate.
 * Set the location providers to return updates less frequently
 *  Reducing the rate at which new updates appear during the window can also 
 *   improve battery efficiency, but at the cost of accuracy. The value of the
 *   trade-off depends on how your application is used. You can reduce the rate of updates by increasing the parameters in requestLocationUpdates() that specify the interval time and minimum distance change.

Restrict a set of providers

Depending on the environment where your application is used or the desired level of accuracy, you might choose to use only the Network Location Provider or only GPS, instead of both. Interacting with only one of the services reduces battery usage at a potential cost of accuracy.*/


public class FindPositionService extends Service {
	public static final String EXTRA_MESSENGER="com.example.ass2note.location.EXTRA_MESSENGER";
	private LocationManager mLocationManager;
	private Bundle extras;
	private Messenger messenger;
	private LocationListener networkListener;
    
	
	public FindPositionService(){
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
        // Code to execute when the service is first created
    }
    
    @Override
    public void onDestroy(){
    	Log.i("FindPositionService","destroyed service");
    	boolean providerEnabled = mLocationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    	if(providerEnabled)  mLocationManager.removeUpdates(networkListener);
    		
    	super.onDestroy();
    	// Code to execute when the service is shutting down
    }
    
    @Override
    public int onStartCommand(Intent intent, int flags, int startId){
    	// Code to execute when the service is starting up 

    	extras = intent.getExtras();
        messenger=(Messenger)extras.get(EXTRA_MESSENGER);
        
        if(extras.containsKey("gpsEnabled")){
        	boolean gpsEnabled = extras.getBoolean("gpsEnabled");
        	boolean networkEnabled = extras.getBoolean("networkEnabled");
        	
       // 	if(networkEnabled)
        }
        
    	findNetworkLocation();
    	return super.onStartCommand(intent, flags, startId);
    }
	
	public void findNetworkLocation(){
		// Acquire a reference to the system Location Manager
		mLocationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
		
		// Define a listener that responds to location updates.
    	// It is called when a new location is found by the network location provider.
		networkListener = new LocationListener() {
		    public void onLocationChanged(Location location) {
		    	Log.i("FindPositionService", "LocationListener registered position on onLocationChanged");
		    	sendLocationMessage(location.getLatitude() * 1000000, location.getLongitude() * 1000000);
				
				mLocationManager.removeUpdates(this);
		    }

		    public void onStatusChanged(String provider, int status, Bundle extras) {System.out.println("status changed");}
		    public void onProviderEnabled(String provider) { System.out.println(" provider enabled");}
		    public void onProviderDisabled(String provider) {System.out.println("provider disabled");}
		  };

		// Register the listener with the Location Manager to receive location updates
		mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, networkListener);
		  
	//	isGPSEnabled();
	//	mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);	
	}
	
	private void sendLocationMessage(double latitude, double longitude){
		Log.i("FindPositionService", "LocationListener registered position on onLocationChanged");
		
    	Bundle bundle = new Bundle();
    	bundle.putDouble("LATITUDE", latitude);
    	bundle.putDouble("LONGITUDE", longitude);
        
    	Message msg=Message.obtain();
    	msg.setData(bundle);
    	try {
    		messenger.send(msg);
        }
        catch (android.os.RemoteException e1) {
        	Log.w(getClass().getName(), "Exception sending message", e1);
        }
	}
	
	private static final int TWO_MINUTES = 1000 * 60 * 2;

	/** Determines whether one Location reading is better than the current Location fix
	  * @param location  The new Location that you want to evaluate
	  * @param currentBestLocation  The current Location fix, to which you want to compare the new one
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

	    // If it's been more than two minutes since the current location, use the new location
	    // because the user has likely moved
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
	    if (isMoreAccurate) return true;
	    else if (isNewer && !isLessAccurate) return true;
	    else if (isNewer && !isSignificantlyLessAccurate 
	    		&& isFromSameProvider) return true;
	    
	    return false;
	}

	/** Checks whether two providers are the same */
	private boolean isSameProvider(String provider1, String provider2) {
	    if (provider1 == null) {
	      return provider2 == null;
	    }
	    return provider1.equals(provider2);
	}
	
	
}
