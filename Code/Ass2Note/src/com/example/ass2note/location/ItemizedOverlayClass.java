package com.example.ass2note.location;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.MotionEvent;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import com.google.android.maps.OverlayItem;

public class ItemizedOverlayClass extends ItemizedOverlay {
	private ArrayList<OverlayItem> itemList = new ArrayList<OverlayItem>();
	private Context mContext;
	private GeoPoint userPosition;
	private double latitude = 0, longitude = 0, latitudeCopy = 0,
			longitudeCopy = 0;

	public ItemizedOverlayClass(Drawable defaultMarker) {
		super(boundCenterBottom(defaultMarker));
	}

	public ItemizedOverlayClass(Drawable defaultMarker, Context context) {
		super(boundCenterBottom(defaultMarker));
		//super(defaultMarker);
		mContext = context;
	}

	@Override
	protected OverlayItem createItem(int i) {
		return itemList.get(i);
	}

	@Override
	public int size() {
		return itemList.size();
	}

	public void addItem(OverlayItem item) {
		itemList.add(item);
		populate();
	}

	/**
	 * onTap callback method will handle the event when an item is tapped by the
	 * user
	 */
	/*@Override
	protected boolean onTap(int index) {
		OverlayItem item = mOverlays.get(index);
		AlertDialog.Builder dialog = new AlertDialog.Builder(mContext);
		dialog.setTitle(item.getTitle());
		dialog.setMessage(item.getSnippet());
		dialog.show();
		return true;
	}*/

	@Override
	public boolean onTouchEvent(MotionEvent event, MapView m) {
		GeoPoint p = null;

		if (event.getAction() == MotionEvent.ACTION_UP) {
			p = m.getProjection().fromPixels((int) event.getX(),(int) event.getY());
			latitude = p.getLatitudeE6();
			longitude = p.getLongitudeE6();

			if (latitudeCopy != 0 && longitudeCopy != 0) {
				addNewGeoPoint(m, latitudeCopy, longitudeCopy);
				latitude = latitudeCopy;
				longitude = longitudeCopy;
				latitudeCopy = 0;
				longitudeCopy = 0;
			} else {
				System.out.println("longitudecopy: " + longitudeCopy + " longitude: " + longitude);
				addNewGeoPoint(m, latitude, longitude);
			}
			return true;
		} else if (event.getAction() == MotionEvent.ACTION_MOVE) {
			if (latitudeCopy == 0 && longitudeCopy == 0) {
				latitudeCopy = latitude;
				longitudeCopy = longitude;
			}
			return false;
		}
		return false;
	}

	public double getLatitude() {
		return latitude;
	}

	public double getLongitude() {
		return longitude;
	}

	public void addNewGeoPoint(MapView m, double latitude, double longitude) {
		// If the overlay contain an item, remove the item.
		if(!itemList.isEmpty())	itemList.remove(0);
		
		Log.i("ItemizedOverlayClass","latitude pin: "+latitude);
		Log.i("ItemizedOverlayClass","longitude pin: "+longitude);
		
		// Create a GeoPoint - add it to an item - add the item to the overlay.
		GeoPoint point = new GeoPoint((int) latitude, (int) longitude);
		OverlayItem overlayitem = new OverlayItem(point,
				"You will be reminded at this position", "a position");
		addItem(overlayitem);
		m.getOverlays().add(this);
		userPosition = point;
	}
	
	public GeoPoint getUserPosition(){
		return userPosition;
	}

	public void setPosition(String latitude2, String longitude2) {
		// TODO Auto-generated method stub
		latitude = Double.parseDouble(latitude2);
		longitude = Double.parseDouble(longitude2);
	}
}