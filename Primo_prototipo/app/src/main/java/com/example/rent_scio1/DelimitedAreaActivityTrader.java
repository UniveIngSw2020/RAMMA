package com.example.rent_scio1;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;

import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.rent_scio1.utils.PositionIterable;
import com.example.rent_scio1.utils.User;
import com.example.rent_scio1.utils.UserClient;
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
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.maps.android.PolyUtil;



import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Stack;


public class DelimitedAreaActivityTrader extends AppCompatActivity implements OnMapReadyCallback, GoogleMap.OnMarkerDragListener {

    private User mTrader;
    private GoogleMap mMap;
    private FirebaseFirestore mStore;

    private static final String TAG = "DelimitedAreaActivityTrader";

    private Polygon polygon=null;

    private PositionIterable markers =new PositionIterable();

    private final Stack<Marker> markersStack =new Stack<>();
    private boolean isThereAnArea=false;

    //posizione del trader
    private Marker trader;

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
        map_trader_delim.getMenu().findItem(R.id.how_to).setVisible(true);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        switch(item.getItemId()){

            case R.id.confirm_changes_limited:


                if(PolyUtil.containsLocation(trader.getPosition(),polygon.getPoints(),true)){
                    storeDelimitedArea();
                    startActivity(new Intent(getApplicationContext(), MapsActivityTrader.class));
                    finishAffinity();
                }
                else {
                    Toast.makeText(getApplicationContext(),"L'area limitata deve contenere il tuo negozio",Toast.LENGTH_LONG).show();
                }
                break;

            case R.id.how_to:
                Toast.makeText(getApplicationContext(), "tutorial", Toast.LENGTH_LONG).show();
                break;

            default:
                return super.onOptionsItemSelected(item);
        }
        return true;
    }

    private void storeDelimitedArea(){

        User u= UserClient.getUser();
        List<GeoPoint> geoPoints=markers.geoPointList();

        u.setDelimited_area(geoPoints);

        DocumentReference locationRef = mStore
                .collection("users")
                .document(Objects.requireNonNull(FirebaseAuth.getInstance().getUid()));

        locationRef.set(u).addOnCompleteListener(task -> {
            if(task.isSuccessful()){
                Log.d(TAG, "OK, delimited area pushata");
            }
        });
        /*
        DatabaseReference databaseReference= FirebaseDatabase.getInstance().getReference();

        databaseReference.child("users").child(u.getUser_id()).child("delimited_area").setValue(geoPoints)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG,"OOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOnSuccess: user Profile is created for: " + u.getUser_id());
                    }
                });*/

        /*DocumentReference documentReference=mStore.collection("users").document(u.getUser_id());

        documentReference.set(u.getUser_id())
                .addOnSuccessListener(aVoid -> Log.d(TAG,"OOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOnSuccess: user Profile is created for: " + u.getUser_id()))
                .addOnFailureListener(e -> System.out.println("onFaiulure: "+ e.toString()));*/
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

        //MARKER NEGOZIO
       addNegozio();

        //Carica area limitata precedente(se presente)
        //carica il mio array di markers e li aggiunge alla mappa
        List<GeoPoint> geoPoints=UserClient.getUser().getDelimited_area();
        if(geoPoints!=null){
            markers=new PositionIterable(geoPoints,mMap);
            costruisci();
            isThereAnArea=true;
        }

    }

    private void addNegozio(){
        GeoPoint traderpos=UserClient.getUser().getTraderposition();
        trader=mMap.addMarker(new MarkerOptions().position(new LatLng(traderpos.getLatitude(),traderpos.getLongitude())));
        trader.setTitle("NEGOZIO");
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
            map_trader_delim.getMenu().findItem(R.id.confirm_changes_limited).setVisible(true);
        }
        else{
            Toast.makeText(getApplicationContext(),"Non puoi settare come area un punto o una retta",Toast.LENGTH_LONG).show();
        }


    }

    private void initViews(){
        map_trader_delim = findViewById(R.id.toolbar_map_trader_delimiter);
        setSupportActionBar(map_trader_delim);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation_area_delimited);
        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {

            switch (item.getItemId()){
                case R.id.costruisci:
                    costruisci();
                    isThereAnArea=true;
                    break;
                case R.id.clear_last:
                    if(!markersStack.empty()){

                        Marker last=markersStack.pop();
                        last.remove();
                        markers.remove(last);

                        if(markers.size()>=3){
                            if(isThereAnArea)
                                costruisci();
                        }
                        else{
                            if(polygon!=null){
                                polygon.remove();
                                polygon=null;
                                isThereAnArea=false;
                                map_trader_delim.getMenu().findItem(R.id.confirm_changes_limited).setVisible(false);
                            }
                        }

                    }
                    break;
                case R.id.clear_all:
                    markers.removeAll();
                    mMap.clear();
                    polygon=null;
                    isThereAnArea=false;

                    addNegozio();

                    map_trader_delim.getMenu().findItem(R.id.confirm_changes_limited).setVisible(false);
                    break;
            }
            return true;
        });

    }

    private void getUserDetails(GoogleMap googleMap){
        if(mTrader == null){
            mTrader = new User();
            DocumentReference userRef = mStore.collection("users").document(FirebaseAuth.getInstance().getUid());
            userRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {

                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if(task.isSuccessful()){
                        Log.d(TAG, "onComplete: successfully get teh user details");
                        User user = task.getResult().toObject(User.class);
                        mTrader = new User (user);
                        /*mTraderLocation.setGeoPoint(user.getTraderposition());
                        mTrader.set*/
                        /*UserClient.setUser(user);*/
                        setCameraView(googleMap);
                    }
                }
            });
        }
    }

    private void setCameraView(GoogleMap googleMap){
        double bottomBundary = mTrader.getTraderposition().getLatitude() - .01;
        double leftBoundary = mTrader.getTraderposition().getLongitude() - .01;
        double topBoundary = mTrader.getTraderposition().getLatitude() + .01;
        double rightBoundary = mTrader.getTraderposition().getLongitude() + .01;

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
        if(isThereAnArea)
            costruisci();
    }
}