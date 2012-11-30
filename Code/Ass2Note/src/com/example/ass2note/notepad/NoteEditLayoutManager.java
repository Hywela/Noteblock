package com.example.ass2note.notepad;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.widget.TextView;
import android.widget.ToggleButton;


public class NoteEditLayoutManager{
	private NotesDbAdapter mDbHelper; 
	private Long mRowId;
	private Context context;
	private ToggleButton alarmPosition;
	private TextView alarmPositionInfo;
	
	public NoteEditLayoutManager(Context con, NotesDbAdapter helper, Long rowId) {
		context = con;
		mDbHelper = helper;
		mRowId = rowId;
	}
	
	public void setAlarmPosition(ToggleButton alarmP, TextView alarmPI){
		alarmPosition = alarmP;
		alarmPositionInfo = alarmPI;
	}

	public boolean changePositionBtnStatus(ToggleButton positionToggle, String snippet, String longi) {
		boolean changePosition = positionToggle.isChecked();

		// If changePosition was set to "on":
		if (changePosition) {
			mDbHelper.updatePositionNotification(mRowId, "true");

			// If the note was previously initiated with latitude and longitude:
			if (!longi.contains("long")) {
				setPositionInfo(true, snippet);
				return true;
			}

			// If the note does not have valid latitude and longitude stored:
			else {
				setPositionInfo(false, "");
				createNoMapsAlert();
				//TODO: Check if there should be a return statement here..
			}
		}
		// If the toggle was set to off:
		else {
			mDbHelper.updatePositionNotification(mRowId, "false");
			setPositionInfo(false, snippet);
		}
		return false;
	}

	private void createNoMapsAlert(){
		// Use the Builder class for convenient dialog construction
		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		builder.setMessage("Would you like to be reminded by position?")
				.setPositiveButton("Start Google Maps!",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int id) {
								// FIRE ZE MISSILES!
							}
						})
				.setNegativeButton("Cancel",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int id) {
								// User cancelled the dialog
							}
						});
		// Create the AlertDialog object and return it
		builder.show();
	}
	
	
	// TODO: Finish this
	public void changeTimeStatus(ToggleButton timeToggle) {
		// boolean on = ((ToggleButton) view).isChecked();
		
	}
	
	public void setPositionInfo(boolean checked, String text) {
		alarmPosition.setChecked(checked);
		alarmPositionInfo.setText(text);
	}
	
	public void closeDB(){
		mDbHelper.close();
	}
}
