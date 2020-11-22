package com.example.rent_scio1.services;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;

import com.example.rent_scio1.utils.Run;
import com.example.rent_scio1.utils.User;
import com.example.rent_scio1.utils.UserClient;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;

import java.util.Calendar;
import java.util.List;
import java.util.Objects;

public class MyLocationService extends Service {

    private static final String TAG = "MyLocationService";
    private LocationManager mLocationManager = null;
    private static final int LOCATION_INTERVAL = 1000;
    private static final float LOCATION_DISTANCE = 10f;
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    private boolean runAlreadyInsert = false;

    private class LocationListener implements android.location.LocationListener{
        Location mLastLocation;

        public LocationListener(String provider) {
            Log.e(TAG, "LocationListener " + provider);
            mLastLocation = new Location(provider);
        }

        @Override
        public void onLocationChanged(@NonNull Location location) {
            Log.e(TAG, "POSIZIONE CAMBIATA");
            mLastLocation.set(location);
            updateUserLocation(location);
//            stopService();
        }

        private void updateUserLocation(Location location){
            if(UserClient.getRun() != null) {
                Log.w(TAG, "update user location ");
                GeoPoint geoPoint = new GeoPoint(location.getLatitude(), location.getLongitude());

                UserClient.getRun().setGeoPoint(geoPoint);
                DocumentReference mDatabase = db.collection("run").document(UserClient.getRun().getRunUID());
                mDatabase.update("geoPoint", geoPoint).addOnSuccessListener(aVoid -> Log.e(TAG, "Cazzo si"));
            }
        }
//        private void stopService(){
//            if(UserClient.getUser() == null || UserClient.getRun() == null){
//                stopForeground(true);
//                stopSelf();
//            }
//        }

    }

    LocationListener[] mLocationListeners = new LocationListener[]{
            new LocationListener(LocationManager.PASSIVE_PROVIDER)
    };
    public MyLocationService() {
    }



    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if(!runAlreadyInsert) {
            final String rawValue = intent.getStringExtra("QRScannerClient");       //TODO PER FAR FUNZIONARE IL SALTA SCANNER -> "MapsActivityClient"
            String user = UserClient.getUser().getUser_id();
            String idComm = rawValue.split(" ")[0];
            String idVehicle = rawValue.split(" ")[1];
            long duration = Long.parseLong(rawValue.split(" ")[2]);
            runAlreadyInsert = true;
            //Crea un nuovo documento vuoto
            DocumentReference ref = db.collection("vehicles").document();

            //Prendo l'id del documento che conterrà la nuova corsa
            String runUID = ref.getId();
            UserClient.setRun(new Run(null, null, user, idComm, idVehicle, runUID, Calendar.getInstance().getTime().getTime(),duration));

            //Salvo la corsa con tutte le informazioni accetto la posizione
            addRunIntoDB(UserClient.getRun());
            lockVehiclebyID(idVehicle);
            Log.e(TAG, "onStartCommand");

        }
        super.onStartCommand(intent, flags, startId);

        return START_STICKY;

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

    private void addRunIntoDB(final Run run){
        try{
            DocumentReference locationRef = db.collection("run").document(run.getRunUID());
            Log.d(TAG, run.getUser());
            locationRef.set(run).addOnCompleteListener(task -> {
                if(task.isSuccessful()){

                }
            });
        }catch(NullPointerException e){
            Log.e(TAG, "saveUserLocation: User instance is null, stopping location service.");
            Log.e(TAG, "saveUserLocation: NullPointerException: "  + e.getMessage() );
        }
    }


    @Override
    public void onCreate() {

        Log.e(TAG, "onCreate");

        initializeLocationManager();

        try {
            mLocationManager.requestLocationUpdates(
                    LocationManager.PASSIVE_PROVIDER,
                    LOCATION_INTERVAL,
                    LOCATION_DISTANCE,
                    mLocationListeners[0]
            );
        } catch (java.lang.SecurityException ex) {
            Log.i(TAG, "fail to request location update, ignore", ex);
        } catch (IllegalArgumentException ex) {
            Log.d(TAG, "network provider does not exist, " + ex.getMessage());
        }

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

    }


    @Override
    public void onDestroy() {
        Log.e(TAG, "onDestroy");
        stopForeground(true);
        if (mLocationManager != null) {
            for (int i = 0; i < mLocationListeners.length; i++) {
                try {
                    if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        return;
                    }
                    mLocationManager.removeUpdates(mLocationListeners[i]);
                } catch (Exception ex) {
                    Log.i(TAG, "fail to remove location listener, ignore", ex);
                }
            }
        }
        super.onDestroy();
    }



    private void initializeLocationManager() {
        Log.e(TAG, "initializeLocationManager - LOCATION_INTERVAL: "+ LOCATION_INTERVAL + " LOCATION_DISTANCE: " + LOCATION_DISTANCE);
        if (mLocationManager == null) {
            mLocationManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
        }
    }
}