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
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import android.app.ListActivity;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Toast;

import com.example.ass2note.R;
import com.example.ass2note.alarm.AlarmManagerService;
import com.example.ass2note.alarm.AlarmReceiver;

public class Notepad extends ListActivity {
	private PendingIntent pendingIntent;
	private static final int ACTIVITY_CREATE = 0;
	private static final int ACTIVITY_EDIT = 1;
	private static final int ACTIVITY_GPS = 2;
	private static final int INSERT_ID = Menu.FIRST;
	private static final int INSERT_GPS = Menu.CATEGORY_SECONDARY;
	private static final int DELETE_ID = Menu.FIRST + 1;
	private ArrayList times = new ArrayList();
	private ArrayList rowid = new ArrayList();
	private NotesDbAdapter mDbHelper;

	int aa = 0;
	double lati = 0;
	double longi = 0;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Log.i("Notepad", " oncreated");
		setContentView(R.layout.noteblock_activity);
		mDbHelper = new NotesDbAdapter(this);
		mDbHelper.open();
		fillData();
		registerForContextMenu(getListView());
		onButtonClick();

		// If the user pressed the notification-panel, display the proper note:
		Intent u = getIntent();
		if (u.hasExtra("notificationSuccess")) {
			Log.i("Notepad", "Special intent notificationSuccess");
			Long key = Long.parseLong(u.getStringExtra("notificationSuccess"));
			startNoteEdit(key);
		}
	}

	/**
	 * Method for starting a note in NoteEdit. The noteId will be sent along
	 * and it starts NoteEdit with startActivityForResult.
	 * @param key
	 */
	private void startNoteEdit(Long key) {
		Intent i = new Intent(this, NoteEdit.class);
		i.putExtra(NotesDbAdapter.KEY_ROWID, key);
		startActivityForResult(i, ACTIVITY_EDIT);
	}

	private long time() {
		Cursor notesCursor = mDbHelper.fetchAllNotes();
		SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm");


    	Date dd = new Date();
        long dat = dd.getTime();
        long temp = 0;
        while (notesCursor.moveToNext()) 
        {
        	long sjekkDate =  notesCursor.getLong( notesCursor.getColumnIndexOrThrow(NotesDbAdapter.KEY_TIME));
    
            if(sjekkDate >= dat){
            	
            	
            	if ( sjekkDate <= temp)
            	temp = sjekkDate;
            	
            	
            }
            if(temp == 0) {
            		temp = sjekkDate;}
            
            
        } //- End while()
        
        Toast.makeText(this, dateFormat.format(temp) , Toast.LENGTH_SHORT).show();
     return temp;

	}// -End time();

	private void fillData() {
		Cursor notesCursor = mDbHelper.fetchAllNotes();
		startManagingCursor(notesCursor);

		// SAVES TIME INTO AN ARRAY LIST

		// Create an array to specify the fields we want to display in the list
		// (only TITLE)
		String[] from = new String[] { NotesDbAdapter.KEY_TITLE };

		// and an array of the fields we want to bind those fields to (in this
		// case just text1)
		int[] to = new int[] { R.id.text1 };

		// Now create a simple cursor adapter and set it to display
		SimpleCursorAdapter notes = new SimpleCursorAdapter(this,
				R.layout.list_row, notesCursor, from, to);
		setListAdapter(notes);

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		menu.add(0, INSERT_ID, 0, R.string.menu_insert);
		menu.add(0, INSERT_GPS, 0, R.string.menu_gps);
		return true;
	}

	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		switch (item.getItemId()) {
		case INSERT_ID:
			createNote();

		case INSERT_GPS:
			// TEST CASES
			startTimeAlarm();
			return true;
		}
		return super.onMenuItemSelected(featureId, item);
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		menu.add(0, DELETE_ID, 0, R.string.menu_delete);
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case DELETE_ID:
			AdapterContextMenuInfo info = (AdapterContextMenuInfo) item
					.getMenuInfo();
			mDbHelper.deleteNote(info.id);
			cancelNotification(info.id); // If the note is still on the panel,
											// remove it.

			// If no more valid notes exists in the DB, stop the alarm:
			if (!validNotes()) {
				Intent i = new Intent(Notepad.this, AlarmManagerService.class);
				i.putExtra("alarmType", "noteDeleted");
				i.putExtra("COMMAND", "Stop Alarm");
				startService(i);
			}

			fillData();
			return true;
		}
		return super.onContextItemSelected(item);
	}

	private void createNote() {
		Intent i = new Intent(this, NoteEdit.class);
		startActivityForResult(i, ACTIVITY_CREATE);
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);

		startNoteEdit(id);

	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
		super.onActivityResult(requestCode, resultCode, intent);
		Log.i("Notepad", "onActivityresult");
		
		// TODO: This can be called elsewhere.
		startLocationAlarm();

		if (resultCode == RESULT_OK && requestCode == ACTIVITY_GPS) {
			if (intent.hasExtra("longitud")) {
				longi = intent.getExtras().getDouble("longitude");
			}
			if (intent.hasExtra("latitude")) {
				lati = intent.getExtras().getDouble("latitude");
			}

		} else
			fillData();
	}

	protected void onButtonClick() {
		Button onButtonClick = (Button) findViewById(R.id.button_new_note);
		onButtonClick.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				createNote();
			}
		});
	}

	private void startTimeAlarm() {
		long closestTime = time();
		Intent i = new Intent(Notepad.this, AlarmManagerService.class);
		i.putExtra("alarmType", "time");
		i.putExtra("time", closestTime);
		i.putExtra("COMMAND", "Start Alarm");
		startService(i);
	}

	/**
	 * Method for starting AlarmManagerService. The service will only be 
	 * started if there exist at least one valid note in the DB.
	 */
	public void startLocationAlarm() {
		Log.i("Notepad", "starting AlarmManagerservice");
		// TODO: Rename validNotes..
		// If there exist at least one valid note in the database:
		if (validNotes()) {
			Intent i = new Intent(Notepad.this, AlarmManagerService.class);
			i.putExtra("alarmType", "position");
			i.putExtra("COMMAND", "Start Alarm");
			startService(i);
		} else {
			Log.i("Notepad", "startAlarmManagerService: No valid notes");
		}
	}

	/**
	 * Method for deciding if the database contain valid notes, or notes that
	 * can be notified.
	 * @return true if there exist at least one valid note in the database, and
	 * false otherwise.
	 */
	private boolean validNotes() {
		// Fetch all notes from the Database.
		Cursor allNotes = mDbHelper.fetchAllNotes();

		// If at least one note is stored in the DB:
		if (allNotes != null) {
			Log.i("Notepad", "validNotes: Maybe valid notes:");
			// Create a list that will contain all positionNotifications:
			ArrayList<String> validNotes = new ArrayList<String>();

			// Add all positionNotification to the list.
			while (allNotes.moveToNext())
				validNotes.add(allNotes.getString(7));

			// If a valid note exist in the list (a note that can be notified):
			if (validNotes.contains("true"))
				return true;
		}
		Log.i("Notepad", "validNotes: no valid notes");
		return false;
	}

	/**
	 * Method for canceling the notification of a note, from the panel. It is
	 * used if the notification has not been displayed before the note is
	 * deleted or removed by other means.
	 * @param id
	 */
	private void cancelNotification(Long id) {
		Log.i("Notepad", "cancelNotification: removing notification from panel");
		int noteId = Integer.parseInt(String.valueOf(id));
		NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		mNotificationManager.cancel(noteId);
	}

}
