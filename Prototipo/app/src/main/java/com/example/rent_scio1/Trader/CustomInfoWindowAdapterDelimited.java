package com.example.rent_scio1.Trader;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;


import com.example.rent_scio1.R;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;



public class CustomInfoWindowAdapterDelimited implements GoogleMap.InfoWindowAdapter {

    private final View mWindow;


    public CustomInfoWindowAdapterDelimited(Context context) {

        mWindow = LayoutInflater.from(context).inflate(R.layout.costum_info_window_delimited, null);
    }

    private void buildInfo(Marker marker){

        LinearLayout linearLayout=mWindow.findViewById(R.id.linearLayout_info_window_delimited);
        linearLayout.removeAllViews();

        if(marker.getTitle()==null){
            Button elimina=new Button(mWindow.getContext());
            elimina.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
            linearLayout.addView(elimina);
        }
        else{
            TextView textView=new TextView(mWindow.getContext());
            textView.setText(marker.getTitle());
            linearLayout.addView(textView);
        }

    }

    @Override
    public View getInfoWindow(Marker marker) {
        buildInfo(marker);
        return mWindow;
    }

    @Override
    public View getInfoContents(Marker marker) {
        buildInfo(marker);
        return mWindow;
    }
}
