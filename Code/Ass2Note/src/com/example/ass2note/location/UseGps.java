package com.example.ass2note.location;

import com.example.ass2note.R;
import com.example.ass2note.notepad.Notepad;

import android.app.Activity;

import android.content.Context;
import android.content.Intent;

import android.location.Location;

import android.location.LocationListener;

import android.location.LocationManager;

import android.os.Bundle;

import android.util.Log;
import android.widget.Toast;

public class UseGps extends Activity
	{
	double longi =0;
	double lati =0;
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState)
		{
		super.onCreate(savedInstanceState);
		
		// setContentView(R.layout.activity_main);
		/* Use the LocationManager class to obtain GPS locations */
		LocationManager mlocManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		LocationListener mlocListener = new MyLocationListener();
		mlocManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0,
				mlocListener);
	}
	
	/* Class My Location Listener */
	public class MyLocationListener implements LocationListener
		{
		public void onLocationChanged(Location loc) {
			loc.getLatitude();
			loc.getLongitude();
			lati = loc.getLatitude();
			longi = loc.getLongitude();
			String Text = "My current location is: " +
			"Latitud = " + loc.getLatitude() +
			"Longitud = " + loc.getLongitude();
			
		

			System.out.println(Text);
			Toast.makeText(getApplicationContext(), Text, Toast.LENGTH_SHORT).show();
			finish();
		}

		public void onProviderDisabled(String provider)
			{
			Toast.makeText(getApplicationContext(),	"Gps Disabled",	Toast.LENGTH_SHORT).show();
			
			finish();
		}

		public void onProviderEnabled(String provider)
			{
			Toast.makeText(getApplicationContext(), "Gps Enabled", Toast.LENGTH_SHORT).show();
			
		}

		public void onStatusChanged(String provider, int status, Bundle extras)
			{
		}
	}/* End of Class MyLocationListener */
	
	@Override
	public void finish() {
		Intent data = new Intent();
		
	  // Prepare data intent 
	  
	  data.putExtra("longitud", longi );
	  data.putExtra("latitude", lati);
	  // Activity finished ok, return the data
	  setResult(RESULT_OK, data);
	
	  super.finish();
	} 	
    
}/* End of UseGps Activity */