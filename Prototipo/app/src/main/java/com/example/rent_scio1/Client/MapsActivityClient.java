package com.example.rent_scio1.Client;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.MenuItem;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.example.rent_scio1.Init.StartActivity;
import com.example.rent_scio1.R;
import com.example.rent_scio1.services.MyLocationService;
import com.example.rent_scio1.utils.permissions.MyPermission;
import com.example.rent_scio1.utils.User;
import com.example.rent_scio1.utils.UserClient;
import com.example.rent_scio1.utils.map.MyMapClient;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

public class MapsActivityClient extends AppCompatActivity implements ActivityCompat.OnRequestPermissionsResultCallback, NavigationView.OnNavigationItemSelectedListener {

    private static final String TAG = "MapsActivityClient";
    private static final String ToQR="QR_code_creation";

    //private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    public static final int ERROR_DIALOG_REQUEST = 9001;
    public static final int PERMISSIONS_REQUEST_ENABLE_GPS = 9002;
    public static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 9003;
    private static final int PERMISSIONS_REQUEST_ACCESS_CAMERA = 9004;

    private boolean mCameraPermissionGranted = false;
    private boolean mLocationPermissionGranted = false;
    //private MyMapClient myMapClient;


    //private Notification notification_delarea;
    //private FirebaseFirestore mStore;
    //private FusedLocationProviderClient mFusedLocationClient;
    //private GoogleMap mMap;
    //private LatLngBounds mMapBoundary;
    private Intent serviceIntent;
    //private ArrayList<User> listTrader = new ArrayList<>();
    private NavigationView navigationView;

    MyMapClient myMapClient;

    private ScannedBarcodeActivity.Action LastAction;

