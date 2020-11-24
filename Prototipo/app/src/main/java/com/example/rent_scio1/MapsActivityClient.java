package com.example.rent_scio1;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
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

import com.example.rent_scio1.services.MyLocationService;
import com.example.rent_scio1.utils.PermissionUtils;
import com.example.rent_scio1.utils.User;
import com.example.rent_scio1.utils.UserClient;
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
import com.google.android.gms.tasks.OnTokenCanceledListener;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;

public class MapsActivityClient extends AppCompatActivity implements OnMapReadyCallback, ActivityCompat.OnRequestPermissionsResultCallback, NavigationView.OnNavigationItemSelectedListener {

    private static final String TAG = "MapsActivityClient";
    private static final String ToQR="QR_code_creation";

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    public static final int ERROR_DIALOG_REQUEST = 9001;
    public static final int PERMISSIONS_REQUEST_ENABLE_GPS = 9002;
    public static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 9003;
    private static final int PERMISSIONS_REQUEST_ACCESS_CAMERA = 9004;
    private boolean mCameraPermissionGranted = false;
    private boolean mLocationPermissionGranted = false;

    private FirebaseFirestore mStore;
    private FusedLocationProviderClient mFusedLocationClient;
    private GoogleMap mMap;
    private LatLngBounds mMapBoundary;
    private Intent serviceIntent;
    private ArrayList<User> listTrader = new ArrayList<>();
    private NavigationView navigationView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps_client);
        Log.d(TAG, "CLIENTEEEEEEEEEOOOOOOOOOOOOOOOOOO ");

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        mStore = FirebaseFirestore.getInstance();
        serviceIntent = new Intent(this, MyLocationService.class);
        getCameraPermission();
        initViews();

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.mapDelimiter);
        mapFragment.getMapAsync(this);

    }

    private void initViews(){
        navigationView = findViewById(R.id.navigationView_Map_Client);
        TextView textView = navigationView.getHeaderView(0).findViewById(R.id.text_email_client);
        textView.setText(UserClient.getUser().getEmail());
        DrawerLayout drawer_map_trader = findViewById(R.id.drawer_map_client1);
        Toolbar toolbar = findViewById(R.id.toolbar_map_client);
        setSupportActionBar(toolbar);
        navigationView.setNavigationItemSelectedListener(this);
        if(UserClient.getRun() != null){
            navigationView.getMenu().findItem(R.id.Assistenza).setVisible(true);
            navigationView.getMenu().findItem(R.id.go_back_shop).setVisible(true);
            navigationView.getMenu().findItem(R.id.nuova_corsa_client).setVisible(false);
            Log.e(TAG, UserClient.getRun().toString());
        }
        else{
            navigationView.getMenu().findItem(R.id.Assistenza).setVisible(false);
            navigationView.getMenu().findItem(R.id.go_back_shop).setVisible(false);
            Log.e(TAG, "sono entrato nel ramo else");
        }

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer_map_trader, toolbar, R.string.drawer_open, R.string.drawer_close);
        drawer_map_trader.addDrawerListener(toggle);
        toggle.syncState();

    }



    ////////////////////////////////////////////////// FUNZIONI PER EVITARE IL BARCODE \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\
    private boolean isLocationServiceRunning() {
        ActivityManager manager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if ("com.example.rent_scio1.services.MyLocationService".equals(service.service.getClassName())) {
                Log.d(TAG, "isLocationServiceRunning: location service is already running.");
                return true;
            }
        }
        Log.d(TAG, "isLocationServiceRunning: location service is not running.");
        return false;
    }
    private void evitaBarcodeScanner(){
        String rawValue = "McjQ8VvrI2YRGboKYDuv26vMav52 2tGBtfOxbHcHNrKhQUtX 80000";
        if (!isLocationServiceRunning()) {
            serviceIntent.putExtra(TAG, rawValue);
            Log.w(TAG, rawValue);
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                MapsActivityClient.this.startForegroundService(serviceIntent);
            } else {
                Log.w(TAG, "parti cazzo");
                startService(serviceIntent);
            }
        }
    }
    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////


    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.logout_client:
                FirebaseAuth.getInstance().signOut();
                UserClient.setUser(null);

                startActivity(new Intent(getApplicationContext(), StartActivity.class));
                finishAffinity();

                break;
            case R.id.nuova_corsa_client:

                //TODO ATTENZIONE!!! REMINDER: SE SI VUOLE EVITARE IL BARCODE UTILIZZA COMM@GMAIL.COM E IL TRENO E DURATA 80000
                //evitaBarcodeScanner();

                Intent intent = new Intent(getApplicationContext(), ScannedBarcodeActivity.class);
                intent.putExtra(ToQR, ScannedBarcodeActivity.Action.ADD);
                startActivity(intent);

                if(UserClient.getRun() != null){

                    navigationView.getMenu().findItem(R.id.Assistenza).setVisible(true);
                    navigationView.getMenu().findItem(R.id.go_back_shop).setVisible(true);
                    navigationView.getMenu().findItem(R.id.nuova_corsa_client).setVisible(false);
                }
                else{
                    navigationView.getMenu().findItem(R.id.nuova_corsa_client).setVisible(true);
                }
                break;
            case R.id.Assistenza:
                help();
                break;
            case R.id.go_back_shop:
                returnShop();
                break;
            /*case R.id.terminate_run:
                terminate_run();
                break;*/
        }
        return true;
    }


    private void getCameraPermission() {
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) {
            mCameraPermissionGranted = true;
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA},
                    PERMISSIONS_REQUEST_ACCESS_CAMERA);
        }
    }

    private void returnShop() {
        //TODO: Molto diffcile ora come ora
    }

    private void help() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        Query getTrader = db.collection("users").whereEqualTo("user_id", UserClient.getRun().getTrader());
        getTrader.get().addOnSuccessListener(queryDocumentSnapshots -> {
            String phoneNumber = "";
            for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                phoneNumber = new User(document.toObject(User.class)).getPhone();
            }
            Intent intent = new Intent(Intent.ACTION_DIAL);
            intent.setData(Uri.parse("tel:" + phoneNumber));
            startActivity(intent);
        });
    }

    private void getPositionTrader(){
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        if(UserClient.getRun() != null){
            Query getTrader = db.collection("users").whereEqualTo("user_id", UserClient.getRun().getTrader());
            getTrader.get().addOnSuccessListener(queryDocumentSnapshots -> {
                for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                    listTrader.add(new User(document.toObject(User.class))) ;
                }
                setMarkerTrader();
            });
        }else{
            Query getTrader = db.collection("users").whereEqualTo("trader", true);
            getTrader.get().addOnSuccessListener(queryDocumentSnapshots -> {
                for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                    listTrader.add(new User(document.toObject(User.class))) ;
                }
                setMarkerTrader();
            });
        }
    }


    private void setMarkerTrader(){
        for (User trader : listTrader) {
            Log.d(TAG, "AGGIUNGO IIIIIIIIIII MARKERRRRRRRRRRRRRRRRRRR" + new LatLng(trader.getTraderposition().getLatitude(), trader.getTraderposition().getLongitude()));
            mMap.addMarker(new MarkerOptions()
                    .position(new LatLng(trader.getTraderposition().getLatitude(), trader.getTraderposition().getLongitude()))
                    .title(trader.getShopname())
                    .snippet("Negozio di: " + trader.getSourname() + " " + trader.getName()));
        }
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
                .setPositiveButton("Yes", (dialog, id) -> {
                    Intent enableGpsIntent = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    startActivityForResult(enableGpsIntent, PERMISSIONS_REQUEST_ENABLE_GPS);
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
            //getUserDetails();
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
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        mLocationPermissionGranted = false;
        if (requestCode == PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION) {// If request is cancelled, the result arrays are empty.
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                mLocationPermissionGranted = true;
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG, "onActivityResult: called.");
        if (requestCode == PERMISSIONS_REQUEST_ENABLE_GPS) {
            if (!mLocationPermissionGranted) {
                getLocationPermission();
            }
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        if (checkMapServices()) {
            if (!mLocationPermissionGranted) {
                getLocationPermission();
            }
        }
    }


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
                getLastKnownLocation();
                //getUserDetails();
            }
        } else {
            // Permission to access the location is missing. Show rationale and request permission
            PermissionUtils.requestPermission(this, LOCATION_PERMISSION_REQUEST_CODE,
                    Manifest.permission.ACCESS_FINE_LOCATION, true);
        }
    }

