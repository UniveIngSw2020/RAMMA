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
import com.example.rent_scio1.utils.Vehicle;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.maps.android.PolyUtil;

import java.util.Calendar;
import java.util.Objects;

import static com.example.rent_scio1.utils.map.MyMap.getmMap;

public class MyLocationService extends Service {

    private static final String TAG = "MyLocationService";

    private LocationManager mLocationManager = null;

    private static final int LOCATION_INTERVAL = 1000;
    private static final float LOCATION_DISTANCE = 10f;

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    private  long lastNotificationArea;
    private  long lastNotificationSpeed;
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
            int speed= (int)(mLastLocation.getSpeed() *3.6);

            updateUserLocation(location, speed);
            setCameraView(location);
            Log.e(TAG,"TIME: "+ (Calendar.getInstance().getTime().getTime() - lastNotificationArea));
            if(Calendar.getInstance().getTime().getTime() - lastNotificationArea > 30000){
                lastNotificationArea = Calendar.getInstance().getTime().getTime();
                checkAreaLimit(location);
            }

            if(Calendar.getInstance().getTime().getTime() - lastNotificationSpeed > 30000) {
                lastNotificationSpeed = Calendar.getInstance().getTime().getTime();
                checkSpeedLimit(speed);
            }

        }


        @Override
        public void onProviderEnabled(@NonNull String provider) { }

        @Override
        public void onProviderDisabled(@NonNull String provider) { }


        private void setCameraView(Location location) {
            try {
                double bottomBundary = location.getLatitude() - .01;
                double leftBoundary = location.getLongitude() - .01;
                double topBoundary = location.getLatitude() + .01;
                double rightBoundary = location.getLongitude() + .01;

                LatLngBounds mMapBoundary = new LatLngBounds(
                        new LatLng(bottomBundary, leftBoundary),
                        new LatLng(topBoundary, rightBoundary)
                );

                getmMap().moveCamera(CameraUpdateFactory.newLatLngBounds(mMapBoundary, 0));
            } catch (Exception e) {
            }
        }


        private void updateUserLocation(Location location, int speed){
            if(UserClient.getRun() != null) {
                Log.w(TAG, "update user location ");
                GeoPoint geoPoint = new GeoPoint(location.getLatitude(), location.getLongitude());

                UserClient.getRun().setGeoPoint(geoPoint);
                UserClient.getRun().setSpeed(speed);

                DocumentReference mDatabase = db.collection("run").document(UserClient.getRun().getRunUID());
                mDatabase.update("geoPoint", geoPoint).addOnSuccessListener(aVoid -> Log.e(TAG, "push posizione"));
                mDatabase.update("speed", speed).addOnSuccessListener(aVoid -> Log.e(TAG, "push speed"));
            }
        }

        private void checkAreaLimit(@NonNull Location location){
            LatLng position = new LatLng(location.getLatitude(), location.getLongitude());
            Log.e(TAG,"PRE notifica area limitata");

            if(UserClient.getTrader() != null && UserClient.getTrader().getDelimited_area() != null && !PolyUtil.containsLocation(position, UserClient.getTrader().convertDelimited_areaLatLng(), true)) {
                Log.e(TAG,"ENTRATO QUA: notifica area limitata");

                //TODO QUESTO CICLO POTREBBE SOSTITUIRE TUTTO IL MyNotify
                for (String token : UserClient.getUser().getTokens()){
                    Log.e(TAG, "messaggio per il cliente");
                    MyFirebaseMessagingServices.sendNotification(MyLocationService.this, token, "Ciao fra", "sei fuori dall'area consentita"); // Eventualmente si può prendere il token corrente e on complete inviare la notifica solo allo smartphone corrente
                }


                for (String token : UserClient.getTrader().getTokens()){
                    Log.e(TAG, "messaggio per il trader");
                    MyFirebaseMessagingServices.sendNotification(MyLocationService.this, token, "Ciao fra", "Il Cliente" + UserClient.getUser().getName() + " fa il furbo e scappa");
                }


            }
        }

        private void checkSpeedLimit(double speed){
            Log.e(TAG,"PRE notifica velocità");
            db.collection("vehicles").document(UserClient.getRun().getVehicle()).get().addOnSuccessListener(documentSnapshot -> {
                Log.e(TAG,"ENTRATO QUA: notifica velocità");
                if(speed > Objects.requireNonNull(documentSnapshot.toObject(Vehicle.class)).getMaxSpeedKMH()) {
                    for (String token : UserClient.getUser().getTokens()) // Eventualmente si può prendere il token corrente e on complete inviare la notifica solo allo smartphone corrente
                        MyFirebaseMessagingServices.sendNotification(MyLocationService.this, token, "Ciao fra", "Vai piano per piacere");

                    if (UserClient.getTrader() != null) {
                        for (String token : UserClient.getTrader().getTokens())
                            MyFirebaseMessagingServices.sendNotification(MyLocationService.this, token, "Ciao fra", "Il Cliente" + UserClient.getUser().getName() + " sta correndo come un pazzo");
                    }
                }
            });
        }

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
        boolean runAlreadyInsert = intent.getBooleanExtra("LoginActivity", false);
        if(!runAlreadyInsert) {
            final String rawValue = intent.getStringExtra("ScannedBarcodeActivity");
            String user = UserClient.getUser().getUser_id();
            String idComm = rawValue.split(" ")[0];
            String idVehicle = rawValue.split(" ")[1];
            long duration = Long.parseLong(rawValue.split(" ")[2]);
            //Crea un nuovo documento vuoto
            DocumentReference ref = db.collection("vehicles").document();

            //Prendo l'id del documento che conterrà la nuova corsa
            String runUID = ref.getId();
            UserClient.setRun(new Run(null, null, user, idComm, idVehicle, runUID, Calendar.getInstance().getTime().getTime(),duration,0));

            //Salvo la corsa con tutte le informazioni accetto la posizione
            addRunIntoDB(UserClient.getRun());
            lockVehiclebyID(idVehicle);
            Log.e(TAG, "onStartCommand");

        }
        lastNotificationArea = Calendar.getInstance().getTime().getTime();
        lastNotificationSpeed = Calendar.getInstance().getTime().getTime();
        if(UserClient.getTrader() == null)
            storeTraderInfo(UserClient.getRun().getTrader());

        super.onStartCommand(intent, flags, startId);

        return START_STICKY;

    }

    private void storeTraderInfo(String id){
        db.collection("users").document(id).get().addOnSuccessListener(documentSnapshot -> {
            Log.d(TAG, "Trader salvato in locale");
            UserClient.setTrader(documentSnapshot.toObject(User.class));
        });
    }

    private void lockVehiclebyID(String id){
        Log.d(TAG, "cerco di lockare il veicolo");
        DocumentReference mDatabase = FirebaseFirestore.getInstance().collection("vehicles").document(id);
        mDatabase.update("rented", true).addOnSuccessListener(aVoid -> Log.d(TAG, "VEICOLO OCCUPATO"));
    }


    private void addRunIntoDB(final Run run){
        try{
            DocumentReference locationRef = db.collection("run").document(run.getRunUID());
            Log.d(TAG, run.getUser());
            locationRef.set(run).addOnCompleteListener(task -> {
                Log.d(TAG,"run pushata");
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
                    "Posizione",
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

        /* TODO TEMPORANEAMENTE COMMENTATO
        unlockVehiclebyID(UserClient.getRun().getVehicle());

        deleteRun(UserClient.getRun().getRunUID());

        MyMapClient.stopNotification();
        */
        stopForeground(true);

        stopSelf();

        if (mLocationManager != null) {
            for (LocationListener mLocationListener : mLocationListeners) {
                try {
                    if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        return;
                    }
                    mLocationManager.removeUpdates(mLocationListener);
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