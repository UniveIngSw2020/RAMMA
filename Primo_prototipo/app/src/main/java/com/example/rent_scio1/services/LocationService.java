package com.example.rent_scio1.services;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;

import com.example.rent_scio1.utils.User;
import com.example.rent_scio1.utils.UserClient;
import com.example.rent_scio1.utils.UserLocation;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;

import java.util.Objects;

public class LocationService extends Service {

    private static final String TAG = "LocationService";
    private FusedLocationProviderClient mFusedLocationClient;
    private LocationCallback mLocationCallback;
    private final static long UPDATE_INTERVAL = 4 * 1000;  /* 4 secs */
    private final static long FASTEST_INTERVAL = 2000;     /* 2 sec */

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate(){
        super.onCreate();
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        mLocationCallback = new LocationCallback(){
            @Override
            public void onLocationResult(LocationResult locationResult) {
                Log.d(TAG, " PPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPOSIZIONE PRESA");
                Location location = locationResult.getLastLocation();

                if (location != null) {
                    Log.d(TAG,"IIIIIIIIIIDDDDDDDDDDD" + FirebaseAuth.getInstance().getUid());
                    if(FirebaseAuth.getInstance().getUid() == null){
                        onDestroy();
                    }else {
                        User user = UserClient.getUser();
                        GeoPoint geoPoint = new GeoPoint(location.getLatitude(), location.getLongitude());
                        UserLocation userLocation = new UserLocation(geoPoint, null, user);
                        Log.d(TAG, user.toString());
                        saveUserLocation(userLocation);
                    }
                }else{
                    Log.d(TAG, " EEEEEEEEEEEEEEEEEEEEEEEEEEEEEEERRORE -> POSIZONE NON PRESA");
                }
            }
        };
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
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand: called.");
        getLocation();
        return START_NOT_STICKY;
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


    private void saveUserLocation(final UserLocation userLocation){
        /*try{*/
            DocumentReference locationRef = FirebaseFirestore.getInstance()
                    .collection("users_location")
                    .document(Objects.requireNonNull(FirebaseAuth.getInstance().getUid()));
            Log.d(TAG,userLocation.getUser().toString());
            locationRef.set(userLocation).addOnCompleteListener(task -> {
                if(task.isSuccessful()){
                    Log.d(TAG, "onComplete: \ninserted user location into database." +
                            "\n latitude: " + userLocation.getGeoPoint().getLatitude() +
                            "\n longitude: " + userLocation.getGeoPoint().getLongitude());
                }
            });
        /*}catch(NullPointerException e){
            Log.e(TAG, "saveUserLocation: User instance is null, stopping location service.");
            Log.e(TAG, "saveUserLocation: NullPointerException: "  + e.getMessage() );
            stopSelf();
        }*/
    }
    public static void stopLooper(){
        Looper.myLooper().quit();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mFusedLocationClient.removeLocationUpdates(mLocationCallback);
        stopForeground(true);//Add this. Since stopping a service in started in foreground is different from normal services.
        stopSelf();
        /*try {
            Looper.myLooper().getThread().join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }*/
        //TODO: E' IL LOOPER CHE CONTINUA A FAR ANDARE IL SERVIZIO SENZA FERMARLO, IL ONDESTROY NON ELIMINA NULLA
        Log.d(TAG,"SERVICE HAS BEEN DESTROYED!!!");
    }
}
