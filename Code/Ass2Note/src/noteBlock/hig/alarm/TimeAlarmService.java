package noteBlock.hig.alarm;

import noteBlock.hig.R;
import noteBlock.hig.notepad.Notepad;
import noteBlock.hig.notepad.NotesDbAdapter;
import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

/**
 * This is a service that notifies the user of a specific note based on which
 * note was set on the time-alarm.
 * 
 * @author Kristoffer Benum , and Solveig Sørheim
 */
public class TimeAlarmService extends IntentService {
	private NotesDbAdapter mDbHelper; 	// The database-class.
	private long rowId;					// The note's Id.
	private String title;				// The note's title.

	public TimeAlarmService() {
		super("TimeAlarmService");
	}

	public TimeAlarmService(String name) {
		super(name);
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		mDbHelper = new NotesDbAdapter(this); 	// Create a new instance of DB.
		mDbHelper.open(); 						// Open the DB.

		rowId = intent.getLongExtra("rowId", 0);

		notifyDatabase();		// Update the timeReminder to false.
		getNotificationInfo();	// Fetch the note's title.
		notifyUser();			// Create a notification on the phone's panel.
		notifyUserWithSound();	// Add a sound.

		startTimeAlarm();

		mDbHelper.close();
	}

	/**
	 * Method for starting AlarmManagerService if there exist more notes with
	 * the timeReminder enabled. The rowId and time of the note with a time
	 * closest to now will be sent as data.
	 */
	private void startTimeAlarm() {
		long closestTime[] = mDbHelper.getClosestTime();
		if (closestTime[0] > 0) {
			Intent i = new Intent(this, AlarmManagerService.class);
			i.putExtra("alarmType", "time");
			i.putExtra("time", closestTime[0]);
			i.putExtra("rowId", closestTime[1]);
			i.putExtra("COMMAND", "Start Alarm");
			startService(i);
		}
	}

	/**
	 * Method for fetching the current note's title from the database.
	 */
	private void getNotificationInfo() {
		Cursor note = mDbHelper.fetchNote(rowId);
		
		// Make sure no leaks occur.
		if (note.moveToFirst()) {
			title = note.getString(note
					.getColumnIndexOrThrow(NotesDbAdapter.KEY_TITLE));
		}
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
			Log.e("TimeAlarmService", e.getMessage());
		}
	}

	/**
	 * Method for creating a notification on the phone's panel, notifying the
	 * user of the specific note.
	 */
	public void notifyUser() {
		NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(
				this).setSmallIcon(R.drawable.ic_launcher)
				.setContentTitle(getString(R.string.notification_title))
				.setContentText(title);

		long vibraPattern[] = { 0, 500, 250, 500 };
		mBuilder.setVibrate(vibraPattern);
		mBuilder.setLights(0xff00ff00, 300, 1000);

		/* Set the notification on the panel to remove itself when the user
		 * presses it. */
		mBuilder.setAutoCancel(true);
		Intent notifyIntent = new Intent(this, Notepad.class);
		notifyIntent.setAction(String.valueOf(rowId));
		notifyIntent.putExtra("notificationSuccess", String.valueOf(rowId));
		PendingIntent pi = PendingIntent.getActivity(this, 0, notifyIntent,	0);
		mBuilder.setContentIntent(pi);

		NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

		mNotificationManager.notify(Integer.parseInt(String.valueOf(rowId)), mBuilder.build());
	}

	/**
	 * Method for setting the timeReminder of the specific note to false.
	 */
	public void notifyDatabase() {
		mDbHelper.updateTimeNotification(rowId, "false");
	}
}
