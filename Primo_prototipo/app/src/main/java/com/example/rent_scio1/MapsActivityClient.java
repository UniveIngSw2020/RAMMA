package com.example.rent_scio1;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.example.rent_scio1.services.LocationService;
import com.example.rent_scio1.utils.PermissionUtils;
import com.example.rent_scio1.utils.User;
import com.example.rent_scio1.utils.UserClient;
import com.example.rent_scio1.utils.UserLocation;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.CancellationToken;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnTokenCanceledListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;

public class MapsActivityClient extends AppCompatActivity implements OnMapReadyCallback, ActivityCompat.OnRequestPermissionsResultCallback, NavigationView.OnNavigationItemSelectedListener {

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private static final String TAG = "MapsActivityClient: ";
    public static final int ERROR_DIALOG_REQUEST = 9001;
    public static final int PERMISSIONS_REQUEST_ENABLE_GPS = 9002;
    public static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 9003;
    private UserLocation mUserLocation;
    private FirebaseFirestore mStore;
    private boolean mLocationPermissionGranted = false;
    private FusedLocationProviderClient mFusedLocationClient;
    private GoogleMap mMap;
    private LatLngBounds mMapBoundary;
    private Intent serviceIntent;
    private ArrayList<User> listTrader = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps_client);
        Log.d(TAG, "CLIENTEEEEEEEEEOOOOOOOOOOOOOOOOOO ");

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        mStore = FirebaseFirestore.getInstance();

        initViews();

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.mapDelimiter);
        mapFragment.getMapAsync(this);
    }

    private void initViews(){
        NavigationView navigationView = findViewById(R.id.navigationView_Map_Client);
        TextView textView = (TextView) navigationView.getHeaderView(0).findViewById(R.id.text_email_client);
        textView.setText(FirebaseAuth.getInstance().getCurrentUser().getEmail());
        DrawerLayout drawer_map_trader = (DrawerLayout) findViewById(R.id.drawer_map_client1);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_map_client);
        setSupportActionBar(toolbar);
        navigationView.setNavigationItemSelectedListener(this);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer_map_trader, toolbar, R.string.drawer_open, R.string.drawer_close);
        drawer_map_trader.addDrawerListener(toggle);
        toggle.syncState();
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.logout_client:
                FirebaseAuth.getInstance().signOut();
                UserClient.setUser(null);
                startActivity(new Intent(getApplicationContext(), StartActivity.class));
                finishAffinity();
                break;
            case R.id.nuova_corsa:
                startActivity(new Intent(getApplicationContext(), QRScannerClient.class));
                break;
        }
        return true;
    }

    private void getPositionTrader(){
        Log.d(TAG, "CIAAAAAAAAAAAAAAAAAAAAAAAOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOO");
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        Query getTrader = db.collection("users").whereEqualTo("trader", true);
        getTrader.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Log.d(TAG, "DAIIIIIIIIIIIIIII: " + task.getResult());
                for (QueryDocumentSnapshot document : task.getResult()) {
                    listTrader.add(new User(document.toObject(User.class))) ;
                    Log.d(TAG, "CIAAAAAAAAAAAAAAAAAAAAAAAOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOO22222222222222 POSIZIONEEEEEEEEEEEEEEE");
                }
                setMarkerTrader();
            } else {
                Log.w(TAG, "-----------------------------------------------------------Error getting documents.", task.getException());
            }
        });
    }

    private void setMarkerTrader(){
        for(User trader : listTrader){
            Log.d(TAG, "AGGIUNGO IIIIIIIIIII MARKERRRRRRRRRRRRRRRRRRR" + new LatLng(trader.getTraderposition().getLatitude(), trader.getTraderposition().getLongitude()));
            mMap.addMarker(new MarkerOptions()
                    .position( new LatLng(trader.getTraderposition().getLatitude(), trader.getTraderposition().getLongitude()))
                    .title(trader.getShopname())
                    .snippet("Negozio di: " + trader.getSourname() + " " + trader.getName()));
        }
    }

    private void startLocationService() {
        if (!isLocationServiceRunning()) {
            serviceIntent = new Intent(this, LocationService.class);

            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {

                MapsActivityClient.this.startForegroundService(serviceIntent);
            } else {
                startService(serviceIntent);
            }
        }
    }

    private boolean isLocationServiceRunning() {
        ActivityManager manager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if ("com.codingwithmitch.googledirectionstest.services.LocationService".equals(service.service.getClassName())) {
                Log.d(TAG, "isLocationServiceRunning: location service is already running.");
                return true;
            }
        }
        Log.d(TAG, "isLocationServiceRunning: location service is not running.");
        return false;
    }

    private boolean checkMapServices() {
        if (isServicesOK()) {
            if (isMapsEnabled()) {
                return true;
            }
        }
        return false;
    }

    private void buildAlertMessageNoGps() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("This application requires GPS to work properly, do you want to enable it?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(@SuppressWarnings("unused") final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                        Intent enableGpsIntent = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        startActivityForResult(enableGpsIntent, PERMISSIONS_REQUEST_ENABLE_GPS);
                    }
                });
        final AlertDialog alert = builder.create();
        alert.show();
    }

    public boolean isMapsEnabled() {
        final LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            buildAlertMessageNoGps();
            return false;
        }
        return true;
    }

    private void getLocationPermission() {
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mLocationPermissionGranted = true;
            getUserDetails();
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }
    }

    public boolean isServicesOK() {
        Log.d(TAG, "isServicesOK: checking google services version");

        int available = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(MapsActivityClient.this);

        if (available == ConnectionResult.SUCCESS) {
            //everything is fine and the user can make map requests
            Log.d(TAG, "isServicesOK: Google Play Services is working");
            return true;
        } else if (GoogleApiAvailability.getInstance().isUserResolvableError(available)) {
            //an error occured but we can resolve it
            Log.d(TAG, "isServicesOK: an error occured but we can fix it");
            Dialog dialog = GoogleApiAvailability.getInstance().getErrorDialog(MapsActivityClient.this, available, ERROR_DIALOG_REQUEST);
            dialog.show();
        } else {
            Toast.makeText(this, "You can't make map requests", Toast.LENGTH_SHORT).show();
        }
        return false;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
        mLocationPermissionGranted = false;
        switch (requestCode) {
            case PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mLocationPermissionGranted = true;
                }
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG, "onActivityResult: called.");
        switch (requestCode) {
            case PERMISSIONS_REQUEST_ENABLE_GPS: {
                if (mLocationPermissionGranted) {
                    getUserDetails();
                } else {
                    getLocationPermission();
                }
            }
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        if (checkMapServices()) {
            if (mLocationPermissionGranted) {
                getUserDetails();
            } else {
                getLocationPermission();
            }
        }
    }


    @SuppressLint("MissingPermission")
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        enableMyLocation();
        getPositionTrader();
    }


    private void enableMyLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            if (mMap != null) {
                mMap.setMyLocationEnabled(true);
                getUserDetails();
            }
        } else {
            // Permission to access the location is missing. Show rationale and request permission
            PermissionUtils.requestPermission(this, LOCATION_PERMISSION_REQUEST_CODE,
                    Manifest.permission.ACCESS_FINE_LOCATION, true);
        }
    }

    private void getUserDetails() {
        if (mUserLocation == null) {
            mUserLocation = new UserLocation();
            DocumentReference userRef = mStore.collection("users").document(FirebaseAuth.getInstance().getUid());
            userRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "onComplete: successfully get teh user details");
                        User user = task.getResult().toObject(User.class);
                        mUserLocation.setUser(user);
                        UserClient.setUser(user);
                        getLastKnownLocation();
                    }
                }
            });
        } else {
            getLastKnownLocation();
        }
    }

    private void getLastKnownLocation() {
        Log.d(TAG, "getLastKnownLocation: called.");
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        mFusedLocationClient.getCurrentLocation(LocationRequest.PRIORITY_HIGH_ACCURACY, new CancellationToken() {
            @Override
            public boolean isCancellationRequested() {
                Log.d(TAG, " isCancellationRequested !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!! -> POSIZONE NON PRESA");
                return false;
            }

            @NonNull
            @Override
            public CancellationToken onCanceledRequested(@NonNull OnTokenCanceledListener onTokenCanceledListener) {
                Log.d(TAG, " onCanceledRequested %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%  -> POSIZONE NON PRESA");
                return null;
            }
        }).addOnCompleteListener(new OnCompleteListener<Location>() {
            @Override
            public void onComplete(@NonNull Task<Location> task) {
                if (task.isSuccessful()){
                    Location location = task.getResult();
                    Log.d(TAG, String.valueOf(location));
                    Log.d(TAG, "POSIZIONE: " + location.toString());
                    Log.d(TAG, " REGISTERRRRRRRR POSZIONE PRESA");
                    startLocationService();
                    mUserLocation = new UserLocation(new GeoPoint(location.getLatitude(), location.getLongitude()), null, UserClient.getUser());
                    setCameraView();            //TODO MOVE CAMERA AUTOMATIC
                }else{
                    Log.d(TAG, " REGISTERRRRRRRR EEEEEEEEEEEEEERRORE -> POSIZONE NON PRESA");
                }
            }
        });

    }

    private void setCameraView() {
        try {
            double bottomBundary = mUserLocation.getGeoPoint().getLatitude() - .01;
            double leftBoundary = mUserLocation.getGeoPoint().getLongitude() - .01;
            double topBoundary = mUserLocation.getGeoPoint().getLatitude() + .01;
            double rightBoundary = mUserLocation.getGeoPoint().getLongitude() + .01;

            mMapBoundary = new LatLngBounds(
                    new LatLng(bottomBundary, leftBoundary),
                    new LatLng(topBoundary, rightBoundary)
            );

            mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(mMapBoundary, 0));
        } catch (Exception e) {
        }
    }

    /*private void saveUserLocation(final UserLocation userLocation) {
        try {
            DocumentReference locationRef = FirebaseFirestore.getInstance()
                    .collection("users_location")
                    .document(Objects.requireNonNull(FirebaseAuth.getInstance().getUid()));
            Log.d(TAG, userLocation.getUser().toString());
            locationRef.set(userLocation).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Log.d(TAG, "onComplete: \ninserted user location into database." +
                            "\n latitude: " + userLocation.getGeoPoint().getLatitude() +
                            "\n longitude: " + userLocation.getGeoPoint().getLongitude());
                }
            });
        } catch (NullPointerException e) {
            Log.e(TAG, "saveUserLocation: User instance is null, stopping location service.");
            Log.e(TAG, "saveUserLocation: NullPointerException: " + e.getMessage());
        }
    }*/

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (isLocationServiceRunning()) {
           stopService(serviceIntent);
        }
    }

                // 7NimVBuSZVhBd6GT0fcsNDOewFo1 id trader comm@gmail.com
                /*FirebaseFirestore db = FirebaseFirestore.getInstance();
                Query getTrader = db.collection("users").whereEqualTo("user_id", "7NimVBuSZVhBd6GT0fcsNDOewFo1");  // TODO Prendere l'ID del commerciante giusto -> POSSIBILE SONO DOPO AVER ATTIVATO AL CORSA
                getTrader.get().addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        String phoneNumber = new String();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            phoneNumber = new User(document.toObject(User.class)).getPhone();

                            Log.d(TAG, "CIAAAAAAAAAAAAAAAAAAAAAAAOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOO phone:" + phoneNumber);
                        }
                        Intent intent = new Intent(Intent.ACTION_DIAL);
                        intent.setData(Uri.parse("tel:" + phoneNumber));
                        startActivity(intent);
                    } else {
                        Log.w(TAG, "-----------------------------------------------------------Error getting documents.", task.getException());
                    }
                });*/

}