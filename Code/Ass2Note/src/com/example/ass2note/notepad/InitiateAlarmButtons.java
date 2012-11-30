package com.example.ass2note.notepad;

import java.util.Calendar;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.widget.DatePicker;
import android.widget.ImageButton;
import android.widget.TimePicker;

import com.example.ass2note.R;
import com.example.ass2note.location.ConnectionService;

public class InitiateAlarmButtons {
	private Context context;
	private Calendar myCalendar = Calendar.getInstance();
	private DatePickerDialog.OnDateSetListener d;
	private TimePickerDialog.OnTimeSetListener t;
	private long da = 0;

	
	public InitiateAlarmButtons(Context cont) {
		context = cont;
		da = myCalendar.getTimeInMillis();
		initiateTimePickerDialog();
	}

	public void initiateTimePickerDialog() {

		d = new DatePickerDialog.OnDateSetListener() {
			boolean dateDialogFired = false;

			public void onDateSet(DatePicker view, int year, int monthOfYear,
					int dayOfMonth) {
				if (dateDialogFired == false) {
					dateDialogFired = true;
					Log.i("test", "setting date");
					myCalendar.set(Calendar.YEAR, year);
					myCalendar.set(Calendar.MONTH, monthOfYear);
					myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

					new TimePickerDialog(context, t,
							myCalendar.get(Calendar.HOUR_OF_DAY),
							myCalendar.get(Calendar.MINUTE), true).show();
				}
			}
		};

		t = new TimePickerDialog.OnTimeSetListener() {
			boolean timeDialogFired = false;

			public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
				if (timeDialogFired == false) {
					timeDialogFired = true;
					Log.i("test", "setting time");

					myCalendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
					myCalendar.set(Calendar.MINUTE, minute);
					da = myCalendar.getTimeInMillis();

					updateTimeInDb();
				} // end if
			} // end onTimeSet
		};
		
	}

	public void initiateAlarmButtonDialog() {
		final Dialog dialog = new Dialog(context);
		// Set the dialog title
		dialog.setTitle(R.string.alarm);
		// dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		dialog.setContentView(R.layout.alert_dialog);
		dialog.show();

		ImageButton positionButton = (ImageButton) dialog
				.findViewById(R.id.positionButton);
		ImageButton timeButton = (ImageButton) dialog
				.findViewById(R.id.timeButton);

		timeButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				System.out.println("pressed timebutton");
				new DatePickerDialog(context, d, myCalendar.get(Calendar.YEAR),
						myCalendar.get(Calendar.MONTH), myCalendar
								.get(Calendar.DAY_OF_MONTH)).show();

				dialog.dismiss();
			}
		});

		positionButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				System.out.println("pressed positionbutton");
				
				Intent intent = new Intent(context, ConnectionService.class);
				intent.putExtra("fromActivity", "NoteEdit");
				context.startService(intent);
				
				dialog.dismiss();
			}
		});
	}

	public long getDa() {
		return da;
	}

	private void updateTimeInDb() {
		Intent i = new Intent(
				"com.example.ass2note.notepad.NoteEdit.connectionReceiver");
		i.putExtra("fromCaller", "test");
		i.putExtra("command", "updateTime");
		i.putExtra("time", da);
		context.sendBroadcast(i);
	}

}
