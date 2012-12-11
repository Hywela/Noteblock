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


/*
 * Her kan du kode alt som skal skje dersom alarmen gjekk av. Dette er ganske
 * likt LocationAlarmService, så vi burde finne en måte å bruke samme kodane på.
 * Men det viktigaste er at det fungerar først. 
 */
public class TimeAlarmService extends IntentService {
	private NotesDbAdapter mDbHelper; // The database-class.
	private long alarmTime;
	private long rowId;
	private String title;

	public TimeAlarmService(){
		super("TimeAlarmService");
	}
	
	public TimeAlarmService(String name) {
		super(name);
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		mDbHelper = new NotesDbAdapter(this); // Create a new instance of DB.
		mDbHelper.open(); // Open the DB.
		
		alarmTime = intent.getLongExtra("alarmTime", 0);
		rowId = intent.getLongExtra("rowId", 0);

		notifyDatabase();
		getNotificationInfo();
		notifyUser();
		notifyUserWithSound();

		startTimeAlarm();

		mDbHelper.close();
	}

	// TODO: Check if the if(...) works..
		private void startTimeAlarm() {
			long closestTime[] = mDbHelper.getClosestTime();
			Log.i("TimeAlarmService", "closestTIme is: " + closestTime);
			if (closestTime[0] > 0) {
				Intent i = new Intent(this, AlarmManagerService.class);
				i.putExtra("alarmType", "time");
				i.putExtra("time", closestTime[0]);
				i.putExtra("rowId", closestTime[1]);
				i.putExtra("COMMAND", "Start Alarm");
				startService(i);
			}
		}

	private void getNotificationInfo(){
		Cursor note = mDbHelper.fetchNote(rowId);
		if(note.moveToFirst()){
			title = note.getString(note.getColumnIndexOrThrow(NotesDbAdapter.KEY_TITLE));
		}
	}
	
	private void notifyUserWithSound(){
		 try {
		        Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
		        Ringtone r = RingtoneManager.getRingtone(getApplicationContext(), notification);
		        r.play();
		    } catch (Exception e) {}
	}
	
	/**
	 * Method for notifying the user with (lights, vibration, sound and) a
	 * note on the panel. TODO: Add lights, vibration, sound?
	 * TODO: THis exist already! Try to use the existing one!
	 */
	public void notifyUser() {
		NotificationCompat.Builder mBuilder = 
				new NotificationCompat.Builder(this)
				.setSmallIcon(R.drawable.ic_launcher)
				.setContentTitle(getString(R.string.notification_title))
				.setContentText(title);
		
		long vibraPattern[] = {0, 500, 250, 500};
		mBuilder.setVibrate(vibraPattern);
		mBuilder.setLights(0xff00ff00, 300, 1000);
		
		/* Set the notification on the panel to remove itself when the user 
		   presses it.*/
		mBuilder.setAutoCancel(true);
		Intent notifyIntent = new Intent(this, Notepad.class);
		notifyIntent.setAction(String.valueOf(rowId));
		notifyIntent.putExtra("notificationSuccess", String.valueOf(rowId));
		PendingIntent intent = PendingIntent.getActivity(this, 0, notifyIntent, 0);
		mBuilder.setContentIntent(intent);

		NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		
		mNotificationManager.notify(Integer.parseInt(String.valueOf(rowId)),mBuilder.build());
	}

	public void notifyDatabase() {
		Log.i("TimeAlarmService", "trying to notify database. rowId: "+ rowId);
		mDbHelper.updateTimeNotification(rowId, "false");
	}
}
