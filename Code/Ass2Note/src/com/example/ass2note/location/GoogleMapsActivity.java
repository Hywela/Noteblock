package com.example.ass2note.location;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Messenger;
import android.util.Log;
import android.view.Menu;
import android.view.View;

import com.example.ass2note.R;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;


public class GoogleMapsActivity extends MapActivity implements LocationListener {
	private MapController mapController;
	private Drawable drawable;
	private ItemizedOverlayClass itemizedoverlay;
	private MapView mapView;
	private Intent findPositionServiceIntent;
	private String latitude=null, longitude=null;
	
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_google_maps);
		
		registerReceiver(positionServiceReceiver, new IntentFilter("com.example.ass2note.location.GoogleMapsActivity.positionServiceReceiver"));
		
		Intent NoteEditIntent = getIntent();
		latitude = NoteEditIntent.getStringExtra("LATITUDE");
		longitude = NoteEditIntent.getStringExtra("LONGITUDE");

		startFindPositionService();
		
		// Fetch the mapView used to display the map, and enable zooming
		mapView = (MapView) findViewById(R.id.mapView);
		mapView.setBuiltInZoomControls(true);

		// Create the controller and zoom in the desired height
		mapController = mapView.getController(); 
		mapController.setZoom(16);

		// Fetch the correct marker for the map
		drawable = this.getResources().getDrawable(R.drawable.pin);
	//	drawable.setBounds(0, -drawable.getIntrinsicHeight(), drawable.getIntrinsicWidth(), 0);
		itemizedoverlay = new ItemizedOverlayClass(drawable, this);
		
		if(latitude!=null && longitude!=null && !latitude.contains("lat") && !longitude.contains("long")){
			itemizedoverlay.setPosition(latitude, longitude);
			itemizedoverlay.addNewGeoPoint(mapView, Double.parseDouble(latitude), Double.parseDouble(longitude));
	    	mapController.animateTo(itemizedoverlay.getUserPosition());
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_google_maps, menu);
		return true;
	}
	
	public boolean startFindPositionService(){
		Log.i("GoogleMapsActivity", "in startfindpositionservice. latitude: "+latitude);
		if(latitude==null || longitude==null ||latitude.matches("lat") || longitude.matches("long")){
			findPositionServiceIntent=new Intent(this, FindPositionService.class);
			findPositionServiceIntent.putExtra(FindPositionService.EXTRA_MESSENGER, new Messenger(handler));
	    	startService(findPositionServiceIntent);
	    	return true;	// if the service was started, return true.
		}
		else{
			Log.i("GoogleMapsActivity", "latiitude stored: " + latitude + " and parsed: " + Double.parseDouble(latitude));
			Log.i("GoogleMapsActivity", "longiitude stored: " + longitude + " and parsed: " + Double.parseDouble(longitude));
	    	return false;	// if the service was not started, return false.
		}
	}

	@Override
	protected boolean isRouteDisplayed() {
		return false;
	}

	public void cancelMaps(View view) {
		Intent i = new Intent();
		setResult(Activity.RESULT_CANCELED, i);
		finish();
	}

	/**
	 * A function called when the user accepts a pin position from Google Maps.
	 * @param view
	 */
	public void confirmMaps(View view) {
		Intent i = new Intent();

		i.putExtra("latitude", String.valueOf(itemizedoverlay.getLatitude()));
		i.putExtra("longitude", String.valueOf(itemizedoverlay.getLongitude()));
		setResult(Activity.RESULT_OK, i);
		finish();
	}

	public void onProviderDisabled(String provider) {
		Log.i("GoogleMapsActivity", "Provider is disabled");
	}

	public void onProviderEnabled(String provider) {
		Log.i("GoogleMapsActivity", "Provider is enabled");
	}

	public void onStatusChanged(String provider, int status, Bundle extras) {
		Log.i("GoogleMapsActivity", "Provider is onStatusChanged");
	}

	public void onLocationChanged(Location location) {
		Log.i("GoogleMapsActivity", "OnLocationChanged");
	}

	@Override
	protected void onResume() {
		super.onResume();
	}
	
	@Override
	protected void onDestroy(){
		Log.i("GoogleMapsActivity","is destroyed");
		super.onDestroy();
	}

	@Override
	protected void onPause() {
		super.onPause();
	}

	@Override
	protected void onStart() {
		super.onStart();
	}
	
	@Override
	protected void onStop(){
		super.onStop();
	}	

	private Handler handler = new Handler() {
	    public void handleMessage(Message message) {
	      Bundle data = message.getData();
	      if (data != null) {
	    	  Log.i("GoogleMapsActivity","longitude found: "+ data.getDouble("LONGITUDE"));
	    	  itemizedoverlay.addNewGeoPoint(mapView, data.getDouble("LATITUDE"), data.getDouble("LONGITUDE"));
	    	  mapController.animateTo(itemizedoverlay.getUserPosition());
	    	  stopService(findPositionServiceIntent);
	      }
	    }
	  };

	  BroadcastReceiver positionServiceReceiver = new BroadcastReceiver(){
		@Override
		public void onReceive(Context context, Intent intent) {
			// TODO Auto-generated method stub
			System.out.println("broadcast called from service");
			helloWorld();
			unregisterReceiver(this);
		}
	  };
	  
	  public void helloWorld(){
		  System.out.println("hello world");
	  }
}