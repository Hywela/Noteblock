package com.example.ass2note.notepad;

import java.text.SimpleDateFormat;

import android.content.Context;
import android.database.Cursor;
import android.widget.EditText;


public class NoteEditSavePopulate {
	private Context context;
	private InitiateAlarmButtons initiateAlarmButtons;
	private NotesDbAdapter mDbHelper;
	private Long mRowId;
	private EditText mTitleText, mBodyText;
	private String positionReminder = "false"; 
	private long time = 0;
	private String lati = "lat", longi = "long";
	private String snippet = "";
	
	
	public NoteEditSavePopulate(Context con, NotesDbAdapter dbHelper, 
			Long rowId, InitiateAlarmButtons initButtons) {
		context = con;
		mDbHelper = dbHelper;
		mRowId = rowId;
		initiateAlarmButtons = initButtons;
	}
	
	public void setLayout(EditText titleText, EditText bodyText){
		mTitleText = titleText;
		mBodyText = bodyText;
	}
	
	public String getPositionReminder(){
		return positionReminder;
	}

	public long getTime(){
		return time;
	}
	
	public String getLatitude(){
		return lati;
	}
	
	public String getLongitude(){
		return longi;
	}
	
	public String getSnippet(){
		return snippet;
	}
	
	public void populateFields() {
		if (mRowId != null) {

			Cursor note = mDbHelper.fetchNote(mRowId);
			
			mTitleText.setText(note.getString(note.getColumnIndexOrThrow(NotesDbAdapter.KEY_TITLE)));
			mBodyText.setText(note.getString(note.getColumnIndexOrThrow(NotesDbAdapter.KEY_BODY)));
			
			
			positionReminder = note.getString(note.getColumnIndexOrThrow(NotesDbAdapter.KEY_POSITION_REMINDER));

			time = note.getLong(note.getColumnIndexOrThrow(NotesDbAdapter.KEY_TIME));

			// View the Date set in the format .....
			SimpleDateFormat dateFormat = new SimpleDateFormat(
					"dd-MM-yyyy HH:mm");
			// mydateview.setText(dateFormat.format(temp));

			// Fetch the position values from the database:
			lati = note.getString(note
					.getColumnIndexOrThrow(NotesDbAdapter.KEY_LATI));
			longi = note.getString(note
					.getColumnIndexOrThrow(NotesDbAdapter.KEY_LONG));
			snippet = note.getString(note
					.getColumnIndexOrThrow(NotesDbAdapter.KEY_SNIPPET));
		}
	}
	
	// Saves the values to the database
		public void saveState(String title, String body, String latitude, 
				String longitude, String positionReminder, String snippet) {
			/*String title = mTitleText.getText().toString();
			String body = mBodyText.getText().toString();

			String latitude = lati;
			String longitude = longi;
*/
			if (mRowId == null) {
				long id = mDbHelper.createNote(title, body, initiateAlarmButtons.getDa(), longitude, latitude,
						positionReminder, snippet);
				if (id > 0) {
					mRowId = id;
				}
			} else {
				mDbHelper.updateNote(mRowId, title, body, longitude, latitude,
						positionReminder, snippet);

			}
		}
}
