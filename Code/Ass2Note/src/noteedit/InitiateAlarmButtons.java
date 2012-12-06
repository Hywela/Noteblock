package noteedit;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.DatePicker;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.example.ass2note.R;
import com.example.ass2note.location.ConnectionService;

public class InitiateAlarmButtons {
	private NoteEdit noteEdit;
	private NoteEditLayoutManager layoutManager;
	private Calendar myCalendar = Calendar.getInstance();
	private long da = 0;
	private int timesCalledDate = 1, timesCalledTime = 1;
	private ToggleButton alarmPosition, alarmTime, showAlarmInfo;
	

	public InitiateAlarmButtons(Context cont, NoteEditLayoutManager layoutM) {
		noteEdit = (NoteEdit) cont;
		da = myCalendar.getTimeInMillis();
		layoutManager = layoutM;
		
		initiateAlarmButtons();	
	}

	private void initiateAlarmButtons(){
		alarmPosition = (ToggleButton) noteEdit.findViewById(R.id.toggleAlarmPosition);
		alarmTime = (ToggleButton) noteEdit.findViewById(R.id.toggleAlarmTime);
		showAlarmInfo = (ToggleButton) noteEdit.findViewById(R.id.showAlarmNoteInfo);
		
		alarmPosition.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				layoutManager.changePositionBtnStatus();
			}
		});
		
		alarmTime.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				layoutManager.changeTimeStatus();
			}
		});
		
		showAlarmInfo.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				layoutManager.showAlarmLayout();	
			}
		});
	}

	DatePickerDialog.OnDateSetListener d = new DatePickerDialog.OnDateSetListener() {
		public void onDateSet(DatePicker view, int year, int monthOfYear,
				int dayOfMonth) {
			timesCalledDate++;

			if ((timesCalledDate % 2) == 0) {
				myCalendar.set(Calendar.YEAR, year);
				myCalendar.set(Calendar.MONTH, monthOfYear);
				myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

				System.out.println("year2: " + year);
				showNewTimePickerDialog();
			}
		}
	};

	TimePickerDialog.OnTimeSetListener t = new TimePickerDialog.OnTimeSetListener() {
		public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
			timesCalledTime++;
			if ((timesCalledTime % 2) == 0) {

				myCalendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
				myCalendar.set(Calendar.MINUTE, minute);
				da = myCalendar.getTimeInMillis();

				Date date = new Date();
				long now = date.getTime();
				if(da > now) updateTime();
				else {
					Toast.makeText(noteEdit, "Illegal time", Toast.LENGTH_SHORT).show();
					showNewTimePickerDialog();
				}
			} // end if
		} // end onTimeSet
	};
	
	public void initiateAlarmButtonDialog() {
		final Dialog dialog = new Dialog(noteEdit);
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
				new DatePickerDialog(noteEdit, d, myCalendar.get(Calendar.YEAR),
						myCalendar.get(Calendar.MONTH), myCalendar
								.get(Calendar.DAY_OF_MONTH)).show();

				
				dialog.dismiss();
			}
		});

		positionButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				Intent intent = new Intent(noteEdit, ConnectionService.class);
				intent.putExtra("fromActivity", "NoteEdit");
				noteEdit.startService(intent);

				dialog.dismiss();
			}
		});
	}

	public long getDa() {
		return da;
	}

	private void updateTime() {
		Intent i = new Intent("com.example.ass2note.notepad.NoteEdit.connectionReceiver");
		i.putExtra("fromCaller", "InitiateAlarmButtons");
		i.putExtra("command", "updateTime");
		i.putExtra("time", da);
		noteEdit.sendBroadcast(i);
	}

	private void showNewTimePickerDialog(){
		new TimePickerDialog(noteEdit, t,
				myCalendar.get(Calendar.HOUR_OF_DAY),
				myCalendar.get(Calendar.MINUTE), true).show();
	}
}
