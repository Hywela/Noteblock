package noteBlock.hig.alarm;

import java.util.Calendar;

import noteBlock.hig.noteedit.InitiateAlarmButtons;

import android.app.Dialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.text.format.DateFormat;
import android.widget.TimePicker;

/**
 * The Time fragment
 * 
 * @author Kristoffer Benum, Solveig Sørheim
 * 
 */
public class TimePickerFragment extends DialogFragment implements
		TimePickerDialog.OnTimeSetListener {

	private InitiateAlarmButtons initi;
	
	
	public TimePickerFragment() { // if this is used somethings wrong
	}
	/**
	 * Constructor the onlye one that should be used
	 * @param in
	 */
	public TimePickerFragment(InitiateAlarmButtons in) {
		initi = in;
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		if (savedInstanceState != null) {
			initi = savedInstanceState.getParcelable("key");
		}
		// Use the current time as the default values for the picker
		final Calendar c = Calendar.getInstance();
		int hour = c.get(Calendar.HOUR_OF_DAY);
		int minute = c.get(Calendar.MINUTE);

		// Create a new instance of TimePickerDialog and return it
		return new TimePickerDialog(getActivity(), this, hour, minute,
				DateFormat.is24HourFormat(getActivity()));
	}
	// Sets the time after the time is set in the picker
	public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
		// Do something with the time chosen by the user
		initi.setTime(hourOfDay, minute);

	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		// TODO Auto-generated method stub
		super.onSaveInstanceState(outState);
		/**
		 *  saves the in object from InitiateAlarmButtons that gets it from NotEdit 
		 */
		outState.putParcelable("key", initi);

	}

}