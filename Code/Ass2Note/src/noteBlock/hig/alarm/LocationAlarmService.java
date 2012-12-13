package noteBlock.hig.alarm;

import java.util.ArrayList;

import noteBlock.hig.R;
import noteBlock.hig.location.ConnectionService;
import noteBlock.hig.location.FindPositionService;
import noteBlock.hig.notepad.Notepad;
import noteBlock.hig.notepad.NotesDbAdapter;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.location.Location;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

/**
 * This is a service that compares the user's location with all the notes's
 * location, and if the distance is less than 100 meters, the user will
 * be notified.
 * @author Kristoffer Benum , and Solveig Sørheim
 */
public class LocationAlarmService extends Service {
	// MAX distance in meters to when the user will be notified.
	private static final int NOTIFICATION_DISTANCE = 100;

	private NotesDbAdapter mDbHelper; 		// The database-class.
	private Intent positionServiceIntent; 	// Intent for FindPositionService.
	private double userLatitude; 			// The user's latitude position.
	private double userLongitude; 			// The user's longitude position.

	// A list of validations for checking if the notes can be notified.
	private ArrayList<String> enablePositionList;
	private ArrayList<String> noteLatitudeList; // All latitudes.
	private ArrayList<String> noteLongitudeList; // All longitudes.
	private ArrayList<String> noteKeyList; // All ID's of the notes.
	private ArrayList<String> titleList; // All titles of the notes.

	public LocationAlarmService() {
		super();
//		Log.i("LocationAlarmService", "created service");
	}

	/**
	 * When the service is created, it opens a database connection, fetches the
	 * necessary data from the DB and calls FindPositionService.
	 */
	@Override
	public void onCreate() {
		super.onCreate();
//		Log.i("LocationAlarmService", "oncreate called");

		IntentFilter intentFilter = new IntentFilter(
				"com.example.ass2note.alarm.LocationAlarmService.LASReceiver");
		registerReceiver(LASReceiver, intentFilter);

		mDbHelper = new NotesDbAdapter(this); 	// Create a new instance of DB.
		mDbHelper.open(); 						// Open the DB.

		// Create new lists for carrying the locations:
		noteLatitudeList = new ArrayList<String>();
		noteLongitudeList = new ArrayList<String>();
		noteKeyList = new ArrayList<String>();
		titleList = new ArrayList<String>();
		enablePositionList = new ArrayList<String>();

		// Put data inside the lists.
		fetchAllLocations();

		// If a note with a positionReminder exists.
		if (doesValidLocationExist()) connectionEnabled();
		else						  stopMe();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
//		Log.i("LocationAlarmService", "onstart called");
		return super.onStartCommand(intent, flags, startId);
	}

