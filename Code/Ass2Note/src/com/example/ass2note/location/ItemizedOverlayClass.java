package com.example.ass2note.location;
import java.util.ArrayList;
import java.util.List;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.MotionEvent;
import android.widget.Toast;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import com.google.android.maps.OverlayItem;


public class ItemizedOverlayClass extends ItemizedOverlay {
 private ArrayList<OverlayItem> mOverlays = new ArrayList<OverlayItem>();
 private Context mContext;
 private double latitude=0, longitude=0, latitudeCopy=0, longitudeCopy=0;
 private GoogleMapsActivity activity;
 
 public ItemizedOverlayClass(Drawable defaultMarker) {
  super(boundCenterBottom(defaultMarker));
 }

 public ItemizedOverlayClass(Drawable defaultMarker, Context context) {
  super(boundCenterBottom(defaultMarker));
  mContext = context;
 }
 
 @Override
 protected OverlayItem createItem(int i) {
  return mOverlays.get(i);
 }
 
 @Override
 public int size() {
  return mOverlays.size();
 }
 
 public void addOverlay(OverlayItem overlay) {
     mOverlays.add(overlay);
     populate();
 }
 
 /**
  * onTap callback method will handle the event when an item is tapped by the user
  */
 @Override
 protected boolean onTap(int index) { 
   OverlayItem item = mOverlays.get(index);
   AlertDialog.Builder dialog = new AlertDialog.Builder(mContext);
   dialog.setTitle(item.getTitle());
   dialog.setMessage(item.getSnippet());
   dialog.show();
   return true;
 }
 
 @Override
 public boolean onTouchEvent(MotionEvent event,MapView m) {
    GeoPoint p = null;
          
          if (event.getAction() == MotionEvent.ACTION_UP) {
              p = m.getProjection().fromPixels((int) event.getX(),(int) event.getY());
              latitude = p.getLatitudeE6() / 1E6;
              longitude = p.getLongitudeE6() / 1E6;

              if(latitudeCopy!=0 && longitudeCopy!=0) {
               addNewGeoPoint(m, latitudeCopy, longitudeCopy);
               latitude = latitudeCopy; longitude = longitudeCopy;
               latitudeCopy=0; longitudeCopy=0;
              }
              else{
               addNewGeoPoint(m, latitude, longitude);
              }
              return true;
          }else if(event.getAction() == MotionEvent.ACTION_MOVE){
           if(latitudeCopy==0 && longitudeCopy==0){
            latitudeCopy = latitude;
            longitudeCopy = longitude;
           }
           return false;
          }
          return false;
      }
 
 public double getLatitude(){
  return latitude;
 }
 
 public double getLongitude(){
  return longitude;
 }
 
 public void setActivity(GoogleMapsActivity maps){
  activity = maps;
 }
 
 public void addNewGeoPoint(MapView m, double latitude, double longitude){
   final List<Overlay> mapOverlays;
         mapOverlays = m.getOverlays();
         mapOverlays.clear();
         m.getOverlays().clear();
         
         mOverlays.remove(0);
         activity.addOverlay(latitude * 1E6, longitude * 1E6);
 }
}