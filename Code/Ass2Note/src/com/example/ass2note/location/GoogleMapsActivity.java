package com.example.ass2note.location;

import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.Toast;

import com.example.ass2note.R;
import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import com.google.android.maps.OverlayItem;

public class GoogleMapsActivity extends MapActivity implements LocationListener{
 private double latitude=0, longitude=0;
 private LocationManager mLocationManager;
 private MapController mapController;
 private List<Overlay> mapOverlays;
 private Drawable drawable;
 private ItemizedOverlayClass itemizedoverlay;
 private GeoPoint point;
 
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_google_maps);
        
        // Fetch the mapView used to display the map, and enable zooming
        final MapView mapView = (MapView) findViewById(R.id.mapView);
        mapView.setBuiltInZoomControls(true);
        
        // Create the controller and zoom in the desired height
        mapController = mapView.getController(); //<4>
        mapController.setZoom(16);
        
        // Update the LocationManager
        mLocationManager = (LocationManager)this.getSystemService(LOCATION_SERVICE);
        
        // Create an Overlay for this map, and get the correct drawable 
        mapOverlays = mapView.getOverlays();
        drawable = this.getResources().getDrawable(R.drawable.pin);
        itemizedoverlay = new ItemizedOverlayClass(drawable, this);
        itemizedoverlay.setActivity(this);
        
        // Get the user's last known location
        Location location = mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER); 
        
        if (location != null) { // If a location is found:
          Log.d("Location found", location.toString());
          
          latitude = (int)(location.getLatitude() * 1000000);
       longitude = (int)(location.getLongitude() * 1000000);
       addOverlay(latitude, longitude);
       mapController.animateTo(point);
    mLocationManager.removeUpdates(this); 
        }
               
       }
    
    
    public void addOverlay(double lati, double longi){
      point = new GeoPoint((int)lati, (int)longi);
      OverlayItem overlayitem = new OverlayItem(point, "You will be reminded at this position", "a position");
      itemizedoverlay.addOverlay(overlayitem);
      mapOverlays.add(itemizedoverlay);
    }
   
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_google_maps, menu);
        return true;
    }

 @Override
 protected boolean isRouteDisplayed() {
  return false;
 }
 
 public void cancelMaps(View view){
  Intent i = new Intent();
  setResult(Activity.RESULT_CANCELED, i);
  finish();
 }
 
 public void confirmMaps(View view){
  Intent i = new Intent();
  
  i.putExtra("latitude", String.valueOf(itemizedoverlay.getLatitude()));
  i.putExtra("longitude", String.valueOf(itemizedoverlay.getLongitude()));
  setResult(Activity.RESULT_OK, i);
  finish();
 }
 
 public void onProviderDisabled(String provider) {}
 public void onProviderEnabled(String provider) {}
 public void onStatusChanged(String provider, int status, Bundle extras) {}
 public void onLocationChanged(Location location) {}
 
 @Override
  protected void onResume() {
      super.onResume();
      mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 10, this); 
    }
  @Override
   protected void onPause() {
     super.onPause();
     mLocationManager.removeUpdates(this); 
   }

  @Override
     protected void onStart() {
         super.onStart();

         // This verification should be done during onStart() because the system calls
         // this method when the user returns to the activity, which ensures the desired
         // location provider is enabled each time the activity resumes from the stopped state.
         final boolean gpsEnabled = mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);

         if (!gpsEnabled) {
             // Show an alert dialog that requests that the user enable
             // the location services, then when the user clicks the "OK" button,
             // enableLocationSettings() is called
          AlertDialog.Builder altDialog= new AlertDialog.Builder(this);
          altDialog.setMessage("Please start your GPS");
          altDialog.setNeutralButton("OK", new DialogInterface.OnClickListener() {
             
            // @Override
             public void onClick(DialogInterface dialog, int which) {
              enableLocationSettings();
             }
            });
          altDialog.show();

         }
     }

     private void enableLocationSettings() {
         Intent settingsIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
         startActivity(settingsIntent);

         // Exit Google Maps so it will be properly updated the next time.
         finish();
     }
     
}