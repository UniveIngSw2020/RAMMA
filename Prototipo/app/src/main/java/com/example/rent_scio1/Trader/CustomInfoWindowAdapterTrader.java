package com.example.rent_scio1.Trader;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.location.Location;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import androidx.core.content.res.ResourcesCompat;
import com.example.rent_scio1.R;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;

import java.util.Locale;


public class CustomInfoWindowAdapterTrader implements GoogleMap.InfoWindowAdapter{

    private static final String TAG = "CustomInfoWindowAdapter";
    private final View mWindow;
    private final Context mContext;

    public CustomInfoWindowAdapterTrader(Context context) {
        mContext = context;
        mWindow = LayoutInflater.from(context).inflate(R.layout.costum_info_window, null);
        mWindow.setClickable(false);
    }


    private void rendowWindowText(Marker marker, View view){

        String title = marker.getTitle();

        TextView titleMarker=view.findViewById(R.id.title_marker);
        titleMarker.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        titleMarker.setTextSize(20);
        titleMarker.setTextColor(Color.rgb(3,50,73));
        titleMarker.setText(title);

        TableLayout table = view.findViewById(R.id.tablelayout_trade);
        table.removeAllViews();

        TextView textView = mWindow.findViewById(R.id.subtitle_marker);
        textView.setText("");
        textView.setVisibility(View.INVISIBLE);


        String snippet = marker.getSnippet();
        if(snippet!=null && !snippet.equals("")){

            String[] strings=snippet.split(" ");
            createTableCustomers(table,strings[0],strings[1]);
        }

    }

    @SuppressLint("SetTextI18n")
    private void createTableCustomers(TableLayout table, String speed, String remainingTime) {

        Typeface typeface = ResourcesCompat.getFont(mContext, R.font.comfortaa_regular);

        TableRow rowTitle= new TableRow(mContext);
        rowTitle.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT, 1.0f));
        rowTitle.setBackgroundColor(Color.argb(50, 172, 202, 204));
        rowTitle.setPadding(0, 8, 0, 0);
        rowTitle.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);

        TextView tv1 = new TextView(mContext);
        tv1.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT, 1.0f));
        tv1.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        tv1.setTypeface(typeface);
        tv1.setTextColor(Color.rgb(3,50,73));
        tv1.setText("Tempo Rimasto");

        TextView tv2 = new TextView(mContext);
        tv2.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT, 1.0f));
        tv2.setPadding(30,0,0,0);
        tv2.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        tv2.setTypeface(typeface);
        tv2.setTextColor(Color.rgb(3,50,73));
        tv2.setText("Velocità attuale");

        rowTitle.addView(tv1);
        rowTitle.addView(tv2);

        table.addView(rowTitle);



        TableRow rowNumbers= new TableRow(mContext);
        rowNumbers.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT, 1.0f));
        rowNumbers.setBackgroundColor(Color.argb(50, 172, 202, 204));
        rowNumbers.setPadding(0, 8, 0, 0);
        rowNumbers.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);

        TextView textView1 = new TextView(mContext);
        textView1.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT, 1.0f));
        textView1.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        textView1.setTypeface(typeface);
        textView1.setTextColor(Color.rgb(3,50,73));
        textView1.setText(remainingTime);

        TextView textView2 = new TextView(mContext);
        textView2.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT, 1.0f));
        tv2.setPadding(30,0,0,0);
        textView2.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        textView2.setTypeface(typeface);
        textView2.setTextColor(Color.rgb(3,50,73));


        textView2.setText(speed + " km/h");

        rowNumbers.addView(textView1);
        rowNumbers.addView(textView2);
        table.addView(rowNumbers);



        TextView textView3 = mWindow.findViewById(R.id.subtitle_marker);
        textView3.setText(R.string.tocca_qui_per_chiamare_il_cliente);
        textView3.setVisibility(View.VISIBLE);

    }


    @Override
    public View getInfoWindow(Marker marker) {
        rendowWindowText(marker, mWindow);
        return mWindow;
    }

    @Override
    public View getInfoContents(Marker marker) {
        rendowWindowText(marker, mWindow);
        return mWindow;
    }

}