    //Polygon delimitedArea;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps_client);
        Log.d(TAG, "CLIENTEEEEEEEEEOOOOOOOOOOOOOOOOOO ");
    }

    @Override
    protected void onResume() {
        super.onResume();

        //mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        //mStore = FirebaseFirestore.getInstance();
        serviceIntent = new Intent(this, MyLocationService.class);
        //getCameraPermission();
        initViews();

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.mapDelimiter);
        /*Query query;
        if(UserClient.getRun() == null)
            query = mStore.collection("users").whereEqualTo("user_id", UserClient.getRun().getTrader());
        else
            query = mStore.collection("users").whereEqualTo("user_id", UserClient.getRun().getTrader());
        query.get().addOnSuccessListener(queryDocumentSnapshots -> {
            for (QueryDocumentSnapshot document : queryDocumentSnapshots) {

            }
        });*/
        // myMapClient = new MyMapClient(this.getApplicationContext());
        LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        assert mapFragment != null;

        myMapClient=new MyMapClient(MapsActivityClient.this,manager,(dialog, which) -> {

            Intent enableGpsIntent = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            startActivityForResult(enableGpsIntent, MyPermission.PERMISSIONS_REQUEST_ENABLE_GPS);
        });

        mapFragment.getMapAsync(myMapClient);

        /*if(UserClient.getRun() != null){
            notification_delarea = createNotificationChannel("delimitedAreaChannel", getString(R.string.delimitedAreaChannel), getString(R.string.delimitedAreaChannelD), R.drawable.ic_not_permitted);
        }*/
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == MyPermission.PERMISSIONS_REQUEST_ENABLE_GPS) {

            new MyPermission(MapsActivityClient.this, this, location -> { })
                    .getLocationPermission(
                            "L'applicazione per settare la posizione del negozio in automatico ha bisogno del permesso della posizione.",
                            "Hai rifiutato il permesso :( , dovrai settare la posizione manualmente o attivare il permesso dalle impostazioni di sistema",
                            "Ok", "Voglio proseguire senza permessi", (dialog, which) ->
                                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, MyPermission.PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION));

        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode==MyPermission.PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION){
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                myMapClient.location();
                Log.e(TAG,"SALUTO A TUTTI COME STATE");
            }
            else{
                recreate();
            }
        }

        if(requestCode==PERMISSIONS_REQUEST_ACCESS_CAMERA){
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                startActivityBarcode(LastAction);
            }
        }
    }

    private void initViews(){
        navigationView = findViewById(R.id.navigationView_Map_Client);
        TextView textView = navigationView.getHeaderView(0).findViewById(R.id.text_email_client);
        DrawerLayout drawer_map_trader = findViewById(R.id.drawer_map_client1);
        Toolbar toolbar = findViewById(R.id.toolbar_map_client);
        setSupportActionBar(toolbar);
        navigationView.setNavigationItemSelectedListener(this);
        if(UserClient.getRun() != null){
            navigationView.getMenu().findItem(R.id.Assistenza).setVisible(true);
            navigationView.getMenu().findItem(R.id.end_run).setVisible(true);
            navigationView.getMenu().findItem(R.id.go_back_shop).setVisible(true);
            navigationView.getMenu().findItem(R.id.nuova_corsa_client).setVisible(false);
            Log.e(TAG, UserClient.getRun().toString());
        }
        else{
            navigationView.getMenu().findItem(R.id.Assistenza).setVisible(false);
            navigationView.getMenu().findItem(R.id.go_back_shop).setVisible(false);
            navigationView.getMenu().findItem(R.id.end_run).setVisible(false);
            Log.e(TAG, "sono entrato nel ramo else");
        }

        if(UserClient.getUser()!=null)
            textView.setText(UserClient.getUser().getEmail());

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer_map_trader, toolbar, R.string.drawer_open, R.string.drawer_close);
        drawer_map_trader.addDrawerListener(toggle);
        toggle.syncState();

    }

    /*private Notification createNotificationChannel(String IDChannel, String nameNot, String descriptionNot, int icon) {
        if (Build.VERSION.SDK_INT >= 26) {

            CharSequence name = nameNot;
            String description = descriptionNot;


            NotificationChannel channel = new NotificationChannel(IDChannel, name, NotificationManager.IMPORTANCE_DEFAULT);

            channel.setDescription(description);


            Notification not = new NotificationCompat.Builder(this, "delimitedAreaChannel")
                    .setSmallIcon(icon)
                    .setContentTitle("My notification")
                    .setContentText("Hello World!")
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT).build();

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);

            Log.e(TAG, "ENTRATO QUA: createNotificationChannel");

            return not;
        }
        return null;
    }*/



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
        String rawValue = "McjQ8VvrI2YRGboKYDuv26vMav52 qBigNbdreFNjna5ufJKC 80000";
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
                LastAction= ScannedBarcodeActivity.Action.ADD;

                getCameraPermission();

                if(mCameraPermissionGranted) {
                    startActivityBarcode(LastAction);
                }

                break;
            case R.id.Assistenza:
                help();
                break;
            case R.id.go_back_shop:
                returnShop();
                break;
            case R.id.end_run:

                LastAction= ScannedBarcodeActivity.Action.DELETE;

                getCameraPermission();

                if(mCameraPermissionGranted) {
                    startActivityBarcode(LastAction);
                }

                break;
        }
        return true;
    }




    private void startActivityBarcode(ScannedBarcodeActivity.Action action){
        if(action==ScannedBarcodeActivity.Action.ADD){

            Intent intent;
            intent = new Intent(getApplicationContext(), ScannedBarcodeActivity.class);
            intent.putExtra(ToQR, ScannedBarcodeActivity.Action.ADD);
            startActivity(intent);

            if (UserClient.getRun() != null) {

                navigationView.getMenu().findItem(R.id.Assistenza).setVisible(true);
                navigationView.getMenu().findItem(R.id.go_back_shop).setVisible(true);
                navigationView.getMenu().findItem(R.id.end_run).setVisible(true);
                navigationView.getMenu().findItem(R.id.nuova_corsa_client).setVisible(false);
            } else {
                navigationView.getMenu().findItem(R.id.nuova_corsa_client).setVisible(true);
            }
        }
        else{
            Intent intent;
            intent = new Intent(getApplicationContext(), ScannedBarcodeActivity.class);
            intent.putExtra(ToQR, ScannedBarcodeActivity.Action.DELETE);
            startActivity(intent);
        }

    }

    private void buildAlertMessagePermissionDeniedCamera(){
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Hai rifiutato il permesso :( , se vuoi scanerizzare il QR devi attivare il permesso dalle impostazioni di sistema")
                .setCancelable(false)
                .setPositiveButton("Apri impostazioni", (dialog, id) -> {
                    Intent enableApplicationDetails = new Intent(Settings.ACTION_APPLICATION_SETTINGS);
                    startActivity(enableApplicationDetails);
                });

        final AlertDialog alert = builder.create();
        alert.show();
    }

    private void buildAlertMessageNoPermissionCamera() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("L'applicazione per scanerizzare il QR ha bisogno dei permessi per utilizzare la fotocamera")
                .setCancelable(false)
                .setPositiveButton("OK", (dialog, id) ->
                        ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.CAMERA}, PERMISSIONS_REQUEST_ACCESS_CAMERA));

        final AlertDialog alert = builder.create();
        alert.show();
    }

    private void getCameraPermission() {
        if(ContextCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            if(!ActivityCompat.shouldShowRequestPermissionRationale(this,android.Manifest.permission.CAMERA)){
                buildAlertMessageNoPermissionCamera();
                return;
            }
            buildAlertMessagePermissionDeniedCamera();
            return;
        }
        mCameraPermissionGranted=true;
    }






