package com.example.ass2note.notepad;



import android.app.IntentService;
import android.content.Intent;

import android.widget.Toast;



public class MyAlarmService extends IntentService {

	  
	  public MyAlarmService() {
	      super("MyAlarmService");
	      
	  }
	  @Override
	  public int onStartCommand(Intent intent, int flags, int startId) {
	      Toast.makeText(this, "service starting", Toast.LENGTH_SHORT).show();
	      return super.onStartCommand(intent,flags,startId);
	  }
	 
	  @Override
	  protected void onHandleIntent(Intent intent) {
		  
		  Toast.makeText(this, "sddddd", Toast.LENGTH_SHORT).show();
	      
	}
}
