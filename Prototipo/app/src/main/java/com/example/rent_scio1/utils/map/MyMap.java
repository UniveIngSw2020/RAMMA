package com.example.rent_scio1.utils.map;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;

import javax.annotation.Nullable;

public class MyMap implements OnMapReadyCallback{

    private static GoogleMap mMap = null;

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
    }

    @Nullable
    public static GoogleMap getmMap() {
        return mMap;
    }

}
