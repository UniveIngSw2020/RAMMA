package com.example.rent_scio1.utils.permissions;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.CancellationToken;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.OnTokenCanceledListener;
import com.google.android.gms.tasks.Task;

public class MyPermission {
    
    private static final String TAG="MyPermission";
    
    public static final int ERROR_DIALOG_REQUEST = 9001;
    public static final int PERMISSIONS_REQUEST_ENABLE_GPS = 9002;
    public static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 9003;

    private final Context context;
    private final Activity activity;
    private final OnSuccessListener<? super Location> listener;

    public MyPermission(Context context, Activity activity,OnSuccessListener<? super Location> listener) {
        this.context = context;
        this.activity = activity;
        this.listener=listener;
    }

    public void getPosition() {

        Log.d(TAG, "getLastKnownLocation: called.");
        FusedLocationProviderClient mFusedLocationClient = LocationServices.getFusedLocationProviderClient(activity);

        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            return;
        }

        Task<Location> l = mFusedLocationClient.getCurrentLocation(LocationRequest.PRIORITY_HIGH_ACCURACY, new CancellationToken() {
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

        });

        l.addOnSuccessListener(listener);

    }

    //prima del richiamo del metodo richiamare -> final LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
    public boolean checkMapServices(String text,String positiveArgs,LocationManager manager,DialogInterface.OnClickListener listener) {
        if (isServicesOK()) {
            return isMapsEnabled(text,positiveArgs,manager,listener);
        }
        return false;
    }


    private boolean isServicesOK() {
        Log.d(TAG, "isServicesOK: checking google services version");

        int available = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(context);

        if (available == ConnectionResult.SUCCESS) {
            //everything is fine and the user can make map requests
            Log.d(TAG, "isServicesOK: Google Play Services is working");
            return true;
        } else if (GoogleApiAvailability.getInstance().isUserResolvableError(available)) {
            //an error occured but we can resolve it
            Log.d(TAG, "isServicesOK: an error occured but we can fix it");
            Dialog dialog = GoogleApiAvailability.getInstance().getErrorDialog(activity, available, ERROR_DIALOG_REQUEST);
            dialog.show();
        } else {
            Toast.makeText(context, "You can't make map requests", Toast.LENGTH_SHORT).show();
        }
        return false;
    }

    private boolean isMapsEnabled(String text,String positiveArgs,LocationManager manager,DialogInterface.OnClickListener listener) {

        if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            buildAlertMessageNoGps(text,positiveArgs,listener);
            return false;
        }

        return true;
    }

    private void buildAlertMessageNoGps(String text,String positiveArgs,DialogInterface.OnClickListener listener) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage(text)
                .setCancelable(false)
                .setPositiveButton(positiveArgs, listener);

        final AlertDialog alert = builder.create();
        alert.show();
    }


    private void buildAlertMessageNoPermission(String text,String positiveArgs,DialogInterface.OnClickListener listener) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage(text)
                .setCancelable(false)
                .setPositiveButton(positiveArgs,listener);

        final AlertDialog alert = builder.create();
        alert.show();
    }

    public boolean getLocationPermission(String textNoPermission,String textPermissionDenied,String positiveArgsNoPermission, String negativeArgsPermissionDenied,DialogInterface.OnClickListener listener) {

        if(ContextCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if(!ActivityCompat.shouldShowRequestPermissionRationale(activity,android.Manifest.permission.ACCESS_FINE_LOCATION)){
                buildAlertMessageNoPermission(textNoPermission,positiveArgsNoPermission,listener);
                return false;
            }
            buildAlertMessagePermissionDenied(textPermissionDenied,negativeArgsPermissionDenied);
            return false;
        }
       return true;
    }


    private void buildAlertMessagePermissionDenied(String text, String negativeArgs){
        final AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage(text)
                .setCancelable(false)
                .setPositiveButton("Apri impostazioni", (dialog, id) -> {

                    Intent enableApplicationDetails = new Intent(Settings.ACTION_APPLICATION_SETTINGS);
                    activity.startActivity(enableApplicationDetails);
                })
                .setNegativeButton(negativeArgs, (dialog, which) -> {
                    //apri mappa in modo "limitato"
                });

        final AlertDialog alert = builder.create();
        alert.show();
    }

}
