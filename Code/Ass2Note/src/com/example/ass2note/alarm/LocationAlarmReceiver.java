package com.example.ass2note.alarm;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class LocationAlarmReceiver extends BroadcastReceiver {
	private Intent alarmServiceIntent;
	

	public LocationAlarmReceiver() {
		Log.i("LocationAlarmReceiver","created receiver");
	}

	// TODO: This method is called when the BroadcastReceiver is receiving
	// an Intent broadcast.
	@Override
	public void onReceive(Context context, Intent intent) {
		Log.i("LocationAlarmReceiver", "onReceive is called");
		alarmServiceIntent = new Intent(context, LocationAlarmService.class);
		context.startService(alarmServiceIntent);
	}
	
}
