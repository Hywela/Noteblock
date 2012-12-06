/*
 * Copyright (C) 2008 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package noteedit;

import java.text.SimpleDateFormat;
import java.util.Date;

import android.app.Activity;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.example.ass2note.R;
import com.example.ass2note.alarm.AlarmManagerService;
import com.example.ass2note.location.GoogleMapsActivity;
import com.example.ass2note.notepad.NotesDbAdapter;

public class NoteEdit extends Activity {
	private static final int MAPSINTENT_ID = 1;
	private NotesDbAdapter mDbHelper;
	private Long mRowId;
	private InitiateAlarmButtons initiateAlarmButtons;
	private NoteEditLayoutManager layoutManager;
	private NoteEditSavePopulate savePopulateManager;
	private IntentFilter intentFilter;

	private ToggleButton showAlarmInfo;
	private boolean gpsEnabled = false, networkEnabled = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.note_edit);
		setTitle(R.string.edit_note);

		Log.i("NoteEdit", "created");
		
		mDbHelper = new NotesDbAdapter(this);
		mDbHelper.open();

		initiateLayout(savedInstanceState);

		savePopulateManager = new NoteEditSavePopulate(this, mDbHelper, mRowId);
		savePopulateManager.populateFields();
		layoutManager = new NoteEditLayoutManager(this, mRowId, savePopulateManager);
		initiateAlarmButtons = new InitiateAlarmButtons(this, layoutManager);

		// Set onClickListeners on buttons.
		initiateButtons();

		intentFilter = new IntentFilter("com.example.ass2note.notepad.NoteEdit.connectionReceiver");

		cancelNotificationOnPanel();
		
		layoutManager.displayAlarmInfo();
	}

	private void cancelNotificationOnPanel(){
		NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		try{
			mNotificationManager.cancel((Integer.parseInt(String.valueOf(mRowId))));
		}catch(Exception e){
			Log.i("NoteEdit", e.getMessage());
		}
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		savePopulateManager.closeDB();
	}

	@Override
	protected void onStart() {
		super.onStart();
		RelativeLayout focuslayout = (RelativeLayout) findViewById(R.id.RequestFocusLayout);
		focuslayout.requestFocus();
	}

	private void initiateLayout(Bundle savedInstanceState) {
		mRowId = (savedInstanceState == null) ? null
				: (Long) savedInstanceState
						.getSerializable(NotesDbAdapter.KEY_ROWID);
		if (mRowId == null) {
			Bundle extras = getIntent().getExtras();
			mRowId = extras != null ? extras.getLong(NotesDbAdapter.KEY_ROWID)
					: null;
		}
	}

	private void initiateButtons() {
		Button confirmButton = (Button) findViewById(R.id.confirm);
		Button alarmButton = (Button) findViewById(R.id.newNoteAlarm);
		
		showAlarmInfo = (ToggleButton) findViewById(R.id.showAlarmNoteInfo);
		showAlarmInfo.setSaveEnabled(false);
		
		
		confirmButton.setOnClickListener(new View.OnClickListener() {
			// ON click CONFIRM
			public void onClick(View view) {
				setResult(RESULT_OK);
				finish();
			}
		});

		alarmButton.setOnClickListener(new View.OnClickListener() {
			// ON click ALARM
			public void onClick(View v) {
				initiateAlarmButtons.initiateAlarmButtonDialog();
			}
		});
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		savePopulateManager.saveState();

		outState.putSerializable(NotesDbAdapter.KEY_ROWID, mRowId);
		outState.putParcelable("alarmToggle", showAlarmInfo.onSaveInstanceState());
	}

	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
		showAlarmInfo.onRestoreInstanceState(savedInstanceState.getParcelable("alarmToggle"));
		layoutManager.showAlarmLayout();
	}

	@Override
	protected void onPause() {
		super.onPause();
		unregisterReceiver(connectionReceiver);
		savePopulateManager.saveState();
	}

	@Override
	protected void onResume() {
		super.onResume();
		registerReceiver(connectionReceiver, intentFilter);
		savePopulateManager.populateFields();
	}

	/**
	 * Function. Receives information from GoogleMapsActivity when the activity
	 * finishes. Both latitude and longitude from the preferred position from
	 * the user is available here.
	 */
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		// The result came from GoogleMapsActivity:
		if (requestCode == MAPSINTENT_ID)
			switch (resultCode) {
			// A new location was selected:
			case Activity.RESULT_OK: {
				// Fetch the new data:
				savePopulateManager.savePosition(data.getStringExtra
						("latitude"), data.getStringExtra("longitude"), 
						data.getStringExtra("snippet"), "true");

				layoutManager.displayAlarmInfo();
				break;
			}
			// Unexpected occurrence happened or no new location was selected:
			case Activity.RESULT_CANCELED: {
				break;
			}
			} // end switch
	} // end onActivityResult

	private BroadcastReceiver connectionReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			String fromCaller = intent.getStringExtra("fromCaller");

			if (fromCaller.contains("ConnectionService")) {
				gpsEnabled = intent.getBooleanExtra("gpsEnabled", false);
				networkEnabled = intent.getBooleanExtra("networkEnabled", false);
				if (networkEnabled) startGoogleMaps();
				else alertToast(getString(R.string.network_alert));
				
			} else if (fromCaller.contains("InitiateAlarmButtons")) {
				Log.i("NoteEdit", "receiver called from InitiateAlarmButtons.");

				String command = intent.getStringExtra("command");
				if (command.contains("updateTime")) {
					SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm");
					long time = intent.getLongExtra("time", 0);
					Log.i("NoteEdit","time is: "+dateFormat.format(time));
					
					savePopulateManager.saveTime(time, "true");
					layoutManager.displayAlarmInfo();

					alertToast(getString(R.string.toast_set_alarm) +" \n"
							+ dateFormat.format(time));

					// TODO: Start this at an other place..
					startTimeAlarm();
				}else if (command.contains("stopTimeAlarm")) {
					stopTimeAlarm(intent.getLongExtra("time", 0));
				}
			}
		}
	};

	private void startGoogleMaps(){
		Intent i = new Intent(NoteEdit.this, GoogleMapsActivity.class);
		i.putExtra("LATITUDE", savePopulateManager.getLatitude());
		i.putExtra("LONGITUDE", savePopulateManager.getLongitude());
		startActivityForResult(i, MAPSINTENT_ID);
	}
	
