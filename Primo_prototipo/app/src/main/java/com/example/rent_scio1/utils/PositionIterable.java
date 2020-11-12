package com.example.rent_scio1.utils;

import androidx.annotation.NonNull;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.function.Consumer;
import java.util.stream.Stream;

public class PositionIterable implements Iterable<LatLng> {

    private ArrayList<Marker> markers=new ArrayList<>();

    @NonNull
    @Override
    public Iterator<LatLng> iterator() {
        ArrayList<LatLng> latLngs=new ArrayList<>();

        for (Marker marker : markers) {
            latLngs.add(marker.getPosition());
        }

        return latLngs.iterator();
    }

    public void add(Marker m){
        markers.add(m);
    }

    public int size(){
        return markers.size();
    }


}
