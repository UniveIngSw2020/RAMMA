package com.example.rent_scio1.utils.map;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;


import javax.annotation.Nullable;

// Classe di utilità utilizzata in MyMapClient e MyMapTrader

public class MyMap implements OnMapReadyCallback{

    private static GoogleMap mMap = null;
    public static boolean followMe = true;

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.getUiSettings().setMapToolbarEnabled(false);


    }

    @Nullable
    public static GoogleMap getmMap() {
        return mMap;
    }

}
