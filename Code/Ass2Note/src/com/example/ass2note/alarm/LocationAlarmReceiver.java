package com.example.ass2note.alarm;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class LocationAlarmReceiver extends BroadcastReceiver {

	public LocationAlarmReceiver() {
		Log.i("LocationAlarmReceiver","created receiver");
	}

	/**
	 * This method is called when the BroadcastReceiver is receiving an Intent 
	 * broadcast from an alarm from AlarmManagerService. It calls 
	 * LocationAlarmService.
	 */
	@Override
	public void onReceive(Context context, Intent intent) {
		Log.i("LocationAlarmReceiver", "onReceive is called");
		Intent alarmServiceIntent = new Intent(context, LocationAlarmService.class);
		context.startService(alarmServiceIntent);
	}
	
}