/*
    private boolean checkMapServices() {
        if (isServicesOK()) {
            return isMapsEnabled();
        }
        return false;
    }

    private void buildAlertMessageNoGps() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("\"L'applicazione per funzionare ha bisogno che la geolocalizzazione sia attiva dalle impostazioni.")
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
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mLocationPermissionGranted = true;
        } else {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
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
        switch (requestCode) {// If request is cancelled, the result arrays are empty.
            case PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mLocationPermissionGranted = true;
                }
                break;
            case PERMISSIONS_REQUEST_ACCESS_CAMERA:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mCameraPermissionGranted = true;
                }
                break;
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
*/

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


    /*@Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        enableMyLocation();
        getPositionTrader();

        Run run = UserClient.getRun();
        if (run != null) {

            NotificationCompat.Builder n=createNotificationChannel();

            long time=run.getStartTime() + run.getDuration() - Calendar.getInstance().getTime().getTime();

            CountDownTimer timerDelimitedArea = new CountDownTimer(time,10000) {
                @Override
                public void onTick(long millisUntilFinished) {

                    delimitedArea(run);
                    //TODO volendo si puo' gestire un po' meglio il discorso dell'id di notifica, così da fare azioni quando bla bla bla

                    startNotification(run, n,0);
                    Log.e(TAG,"TICK TIMER");
                }

                @Override
                public void onFinish() {

                }
            }.start();

        }
    }*/

    /*private NotificationCompat.Builder createNotificationChannel(){
        if (Build.VERSION.SDK_INT >= 26) {

            CharSequence name = getString(R.string.delimitedAreaChannel);
            String description = getString(R.string.delimitedAreaChannelD);

            int importance = NotificationManager.IMPORTANCE_DEFAULT;

            NotificationChannel channel = new NotificationChannel("delimitedAreaChannel", name, importance);

            channel.setDescription(description);

            Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

            NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "delimitedAreaChannel")
                    .setSmallIcon(R.drawable.ic_not_permitted)
                    .setContentTitle("My notification")
                    .setContentText("Hello World!")
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                    .setAutoCancel(true)
                    .setTimeoutAfter(60000)
                    .setSound(alarmSound);

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);

            // notificationId is a unique int for each notification that you must define
            Log.e(TAG,"ENTRATO QUA: createNotificationChannel");

            return builder;
        }

        return null;
    }*/





   /* private void enableMyLocation() {
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
    }*/

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

    /*private void getLastKnownLocation() {
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
    }*/

    /*private void setCameraView(Location location) {
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
    }*/


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (isLocationServiceRunning()) { //TODO: richimaare queste due righe al momento della terminazionedella corsa
            //myMapClient.stopNotification();
            stopService(serviceIntent);
        }
    }

}