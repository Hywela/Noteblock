package noteBlock.hig.alarm;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * This is a broadcastReceiver whose main purpose is to fetch all alarms from
 * AlarmManagerService, and decide what to do with these alarms.
 * @author Kristoffer Benum , and Solveig S�rheim
 *
 */
public class AlarmReceiver extends BroadcastReceiver {

	/**
	 * This method is called when the BroadcastReceiver is receiving an Intent
	 * broadcast from an alarm from AlarmManagerService.
	 */
	@Override
	public void onReceive(Context context, Intent intent) {
//		Log.i("AlarmReceiver", "Received alarm");
		String alarmType = intent.getStringExtra("alarmType");
		
		// If the alarm trigger was position based:
		if (alarmType.contains("position")) {
			Intent alarmServiceIntent = new Intent(context,	LocationAlarmService.class);
			context.startService(alarmServiceIntent);
			
		// If the alarm trigger was time-based:
		}else if(alarmType.contains("time")){
			long rowId = intent.getLongExtra("rowId", 0);
			Intent alarmServiceIntent = new Intent(context,	TimeAlarmService.class);
			alarmServiceIntent.putExtra("rowId", rowId);
			context.startService(alarmServiceIntent);
			
		// The alarmType was not set or is invalid:
		}else
			Log.w("AlarmReceiver", "alarmType is invalid");
	}
}