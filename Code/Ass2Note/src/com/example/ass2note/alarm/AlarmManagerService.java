package com.example.ass2note.alarm;

import java.util.ArrayList;

import com.example.ass2note.notepad.NotesDbAdapter;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.IBinder;
import android.os.SystemClock;
import android.util.Log;

public class AlarmManagerService extends Service {
	private static final int MINUTE_IN_MILLIS = 60000;
	private AlarmManager alarmManager;
	private PendingIntent pi;
	
	
    public AlarmManagerService() {
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
    

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.i("AlarmManagerService", "onStart");
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
	
    
    private void stopAlarm(){
    	Log.i("AlarmManagerService", "stopAlarm");
		Context context = this.getApplicationContext();
		AlarmManager a =  (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
    	Intent myntent = new Intent(context, LocationAlarmReceiver.class);
		PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, myntent, PendingIntent.FLAG_UPDATE_CURRENT);
   	 	pendingIntent.cancel();
   	 	a.cancel(pendingIntent);
    }
    
    public void startAlarm() {
		Log.i("AlarmManagerService", "Starting a new alarm-loop");
		Context context = this.getApplicationContext();
		alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
		Intent i = new Intent(context, LocationAlarmReceiver.class);
		
		pi = PendingIntent.getBroadcast(context, 0, i, PendingIntent.FLAG_UPDATE_CURRENT);
		
		
		alarmManager.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,
				SystemClock.elapsedRealtime() + 1000, MINUTE_IN_MILLIS, pi);
	}
    
}
