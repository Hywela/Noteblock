package com.example.ass2note.location;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Messenger;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;

import com.example.ass2note.R;
import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;

public class GoogleMapsActivity extends MapActivity {
	private ItemizedOverlayClass itemizedoverlay;
	private Intent findPositionServiceIntent;
	private MapController mapController;
	private Drawable drawable;
	private MapView mapView;
	private String latitude = null, longitude = null;


	/**
	 * When onCreate is called, it will start FindPositionService if the values
	 * sent by NoteEdit is invalid. It initializes MapView, MapController, 
	 * drawable, and creates an instance of ItemizedOverlayClass. If the values
	 * sent by NoteEdit was valid, it adds that position to the map.
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_google_maps);
		
		View bottomLevelLayout = findViewById(R.id.waitMapsLayout);
		bottomLevelLayout.setVisibility(View.INVISIBLE);

		// Get the position values from the database:
		Intent NoteEditIntent = getIntent();
		latitude = NoteEditIntent.getStringExtra("LATITUDE");
		longitude = NoteEditIntent.getStringExtra("LONGITUDE");

		// Find the user's current position if the current position is invalid.
		startFindPositionService();

		// Fetch the mapView used to display the map, and enable zooming
		mapView = (MapView) findViewById(R.id.mapView);
		mapView.setBuiltInZoomControls(true);

		// Create the controller and zoom in the desired height
		mapController = mapView.getController();
		mapController.setZoom(16);

		// TODO: Find out if the pin should be placed at the bottom, or bottom left of the geopoint
		// Fetch the correct marker for the map
		drawable = this.getResources().getDrawable(R.drawable.pin);
		// This commented code is code for setting the marker at the bottom left end of the pin:
		// drawable.setBounds(0, -drawable.getIntrinsicHeight(), drawable.getIntrinsicWidth(), 0);
		
		itemizedoverlay = new ItemizedOverlayClass(drawable, this);
		mapView.getOverlays().add(itemizedoverlay);

		// If the latitude and longitude values stored in the DB are valid:
		if (latitude != null 		 && longitude != null 
		&& !latitude.contains("lat") && !longitude.contains("long")) {
			
			// Send the latitude and longitude values to ItemizedOverlayClass.
			itemizedoverlay.setPosition(Double.parseDouble(latitude), Double.parseDouble(longitude));
			
			// Add the old position values to the map. 
			itemizedoverlay.addNewGeoPoint(mapView, Double.parseDouble(latitude), Double.parseDouble(longitude));
			
			// Move the map to the old position.
			mapController.animateTo(itemizedoverlay.getUserPosition());
		} // End if.
	}
	
	// TODO: Will this class die if onstop is called?
/*	@Override
	protected void onStop() {
		super.onStop();
		finish();
	}*/

	/**
	 * Method for starting FindPositionService to find the user's current 
	 * position. This method will only run if valid latitude and longitude 
	 * positions are not stored in the DB.
	 * @return
	 */
	public boolean startFindPositionService() {
		Log.i("GoogleMapsActivity", "startfindpositionservice.");
		
		// If the user's preferred notification position was not stored in the DB:
		if (latitude == null     	|| longitude == null 
		 || latitude.matches("lat")	|| longitude.matches("long")) {
			
			View topLevelLayout = findViewById(R.id.mapButtons);
			View bottomLevelLayout = findViewById(R.id.waitMapsLayout);
			topLevelLayout.setVisibility(View.INVISIBLE);
			bottomLevelLayout.setVisibility(View.VISIBLE);
			
			/* Call FindPositionService and send this activity's handler with a
			 * messenger to the service: */
			findPositionServiceIntent = new Intent(this, FindPositionService.class);
			findPositionServiceIntent.putExtra(FindPositionService.EXTRA_MESSENGER, new Messenger(handler));
			startService(findPositionServiceIntent);
			
			// If the service was started, return true.
			return true;
		}
		// If the service was not started, return false.
		else return false; 
	}

	/**
	 * Method that is not implemented.
	 */
	@Override
	protected boolean isRouteDisplayed() {
		return false;
	}

	/**
	 * A method called when the user presses "cancel". It sets the results to
	 * canceled and calls finish.
	 * @param view
	 */
	public void cancelMaps(View view) {
		Intent i = new Intent();
		setResult(Activity.RESULT_CANCELED, i);
		finish();
	}

	/**
	 * A function called when the user accepts a pin position from Google Maps.
	 * It adds the latitude and longitude values to a result and calls finish.
	 * @param view
	 */
	public void confirmMaps(View view) {
		// Create an intent that will contain the user's current position.
		Intent i = new Intent();
		
		// Put the position values inside the intent:
		i.putExtra("latitude", String.valueOf(itemizedoverlay.getLatitude()));
		i.putExtra("longitude", String.valueOf(itemizedoverlay.getLongitude()));
		
		// Put the intent to setResult, and call finish:
		setResult(Activity.RESULT_OK, i);
		finish();
	}

	/**
	 * Handler for fetching a message from FindPositionService. This message
	 * contains the user's current position, or null. When the position is
	 * fetched in the handler, the handler adds the user's current position
	 * to the map and move the map to that position. 
	 */
	private Handler handler = new Handler() {
		public void handleMessage(Message message) {
			Log.i("GoogleMapsActivity",	"Handler called from FindPositionService");
			
			// Get the data from FindPositionService.
			Bundle data = message.getData();

			// If the user's position was found:
			if (data != null) {
				View topLevelLayout = findViewById(R.id.mapButtons);
				View bottomLevelLayout = findViewById(R.id.waitMapsLayout);
				topLevelLayout.setVisibility(View.VISIBLE);
				bottomLevelLayout.setVisibility(View.INVISIBLE);
				
				double lati = data.getDouble("LATITUDE");
				double longi = data.getDouble("LONGITUDE");
				
				itemizedoverlay.setPosition(lati, longi);
				
				// Add the user's position to the map.
				itemizedoverlay.addNewGeoPoint(mapView, lati, longi);
				
				// Move the map to the user's position.
				mapController.animateTo(itemizedoverlay.getUserPosition());
				
				// Stop FindPositionService.
				stopService(findPositionServiceIntent);
			}
		} // end handlerMessage
	}; // End Handler handler.
	
	public void stopFindPositionService(View view){
		stopService(findPositionServiceIntent);
		finish();
	}
}