package com.example.rent_scio1.Trader;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;

import com.example.rent_scio1.R;
import com.example.rent_scio1.utils.User;
import com.example.rent_scio1.utils.UserClient;
import com.example.rent_scio1.utils.permissions.MyPermission;
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
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.maps.android.PolyUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class SetPositionActivityTrader extends AppCompatActivity implements OnMapReadyCallback,GoogleMap.OnMarkerDragListener {

    private GoogleMap mMap;
    private static final String TAG="SetPositionActivityTrader";

    private Marker shop;
    private Toolbar toolbar_map;

    MyPermission permission;

    private boolean mLocationPermissionGranted = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_position_trader);

        initViews();
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map_permission);
        mapFragment.getMapAsync(this);

    }

    public void initViews(){
        toolbar_map = findViewById(R.id.toolbar_map_permission_delimited);
        setSupportActionBar(toolbar_map);

        Intent intent=getIntent();
        if(intent.getBooleanExtra("IMPOSTAZIONI",false)){

            Objects.requireNonNull(getSupportActionBar()).setDisplayShowHomeEnabled(true);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        else{

            Objects.requireNonNull(getSupportActionBar()).setDisplayShowHomeEnabled(false);
            getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_map_permission, menu);
        toolbar_map.getMenu().findItem(R.id.confirm_position).setVisible(false);
        toolbar_map.getMenu().findItem(R.id.locate_me).setVisible(true);
        return true;
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch(item.getItemId()){
            case R.id.locate_me:

                permission = new MyPermission(SetPositionActivityTrader.this, this, location -> {

                    mMap.clear();
                    shop=mMap.addMarker(new MarkerOptions().position( new LatLng(location.getLatitude(),location.getLongitude() )));
                    shop.setDraggable(true);
                    toolbar_map.getMenu().findItem(R.id.confirm_position).setVisible(true);
                    setCameraView(location, null);

                });

                LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

                boolean bol=permission.checkMapServices(
                        "L'applicazione per settare la posizione del negozio in automatico ha bisogno che la geolocalizzazione sia attiva dalle impostazioni.",
                        "OK",manager, (dialog, which) -> {

                            Intent enableGpsIntent = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                            startActivityForResult(enableGpsIntent, MyPermission.PERMISSIONS_REQUEST_ENABLE_GPS);
                        });

                if (bol) {

                    if( !mLocationPermissionGranted ){
                        mLocationPermissionGranted=permission.getLocationPermission(
                                "L'applicazione per settare la posizione del negozio in automatico ha bisogno del permesso della posizione.",
                                "Hai rifiutato il permesso :( , dovrai settare la posizione manualmente o attivare il permesso dalle impostazioni di sistema",
                                "Ok", "Voglio proseguire senza permessi", (dialog, which) ->
                                        ActivityCompat.requestPermissions(SetPositionActivityTrader.this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, MyPermission.PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION));
                    }

                }
                permission.getPosition();

                break;

            case R.id.confirm_position:

                User user=UserClient.getUser();

                GeoPoint newPosition=new GeoPoint(shop.getPosition().latitude, shop.getPosition().longitude);

                if(user.getDelimited_area()==null || PolyUtil.containsLocation(new LatLng(newPosition.getLatitude(),newPosition.getLongitude()),user.convertDelimited_areaLatLng(),true) ){

                    UserClient.getUser().setTraderPosition(newPosition);


                    DocumentReference mDatabase = FirebaseFirestore.getInstance().collection("users").document(UserClient.getUser().getUser_id());
                    mDatabase.update("traderPosition", UserClient.getUser().getTraderPosition())
                            .addOnSuccessListener(aVoid -> {
                                startActivity(new Intent(getApplicationContext(), MapsActivityTrader.class));
                                Log.d(TAG, "POSIZIONE TRADER AGGIORNATA");
                            });
                }
                else {

                    AlertDialog.Builder builder = new AlertDialog.Builder(SetPositionActivityTrader.this);
                    builder.setTitle("Attenzione");
                    builder.setMessage("Il posizionamento selezionato comporterà l'eliminazione dell'area limitata.\n Vuoi procedere comunque?");

                    builder.setPositiveButton("Sì", (dialog, id) -> {

                        UserClient.getUser().setTraderPosition(newPosition);


                        DocumentReference mDatabase = FirebaseFirestore.getInstance().collection("users").document(UserClient.getUser().getUser_id());
                        mDatabase.update("traderPosition", UserClient.getUser().getTraderPosition())
                                .addOnSuccessListener(aVoid -> {
                                    startActivity(new Intent(getApplicationContext(), MapsActivityTrader.class));
                                    Log.d(TAG, "POSIZIONE TRADER AGGIORNATA");
                                });

                        mDatabase.update("delimited_area", null);
                        mDatabase.update("delimited_areaLatLng", null);
                        UserClient.getUser().setDelimited_area(null);

                    });
                    builder.setNegativeButton("No", (dialog, id) -> dialog.dismiss());
                    AlertDialog alert = builder.create();
                    alert.show();
                }

                break;
            case android.R.id.home:
                onBackPressed();
                break;
            default:
                super.onOptionsItemSelected(item);
                break;
        }

        return true;
    }

    @Override
    public void onBackPressed() {

        if(toolbar_map.getMenu().findItem(R.id.confirm_position).isVisible()) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("ATTENZIONE:");
            builder.setMessage("Sei sicuro di voler uscire dalla schermata senza confermare i cambiamenti?\n");

            builder.setPositiveButton("Sì", (dialog, id) -> {
                dialog.dismiss();

                finish();
            });
            builder.setNegativeButton("No", (dialog, id) -> {
                dialog.dismiss();
            });
            builder.create().show();
        }
        else{
            finish();
        }
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setOnMarkerDragListener(this);

        User user=UserClient.getUser();

        if( user.getTraderPosition()!=null ){

            shop=mMap.addMarker(new MarkerOptions().position(new LatLng(user.getTraderPosition().getLatitude(),user.getTraderPosition().getLongitude())));
            shop.setDraggable(true);
            setCameraView(null, shop.getPosition());
            areaLimitata();
        }

        mMap.setOnMapClickListener(latLng -> {
            shop.remove();
            shop=mMap.addMarker(new MarkerOptions().position(latLng));
            shop.setDraggable(true);
            toolbar_map.getMenu().findItem(R.id.confirm_position).setVisible(true);
            setCameraView(null, shop.getPosition());
        });

    }

    @Override
    public void onMarkerDragStart(Marker marker) {

    }

    @Override
    public void onMarkerDrag(Marker marker) {

    }

    @Override
    public void onMarkerDragEnd(Marker marker) {
        shop=marker;
        toolbar_map.getMenu().findItem(R.id.confirm_position).setVisible(true);
    }

    @Override
    protected void onActivityResult ( int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG, "onActivityResult: called.");
        if (requestCode == MyPermission.PERMISSIONS_REQUEST_ENABLE_GPS) {
            if (!mLocationPermissionGranted) {

                mLocationPermissionGranted=permission.getLocationPermission(
                        "L'applicazione per settare la posizione del negozio in automatico ha bisogno del permesso della posizione.",
                        "Hai rifiutato il permesso :( , dovrai settare la posizione manualmente o attivare il permesso dalle impostazioni di sistema",
                        "Ok", "Voglio proseguire senza permessi", (dialog, which) ->
                                ActivityCompat.requestPermissions(SetPositionActivityTrader.this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, MyPermission.PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION));
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode==MyPermission.PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION){
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                permission.getPosition();
            }
        }
    }

    private void setCameraView(Location location,LatLng latLng) {
        try {

            if(location!=null){
                double bottomBoundary = location.getLatitude() - .01;
                double leftBoundary = location.getLongitude() - .01;
                double topBoundary = location.getLatitude() + .01;
                double rightBoundary = location.getLongitude() + .01;

                LatLngBounds mMapBoundary = new LatLngBounds(
                        new LatLng(bottomBoundary, leftBoundary),
                        new LatLng(topBoundary, rightBoundary)
                );

                mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(mMapBoundary, 0));
            }

            if(latLng!=null){
                double bottomBoundary = latLng.latitude - .01;
                double leftBoundary = latLng.longitude - .01;
                double topBoundary = latLng.latitude + .01;
                double rightBoundary = latLng.longitude + .01;

                LatLngBounds mMapBoundary = new LatLngBounds(
                        new LatLng(bottomBoundary, leftBoundary),
                        new LatLng(topBoundary, rightBoundary)
                );

                mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(mMapBoundary, 0));
            }

        } catch (Exception ignored) {
        }
    }

    private void areaLimitata(){
        if(UserClient.getUser()!=null && UserClient.getUser().getDelimited_area()!=null){
            List<LatLng> latLngs = new ArrayList<>();

            List<GeoPoint> geoPoints = UserClient.getUser().getDelimited_area();
            for (GeoPoint a:geoPoints) {
                latLngs.add(new LatLng(a.getLatitude(),a.getLongitude()));
            }

            PolygonOptions polygonOptions=new PolygonOptions().addAll(latLngs).clickable(true);
            Polygon polygon=mMap.addPolygon(polygonOptions);
            polygon.setStrokeColor(Color.BLACK);
            polygon.setClickable(false);
        }
    }
}