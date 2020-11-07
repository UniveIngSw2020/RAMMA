package com.example.rent_scio1;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;

public class DelimitedAreaActivityTrader extends AppCompatActivity implements OnMapReadyCallback,GoogleMap.OnMapClickListener{

    private Button setArea;
    Polygon areaLimitata;
    private GoogleMap mMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_delimited_area_trader);


        //bottone conferma area limitata
        setArea=findViewById(R.id.set_area);
        setArea.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });


        //inizializzazione mappa 1
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

    }

    //inizializzazione mappa 2
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap=googleMap;

        //aggiunta di un poligono senza punti
        areaLimitata = mMap.addPolygon(new PolygonOptions().clickable(true));

        //tag poligono
        areaLimitata.setTag("Area Limitata");
    }


    @Override
    public void onMapClick(LatLng latLng) {

    }
}