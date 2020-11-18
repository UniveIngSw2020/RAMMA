package com.example.rent_scio1;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.example.rent_scio1.utils.User;
import com.example.rent_scio1.utils.UserClient;
import com.example.rent_scio1.utils.UserLocation;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;

import java.util.ArrayList;
import java.util.List;

public class MapsActivityTrader extends AppCompatActivity implements OnMapReadyCallback, ActivityCompat.OnRequestPermissionsResultCallback, NavigationView.OnNavigationItemSelectedListener {

    private UserLocation mTraderLocation;

    private FirebaseAuth mAuth;
    private FirebaseFirestore mStore;

    private static final String TAG = "MapsActivityTrader";
    //private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private GoogleMap mMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps_trader);

        mAuth = FirebaseAuth.getInstance();
        mStore = FirebaseFirestore.getInstance();

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.mapDelimiter);
        mapFragment.getMapAsync(this);

        initViews();
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
                        UserClient.setUser(user);
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

        googleMap.addMarker(new MarkerOptions()
                .position( new LatLng(mTraderLocation.getGeoPoint().getLatitude(), mTraderLocation.getGeoPoint().getLongitude()))
                .title("Tu sei qui!"));
    }

    private void initViews(){
        NavigationView navigationView = findViewById(R.id.navigationView_Map_Trader);
        TextView textView = (TextView) navigationView.getHeaderView(0).findViewById(R.id.text_email_trader);
        textView.setText(mAuth.getCurrentUser().getEmail());
        DrawerLayout drawer_map_trader = (DrawerLayout) findViewById(R.id.drawer_map_trader1);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_map_trader);
        setSupportActionBar(toolbar);
        navigationView.setNavigationItemSelectedListener(this);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer_map_trader, toolbar, R.string.drawer_open, R.string.drawer_close);
        drawer_map_trader.addDrawerListener(toggle);
        toggle.syncState();
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        getUserDetails(googleMap);
        areaLimitata();
        //enableMyLocation();  // TODO attivare GPS in automatico
        //mMap.setMyLocationEnabled(true);
    }

    private void areaLimitata(){
        User u=UserClient.getUser();

        if(u!=null){

            List<LatLng> latLngs = new ArrayList<>();

            List<GeoPoint> geoPoints = u.getDelimited_area();
            for (GeoPoint a:geoPoints) {
                latLngs.add(new LatLng(a.getLatitude(),a.getLongitude()));
            }

            PolygonOptions polygonOptions=new PolygonOptions().addAll(latLngs).clickable(true);
            Polygon polygon=mMap.addPolygon(polygonOptions);
            polygon.setStrokeColor(Color.BLACK);
        }
    }

    /*private void enableMyLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            if (mMap != null) {
                mMap.setMyLocationEnabled(true);
            }
        } else {
            // Permission to access the location is missing. Show rationale and request permission
            PermissionUtils.requestPermission(this, LOCATION_PERMISSION_REQUEST_CODE,
                    Manifest.permission.ACCESS_FINE_LOCATION, true);
        }
    }*/

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.logout:
                mAuth.signOut();
                UserClient.setUser(null);
                startActivity(new Intent(getApplicationContext(), StartActivity.class));
                finishAffinity();
                break;
            case R.id.nuova_corsa:
                startActivity(new Intent(getApplicationContext(), NuovaCorsaActivityTrader.class));
                break;
            case R.id.Parco_mezzi:
                startActivity(new Intent(getApplicationContext(), VehicleListActivityTrader.class));
                break;
            case R.id.area_limited:
                startActivity(new Intent(getApplicationContext(), DelimitedAreaActivityTrader.class));
                break;
        }
        return true;
    }
}