//    private void getUserDetails() {
//        if (UserClient.getRun() == null) {
//            DocumentReference userRef = mStore.collection("users").document(FirebaseAuth.getInstance().getUid());
//            userRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
//                @Override
//                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
//                    if (task.isSuccessful()) {
//                        Log.d(TAG, "onComplete: successfully get teh user details");
//                        User user = task.getResult().toObject(User.class);
//                        //mRun.setUser(user);
//                        UserClient.setUser(user);
//                        //getLastKnownLocation();
//                    }
//                }
//            });
//        } else {
//            //getLastKnownLocation();
//        }
//    }

    private void getLastKnownLocation() {
        Log.d(TAG, "getLastKnownLocation: called.");
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        //TODO MOVE CAMERA AUTOMATIc
        mFusedLocationClient.getCurrentLocation(LocationRequest.PRIORITY_HIGH_ACCURACY, new CancellationToken() {
            @Override
            public boolean isCancellationRequested() {
                return false;
            }

            @NonNull
            @Override
            public CancellationToken onCanceledRequested(@NonNull OnTokenCanceledListener onTokenCanceledListener) {
                return this;
            }
        }).addOnSuccessListener(this::setCameraView);
    }

    private void setCameraView(Location location) {
        try {

            double bottomBundary = location.getLatitude() - .01;
            double leftBoundary = location.getLongitude() - .01;
            double topBoundary = location.getLatitude() + .01;
            double rightBoundary = location.getLongitude() + .01;

            mMapBoundary = new LatLngBounds(
                    new LatLng(bottomBundary, leftBoundary),
                    new LatLng(topBoundary, rightBoundary)
            );

            mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(mMapBoundary, 0));
        } catch (Exception e) {
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (isLocationServiceRunning()) { //TODO: richimaare queste due righe al momento della terminazionedella corsa
            stopService(serviceIntent);
        }
    }

}