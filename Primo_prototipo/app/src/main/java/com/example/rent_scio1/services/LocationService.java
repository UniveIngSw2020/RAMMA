package com.example.rent_scio1.services;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.icu.util.Calendar;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;

import com.example.rent_scio1.utils.Run;
import com.example.rent_scio1.utils.UserClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;

import java.util.Map;
import java.util.Objects;
import java.util.Observable;

public class LocationService extends Service {

    private static final String TAG = "LocationService";
    private FusedLocationProviderClient mFusedLocationClient;
    private LocationCallback mLocationCallback;
    private final static long UPDATE_INTERVAL = 4 * 1000;  /* 4 secs */
    private final static long FASTEST_INTERVAL = 2000;     /* 2 sec */
    private LatLngBounds mMapBoundary;
    private String idVehicle = new String();
    private String idComm ;

    //private Intent service = getApplicationContext(this);

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate(){
        super.onCreate();
        if (Build.VERSION.SDK_INT >= 26) {
            String CHANNEL_ID = "my_channel_01";
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID,
                    "My Channel",
                    NotificationManager.IMPORTANCE_DEFAULT);

            ((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE)).createNotificationChannel(channel);

            Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                    .setContentTitle("")
                    .setContentText("").build();

            startForeground(1, notification);
        }

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand: called.");
        final String rawValue = intent.getStringExtra("QRScannerClient");
        idComm = rawValue.split(" ")[0];
        idVehicle = rawValue.split(" ")[1];

        lockVehiclebyID(idVehicle);


        mLocationCallback = new LocationCallback(){
            @Override
            public void onLocationResult(LocationResult locationResult) {
                Log.d(TAG, " PPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPOSIZIONE PRESA");
                Location location = locationResult.getLastLocation();
                if (location != null) {
                    Log.d(TAG,"IIIIIIIIIIDDDDDDDDDDD" + FirebaseAuth.getInstance().getUid());
                    if(FirebaseAuth.getInstance().getUid() == null){
                        stopRequest();

                    }else {
                        String user = UserClient.getUser().getUser_id();
                        GeoPoint geoPoint = new GeoPoint(location.getLatitude(), location.getLongitude());
                        Log.d(TAG, "CREATA LA RUNNNNNNNNNNNNNNNNNNNNNNNNNNNNN");
                        UserClient.setRun(new Run(geoPoint, null, user, idComm , idVehicle, Calendar.getInstance().getTime(),80000));

                        saveUserLocation(UserClient.getRun());
                    }
                }else{
                    Log.d(TAG, " EEEEEEEEEEEEEEEEEEEEEEEEEEEEEEERRORE -> POSIZONE NON PRESA");
                }
            }

        };

        getLocation();
/*
        DocumentReference locationRef = FirebaseFirestore.getInstance()
                .collection("run")
                .document(Objects.requireNonNull(FirebaseAuth.getInstance().getUid()));
        run.setTrader(rawValue.split(" ")[0]);
        run.setVehicle(rawValue.split(" ")[1]);
        UserClient.getUser().setFk_run(run);
        locationRef.set(run).addOnCompleteListener(task -> {
            if(task.isSuccessful()){
                Log.d(TAG, "onComplete: \ninserted tarder_uid and vehicle_uid into database." +
                        "\n tarder_uid: " + run.getTrader() +
                        "\n vehicle_uid: " + run.getUser());
            }
        });*/
        return START_NOT_STICKY;
    }


    private void lockVehiclebyID(String id){
        Log.d(TAG, "cerco di lockare il veicolo");
        DocumentReference mDatabase = FirebaseFirestore.getInstance().collection("vehicles").document(id);
        mDatabase.update("rented", true).addOnSuccessListener(aVoid -> Log.d(TAG, "VEICOLO OCCUPATO"));
    }

    private void unlockVehiclebyID(String id){
        DocumentReference mDatabase = FirebaseFirestore.getInstance().collection("vehicles").document(id);
        mDatabase.update("rented", false).addOnSuccessListener(aVoid -> Log.d(TAG, "VEICOLO LIBERATO"));
    }

    private void getLocation () {
        // Create the location request to start receiving updates
        LocationRequest mLocationRequestHighAccuracy = new LocationRequest();
        mLocationRequestHighAccuracy.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequestHighAccuracy.setInterval(UPDATE_INTERVAL);
        mLocationRequestHighAccuracy.setFastestInterval(FASTEST_INTERVAL);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "getLocation: stopping the location service.");
            stopSelf();
            return;
        }

        mFusedLocationClient.requestLocationUpdates(mLocationRequestHighAccuracy, mLocationCallback, Looper.myLooper());// Looper.myLooper tells this to repeat forever until thread is destroyed

    }


    private void saveUserLocation(final Run run){
        try{
            DocumentReference locationRef = FirebaseFirestore.getInstance()
                    .collection("run")
                    .document(Objects.requireNonNull(FirebaseAuth.getInstance().getUid()));
            Log.d(TAG, run.getUser().toString());
            locationRef.set(run).addOnCompleteListener(task -> {
                if(task.isSuccessful()){
                    Log.d(TAG, "onComplete: \ninserted user location into database." +
                            "\n latitude: " + run.getGeoPoint().getLatitude() +
                            "\n longitude: " + run.getGeoPoint().getLongitude());
                }
            });
        }catch(NullPointerException e){
            Log.e(TAG, "saveUserLocation: User instance is null, stopping location service.");
            Log.e(TAG, "saveUserLocation: NullPointerException: "  + e.getMessage() );
            stopSelf();
        }
    }


    public void stopRequest(){
        mFusedLocationClient.removeLocationUpdates(mLocationCallback);
        unlockVehiclebyID(idVehicle);
    }

    @Override
    public void onDestroy() {

        stopForeground(true);//Add this. Since stopping a service in started in foreground is different from normal services.
        stopSelf();
        Log.d(TAG,"SERVICE HAS BEEN DESTROYED!!!");
    }



    /*private LatLngBounds setCameraView(){
        try {
            double bottomBundary = mRun.getGeoPoint().getLatitude() - .01;
            double leftBoundary = mRun.getGeoPoint().getLongitude() - .01;
            double topBoundary = mRun.getGeoPoint().getLatitude() + .01;
            double rightBoundary = mRun.getGeoPoint().getLongitude() + .01;

            mMapBoundary = new LatLngBounds(
                    new LatLng(bottomBundary, leftBoundary),
                    new LatLng(topBoundary, rightBoundary)
            );


        }catch (Exception e){
            Log.d(TAG, "Errore setCameraView");
        }finally {
            return mMapBoundary;
        }
    }*/

}
