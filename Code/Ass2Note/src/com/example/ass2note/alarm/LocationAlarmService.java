package com.example.ass2note.alarm;

import java.util.ArrayList;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.support.v4.app.NotificationCompat;
import android.util.Log;


import com.example.ass2note.location.FindPositionService;
import com.example.ass2note.notepad.Notepad;
import com.example.ass2note.notepad.NotesDbAdapter;

public class LocationAlarmService extends Service {
	private static final int NOTIFICATION_DISTANCE = 100;
	private Intent positionServiceIntent; // Intent for starting FindPositionService
	private double userLatitude; // The user's latitude position.
	private double userLongitude; // The user's longitude position.
	private ArrayList<String> noteLatitudeList; // All latitudes stored in the DB.
	private ArrayList<String> noteLongitudeList; // All longitudes stored in the DB.
	private ArrayList<String> noteKeyList;
	private ArrayList<String> titleList;
	private ArrayList<String> enablePositionList;
	private NotesDbAdapter mDbHelper;
	

	/**
	 * Constructor. Called when the service starts.
	 */
	public LocationAlarmService() {
		super();
		Log.i("LocationAlarmService", "created service");
	}

	@Override
	public void onCreate() {
		super.onCreate();
		mDbHelper = new NotesDbAdapter(this);
		mDbHelper.open();

		// Create new lists for carrying the locations:
		noteLatitudeList = new ArrayList();
		noteLongitudeList = new ArrayList();
		noteKeyList = new ArrayList();
		titleList = new ArrayList();
		enablePositionList = new ArrayList();

		// Put data inside the lists.
		fetchAllLocations();
		
		// Start service to find the user's current position.
		startFindPositionService();
	}

	/**
	 * This onStart saves the latitudes and longitudes to their respective
	 * ArrayLists, and calls FindPositionservice for fetching the User's current
	 * position.
	 * 
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
	public IBinder onBind(Intent intent) {
		return null;
	}

	private void startFindPositionService() {
		// Call FindPositionService for fetching the user's current position:
		positionServiceIntent = new Intent(LocationAlarmService.this, FindPositionService.class);
		positionServiceIntent.putExtra(FindPositionService.EXTRA_MESSENGER,	new Messenger(handy));
		startService(positionServiceIntent);
	}

	/**
	 * Handler for receiving latitude and longitude position of the user from
	 * FindPositionService. After fetching the position, compare the user's
	 * position with all the notes position, and stop this service.
	 */
	private Handler handy = new Handler() {
		public void handleMessage(Message message) {
			Log.i("LocationAlarmService", "Handler handy called");
			Bundle data = message.getData();

			// If information was successfully sent:
			if (data != null) {
				userLatitude = data.getDouble("LATITUDE") / 1E6;
				userLongitude = data.getDouble("LONGITUDE") / 1E6;
				stopService(positionServiceIntent); // Stop findPositionService.
			}

			comparePositions(); // Compare the user's position with all notes.
			stopMe(); // Stop this service.
		}
	};

	private void stopMe() {
		Log.i("LocationAlarmService", "stopService");
		mDbHelper.close();
		stopSelf();
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
					notifyDatabase(number);
					notifyUser(number);
				} else
					Log.i("LocationAlarmService", "user is still too far away");
			} else
				Log.i("LocationAlarmService",
						"comparePositions, found invalid value");
		} // End for
	}

	public void notifyDatabase(int number) {
		Log.i("LocationAlarmService", "trying to notify database");
		Long key = Long.parseLong(noteKeyList.get(number).toString());
		disableNotificationDb(key);
		fetchAllLocations();
		
		if(!doesValidLocationExist());{
			notifyAlarmManager();
		}
	}
	
	private void notifyAlarmManager(){
		Intent i = new Intent(LocationAlarmService.this, AlarmManagerService.class);
		i.putExtra("COMMAND", "Stop Alarm");
		startService(i);
	}

	/**
	 * Method for notifying the user with (lights, vibration, sound and) a
	 * note on the panel. 
	 */
	public void notifyUser(int number) {
		NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(
				this)
				.setSmallIcon(R.drawable.ic_launcher)
				.setContentTitle("NoteBlock Reminder!")
				.setContentText("You have been reminded of: "
						+ titleList.get(number).toString());

		// Remove the notification on the panel.
		mBuilder.setAutoCancel(true);
		// mBuilder.setOnlyAlertOnce(true);

		// Creates an explicit intent for an Activity in your app
		// Intent resultIntent = new Intent(this, Notepad.class);

		// The stack builder object will contain an artificial back stack for
		// the
		// started Activity.
		// This ensures that navigating backward from the Activity leads out of
		// your application to the Home screen.
		/*
		 * TaskStackBuilder stackBuilder = TaskStackBuilder.create(this); //
		 * Adds the back stack for the Intent (but not the Intent itself)
		 * stackBuilder.addParentStack(NoteEdit.class); // Adds the Intent that
		 * starts the Activity to the top of the stack
		 * stackBuilder.addNextIntent(resultIntent); PendingIntent
		 * resultPendingIntent = stackBuilder.getPendingIntent( 0,
		 * PendingIntent.FLAG_UPDATE_CURRENT );
		 * mBuilder.setContentIntent(resultPendingIntent);
		 */

		Intent notifyIntent = new Intent(this, Notepad.class);
		notifyIntent.putExtra("notificationSuccess", noteKeyList.get(number) //TODO: Make sure Notepad get this key
				.toString());
		notifyIntent.setFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);

		PendingIntent intent = PendingIntent.getActivity(this, 0, notifyIntent,
				0);
		mBuilder.setContentIntent(intent);

		NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		mNotificationManager.notify(
				Integer.parseInt(noteKeyList.get(number).toString()),
				mBuilder.build());
	}

	private void disableNotificationDb(Long key) {
		Log.i("LocationAlarmService","disableNotificationDb, setting note to false");
		mDbHelper.updatePositionNotification(key, "false");
	}

	// TODO: Make this better
	public boolean doesValidLocationExist() {
		Log.i("LocationAlarmService", "isLocationEmpty");
		//fetchAllLocations();
		if (noteLatitudeList.isEmpty() || noteLongitudeList.isEmpty())
			return false;
		else {
			for (int i = 0; i < noteLatitudeList.size(); i++) {
				if (!noteLatitudeList.get(i).toString().contains("lat")
						&& !noteLongitudeList.get(i).toString()
								.contains("long")
						&& enablePositionList.get(i).toString()
								.contains("true")) {
					return true;
				}
			}
		}
		return false;
	}

	public void fetchAllLocations() {
		Log.i("LocationAlarmService", "fetchAllLocations");

		// Fetch the notes from the Database.
		Cursor allNotes = mDbHelper.fetchAllNotes();

		// If there are notes in the database:
		if (allNotes != null) {
			while (allNotes.moveToNext()) {
				// Fetch location or default value:
				noteLatitudeList.add(allNotes.getString(5));
				noteLongitudeList.add(allNotes.getString(6));
				noteKeyList.add(allNotes.getString(0));
				titleList.add(allNotes.getString(1));
				enablePositionList.add(allNotes.getString(7));
			}
		}
	}

}
