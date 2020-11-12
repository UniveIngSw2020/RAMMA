package com.example.rent_scio1;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;

import java.util.Objects;

public class DelimitedAreaActivityTrader extends AppCompatActivity implements OnMapReadyCallback,GoogleMap.OnMapClickListener{

    private Button setArea;
    Polygon areaLimitata;
    private GoogleMap mMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_delimited_area_trader);

        initViews();


        //inizializzazione mappa 1
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.mapDelimiterSettaArea);
        mapFragment.getMapAsync(this);

    }

    private void initViews(){
        Toolbar map_trader_delim = findViewById(R.id.toolbar_map_trader_delimiter);
        setSupportActionBar(map_trader_delim);
        Objects.requireNonNull(getSupportActionBar()).setTitle("");
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    //inizializzazione mappa 2
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap=googleMap;

        //aggiunta di un poligono senza punti
        //areaLimitata = mMap.addPolygon(new PolygonOptions().clickable(true));

        //tag poligono
        //areaLimitata.setTag("Area Limitata");
    }


    @Override
    public void onMapClick(LatLng latLng) {

    }
}