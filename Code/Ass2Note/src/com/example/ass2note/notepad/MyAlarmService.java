package com.example.ass2note.notepad;



import java.util.ArrayList;
import java.util.Calendar;
import java.util.TimeZone;

import android.app.AlarmManager;
import android.app.Dialog;
import android.app.PendingIntent;
import android.app.Service;
import android.app.TimePickerDialog;

import android.content.Context;
import android.content.Intent;

import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.provider.AlarmClock;

import android.text.format.DateFormat;
import android.text.format.Time;
import android.util.Log;
import android.widget.Toast;



public abstract class MyAlarmService extends Service {



 
	 
	
@Override

public void onCreate() {

// TODO Auto-generated method stub

Toast.makeText(this, "MyAlarmService.onCreate()", Toast.LENGTH_LONG).show();

}

@Override
public int onStartCommand(Intent intent, int flags, int startId) {
    Log.i("LocalService", "Received start id " + startId + ": " + intent);
    Toast.makeText(this, "Command", Toast.LENGTH_LONG).show();
    
    return START_STICKY;
}

@Override

public IBinder onBind(Intent intent) {

// TODO Auto-generated method stub

Toast.makeText(this, "MyAlarmService.onBind()", Toast.LENGTH_LONG).show();

return null;

}



@Override

public void onDestroy() {

// TODO Auto-generated method stub

super.onDestroy();

Toast.makeText(this, "MyAlarmService.onDestroy()", Toast.LENGTH_LONG).show();

}



@Override

public void onStart(Intent intent, int startId) {

// TODO Auto-generated method stub

super.onStart(intent, startId);

		

Toast.makeText(this, "MyAlarmService.onStart()", Toast.LENGTH_LONG).show();

}



@Override

public boolean onUnbind(Intent intent) {

// TODO Auto-generated method stub

Toast.makeText(this, "MyAlarmService.onUnbind()", Toast.LENGTH_LONG).show();

return super.onUnbind(intent);

}
public void timecheck() {
    // Use the current time as the default values for the picker

	
	


} 



public abstract  void onReceiveMessage(Message msg);
}// End Class
