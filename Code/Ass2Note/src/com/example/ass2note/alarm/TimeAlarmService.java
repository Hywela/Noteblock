package com.example.ass2note.alarm;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.widget.Toast;


/*
 * Her kan du kode alt som skal skje dersom alarmen gjekk av. Dette er ganske
 * likt LocationAlarmService, s� vi burde finne en m�te � bruke samme kodane p�.
 * Men det viktigaste er at det fungerar f�rst. 
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
