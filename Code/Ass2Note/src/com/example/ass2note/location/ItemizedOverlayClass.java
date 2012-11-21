package com.example.ass2note.location;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.util.Log;
import android.widget.Toast;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.MapView;
import com.google.android.maps.OverlayItem;

public class ItemizedOverlayClass extends ItemizedOverlay <OverlayItem>{
	private ArrayList<OverlayItem> itemList = new ArrayList<OverlayItem>();
	private GeoPoint userPosition;
	private Context context;
	private String title = "Do you want to be reminded close to this location?", snippet = "";
	private double latitude = 0, longitude = 0;
	
	
	public ItemizedOverlayClass(Drawable defaultMarker) {
		super(boundCenterBottom(defaultMarker));
	}

	/**
	 * Constructor. This constructor adds the default drawable marker to the
	 * itemized overlay.
	 * @param defaultMarker is the used drawable.
	 * @param c is the context of the calling class.
	 */
	public ItemizedOverlayClass(Drawable defaultMarker, Context c) {
		super(boundCenterBottom(defaultMarker));
		//super(defaultMarker);
		populate();
		context = c;
	}

	/**
	 * This method is not implemented.
	 */
	@Override
	protected OverlayItem createItem(int i) {
		return itemList.get(i);
	}

	/**
	 * Method for returning the size of the itemList.
	 */
	@Override
	public int size() {
		return itemList.size();
	}
	 
	@Override
	public void draw(Canvas canvas, MapView mapView, boolean shadow) {
		super.draw(canvas, mapView, shadow);
	}
	
	/**
	 * onTap callback method will handle the event when an item is tapped by the
	 * user
	 */
	@Override
	protected boolean onTap(int index) {
		Toast.makeText(context, itemList.get(index).getTitle() + " \n" 
			+ itemList.get(index).getSnippet(), Toast.LENGTH_LONG).show();
		return true;
	}
	
	@Override
	public boolean onTap(GeoPoint p, MapView mapView) {
		if(super.onTap(p, mapView)) return true;
		
		latitude = p.getLatitudeE6();
		longitude = p.getLongitudeE6();

		addNewGeoPoint(mapView, latitude, longitude);
		return true;
	}
	
	/**
	 * Method for returning the current preferred latitude.
	 * @return latitude
	 */
	public double getLatitude() {
		return latitude;
	}

	/**
	 * Method for returning the current preferred longitude.
	 * @return longitude
	 */
	public double getLongitude() {
		return longitude;
	}

	/**
	 * Method for adding a new GeoPoint an item and then adding the item to the
	 * MapView.
	 * @param mapView
	 * @param latitude
	 * @param longitude
	 */
	public void addNewGeoPoint(MapView mapView, double latitude, double longitude) {
		// If the overlay contain an item, remove the item.
		if(!itemList.isEmpty())	itemList.remove(0);
		
		// Create a GeoPoint.
		GeoPoint point = new GeoPoint((int) latitude, (int) longitude);
		
		// Try to get the address of the selected geopoint.
		getAddress();
		
		// Add the item to the list of items. TODO: Find out if I can use this list.
		System.out.println(snippet);
		addItem(point, title, snippet);
		
		// Add this overlay to the mapView.
		mapView.getOverlays().add(this);
		
		// Update the user geoPoint.
		userPosition = point;
	}
	
	/**
	 * Method for adding an item to the list of items.
	 * TODO: Find out why there is a list of items.
	 * @param item
	 */
	public void addItem(GeoPoint point, String title, String text) {
		OverlayItem overlayitem = new OverlayItem(point, title, text);
		itemList.add(overlayitem);
		populate();
	}
	
	/**
	 * Method for returning the new preferred GeoPosition of the user.
	 * @return GeoPoint userPosition.
	 */
	public GeoPoint getUserPosition(){
		return userPosition;
	}

	/**
	 * Method for transferring the user's previous preferred position stored in
	 * the database, to ItemizedOverlayClass.
	 * @param latitude2
	 * @param longitude2
	 */
	public void setPosition(String latitude2, String longitude2) {
		latitude = Double.parseDouble(latitude2);
		longitude = Double.parseDouble(longitude2);
	}
	
	/**
	 * Method for fetching address information of the preferred location on the
	 * map, or where the "pin" is placed. This method tries to fetch the 
	 * country name, province, street, postal code and city name. It needs
	 * Internet access to fetch this information.
	 */
	private void getAddress(){
		try {
			// Create a new GeoCoder:
            Geocoder geo = new Geocoder(context, Locale.getDefault());
            
            /* Create a new list and give it the address values from the 
             * preferred position.*/
            List<Address> addresses = geo.getFromLocation(latitude/1E6, longitude/1E6, 1);
            
            // If no address data was found:
            if (addresses.isEmpty()) {
            	Log.i("ItemizedOverlay","waiting for location");
            }
            // If address data was found:
            else {
            	// If the data contained information that was not null:
                if (addresses.size() > 0) {
                	// Add only the values that are not null:
                	// Country:
                	if(addresses.get(0).getCountryName()!=null) snippet = 
                			addresses.get(0).getCountryName() + ": ";
                	
                	// Province:
                	if(addresses.get(0).getAdminArea()!=null) snippet = 
                			snippet + addresses.get(0).getAdminArea();
                	if(snippet != "") snippet = snippet + "\n";
                	
                	// Street:
                	if(addresses.get(0).getThoroughfare()!=null) snippet =
                		snippet + addresses.get(0).getThoroughfare() + "\n";
                	
                	// Postal Code:
                	if(addresses.get(0).getPostalCode()!=null) snippet = 
                			snippet + addresses.get(0).getPostalCode() + " ";
                	
                	// City:
                	if(addresses.get(0).getLocality()!=null) snippet = 
                			snippet + addresses.get(0).getLocality();
                }
            }
        }
        catch (Exception e) {
            e.printStackTrace(); // getFromLocation() may sometimes fail
            snippet = "";		// Set default value to snippet.
        }
	}
	
}