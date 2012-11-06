package com.example.ass2note.notepad;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;
public class BrodcastService extends Service{
    private static String TAG = "Inchoo.net tutorial";
    @Override
    public IBinder onBind(Intent arg0) {
        // TODO Auto-generated method stub
        return null;
    }
    @Override
    public void onStart(Intent intent, int startId) {
        // TODO Auto-generated method stub
        super.onStart(intent, startId);
        Log.d(TAG, "FirstService started");
        Intent i = new Intent("android.intent.action.M").putExtra("some_msg", "I will be sent!");
        this.sendBroadcast(i);
        this.stopSelf();
    }
    @Override
    public void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
        Log.d(TAG, "FirstService destroyed");
    }
}