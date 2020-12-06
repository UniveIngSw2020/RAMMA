package com.example.rent_scio1.Trader;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;
import android.widget.Toolbar;

import com.example.rent_scio1.R;
import com.example.rent_scio1.utils.UserClient;
import com.example.rent_scio1.utils.permissions.MyPermission;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;

public class SetPositionActivityTrader extends FragmentActivity implements OnMapReadyCallback,GoogleMap.OnMarkerDragListener {

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

    private void initViews(){
        toolbar_map = findViewById(R.id.toolbar_map_permission_delimited);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_map_permission, menu);
        toolbar_map.getMenu().findItem(R.id.confirm_position).setVisible(false);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        switch(item.getItemId()){
            case R.id.locate_me:

                permission = new MyPermission(SetPositionActivityTrader.this, this, location -> {

                    mMap.clear();
                    shop=mMap.addMarker(new MarkerOptions().position( new LatLng(location.getLatitude(),location.getLongitude() )));
                    toolbar_map.getMenu().findItem(R.id.confirm_position).setVisible(true);

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

                Toast.makeText(getApplicationContext(), "ciao sei nel locaTEme", Toast.LENGTH_LONG).show();
                break;

            case R.id.confirm_position:

                UserClient.getUser().setTraderposition(new GeoPoint(shop.getPosition().latitude, shop.getPosition().longitude));

                DocumentReference mDatabase = FirebaseFirestore.getInstance().collection("users").document(UserClient.getUser().getUser_id());
                mDatabase.update("traderposition", UserClient.getUser().getTraderposition())
                        .addOnSuccessListener(aVoid -> {
                            startActivity(new Intent(getApplicationContext(), MapsActivityTrader.class));
                            Log.d(TAG, "POSIZIONE TRADER AGGIORNATA");
                        });

                Toast.makeText(getApplicationContext(), "ciao sei nel confirm position", Toast.LENGTH_LONG).show();
                break;

            default:
                break;
        }

        return super.onOptionsItemSelected(item);
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

        mMap.setOnMapClickListener(latLng -> {
            mMap.clear();

            shop=mMap.addMarker(new MarkerOptions().position(latLng));
            shop.setDraggable(true);
            toolbar_map.getMenu().findItem(R.id.confirm_position).setVisible(true);
        });


        /*
        //TODO gettarsi la stringa
        Places.initialize(getApplicationContext(), "AIzaSyB4cHCVsFMmJEBrYM1lkNpG_BgwoxMM8vo");

        // Initialize the AutocompleteSupportFragment.
        AutocompleteSupportFragment autocompleteFragment = (AutocompleteSupportFragment) getSupportFragmentManager().findFragmentById(R.id.autocomplete_fragment);

        // Specify the types of place data to return.
        autocompleteFragment.setPlaceFields(Arrays.asList(Place.Field.ID, Place.Field.NAME));

        // Set up a PlaceSelectionListener to handle the response.
        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(@org.jetbrains.annotations.NotNull @NotNull Place place) {

                mMap.clear();
                shop=mMap.addMarker(new MarkerOptions().position(place.getLatLng()).title(place.getAddress()));
                shop.setDraggable(true);
            }

            @Override
            public void onError(@NonNull Status status) {
                // TODO: Handle the error.
                Log.i(TAG, "An error occurred : " + status);
            }
        });*/
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
}