package com.example.rent_scio1;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.example.rent_scio1.utils.PositionIterable;
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
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Objects;
import java.util.Stack;

public class DelimitedAreaActivityTrader extends AppCompatActivity implements OnMapReadyCallback, GoogleMap.OnMarkerDragListener {

    private UserLocation mTraderLocation;
    private GoogleMap mMap;
    private FirebaseFirestore mStore;

    private static final String TAG = "DelimitedAreaActivityTrader";

    private Polygon polygon=null;

    private final PositionIterable markers =new PositionIterable();
    private final Stack<Marker> markersStack =new Stack<>();
    private Toolbar map_trader_delim;


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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.confirm_changes_limited_area, menu);
        map_trader_delim.getMenu().findItem(R.id.confirm_changes_limited).setVisible(false);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        switch(item.getItemId()){
            case R.id.confirm_changes_limited:
                /*LOTTO INSERISCI QUA IL CODICE MERDOSO PER IL CONFERMA.*/
                break;
        }
        return true;
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
            Marker marker=mMap.addMarker(new MarkerOptions().position(latLng));
            marker.setDraggable(true);

            markersStack.push(marker);
            markers.add(marker);
        });

        mMap.setOnMarkerDragListener(this);
    }

    private void costruisci(){

        if(markers.size()>=3){

            if( polygon!=null ){
                polygon.remove();
                polygon=null;

            }

            markers.sort();

            PolygonOptions polygonOptions=new PolygonOptions().addAll(markers).clickable(true);
            polygon=mMap.addPolygon(polygonOptions);
            polygon.setStrokeColor(Color.rgb(0,0,0));
            polygon.setFillColor(0x7F00FF00);

        }
        else{
            Toast.makeText(getApplicationContext(),"Non puoi settare come area un punto o una retta",Toast.LENGTH_LONG).show();
        }
    }


    private void initViews(){
        map_trader_delim = findViewById(R.id.toolbar_map_trader_delimiter);
        setSupportActionBar(map_trader_delim);
        Objects.requireNonNull(getSupportActionBar()).setTitle("");
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation_area_delimited);
        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {

            switch (item.getItemId()){
                case R.id.costruisci:
                    costruisci();
                    map_trader_delim.getMenu().findItem(R.id.confirm_changes_limited).setVisible(true);
                    break;
                case R.id.clear_last:
                    if(!markersStack.empty()){

                        Marker last=markersStack.pop();

                        last.remove();
                        markers.remove(last);




                        if(markers.size()>=3){
                            costruisci();
                        }
                        else{
                            if(polygon!=null){
                                polygon.remove();
                                polygon=null;
                            }
                        }

                    }
                    break;
                case R.id.clear_all:
                    markers.removeAll();
                    mMap.clear();
                    break;
            }
            return true;
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

    @Override
    public void onMarkerDragStart(Marker marker) {
        markers.remove(marker);

    }

    @Override
    public void onMarkerDrag(Marker marker) {

    }

    @Override
    public void onMarkerDragEnd(Marker marker) {
        markers.add(marker);
        costruisci();
    }
}