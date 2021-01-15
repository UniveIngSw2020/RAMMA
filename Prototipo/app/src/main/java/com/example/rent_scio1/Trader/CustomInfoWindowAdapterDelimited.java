package com.example.rent_scio1.Trader;

import android.content.Context;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;


import androidx.core.content.res.ResourcesCompat;

import com.example.rent_scio1.R;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;

//classe rappresentante l'info window dei marker che delimitano l'area limitata del commerciante, oltre che il negozio: a runtime decidiamo se visualizzare il bottone elimina o il nome del negozio.

public class CustomInfoWindowAdapterDelimited implements GoogleMap.InfoWindowAdapter {

    private final View mWindow;


    public CustomInfoWindowAdapterDelimited(Context context) {

        mWindow = LayoutInflater.from(context).inflate(R.layout.costum_info_window_delimited, null);
    }

    private void buildInfo(Marker marker){

        Drawable drawable = ResourcesCompat.getDrawable(mWindow.getResources(), R.drawable.rounded_button, null);
        Drawable drawable_textview = ResourcesCompat.getDrawable(mWindow.getResources(), R.drawable.rounded_textview_marker, null);
        Typeface typeface = ResourcesCompat.getFont(mWindow.getContext(), R.font.comfortaa_regular);

        LinearLayout linearLayout=mWindow.findViewById(R.id.linearLayout_info_window_delimited);
        linearLayout.removeAllViews();

        if(marker.getTitle()==null){
            Button elimina=new Button(mWindow.getContext());
            elimina.setText(R.string.elimina);
            elimina.setBackground(drawable);
            elimina.setTypeface(typeface);
            elimina.setTextColor(mWindow.getContext().getColor(R.color.back));
            elimina.setTextSize(20);
            elimina.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
            linearLayout.addView(elimina);
        }
        else{
            TextView textView=new TextView(mWindow.getContext());
            textView.setText(marker.getTitle());
            textView.setBackground(drawable_textview);
            textView.setTextColor(mWindow.getContext().getColor(R.color.back));
            textView.setTextSize(20);
            textView.setTypeface(typeface);
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
