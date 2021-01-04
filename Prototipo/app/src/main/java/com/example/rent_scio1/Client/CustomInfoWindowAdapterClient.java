package com.example.rent_scio1.Client;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.example.rent_scio1.R;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;

public class CustomInfoWindowAdapterClient implements GoogleMap.InfoWindowAdapter{

    private final View mWindow;

    public CustomInfoWindowAdapterClient(Context context) {
        mWindow = LayoutInflater.from(context).inflate(R.layout.costum_info_window_client, null);
    }

    private void addShopName(Marker marker){
        String title=marker.getTitle();
        TextView textView=mWindow.findViewById(R.id.title_marker_info_trader);
        textView.setText(title);
    }

    @Override
    public View getInfoWindow(Marker marker) {
        addShopName(marker);
        return mWindow;
    }

    @Override
    public View getInfoContents(Marker marker) {
        return null;
    }
}
