package noteBlock.hig.location;

import android.app.IntentService;
import android.content.Intent;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.os.IBinder;

/**
 * This is a service that finds out whether the phone has enabled the GPS and
 * if the phone has network connection.
 * @author Kristoffer Benum , and Solveig Sørheim
 */
public class ConnectionService extends IntentService {
	private static final String NOTEEDIT_RECEIVER = "com.example.ass2note.notepad.NoteEdit.connectionReceiver";
	private static final String LOCATION_SERVICE_RECEIVER = "com.example.ass2note.alarm.LocationAlarmService.LASReceiver";
	
	public ConnectionService() {
		super("ConnectionService");
	}

	public ConnectionService(String name) {
		super(name);
	}

	@Override
	public IBinder onBind(Intent intent) {
		throw new UnsupportedOperationException("Not being implemented");
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		String fromActivity = intent.getStringExtra("fromActivity");
		
		if (fromActivity.contains("NoteEdit")) 					sendBroadcastTo(NOTEEDIT_RECEIVER);
		else if(fromActivity.contains("LocationAlarmService")) 	sendBroadcastTo(LOCATION_SERVICE_RECEIVER);
	}

	/**
	 * This method sends the status of the gps and the network to a specified
	 * receiver. The status is whether they are enabled or not.
	 * @param receiver
	 */
	private void sendBroadcastTo(String receiver){
		Intent i = new Intent(receiver);
		i.putExtra("fromCaller", "ConnectionService");
		i.putExtra("gpsEnabled", isGPSEnabled());
		i.putExtra("networkEnabled", isNetworkEnabled());
		sendBroadcast(i);
	}
	
	/**
	 * Method for checking if the gps is enabled or not.
	 * @return a boolean value of whether the gps is enabled or not. If it is
	 * enabled, true is returned. False otherwise.
	 */
	private boolean isGPSEnabled() {
		LocationManager service = (LocationManager) getSystemService(LOCATION_SERVICE);
		boolean gpsEnabled = service.isProviderEnabled(LocationManager.GPS_PROVIDER);

		return gpsEnabled;
	}

	/**
	 * Method for checking if the network is enabled or not.
	 * @return a boolean value of whether the wifi or 3g is enabled or not. If
	 * either 3g or WiFi is enabled, it returns true. False otherwise.
	 */
	private boolean isNetworkEnabled() {
		boolean networkConnection = false;
		
		ConnectivityManager manager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
		
		// For 3G check
		boolean is3g = manager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).isConnectedOrConnecting();
		 
		// For WiFi Check
		boolean isWifi = manager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).isConnectedOrConnecting();

		if (is3g || isWifi) networkConnection = true;
		 
		return networkConnection;
	}

}