/*	private long getClosestTime()[] {
		Cursor notesCursor = mDbHelper.fetchAllNotes();
		// SimpleDateFormat dateFormat = new
		// SimpleDateFormat("dd-MM-yyyy HH:mm");

		Date date = new Date();
		long now = date.getTime();
		long closestTime = 0;
		long timeId = 0;
		
		while (notesCursor.moveToNext()) {
			long timeInDb = notesCursor.getLong(notesCursor
					.getColumnIndexOrThrow(NotesDbAdapter.KEY_TIME));
			String timReminder = notesCursor.getString(notesCursor
					.getColumnIndexOrThrow(NotesDbAdapter.KEY_TIME_REMINDER));

			if ((timeInDb >= now && timeInDb <= closestTime && timReminder.contains("true"))
					|| (closestTime == 0 && timReminder.contains("true"))){
				closestTime = timeInDb;
				timeId = notesCursor.getLong(notesCursor.getColumnIndexOrThrow
						(NotesDbAdapter.KEY_ROWID));
			}

			SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm");
			Log.i("NoteEdit", "getclosestTime: time is: " + dateFormat.format(timeInDb));
		} // - End while()
		
		long clTime[] = {closestTime, timeId};
		
		return clTime;
	}// -End time();*/

	private void startTimeAlarm() {
		long closestTime[] = mDbHelper.getClosestTime();
		if (closestTime[0] > 0) {
			Intent i = new Intent(this, AlarmManagerService.class);
			i.putExtra("alarmType", "time");
			i.putExtra("time", closestTime[0]);
			i.putExtra("rowId", closestTime[1]);
			i.putExtra("COMMAND", "Start Alarm");
			startService(i);
		}
	}
	
	private void stopTimeAlarm(long time) {
		if (time > 0) {
			Intent i = new Intent(this, AlarmManagerService.class);
			i.putExtra("alarmType", "time");
			i.putExtra("time", time);
			i.putExtra("rowId", mRowId);
			i.putExtra("COMMAND", "Stop Alarm");
			startService(i);
		}
	}

	private void alertToast(String message) {
		Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG)
				.show();
	}

	/*
	 * private void alertGPSConnection(){ AlertDialog.Builder altDialog = new
	 * AlertDialog.Builder(this);
	 * altDialog.setMessage("Please start your GPS and try again. The GPS " +
	 * "needs to be ON all the time for this function to work.");
	 * altDialog.setNeutralButton("OK", new DialogInterface.OnClickListener() {
	 * // @Override public void onClick(DialogInterface dialog, int which) { //
	 * enableLocationSettings(); } });
	 * altDialog.setNegativeButton(R.string.cancel, new
	 * DialogInterface.OnClickListener() { public void onClick(DialogInterface
	 * dialog, int id) { // User cancelled the dialog } }); altDialog.show(); }
	 */

	/*
	 * private void enableLocationSettings() { Intent settingsIntent = new
	 * Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
	 * startActivity(settingsIntent); }
	 */

}