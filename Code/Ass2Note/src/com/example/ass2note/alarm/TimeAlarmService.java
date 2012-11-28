package com.example.ass2note.alarm;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.widget.Toast;


/*
 * Her kan du kode alt som skal skje dersom alarmen gjekk av. Dette er ganske
 * likt LocationAlarmService, så vi burde finne en måte å bruke samme kodane på.
 * Men det viktigaste er at det fungerar først. 
 */
public class TimeAlarmService extends Service {
    public TimeAlarmService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }
    
    @Override
    public void onCreate() {
    	super.onCreate();
    	
    //	Context c = getApplicationContext();
    //	Toast.makeText(c, "Inside TimeAlarmService!", Toast.LENGTH_LONG).show();
    	stopSelf();
    }
}
