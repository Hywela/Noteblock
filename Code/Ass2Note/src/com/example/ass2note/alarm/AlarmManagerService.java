package com.example.ass2note.alarm;

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
		long time = intent.getLongExtra("time", 0);
		
		// Choose to start alarm by time or position:
		if		(alarmType.contains("time")) 	 timeAlarm(command, time);
		else if	(alarmType.contains("position")) positionAlarm(command);
		else if (alarmType.contains("noteDeleted")) positionAlarm("Stop Alarm"); //TODO: add time-check
		else Log.i("AlarmManagerService", "alarmType contained unknown value");
		
		Log.i("AlarmManagerService", "Stopping self");
		stopSelf();
		return super.onStartCommand(intent, flags, startId);
	}
	
	
	private void timeAlarm(String command, long time){
		if		(command.contains("Start Alarm")) startTimeAlarm(time);
		else if	(command.contains("Stop Alarm"))  stopTimeAlarm();
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
	
	private void startTimeAlarm(long closestTime){
		Log.i("AlarmManagerService", "Starting a new timeAlarm loop. time is: " + closestTime);
		
		// Intent for calling the correct receiver.
		alarmReceiverIntent.putExtra("alarmType", "time");
    	
    	// we know mobiletuts updates at right around 1130 GMT.
    	// let's grab new stuff at around 11:45 GMT, inexactly
    	Calendar calendar = Calendar.getInstance();

    	calendar.setTimeInMillis(closestTime);

		// PendingIntent for making broadcast available.
		PendingIntent pi = PendingIntent.getBroadcast(context, 
				TIME_REQUEST_CODE, alarmReceiverIntent, 
				PendingIntent.FLAG_UPDATE_CURRENT);
		
    	alarmManager.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pi);
	}
	
	// TODO: Tweek this. The alarm WILL stop. Check if its ok to do so.
	private void stopTimeAlarm(){
		Log.i("AlarmManagerService", "Stopping time alarm");
		
		// Let the receiver know that its a time-alarm.
		alarmReceiverIntent.putExtra("alarmType", "time");
    	
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