	@Override
	public void onDestroy() {
		unregisterReceiver(LASReceiver);
		super.onDestroy();
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	/**
	 * Handler for receiving the current latitude and longitude position of the
	 * user from FindPositionService. After fetching the position, compare the
	 * user's position with all the notes position, and stop this service.
	 */
	private Handler handy = new Handler() {
		public void handleMessage(Message message) {
//			Log.i("LocationAlarmService", "Handler handy called");
			Bundle data = message.getData();

			stopService(positionServiceIntent); // Stop findPositionService.

			// If new location is sent:
			if (data != null) {
				userLatitude = data.getDouble("LATITUDE") / 1E6;
				userLongitude = data.getDouble("LONGITUDE") / 1E6;
				comparePositions();
				// If no location was found:
			} else {
				notifyUser(getString(R.string.stoppedAlarm),
						getString(R.string.noGpsNetwork), 4444);
			}

			stopMe(); // Stop this service.
		}
	};

	/**
	 * Method for closing the database and killing this LocationAlarmService.
	 */
	private void stopMe() {
//		Log.i("LocationAlarmService", "stopService");

		// If no more notes with positionReminder exist, tell the alarm to stop.
		if (!doesValidLocationExist()) stopAlarmManager();

		mDbHelper.close(); 	// Close the database connection.
		stopSelf(); 		// Stop this service.
	}

	/**
	 * A method for comparing all the note's positions with the user's current
	 * position. If the distance between the two positions is less than 100
	 * meters, notify the user.
	 */
	private void comparePositions() {
//		Log.i("LocationALarmService", "Started comparePositions");

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
//				Log.i("LocationAlarmService", "Distance in meters: " + results[0]);

				// If the distance is less than 100 meters, alert user:
				if (results[0] <= NOTIFICATION_DISTANCE) {
					notifyDatabase(number); // Update the DB.
					notifyUser(number); // Notify the user.
					notifyUserWithSound();
				} else
					Log.i("LocationAlarmService", "user is still too far away");
			} else
				Log.e("LocationAlarmService","comparePositions, found invalid value");
		} // End for
	}

	/**
	 * Method for updating a specific note in the database, and setting the
	 * positionReminder to false.
	 * 
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
	}

	/**
	 * Method for starting AlarmManagerService so it will stop the alarm
	 * associated with location.
	 */
	private void stopAlarmManager() {
//		Log.i("LocationAlarmService", "stopAlarmManager stopping alarm");
		Intent i = new Intent(LocationAlarmService.this,
				AlarmManagerService.class);
		i.putExtra("COMMAND", "Stop Alarm");
		i.putExtra("alarmType", "position");
		startService(i);
	}

	/**
	 * Method for letting the phone make a notification-sound.
	 */
	private void notifyUserWithSound() {
		try {
			Uri notification = RingtoneManager
					.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
			Ringtone r = RingtoneManager.getRingtone(getApplicationContext(),
					notification);
			r.play();
		} catch (Exception e) {
		}
	}

	/**
	 * Method for notifying the user of a specific note. 
	 * @param number is the rowId of the note.
	 */
	public void notifyUser(int number) {
		String title = getString(R.string.notification_title);
		String content = getString(R.string.position_notification_content)
				+ titleList.get(number).toString();
		int requestCode = Integer.parseInt(noteKeyList.get(number).toString());

		notifyUser(title, content, requestCode);
	}

	/**
	 * Method for deciding if a valid location exist in the database. By valid,
	 * it means that the latitude and longitude values contain a double number,
	 * and that the note's respective positionNotification value is "true".
	 * 
	 * @return true when a valid location does exist, false otherwise.
	 */
	public boolean doesValidLocationExist() {
//		Log.i("LocationAlarmService", "doesValidLocationExist");

		if (enablePositionList.contains("true")) {
//			Log.i("LocationAlarmService", "contained true");
			return true;
		}
		return false;
	}

	/**
	 * Method for fetching all notes from the database, and put the necessary
	 * information inside their own lists for other methods to use.
	 */
	public void fetchAllLocations() {
//		Log.i("LocationAlarmService", "fetchAllLocations");

		// Empty the lists so they contain only the most recent values:
		if (!noteKeyList.isEmpty()) {
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
				noteLatitudeList.add(allNotes.getString(4));
				noteLongitudeList.add(allNotes.getString(5));
				noteKeyList.add(allNotes.getString(0));
				titleList.add(allNotes.getString(1));
				enablePositionList.add(allNotes.getString(6));
			}
		}
	}

	/**
	 * Method for starting the ConnectionService so it can find out whether
	 * the user has internet or gps access.
	 */
	private void connectionEnabled() {
//		Log.i("LAS", "connectionEnabled");
		Intent intent = new Intent(LocationAlarmService.this,
				ConnectionService.class);
		intent.putExtra("fromActivity", "LocationAlarmService");
		startService(intent);
	}

	/**
	 * A broadcastReceiver that receives broadcasts from ConnectionService.
	 * It starts FindPositionService for finding the user's locationl.
	 */
	BroadcastReceiver LASReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
//			Log.i("LocationAlarmService", "receiver called");
			boolean gpsEnabled = intent.getBooleanExtra("gpsEnabled", false);
			boolean networkEnabled = intent.getBooleanExtra("networkEnabled",
					false);

			// Start service to find the user's current position.
			startFindPositionService(gpsEnabled, networkEnabled);
		}

	};

	/**
	 * Method for creating an intent to start FindPositionService. A Message is
	 * sent to FindPositionService so is can return data to this service's
	 * handler.
	 */
	private void startFindPositionService(boolean gpsEnabled,
			boolean networkEnabled) {
//		Log.i("LocationAlarmService", "StartfindpositionService");

		// Call FindPositionService for fetching the user's current position:
		positionServiceIntent = new Intent(LocationAlarmService.this,
				FindPositionService.class);
		positionServiceIntent.putExtra(FindPositionService.EXTRA_MESSENGER,
				new Messenger(handy));
		positionServiceIntent.putExtra("gpsEnabled", gpsEnabled);
		positionServiceIntent.putExtra("networkEnabled", networkEnabled);
		positionServiceIntent.putExtra("from", "LocationAlarmService");
		startService(positionServiceIntent);
	}

	/**
	 * Method for displaying a notification on the phone's panel. It displays
	 * the title and content of the caller's choice.
	 * @param title
	 * @param content
	 * @param requestCode
	 */
	private void notifyUser(String title, String content, int requestCode) {
		NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(
				LocationAlarmService.this).setSmallIcon(R.drawable.ic_launcher)
				.setContentTitle(title).setContentText(content);

		mBuilder.setAutoCancel(true);
		Intent notifyIntent = new Intent(LocationAlarmService.this,
				Notepad.class);

		// If the notification is for alerting the user of a note:
		if (requestCode != 4444) {
			long vibraPattern[] = { 0, 500, 250, 500 };
			mBuilder.setVibrate(vibraPattern);
			mBuilder.setLights(0xff00ff00, 300, 1000);

			notifyIntent.setAction(String.valueOf(requestCode));
			notifyIntent.putExtra("notificationSuccess",
					String.valueOf(requestCode));
		}

		PendingIntent intent = PendingIntent.getActivity(
				LocationAlarmService.this, requestCode, notifyIntent, 0);
		mBuilder.setContentIntent(intent);

		NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		mNotificationManager.notify(requestCode, mBuilder.build());
	}
}
