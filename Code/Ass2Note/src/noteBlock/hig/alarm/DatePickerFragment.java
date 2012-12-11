package noteBlock.hig.alarm;

import java.util.Calendar;

import noteBlock.hig.noteedit.InitiateAlarmButtons;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.widget.DatePicker;




public class DatePickerFragment  extends DialogFragment
				implements DatePickerDialog.OnDateSetListener 	{
	
	private InitiateAlarmButtons initi;
	public DatePickerFragment() {}
	
	public DatePickerFragment(InitiateAlarmButtons in) { initi = in;}	

@Override
public Dialog onCreateDialog(Bundle savedInstanceState) {
// Use the current date as the default date in the picker
	if(savedInstanceState != null){initi = savedInstanceState.getParcelable("key");} 
	
final Calendar	 c = Calendar.getInstance();
	int year = c.get(Calendar.YEAR);
	int month = c.get(Calendar.MONTH);
	int day = c.get(Calendar.DAY_OF_MONTH);

// Create a new instance of DatePickerDialog and return it
	 return new DatePickerDialog(getActivity(), this, year, month, day);
}

public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
	// TODO Auto-generated method stub
	initi.setDate(year, monthOfYear, dayOfMonth);
}
@Override
public void onSaveInstanceState(Bundle outState) {
	// TODO Auto-generated method stub
	super.onSaveInstanceState(outState);
	
	outState.putParcelable("key", initi);
	
	
}



}