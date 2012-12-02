package com.example.ass2note.alarm;

import java.util.ArrayList;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.database.Cursor;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.example.ass2note.R;
import com.example.ass2note.location.ConnectionService;
import com.example.ass2note.location.FindPositionService;
import com.example.ass2note.notepad.Notepad;
import com.example.ass2note.notepad.NotesDbAdapter;

public class LocationAlarmService extends Service {
	private static final int NOTIFICATION_DISTANCE = 100;	// MAX distance in meters to when the user will be notified.
	private Intent positionServiceIntent; 					// Intent for starting FindPositionService
	private double userLatitude; 						// The user's latitude position.
	private double userLongitude; 						// The user's longitude position.
	private ArrayList<String> noteLatitudeList; 	// All latitudes stored in the DB.
	private ArrayList<String> noteLongitudeList; 	// All longitudes stored in the DB.
	private ArrayList<String> noteKeyList;			// All ID's of the notes stored in the DB.
	private ArrayList<String> titleList;			// All titles of the notes stored in the DB.
	private ArrayList<String> enablePositionList;	// A list of validations for checking if the notes can be notified.
	private NotesDbAdapter mDbHelper;			// The database-class.
	private IntentFilter intentFilter;
	private Context context;
	private Resources res;
	

	/**
	 * Constructor. Called when the service starts.
	 */
	public LocationAlarmService() {
		super();
		Log.i("LocationAlarmService", "created service");
	}

	/**
	 * When the service is created, it opens a database connection, fetches
	 * the necessary data from the DB and calls FindPositionService.
	 */
	@Override
	public void onCreate() {
		super.onCreate();

		context = getApplicationContext();
		res = context.getResources();
		
		intentFilter = new IntentFilter("com.example.ass2note.alarm.LocationAlarmService.LASReceiver");
		registerReceiver(LASReceiver, intentFilter);
		
		mDbHelper = new NotesDbAdapter(this);	// Create a new instance of DB.
		mDbHelper.open();						// Open the DB.

		// Create new lists for carrying the locations:
		noteLatitudeList = new ArrayList<String>();
		noteLongitudeList = new ArrayList<String>();
		noteKeyList = new ArrayList<String>();
		titleList = new ArrayList<String>();
		enablePositionList = new ArrayList<String>();

		// Put data inside the lists.
		fetchAllLocations();
		
		
		if(doesValidLocationExist()) connectionEnabled();
		else stopMe();
	}

