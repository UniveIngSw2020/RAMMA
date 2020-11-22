package com.example.rent_scio1.utils;


import androidx.annotation.NonNull;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.firestore.GeoPoint;

import java.util.ArrayList;

import java.util.Iterator;
import java.util.List;


public class PositionIterable implements Iterable<LatLng> {

    private final ArrayList<Marker> markers;


    public PositionIterable(List<GeoPoint> geoPoints, GoogleMap mMap){

        markers=new ArrayList<>();

        for (GeoPoint geoPoint:geoPoints) {

            Marker marker=mMap.addMarker(new MarkerOptions().position(new LatLng(geoPoint.getLatitude(),geoPoint.getLongitude())));
            marker.setDraggable(true);

            markers.add(marker);
        }

    }

    public PositionIterable(){
        markers=new ArrayList<>();
    }

    public ArrayList<Marker> getMarkers() {
        return markers;
    }

    @NonNull
    @Override
    public Iterator<LatLng> iterator() {
        ArrayList<LatLng> latLngs=new ArrayList<>();

        for (Marker marker : markers) {
            latLngs.add(marker.getPosition());
        }

        return latLngs.iterator();
    }

    public List<GeoPoint> geoPointList(){
        List<GeoPoint> geoPoints=new ArrayList<>();
        for (Marker marker : markers) {
            geoPoints.add(new GeoPoint(marker.getPosition().latitude,marker.getPosition().longitude));
        }

        return geoPoints;
    }

    public void add(Marker m){

        markers.add(m);
    }

    public int size(){
        return markers.size();
    }

    public boolean remove(Marker marker){
        return markers.remove(marker);
    }

    public void removeAll(){
        markers.removeAll(markers);
    }


    //trova il centro della figura (media)
    private LatLng center(){
        double lat=0;
        double lng=0;
        for (LatLng latLng : this) {
            lat+=latLng.latitude;
            lng+=latLng.longitude;
        }
        lat=lat/size();
        lng=lng/size();
        return new LatLng(lat,lng);
    }

    //formuletta distanza punto punto
    private double getDistance(LatLng a,LatLng b){
        double x=a.longitude-b.longitude;
        double y=a.latitude-b.latitude;

        return Math.sqrt(x*x+y*y);
    }

    private double getAngle(LatLng a,LatLng b){
        double x=b.longitude-a.longitude;
        double y=b.latitude-a.latitude;

        //arcotangente
        double angle=Math.atan2(y,x);

        //rendo l'angolo positivo per comodità
        if(angle <= 0)
            angle= 2*Math.PI +angle;


        return angle;
    }

    public void sort(){
        final LatLng center=center();

        for (Marker marker:markers) {

            LatLng prev=marker.getPosition();
            marker.setPosition(new LatLng(prev.latitude-center.latitude,prev.longitude-center.longitude));
        }

        markers.sort((o1, o2) -> {


            if(o1.getPosition().equals(o2.getPosition())){
                return 0;
            }

            double angle1=getAngle(new LatLng(0,0),o1.getPosition());
            double angle2=getAngle(new LatLng(0,0),o2.getPosition());

            if(angle1<angle2){
                return -1;
            }

            double distance1=getDistance(new LatLng(0,0),o1.getPosition());
            double distance2=getDistance(new LatLng(0,0),o2.getPosition());

            if(angle1==angle2 && distance1 < distance2){
                return -1;
            }

            return 1;
        });

        for (Marker marker:markers) {

            LatLng prev=marker.getPosition();
            marker.setPosition(new LatLng(prev.latitude+center.latitude,prev.longitude+center.longitude));
        }
    }

}
