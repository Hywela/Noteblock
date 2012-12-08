package ass2note.alarm;

import java.util.Calendar;


import android.app.Dialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.text.format.DateFormat;
import android.widget.TimePicker;
import ass2note.noteedit.InitiateAlarmButtons;



public class TimePickerFragment extends DialogFragment
implements TimePickerDialog.OnTimeSetListener {
	
	private InitiateAlarmButtons initi;
						
	public TimePickerFragment() {   }	
	
public TimePickerFragment(InitiateAlarmButtons in) { initi = in;}	
	
	
@Override
public Dialog onCreateDialog(Bundle savedInstanceState) {
	if(savedInstanceState != null){initi = savedInstanceState.getParcelable("key");}
// Use the current time as the default values for the picker
 final Calendar c = Calendar.getInstance();
int hour = c.get(Calendar.HOUR_OF_DAY);
int minute = c.get(Calendar.MINUTE);

// Create a new instance of TimePickerDialog and return it
return new TimePickerDialog(getActivity(), this, hour, minute,
DateFormat.is24HourFormat(getActivity()));
}

public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
// Do something with the time chosen by the user
	initi.setTime(hourOfDay, minute);

		}
@Override
public void onSaveInstanceState(Bundle outState) {
	// TODO Auto-generated method stub
	super.onSaveInstanceState(outState);
	
	outState.putParcelable("key", initi);
	
	
}


}