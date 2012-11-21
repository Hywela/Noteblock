package com.example.ass2note.alarm;

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
	
	public AlarmManagerService() {
		// TODO Auto-generated constructor stub
	}
	
    @Override
    public void onCreate() {
    	super.onCreate();
    	Log.i("AlarmManagerService", "onCreate");
    }


    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }
    
    /**
     * AlarmManagerService's onStartCommand. It receives a command from the 
     * caller class, and executes the proper methods based on that command.
     * It may start or stop the alarm as appropriate. After the command has
     * been executed, it kills/stops the service/itself. 
     */
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.i("AlarmManagerService", "onStart");
		
		// Fetch the command from the caller class.
		String command = intent.getStringExtra("COMMAND");
		
		if(command.contains("Start Alarm")){
			Log.i("AlarmManagerService", "Starting Alarm Command");
			startAlarm();
		}else if(command.contains("Stop Alarm")){
			Log.i("AlarmManagerService", "Stopping Alarm Command");
			stopAlarm();
		}else{
			Log.e("AlarmManagerService", "Command contained unknown value");
		}
		
		Log.i("AlarmManagerService", "Stopping self");
		stopSelf();
		return super.onStartCommand(intent, flags, startId);
	}
	
    /**
     * Method for fetching the alarm associated with location, and stop that
     * alarm. 
     */
    private void stopAlarm(){
    	Log.i("AlarmManagerService", "stopAlarm");
		Context context = this.getApplicationContext();
		
		// Fetch the current alarm.
		AlarmManager a =  (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
		
		// Create an Intent similar to the intent in the current alarm.
    	Intent myIntent = new Intent(context, LocationAlarmReceiver.class);
    	
    	// Create a PendingIntent similar to the pendingIntent in the current alarm.
		PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, myIntent, PendingIntent.FLAG_UPDATE_CURRENT);
		
   	 	pendingIntent.cancel();		// Cancel the pendingIntent.
   	 	a.cancel(pendingIntent);	// Cancel the alarm.
    }
    
    /**
     * Method for starting a new alarm-loop. The alarm is set repeating every
     * five minutes.
     */
    private void startAlarm() {
		Log.i("AlarmManagerService", "Starting a new alarm-loop");
		
		Context context = this.getApplicationContext();
		
		// Manager for starting and stopping the alarm connected to LocationAlarmReceiver.
		AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
		
		// Intent for calling the correct receiver.S
		Intent i = new Intent(context, LocationAlarmReceiver.class);
		
		// PendingIntent for making broadcast available.
		PendingIntent pi = PendingIntent.getBroadcast(context, 0, i, PendingIntent.FLAG_UPDATE_CURRENT);
		
		// Start the alarm now, and start it again every 5 minutes:
		alarmManager.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,
				SystemClock.elapsedRealtime(), MINUTE_IN_MILLIS, pi);
	}
    
}
