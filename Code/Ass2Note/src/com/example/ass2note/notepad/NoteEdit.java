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

import android.os.SystemClock;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.example.ass2note.R;
import com.example.ass2note.location.GoogleMapsActivity;

public class NoteEdit extends Activity {
	private static final int MAPSINTENT_ID = 1;
	private TextView mydateview;
	private TextView mytimeview;
	private EditText mTitleText;
	private EditText mBodyText;
	private ArrayList time = new ArrayList();
	private String lati = "lat";
	private String longi = "long";
	private String positionReminder = "false";
	private Long mRowId;
	private NotesDbAdapter mDbHelper;
	long da;
	

	Calendar myCalendar = Calendar.getInstance();

	DatePickerDialog.OnDateSetListener d = new DatePickerDialog.OnDateSetListener() {
		public void onDateSet(DatePicker view, int year, int monthOfYear,
				int dayOfMonth) {
			myCalendar.set(Calendar.YEAR, year);
			myCalendar.set(Calendar.MONTH, monthOfYear);
			myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

			new TimePickerDialog(NoteEdit.this, t,
					myCalendar.get(Calendar.HOUR_OF_DAY),
					myCalendar.get(Calendar.MINUTE), true).show();
		}
	};

	TimePickerDialog.OnTimeSetListener t = new TimePickerDialog.OnTimeSetListener() {
		public void onTimeSet(TimePicker view, int hourOfDay, int minute) {

			myCalendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
			myCalendar.set(Calendar.MINUTE, minute);
			Date dat = myCalendar.getTime();
			SimpleDateFormat ss = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

			da = myCalendar.getTimeInMillis();

			updateTime(da);

		}

	};

	private void updateTime(long da) {
		// SimpleDateFormat ss = new SimpleDateFormat("YYYY-MM-DD HH:mm:SS");
		SimpleDateFormat dateFormat = new SimpleDateFormat(
				"dd-MM-yyyy HH:mm:ss");

		mydateview.setText(dateFormat.format(da));
		saveTime(da);

	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mDbHelper = new NotesDbAdapter(this);
		mDbHelper.open();

		setContentView(R.layout.note_edit);
		setTitle(R.string.edit_note);
		da = myCalendar.getTimeInMillis();
		mTitleText = (EditText) findViewById(R.id.title);
		mBodyText = (EditText) findViewById(R.id.body);
		mydateview = (TextView) findViewById(R.id.date);
		mytimeview = (TextView) findViewById(R.id.time);

		Button datebutton = (Button) findViewById(R.id.dateButton);
		Button timebutton = (Button) findViewById(R.id.timeButton);
		Button confirmButton = (Button) findViewById(R.id.confirm);

		mRowId = (savedInstanceState == null) ? null : (Long) 
				savedInstanceState.getSerializable(NotesDbAdapter.KEY_ROWID);
		if (mRowId == null) {
			Bundle extras = getIntent().getExtras();
			mRowId = extras != null ? extras.getLong(NotesDbAdapter.KEY_ROWID)
					: null;
		}

		populateFields();

		// TO SELF MAKE TEXT FIELDS EDDITABLE SO WHEN CLICKED DATE AND TIME
		// INPUT SHOULD BE TRIGGERED
		// TOOO BE DELETED
		// !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
		// or chanced
		timebutton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				new TimePickerDialog(NoteEdit.this, t, myCalendar
						.get(Calendar.HOUR_OF_DAY), myCalendar
						.get(Calendar.MINUTE), true).show();

			}
		});
		// ON click DATE
		datebutton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				new DatePickerDialog(NoteEdit.this, d, myCalendar
						.get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
						myCalendar.get(Calendar.DAY_OF_MONTH)).show();
			}
		});

		confirmButton.setOnClickListener(new View.OnClickListener() {
			// ON click CONFIRM
			public void onClick(View view) {
				setResult(RESULT_OK);
				finish();
			}
		});

	}

	// Gets the values from the database
	private void populateFields() {
		if (mRowId != null) {

			Cursor note = mDbHelper.fetchNote(mRowId);
			startManagingCursor(note);
			mTitleText.setText(note.getString(note
					.getColumnIndexOrThrow(NotesDbAdapter.KEY_TITLE)));
			mBodyText.setText(note.getString(note
					.getColumnIndexOrThrow(NotesDbAdapter.KEY_BODY)));
			positionReminder = note
					.getString(note
							.getColumnIndexOrThrow(NotesDbAdapter.KEY_POSITION_REMINDER));

			// Toast.makeText(this, note.getString(
			// note.getColumnIndexOrThrow(NotesDbAdapter.KEY_TEST)) ,
			// Toast.LENGTH_SHORT).show();

			long temp = note.getLong(note
					.getColumnIndexOrThrow(NotesDbAdapter.KEY_TIME));

			// View the Date set in the format .....
			SimpleDateFormat dateFormat = new SimpleDateFormat(
					"dd-MM-yyyy HH:mm");
			mydateview.setText(dateFormat.format(temp));

			// Fetch the position values from the database:
			lati = note.getString(note
					.getColumnIndexOrThrow(NotesDbAdapter.KEY_LATI));
			longi = note.getString(note
					.getColumnIndexOrThrow(NotesDbAdapter.KEY_LONG));
		}
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		saveState();
		outState.putSerializable(NotesDbAdapter.KEY_ROWID, mRowId);
	}

	@Override
	protected void onPause() {
		super.onPause();
		saveState();
	}

	@Override
	protected void onResume() {
		super.onResume();
		populateFields();
	}

	private void saveTime(long time) {
		if (mRowId == null) {} 
		else mDbHelper.updateTime(mRowId, time);
	}

	// Saves the values to the database
	private void saveState() {
		String title = mTitleText.getText().toString();
		String body = mBodyText.getText().toString();

		String latitude = lati;
		String longitude = longi;

		if (mRowId == null) {
			long id = mDbHelper.createNote(title, body,
					da , longitude, latitude,
					positionReminder);
			if (id > 0) {
				mRowId = id;
			}
		} else {
			mDbHelper.updateNote(mRowId, title, body, longitude, latitude,
					positionReminder);

		}
	}

	/**
	 * Method for starting GoogleMapsActivity. The latitude and longitude 
	 * values in the database for this note is sent to the activity, and the
	 * activity is started with startActivityForResult.
	 * @param view
	 */
	public void startGoogleMaps(View view) {
		Intent i = new Intent(NoteEdit.this, GoogleMapsActivity.class);
		i.putExtra("LATITUDE", lati);
		i.putExtra("LONGITUDE", longi);
		startActivityForResult(i, MAPSINTENT_ID);
	}

	/**
	 * Function. Receives information from GoogleMapsActivity when the activity
	 * finishes. Both latitude and longitude from the preferred position from 
	 * the user is available here.
	 */
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		Log.i("onActivityResult", "Result fetched");
		
		// The result came from GoogleMapsActivity:
		if (requestCode == MAPSINTENT_ID)
			switch (resultCode) {
			// A new location was selected:
			case Activity.RESULT_OK: {
				// Fetch the new data:
				lati = data.getStringExtra("latitude");
				longi = data.getStringExtra("longitude");
				positionReminder = "true";
				
				// Save the data in the database.
				saveState();
				break;
			}
			// Unexpected occurrence happened or no new location was selected: 
			case Activity.RESULT_CANCELED: {
				break;
			}
			} // end switch
	} // end onActivityResult
	
}