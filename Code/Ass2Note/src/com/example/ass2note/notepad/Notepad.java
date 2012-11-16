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


import java.security.acl.LastOwnerException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.TimeZone;

import com.example.ass2note.R;
import com.example.ass2note.R.id;
import com.example.ass2note.R.layout;
import com.example.ass2note.R.string;
import com.example.ass2note.location.TimeAndDate;
import com.example.ass2note.location.UseGps;


import android.app.AlarmManager;
import android.app.Dialog;
import android.app.ListActivity;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.ParseException;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.text.format.Time;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Toast;
import android.widget.AdapterView.AdapterContextMenuInfo;

public class Notepad extends ListActivity {
	private PendingIntent pendingIntent;	
    private static final int ACTIVITY_CREATE=0;
    private static final int ACTIVITY_EDIT=1;
    private static final int ACTIVITY_GPS=2;
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
        setContentView(R.layout.noteblock_activity);
        mDbHelper = new NotesDbAdapter(this);
        mDbHelper.open();
        fillData();
        registerForContextMenu(getListView());
        onButtonClick();
       
      
      
    }
    private long time() {
        Cursor notesCursor = mDbHelper.fetchAllNotes();
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm");
        
    	Date dd = new Date();
        long dat = dd.getTime();
        
        while (notesCursor.moveToNext()) 
        {
        	long sjekkDate =  notesCursor.getLong( notesCursor.getColumnIndexOrThrow(NotesDbAdapter.KEY_TIME));
     
            if(sjekkDate >= dat){
            	dat = sjekkDate;

            }// - End if()
            
        } //- End while()
        
       // Toast.makeText(this, dateFormat.format(dat) , Toast.LENGTH_SHORT).show();
     return dat;
    		 
  
    }// -End time();

    
    private void fillData() {
        Cursor notesCursor = mDbHelper.fetchAllNotes();
        startManagingCursor(notesCursor);
        	
        
        
        // SAVES TIME INTO AN ARRAY LIST
      
        // Create an array to specify the fields we want to display in the list (only TITLE)
        String[] from = new String[]{NotesDbAdapter.KEY_TITLE};
     
        // and an array of the fields we want to bind those fields to (in this case just text1)
        int[] to = new int[]{R.id.text1};
        
        // Now create a simple cursor adapter and set it to display
        SimpleCursorAdapter notes = 
            new SimpleCursorAdapter(this, R.layout.list_row, notesCursor, from, to);
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
        switch(item.getItemId()) {
            case INSERT_ID:
                createNote();
                
                
            case INSERT_GPS:
            	// TEST CASES
            	setRecurringAlarm(Notepad.this);
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
        switch(item.getItemId()) {
            case DELETE_ID:
                AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
                mDbHelper.deleteNote(info.id);
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
        Intent i = new Intent(this, NoteEdit.class);
        i.putExtra(NotesDbAdapter.KEY_ROWID, id);
        startActivityForResult(i, ACTIVITY_EDIT);
    
        
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
       
        
        
        if (resultCode == RESULT_OK && requestCode == ACTIVITY_GPS) {
            if (intent.hasExtra("longitud")) {
            longi = intent.getExtras().getDouble("longitude");}
            if (intent.hasExtra("latitude")) {
                lati = intent.getExtras().getDouble("latitude");}
   
            } else
            	  	fillData();
    }
    
    protected void onButtonClick(){
    	Button onButtonClick = (Button) findViewById(R.id.button_new_note);
    	onButtonClick.setOnClickListener(new View.OnClickListener(){
    		public void onClick(View v){
    			createNote();
    		}
    	});
    }
    protected void findGps(){
    	 Intent i = new Intent(this, UseGps.class);
    	 i.putExtra("Value1", lati);
    	 i.putExtra("Value2", longi);
    	 startActivityForResult(i, ACTIVITY_GPS);
        
    }

  
    private void setRecurringAlarm(Context context) {
        // we know mobiletuts updates at right around 1130 GMT.
        // let's grab new stuff at around 11:45 GMT, inexactly
    	Calendar calendar = Calendar.getInstance();

        calendar.setTimeInMillis(time());

        
        Intent myIntent = new Intent(this, AlarmReceiver.class);
        
        PendingIntent pendingintent = PendingIntent.getBroadcast(context, 0, myIntent , 0);
     
        /*AlarmManager alarms = (AlarmManager) context.getSystemService(
                Context.ALARM_SERVICE);
        alarms.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingintent);*/
       pendingintent.cancel();
       
    }
    
    
 
    
 
    
    
}
