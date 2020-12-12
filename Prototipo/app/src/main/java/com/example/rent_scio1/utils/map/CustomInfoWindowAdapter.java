package com.example.rent_scio1.utils.map;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import androidx.core.content.res.ResourcesCompat;

import com.example.rent_scio1.Client.MapsActivityClient;
import com.example.rent_scio1.R;
import com.example.rent_scio1.utils.UserClient;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;



public class CustomInfoWindowAdapter implements GoogleMap.InfoWindowAdapter {

    private final View mWindow;
    private Context mContext;

    public CustomInfoWindowAdapter(Context context) {
        mContext = context;
        mWindow = LayoutInflater.from(context).inflate(R.layout.costum_info_window, null);
    }

    private void rendowWindowText(Marker marker, View view){

        String title = marker.getTitle();
        String snippet = marker.getSnippet();

        if(snippet==null){

        }

        TextView tvTitle = view.findViewById(R.id.title);

        if(!title.equals("")){
            tvTitle.setText(title);
        }

        TextView tvSnippet = view.findViewById(R.id.snippet);

        tvSnippet.setText(snippet);







    }

    private void createTable(View view) {
        Typeface typeface = ResourcesCompat.getFont(mContext, R.font.comfortaa_regular);

        TableLayout table = view.findViewById(R.id.gridview_maps_client);

        /*da gettare il tempo rimasto e l'altra cosa che non ricordo ora all'interno delle textview*/

        TableRow row;
        row = new TableRow(mContext);
        row.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT, 1.0f));
        row.setBackgroundColor(mContext.getColor(R.color.text));
        row.setPadding(0, 8, 0, 0);
        row.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);


        TextView tv1 = new TextView(mContext);
        tv1.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT, 1.0f));
        tv1.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        tv1.setTypeface(typeface);
        tv1.setTextColor(mContext.getColor(R.color.back));


        TextView tv2 = new TextView(mContext);
        tv2.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT, 1.0f));
        tv2.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        tv2.setTypeface(typeface);
        tv2.setTextColor(mContext.getColor(R.color.back));


        row.addView(tv1);
        row.addView(tv2);

        table.addView(row);
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