	/**
	 * This onStart is for now not implemented.
	 * @param intent
	 * @param flags
	 * @param startId
	 */
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.i("LocationAlarmService", "onstart called");
		return super.onStartCommand(intent, flags, startId);
	}
	
	@Override
	public void onDestroy() {
		unregisterReceiver(LASReceiver);
		super.onDestroy();
	}

	/**
	 * onBind will not bind to any Activity, so it returns null.
	 */
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	/**
	 * Method for creating an intent to start FindPositionService. A Message is
	 * sent to FindPositionService so is can return data to this service's 
	 * handler, which is called handy. The data which will be returned is the
	 * user's current position or null.
	 */
	private void startFindPositionService(boolean gpsEnabled, boolean networkEnabled) {
		// Call FindPositionService for fetching the user's current position:
		positionServiceIntent = new Intent(LocationAlarmService.this, FindPositionService.class);
		positionServiceIntent.putExtra(FindPositionService.EXTRA_MESSENGER,	new Messenger(handy));
		positionServiceIntent.putExtra("gpsEnabled", gpsEnabled);
		positionServiceIntent.putExtra("networkEnabled", networkEnabled);
		startService(positionServiceIntent);
	}

	/**
	 * Handler for receiving the current latitude and longitude position of the
	 * user from FindPositionService. After fetching the position, compare the 
	 * user's position with all the notes position, and stop this service.
	 */
	private Handler handy = new Handler() {
		public void handleMessage(Message message) {
			Log.i("LocationAlarmService", "Handler handy called");
			Bundle data = message.getData();

			// If new location is sent:
			if (data != null) {
				userLatitude = data.getDouble("LATITUDE") / 1E6;
				userLongitude = data.getDouble("LONGITUDE") / 1E6;
				stopService(positionServiceIntent); // Stop findPositionService.
			}

			comparePositions(); // Compare the user's position with all notes.
			stopMe(); // Stop this service.
		}
	};

	/**
	 * Method for closing the database and killing this LocationAlarmService.
	 */
	private void stopMe() {
		Log.i("LocationAlarmService", "stopService");
		
		// If no more valid notes exist, tell the alarm to stop.
		if(!doesValidLocationExist()) stopAlarmManager();
		
		mDbHelper.close();	// Close the database connection.
		stopSelf();			// Stop this service.
	}

	/**
	 * A method for comparing all the note's positions with the user's current
	 * position. If the distance between the two positions is less than 100
	 * meters, notify the user.
	 */
	private void comparePositions() {
		Log.i("LocationALarmService", "Started comparePositions");

		// Loop through all the notes:
		for (int number = 0; number < noteLatitudeList.size(); number++) {

			// If the note is not invalid (it contains a valid double):
			if (!noteLatitudeList.get(number).toString().contains("lat")
					&& enablePositionList.get(number).toString()
							.contains("true")) {

				// Fetch and transform the doubles to the proper format:
				double noteLati = Double.parseDouble(noteLatitudeList.get(
						number).toString()) / 1E6;
				double noteLongi = Double.parseDouble(noteLongitudeList.get(
						number).toString()) / 1E6;

				/*
				 * Find the distance between the note and the user's location,
				 * and store the result in results:
				 */
				float results[] = new float[2];
				Location.distanceBetween(userLatitude, userLongitude, noteLati,
						noteLongi, results);
				Log.i("LocationAlarmService", "Distance in meters: "
						+ results[0]);

				// If the distance is less than 100 meters, alert user:
				if (results[0] <= NOTIFICATION_DISTANCE) {
					System.out.println("NOOOOTIFICATION ALERT!!");
					notifyDatabase(number);			// Update the DB.
					notifyUser(number);				// Notify the user.
				} else
					Log.i("LocationAlarmService", "user is still too far away");
			} else
				Log.i("LocationAlarmService",
						"comparePositions, found invalid value");
		} // End for
	}

	/**
	 * Method for 
	 * @param number
	 */
	public void notifyDatabase(int number) {
		Log.i("LocationAlarmService", "trying to notify database");
		
		// Get the ID of the note that will be changed.
		Long key = Long.parseLong(noteKeyList.get(number).toString());
		
		// Set the value of "positionNotification" to false.
		mDbHelper.updatePositionNotification(key, "false");
		
		// Update the current lists:
		fetchAllLocations();
		
		// TODO. find out why this is commented out:
		/*// If no more valid notes exist, tell the alarm to stop.
		if(!doesValidLocationExist())
			stopAlarmManager();*/
	}
	
	/**
	 * Method for starting AlarmManagerService so it will stop the alarm 
	 * associated with location.
	 */
	private void stopAlarmManager(){
		Log.i("LocationAlarmService", "stopAlarmManager stopping alarm");
		Intent i = new Intent(LocationAlarmService.this, AlarmManagerService.class);
		i.putExtra("COMMAND", "Stop Alarm");
		i.putExtra("alarmType", "position");
		startService(i);
	}

	/**
	 * Method for notifying the user with (lights, vibration, sound and) a
	 * note on the panel. TODO: Add lights, vibration, sound?
	 */
	public void notifyUser(int number) {
		NotificationCompat.Builder mBuilder = 
				new NotificationCompat.Builder(this)
				.setSmallIcon(R.drawable.ic_launcher)
				.setContentTitle(getString(R.string.notification_title))
				.setContentText(getString(R.string.notification_content)
						+ titleList.get(number).toString());
		
		/* Set the notification on the panel to remove itself when the user 
		   presses it.*/
		mBuilder.setAutoCancel(true);
		Intent notifyIntent = new Intent(this, Notepad.class);
		notifyIntent.setAction(String.valueOf(number));
		notifyIntent.putExtra("notificationSuccess", noteKeyList.get(number).toString());
		PendingIntent intent = PendingIntent.getActivity(this, 0, notifyIntent, 0);
		mBuilder.setContentIntent(intent);

		NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		mNotificationManager.notify(Integer.parseInt(noteKeyList.get(number).toString()),mBuilder.build());
	}

	/**
	 * Method for deciding if a valid location exist in the database. By valid,
	 * it means that the latitude and longitude values contain a double number,
	 * and that the note's respective positionNotification value is "true".
	 * @return true when a valid location does exist, false otherwise.
	 */
	public boolean doesValidLocationExist() {
		Log.i("LocationAlarmService", "doesValidLocationExist");
		
		if(enablePositionList.contains("true")) {
			Log.i("LocationAlarmService", "contained true");
			return true;
		}
		return false;
	}

	/**
	 * Method for fetching all notes from the database, and put the necessary
	 * information inside their own lists for other methods to use.
	 */
	public void fetchAllLocations() {
		Log.i("LocationAlarmService", "fetchAllLocations");
		
		// Empty the lists so they contain only the most recent values:
		if(!noteKeyList.isEmpty()){
			noteLatitudeList.clear();
			noteLongitudeList.clear();
			noteKeyList.clear();
			titleList.clear();
			enablePositionList.clear();
		}

		// Fetch the notes from the Database.
		Cursor allNotes = mDbHelper.fetchAllNotes();
		
		// If there are notes in the database:
		if (allNotes != null) {
			while (allNotes.moveToNext()) {
				// Fetch location or default value:
				noteLatitudeList.add(allNotes.getString(5));
				noteLongitudeList.add(allNotes.getString(6));
				noteKeyList.add(allNotes.getString(0));
				titleList.add(allNotes.getString(2));
				enablePositionList.add(allNotes.getString(7));
			}
		}
	}
	
	private void connectionEnabled(){
		Log.i("LAS", "connectionEnabled");
		Intent intent = new Intent(LocationAlarmService.this, ConnectionService.class);
		intent.putExtra("fromActivity", "LocationAlarmService");
		startService(intent);
	}
	
	BroadcastReceiver LASReceiver = new BroadcastReceiver(){
		@Override
		public void onReceive(Context context, Intent intent) {
			Log.i("LocationAlarmService", "receiver called");
			boolean gpsEnabled = intent.getBooleanExtra("gpsEnabled", false);
			boolean networkEnabled = intent.getBooleanExtra("networkEnabled", false);
			
			// Start service to find the user's current position.
			startFindPositionService(gpsEnabled, networkEnabled);
		}
		
	};

}
