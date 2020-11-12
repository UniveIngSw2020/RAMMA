package com.example.rent_scio1;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.example.rent_scio1.utils.User;
import com.example.rent_scio1.utils.UserLocation;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Objects;

public class DelimitedAreaActivityTrader extends AppCompatActivity implements OnMapReadyCallback {

    private UserLocation mTraderLocation;
    private GoogleMap mMap;
    private FirebaseFirestore mStore;

    private static final String TAG = "DelimitedAreaActivityTrader";


    private final ArrayList<LatLng> markers =new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_delimited_area_trader);

        initViews();
        mStore = FirebaseFirestore.getInstance();

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
    @SuppressLint("MissingPermission")
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        getUserDetails(googleMap);
        //enableMyLocation();  // TODO attivare GPS in automatico
        //mMap.setMyLocationEnabled(true);

        mMap.setOnMapClickListener(latLng -> {
            mMap.addMarker(new MarkerOptions().position(latLng));
            markers.add(latLng);
            Log.d(TAG,((Integer)markers.size()).toString());
        });

    }

    private void getUserDetails(GoogleMap googleMap){
        if(mTraderLocation == null){
            mTraderLocation = new UserLocation();
            DocumentReference userRef = mStore.collection("users").document(FirebaseAuth.getInstance().getUid());
            userRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {

                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if(task.isSuccessful()){
                        Log.d(TAG, "onComplete: successfully get teh user details");
                        User user = task.getResult().toObject(User.class);
                        mTraderLocation.setUser(user);
                        mTraderLocation.setGeoPoint(user.getTraderposition());
                        /*UserClient.setUser(user);*/
                        setCameraView(googleMap);
                    }
                }
            });
        }
    }

    private void setCameraView(GoogleMap googleMap){
        double bottomBundary = mTraderLocation.getGeoPoint().getLatitude() - .01;
        double leftBoundary = mTraderLocation.getGeoPoint().getLongitude() - .01;
        double topBoundary = mTraderLocation.getGeoPoint().getLatitude() + .01;
        double rightBoundary = mTraderLocation.getGeoPoint().getLongitude() + .01;

        LatLngBounds mMapBoundary = new LatLngBounds(
                new LatLng(bottomBundary, leftBoundary),
                new LatLng(topBoundary, rightBoundary)
        );

        mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(mMapBoundary, 0));

    }
}