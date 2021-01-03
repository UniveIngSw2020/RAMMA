package com.example.rent_scio1.Trader;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;


import com.example.rent_scio1.R;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;



public class CustomInfoWindowAdapterDelimited implements GoogleMap.InfoWindowAdapter {

    private final View mWindow;


    public CustomInfoWindowAdapterDelimited(Context context) {

        mWindow = LayoutInflater.from(context).inflate(R.layout.costum_info_window_delimited, null);
        mWindow.setClickable(false);
    }

    @Override
    public View getInfoWindow(Marker marker) {
        return mWindow;
    }

    @Override
    public View getInfoContents(Marker marker) {
        return null;
    }
}
