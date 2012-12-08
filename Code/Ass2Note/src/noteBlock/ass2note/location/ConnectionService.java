package noteBlock.ass2note.location;

import android.app.IntentService;
import android.content.Intent;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.os.IBinder;
import android.util.Log;

public class ConnectionService extends IntentService {
	public ConnectionService() {
		super("ConnectionService");
	}

	public ConnectionService(String name) {
		super(name);
	}

	@Override
	public IBinder onBind(Intent intent) {
		// TODO: Return the communication channel to the service.
		throw new UnsupportedOperationException("Not yet implemented");
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		Log.i("ConnectionService", "onHandleIntent");
		String fromActivity = intent.getStringExtra("fromActivity");
		if (fromActivity.contains("NoteEdit")) 
			sendBroadcastTo("com.example.ass2note.notepad.NoteEdit.connectionReceiver");
		else if(fromActivity.contains("LocationAlarmService"))
			sendBroadcastTo("com.example.ass2note.alarm.LocationAlarmService.LASReceiver");
	}

	private void sendBroadcastTo(String receiver){
		Intent i = new Intent(receiver);
		i.putExtra("fromCaller", "ConnectionService");
		i.putExtra("gpsEnabled", isGPSEnabled());
		i.putExtra("networkEnabled", isNetworkEnabled());
		sendBroadcast(i);
	}
	
	private boolean isGPSEnabled() {
		// This verification should be done during onStart() because the system
		// calls
		// this method when the user returns to the activity, which ensures the
		// desired
		// location provider is enabled each time the activity resumes from the
		// stopped state.
		LocationManager service = (LocationManager) getSystemService(LOCATION_SERVICE);
		boolean gpsEnabled = service.isProviderEnabled(LocationManager.GPS_PROVIDER);

		return gpsEnabled;
	}

	private boolean isNetworkEnabled() {
		boolean networkConnection = false;
		
		ConnectivityManager manager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
		
		// For 3G check
		 boolean is3g = manager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).isConnectedOrConnecting();
		 
		// For WiFi Check
		boolean isWifi = manager.getNetworkInfo(ConnectivityManager.TYPE_WIFI)
				.isConnectedOrConnecting();

		if (is3g || isWifi) networkConnection = true;
		 
		return networkConnection;
	}

}
