package ass2note.location;

import java.util.ArrayList;

import android.R;
import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import ass2note.notepad.Notepad;

public class TimeAndDate extends IntentService {
// Variabels
public  ArrayList time;
public ArrayList rowid;	
public TimeAndDate() {
    super("TimeAndDate");
}
	
@Override
protected void onHandleIntent(Intent intent) {
	
	time = intent.getExtras().getIntegerArrayList("Time"); 
	rowid = intent.getExtras().getIntegerArrayList("RowId");
			time("new");
	
}
	
//functions
public void time(String title ) {
		
	ArrayList<Integer> test2 = new ArrayList<Integer>();
//for (int i = 0; i <time.size(); i++){
	String test = time.get(1).toString();
	 
	// 11 / 11
		
	  char nnn = 0;int nn;
	
		nnn = test.charAt(1);
		//String ss = nnn;
		//  test2.add((int)nnn);
		  
	
	//}
		
	//for ( int i=0; i <time.size(); i++){	
	
		//int tim = (Integer) time.get(i);
		//Toast.makeText(getApplicationContext(),   ,	Toast.LENGTH_SHORT).show();
	//}
	
	final int NOTIF_ID = 1234;  
	 NotificationManager notifManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);  
	 Notification note = new Notification(R.drawable.ic_dialog_alert , title, System.currentTimeMillis());  
	 PendingIntent intent = PendingIntent.getActivity(this, 0, new Intent(this, Notepad.class), 0);  
	 note.setLatestEventInfo(this, "New E-mail", "You have one unread message.", intent);  
	 notifManager.notify(NOTIF_ID, note);  
	 // notifManager.cancel(NOTIF_ID); 
	
}





	
	
	
}
