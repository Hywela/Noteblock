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

package com.example.ass2note.notepad;

import java.text.SimpleDateFormat;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.example.ass2note.R;
import com.example.ass2note.location.GoogleMapsActivity;

public class NoteEdit extends Activity {
	private static final int MAPSINTENT_ID = 1;
	private NotesDbAdapter mDbHelper;
	private Long mRowId;
	private InitiateAlarmButtons initiateAlarmButtons;
	private NoteEditLayoutManager layoutManager;
	private NoteEditSavePopulate savePopulateManager; 
	private IntentFilter intentFilter;
	
	private EditText mTitleText, mBodyText;
	private ToggleButton showAlarmInfo;
	private String lati = "lat", longi = "long";
	private String positionReminder = "false"; 
	private String snippet = "";
	private boolean gpsEnabled = false, networkEnabled = false;
	private long time = 0;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.note_edit);
		setTitle(R.string.edit_note);

		mDbHelper = new NotesDbAdapter(this);
		mDbHelper.open();
		
		initiateLayout(savedInstanceState);
		
		initiateAlarmButtons = new InitiateAlarmButtons(this);
		savePopulateManager = new NoteEditSavePopulate(this, mDbHelper, mRowId, initiateAlarmButtons);
		savePopulateManager.setLayout(mTitleText, mBodyText);
		populateFields();

		// Set onClickListeners on buttons.
		initiateButtons();
		
		ToggleButton alarmPosition = (ToggleButton) findViewById(R.id.toggleAlarmPosition);
		TextView alarmPositionInfo = (TextView) findViewById(R.id.alarmPositionInfo);
		
		layoutManager = new NoteEditLayoutManager(this, mDbHelper, mRowId);
		layoutManager.setAlarmPosition(alarmPosition, alarmPositionInfo);
		
		
		intentFilter = new IntentFilter("com.example.ass2note.notepad.NoteEdit.connectionReceiver");


		displayAlarmInfo();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		layoutManager.closeDB();
	}
	
	@Override
	protected void onStart() {
		super.onStart();
		RelativeLayout focuslayout = (RelativeLayout) findViewById(R.id.RequestFocusLayout);
		focuslayout.requestFocus();
	}

	private void initiateLayout(Bundle savedInstanceState) {
		mTitleText = (EditText) findViewById(R.id.title);
		mBodyText = (EditText) findViewById(R.id.body);

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
		saveState();
		
		outState.putSerializable(NotesDbAdapter.KEY_ROWID, mRowId);
		outState.putParcelable("alarmToggle", showAlarmInfo.onSaveInstanceState());
	}

	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
		showAlarmInfo.onRestoreInstanceState(savedInstanceState.getParcelable("alarmToggle"));
		showAlarmInfo(showAlarmInfo);
	}

	@Override
	protected void onPause() {
		super.onPause();
		unregisterReceiver(connectionReceiver);
		saveState();
	}

	@Override
	protected void onResume() {
		super.onResume();
		registerReceiver(connectionReceiver, intentFilter);
		populateFields();
	}

	private void populateFields(){
		savePopulateManager.populateFields();
		lati = savePopulateManager.getLatitude();
		longi = savePopulateManager.getLongitude();
		snippet = savePopulateManager.getSnippet();
		positionReminder = savePopulateManager.getPositionReminder();
		time = savePopulateManager.getTime();
		System.out.println("populatefields " + longi);
	}
	
	private void saveTime(long time) {
		if (mRowId == null) {
		} else
			mDbHelper.updateTime(mRowId, time);
	}

	private void saveState(){
		savePopulateManager.saveState(mTitleText.getText().toString(), 
				mBodyText.getText().toString(), lati, longi, positionReminder,
				snippet);
	}
	
	/**
	 * Function. Receives information from GoogleMapsActivity when the activity
	 * finishes. Both latitude and longitude from the preferred position from
	 * the user is available here.
	 */
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		Log.i("NoteEdit", "onActivityResult");

		// The result came from GoogleMapsActivity:
		if (requestCode == MAPSINTENT_ID)
			switch (resultCode) {
			// A new location was selected:
			case Activity.RESULT_OK: {
				// Fetch the new data:
				lati = data.getStringExtra("latitude");
				longi = data.getStringExtra("longitude");
				snippet = data.getStringExtra("snippet");
				positionReminder = "true";

				// Save the data in the database.
				saveState();
				displayAlarmInfo();
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

				if (networkEnabled) {
					Intent i = new Intent(NoteEdit.this, GoogleMapsActivity.class);
					i.putExtra("LATITUDE", lati);
					i.putExtra("LONGITUDE", longi);
					startActivityForResult(i, MAPSINTENT_ID);
				} else
					alertToast(getString(R.string.network_alert));
			} else if (fromCaller.contains("test")) {
				Log.i("NoteEdit", "receiver called from test");
				String command = intent.getStringExtra("command");
				if (command.contains("updateTime")) {
					Long updatedTime = intent.getLongExtra("time", 0);
					saveTime(updatedTime);
				}
			}

		}
	};

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

	public void showAlarmInfo(View view) {
		View v = (View) findViewById(R.id.alarmInfo);
		boolean on = ((ToggleButton) view).isChecked();
		
		if (on)	v.setVisibility(View.VISIBLE);
		else	v.setVisibility(View.GONE);
	}

	public void changePositionStatus(View view) {
		boolean posReminder = layoutManager.changePositionBtnStatus
				((ToggleButton) view, snippet, longi);
		if(posReminder) positionReminder = "true";
		else			positionReminder = "false";
	}

	// TODO: Finish this
	public void changeTimeStatus(View view) {
		layoutManager.changeTimeStatus((ToggleButton) view);
	}

	// TODO: add time check here
	private void displayAlarmInfo() {
		if (!longi.contains("long")) {
			showAlarmInfo.setVisibility(View.VISIBLE);

			if (positionReminder.contains("true")) layoutManager.setPositionInfo(true, snippet);
			else								   layoutManager.setPositionInfo(false, snippet);
		}
	}

}