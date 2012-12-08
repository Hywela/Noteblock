package ass2note.alarm;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.os.SystemClock;
import android.util.Log;


public class AlarmManagerService extends Service {
	private static final int MINUTE_IN_MILLIS = 60000;	// Interval between position reminder checks.
	private static final int LOCATION_REQUEST_CODE = 10;
	private static final int TIME_REQUEST_CODE = 11;
	private Context context;
	private AlarmManager alarmManager;
	private Intent alarmReceiverIntent;
	
	
	public AlarmManagerService() {
	}
	
    @Override
    public void onCreate() {
    	super.onCreate();
    	Log.i("AlarmManagerService", "onCreate");
    	
    	// Get the main activity context.
		context = this.getApplicationContext();

		// Manager for starting and stopping the alarm connected to LocationAlarmReceiver.
		alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
		
		alarmReceiverIntent = new Intent(context, AlarmReceiver.class);
    }


    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }
    
    /**
     * AlarmManagerService's onStartCommand.TODO: update this
     */
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.i("AlarmManagerService", "onStart");
		
		// Get the sent information:
		String alarmType = intent.getStringExtra("alarmType");
		String command = intent.getStringExtra("COMMAND");
		
		// Choose to start alarm by time or position:
		if		(alarmType.contains("time")) 	 timeAlarm(command, intent);
		else if	(alarmType.contains("position")) positionAlarm(command);
		else Log.i("AlarmManagerService", "alarmType contained unknown value");
		
		Log.i("AlarmManagerService", "Stopping self");
		stopSelf();
		return super.onStartCommand(intent, flags, startId);
	}
	
	
	private void timeAlarm(String command, Intent intent){
		long time = intent.getLongExtra("time", 0);
		long rowId = intent.getLongExtra("rowId", 0);
		
		if		(command.contains("Start Alarm")) startTimeAlarm(time, rowId);
		else if	(command.contains("Stop Alarm"))  stopTimeAlarm(time, rowId);
		else Log.e("AlarmManagerService", "Command contained unknown value");
	}
	
	private void positionAlarm(String command){
		if		(command.contains("Start Alarm")) startLocationAlarm();
		else if	(command.contains("Stop Alarm"))  stopLocationAlarm();
		else Log.e("AlarmManagerService", "Command contained unknown value");
	}
	
	
	// ********************************************************************* \\
	// **************************** Time Alarm ***************************** \\
	// ********************************************************************* \\
	
	private void startTimeAlarm(long time, long rowId){
		SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm");
		Log.i("AlarmManagerService", "Starting a new timeAlarm loop. time is: " + dateFormat.format(time));
		
		// Intent for calling the correct receiver.
		alarmReceiverIntent.putExtra("alarmType", "time");
		alarmReceiverIntent.putExtra("alarmTime", time);
		alarmReceiverIntent.putExtra("rowId", rowId);
    	
		// PendingIntent for making broadcast available.
		PendingIntent pi = PendingIntent.getBroadcast(context, 
				TIME_REQUEST_CODE, alarmReceiverIntent, 
				PendingIntent.FLAG_UPDATE_CURRENT);
		
    	alarmManager.set(AlarmManager.RTC_WAKEUP, time/*calendar.getTimeInMillis()*/, pi);
	}
	
	// TODO: Tweek this. The alarm WILL stop. Check if its ok to do so.
	private void stopTimeAlarm(long time, long rowId){
		Log.i("AlarmManagerService", "Stopping time alarm");
		
		// Let the receiver know that its a time-alarm.
		alarmReceiverIntent.putExtra("alarmType", "time");
		alarmReceiverIntent.putExtra("alarmTime", time);
		alarmReceiverIntent.putExtra("rowId", rowId);
    	
    	// Create a PendingIntent similar to the pendingIntent in the current alarm.
		PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 
				TIME_REQUEST_CODE, alarmReceiverIntent, 
				PendingIntent.FLAG_UPDATE_CURRENT);
		
   	 	pendingIntent.cancel();				// Cancel the pendingIntent.
   	 	alarmManager.cancel(pendingIntent);	// Cancel the alarm.
	}
	
	
	// ********************************************************************* \\
	// ************************** Position Alarm *************************** \\
	// ********************************************************************* \\
	
	 /**
     * Method for starting a new alarm-loop. The alarm is set repeating every
     * five minutes.
     */
    private void startLocationAlarm() {
		Log.i("AlarmManagerService", "Starting a new locationAlarm-loop");
		
		// Let the receiver know its a position-alarm.
		alarmReceiverIntent.putExtra("alarmType", "position");
		
		// PendingIntent for making broadcast available.
		PendingIntent pi = PendingIntent.getBroadcast(context, 
				LOCATION_REQUEST_CODE, alarmReceiverIntent, 
				PendingIntent.FLAG_UPDATE_CURRENT);
		
		// Start the alarm now, and start it again every 5 minutes:
		alarmManager.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,
				SystemClock.elapsedRealtime(), MINUTE_IN_MILLIS*5, pi);
	}
    
    /**
     * Method for fetching the alarm associated with location, and stop that
     * alarm. 
     */
    private void stopLocationAlarm(){
    	Log.i("AlarmManagerService", "stopLocationAlarm");
		
		// Create an Intent similar to the intent in the current alarm.
    	alarmReceiverIntent.putExtra("alarmType", "position");
    	
    	// Create a PendingIntent similar to the pendingIntent in the current alarm.
		PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 
				LOCATION_REQUEST_CODE, alarmReceiverIntent, 
				PendingIntent.FLAG_UPDATE_CURRENT);
		
   	 	pendingIntent.cancel();				// Cancel the pendingIntent.
   	 	alarmManager.cancel(pendingIntent);	// Cancel the alarm.
    }
    
}
