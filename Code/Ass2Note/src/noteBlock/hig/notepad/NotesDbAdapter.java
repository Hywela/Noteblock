/**
 * The NotesDbAdapter is the database that creat, close manipulate the notes
 * 
 */

package noteBlock.hig.notepad;


import java.text.SimpleDateFormat;
import java.util.Date;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Simple notes database access helper class. Defines the basic CRUD operations
 */
public class NotesDbAdapter {
	public static final String KEY_TEST = "date_created"; // curently not used 
    public static final String KEY_TITLE = "title";
    public static final String KEY_BODY = "body";
    public static final String KEY_TIME = "time";
    public static final String KEY_LATI = "latitude";
    public static final String KEY_LONG = "longitude";
    public static final String KEY_POSITION_REMINDER = "positionReminder";// true or false
    public static final String KEY_SNIPPET = "snippet";
    public static final String KEY_TIME_REMINDER = "timeReminder";	// true or false
    
    public static final String KEY_ROWID = "_id";
 
    private static final String TAG = "NotesDbAdapter";
    private DatabaseHelper mDbHelper;
    private SQLiteDatabase mDb;

    /**
     * Database creation sql statement
     */	
    private static final String DATABASE_CREATE =
        "create table notes (_id integer primary key autoincrement, date_created TIMESTAMP NOT NULL DEFAULT current_timestamp, "
        + "title text not null, body text not null," 
        		+ " time INTEGER not null, latitude text not null, longitude text not null, " 
        		+ " positionReminder text not null, snippet text not null, "
        		+ " timeReminder text not null);" ;

    private static final String DATABASE_NAME = "data";
    private static final String DATABASE_TABLE = "notes";
   
    private static final int DATABASE_VERSION = 2;

    private final Context mCtx;

    private static class DatabaseHelper extends SQLiteOpenHelper {

        DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {

            db.execSQL(DATABASE_CREATE);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
                    + newVersion + ", which will destroy all old data");
            db.execSQL("DROP TABLE IF EXISTS notes" + "datetime('now')");
            onCreate(db);
        }
    }

    /**
     * Constructor - takes the context to allow the database to be
     * opened/created
     * 
     * @param ctx the Context within which to work
     */
    public NotesDbAdapter(Context ctx) {
        this.mCtx = ctx;
    }

    /**
     * Open the notes database. If it cannot be opened, try to create a new
     * instance of the database. If it cannot be created, throw an exception to
     * signal the failure
     * 
     * @return this (self reference, allowing this to be chained in an
     *         initialization call)
     * @throws SQLException if the database could be neither opened or created
     */
    public NotesDbAdapter open() throws SQLException {
        mDbHelper = new DatabaseHelper(mCtx);
        mDb = mDbHelper.getWritableDatabase();
        return this;
    }

    public void close() {
        mDbHelper.close();
    }


    /**
     * Create a new note using the title and body provided. If the note is
     * successfully created return the new rowId for that note, otherwise return
     * a -1 to indicate failure.
     * 
     * @param title the title of the note
     * @param body the body of the note
     * @param time input in the note
     * @param latitude input in the note
     * @param longitude input in the note
     * @param postion reminder on  or off 
     * @param postion reminder on  or off
     * @param snippet
     * @return rowId or -1 if failed
     */		// Puts the intial values into the database
    public long createNote(String title, String body,  long time, 
    		String longitude, String latitude, String positionReminder, 
    		String snippet, String timeReminder) {
        ContentValues initialValues = new ContentValues();
        
        initialValues.put(KEY_TITLE, title);
        initialValues.put(KEY_BODY, body);
        initialValues.put(KEY_TIME, time);
        initialValues.put(KEY_LATI,latitude);
        initialValues.put(KEY_LONG,longitude);
        initialValues.put(KEY_POSITION_REMINDER,positionReminder);
        initialValues.put(KEY_TIME_REMINDER, timeReminder);
        initialValues.put(KEY_SNIPPET, snippet);
        return mDb.insert(DATABASE_TABLE, null, initialValues);
    }

    /**
     * Delete the note with the given rowId
     * 
     * @param rowId id of note to delete
     * @return true if deleted, false otherwise
     */
    public boolean deleteNote(long rowId) {

        return mDb.delete(DATABASE_TABLE, KEY_ROWID + "=" + rowId, null) > 0;
    }

    /**
     * Return a Cursor over the list of all notes in the database
     * 
     * @return Cursor over all notes
     */
    public Cursor fetchAllNotes() {

        return mDb.query(DATABASE_TABLE, new String[] {KEY_ROWID, 
        		KEY_TITLE, KEY_BODY, KEY_TIME , KEY_LATI, KEY_LONG, 
        		KEY_POSITION_REMINDER, KEY_SNIPPET, KEY_TIME_REMINDER}, null, null, null, null, null, null);
    }
   
    /**
     * Return a Cursor positioned at the note that matches the given rowId
     * 
     * @param rowId id of note to retrieve
     * @return Cursor positioned to matching note, if found
     * @throws SQLException if note could not be found/retrieved
     */
    
	public Cursor fetchNote(long rowId) throws SQLException {

        Cursor mCursor =

            mDb.query(true, DATABASE_TABLE, new String[] {KEY_ROWID, KEY_TITLE, KEY_BODY,  KEY_TIME ,KEY_LATI , KEY_LONG , 
                    KEY_POSITION_REMINDER, KEY_SNIPPET, KEY_TIME_REMINDER
                    }, KEY_ROWID + "=" + rowId, null,
                    null, null, null, null );	
        if (mCursor != null) {
            mCursor.moveToFirst();
        }
        return mCursor;
        
    }

   
    /**
     * Update the note using the details provided. The note to be updated is
     * specified using the rowId, and it is altered to use the title and body
     * values passed in
     * 
     * @param rowId id of note to update
     * @param title value to set note title to
     * @param body value to set note body to
     * etc...
     * @return true if the note was successfully updated, false otherwise
     */				// Updates the note with the values
    public boolean updateNote(long rowId, String title, String body, long time,
    		String longitude, String latitude, String positionReminder,
    		String snippet, String timeReminder) {
        ContentValues args = new ContentValues();
      SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    // if (time != null) { args.put(KEY_TIME, dateFormat.format(time));}
      	
      	args.put(KEY_TITLE, title);
        args.put(KEY_BODY, body); 
        args.put(KEY_TIME, time);
        args.put(KEY_LATI, latitude);
        args.put(KEY_LONG,longitude );
        args.put(KEY_POSITION_REMINDER, positionReminder);
        args.put(KEY_SNIPPET, snippet);
        args.put(KEY_TIME_REMINDER, timeReminder);
        return mDb.update(DATABASE_TABLE, args, KEY_ROWID + "=" + rowId, null) > 0;
    }
    
    // Updates time
    public boolean updateTime(long rowId, long time, String timeReminder) {
        ContentValues args = new ContentValues();
        args.put(KEY_TIME, time);
        args.put(KEY_TIME_REMINDER, timeReminder);

		Log.i("NoteDBAD", "updateTime: timereminder is: "+ timeReminder);
		
        return mDb.update(DATABASE_TABLE, args, KEY_ROWID + "=" + rowId, null) > 0;
    }
    // Updates the postion and sets the reminder on or off
    public boolean updatePositionNotification(long rowId, String positionNotification){
    	ContentValues args = new ContentValues();
    	args.put(KEY_POSITION_REMINDER, positionNotification);
    	return mDb.update(DATABASE_TABLE, args, KEY_ROWID + "=" + rowId, null) > 0;
    }
    // Set the reminder on or off
    public boolean updateTimeNotification(long rowId, String timeNotification){
    	ContentValues args = new ContentValues();
    	args.put(KEY_TIME_REMINDER, timeNotification);
    	return mDb.update(DATABASE_TABLE, args, KEY_ROWID + "=" + rowId, null) > 0;
    }
    // gets the closest time to the current date
    public long getClosestTime()[] {
		Cursor notesCursor = fetchAllNotes();
		// SimpleDateFormat dateFormat = new
		// SimpleDateFormat("dd-MM-yyyy HH:mm");

		Date date = new Date();
		long now = date.getTime();
		long closestTime = 0;
		long timeId = 0;
		
		while (notesCursor.moveToNext()) {
			long timeInDb = notesCursor.getLong(notesCursor
					.getColumnIndexOrThrow(NotesDbAdapter.KEY_TIME));
			String timReminder = notesCursor.getString(notesCursor
					.getColumnIndexOrThrow(NotesDbAdapter.KEY_TIME_REMINDER));

			if ((timeInDb >= now && timeInDb <= closestTime && timReminder.contains("true"))
					|| (closestTime == 0 && timReminder.contains("true"))){
				closestTime = timeInDb;
				timeId = notesCursor.getLong(notesCursor.getColumnIndexOrThrow
						(NotesDbAdapter.KEY_ROWID));
			}

			SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm");
			Log.i("NoteEdit", "getclosestTime: time is: " + dateFormat.format(timeInDb));
		} // - End while()
		
		long clTime[] = {closestTime, timeId};
		
		return clTime;
	}// -End time();
}
