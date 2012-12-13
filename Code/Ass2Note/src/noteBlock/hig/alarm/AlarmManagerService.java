package noteBlock.hig.alarm;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.os.SystemClock;
import android.util.Log;

/**
 * This is a service that manages the two alarms, time and position. It starts
 * and stops these alarms.
 * @author Kristoffer Benum , and Solveig Sørheim
 *
 */
public class AlarmManagerService extends Service {
	// Interval between position reminder checks.
	private static final int MINUTE_IN_MILLIS = 60000;
	private static final int LOCATION_REQUEST_CODE = 10;
	private static final int TIME_REQUEST_CODE = 11;

	private Context context; // The application context.
	private AlarmManager alarmManager; // Managing both time and position alarm.
	private Intent alarmReceiverIntent; // Specifies whom the receiver of the

	@Override
	public void onCreate() {
		super.onCreate();
//		Log.i("AlarmManagerService", "onCreate");

		context = this.getApplicationContext();
		alarmManager = (AlarmManager) context
				.getSystemService(Context.ALARM_SERVICE);
		alarmReceiverIntent = new Intent(context, AlarmReceiver.class);
	}

	@Override
	public IBinder onBind(Intent intent) {
		throw new UnsupportedOperationException("Not being implemented");
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
//		Log.i("AlarmManagerService", "onStart");

		// Get the sent information sent by the caller:
		String alarmType = intent.getStringExtra("alarmType");
		String command = intent.getStringExtra("COMMAND");

		// Choose to start alarm by time or position:
		if (alarmType.contains("time"))		timeAlarm(command, intent);
		if (alarmType.contains("position"))	positionAlarm(command);

//		Log.i("AlarmManagerService", "Stopping self");
		stopSelf();
		return super.onStartCommand(intent, flags, startId);
	}

	/**
	 * Method for starting or stopping the time-alarm.
	 * @param command decide whether to start or stop the alarm.
	 * @param intent contains time and rowId for the alarm.
	 */
	private void timeAlarm(String command, Intent intent) {
		long time = intent.getLongExtra("time", 0);
		long rowId = intent.getLongExtra("rowId", 0);

		alarmReceiverIntent.putExtra("alarmType", "time");
		alarmReceiverIntent.putExtra("rowId", rowId);

		// PendingIntent for making broadcast available.
		PendingIntent pi = PendingIntent.getBroadcast(context,
				TIME_REQUEST_CODE, alarmReceiverIntent,
				PendingIntent.FLAG_UPDATE_CURRENT);

		if (command.contains("Start Alarm")) {
			alarmManager.set(AlarmManager.RTC_WAKEUP, time, pi);
		} else if (command.contains("Stop Alarm")) {
			Log.i("AlarmManagerService", "command = stop alarm");
			pi.cancel(); 					// Cancel the pendingIntent.
			alarmManager.cancel(pi); 		// Cancel the alarm.
			if (time != 0) alarmManager.set(AlarmManager.RTC_WAKEUP, time, pi);
			
		} else
			Log.e("AlarmManagerService", "Command contained unknown value");
	}

	/**
	 * Method for starting or stopping the position alarm.
	 * @param command decides whether to start or stop the alarm.
	 */
	private void positionAlarm(String command) {
		// Let the receiver know its a position-alarm.
		alarmReceiverIntent.putExtra("alarmType", "position");

		PendingIntent pi = PendingIntent.getBroadcast(context,
				LOCATION_REQUEST_CODE, alarmReceiverIntent,
				PendingIntent.FLAG_UPDATE_CURRENT);

		if (command.contains("Start Alarm")) {
			// Start the alarm now, and start it again every 5 minutes:
			alarmManager.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,
					SystemClock.elapsedRealtime(), MINUTE_IN_MILLIS * 5, pi);
		} else if (command.contains("Stop Alarm")) {
			pi.cancel(); 						// Cancel the pendingIntent.
			alarmManager.cancel(pi); 			// Cancel the alarm.
		} else
			Log.e("AlarmManagerService", "Command contained unknown value");
	}
}
