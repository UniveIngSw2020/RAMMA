package com.example.rent_scio1.Trader;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;

import android.widget.Button;


import com.example.rent_scio1.R;
import com.example.rent_scio1.utils.permissions.MyPermission;
import com.example.rent_scio1.utils.UserClient;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;

public class SetShopActivityTrader extends AppCompatActivity{

    private static final String TAG = "SetShopActivityTrader";

    private boolean mLocationPermissionGranted = false;

    MyPermission permission;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_shop_trader);
    }

    @Override
    protected void onResume() {
        super.onResume();

        permission=new MyPermission(SetShopActivityTrader.this,this, location -> {

            Log.d(TAG, String.valueOf(location));
            Log.d(TAG, "POSIZIONE: " + location.toString());

            UserClient.getUser().setTraderposition(new GeoPoint(location.getLatitude(), location.getLongitude()));

            DocumentReference mDatabase = FirebaseFirestore.getInstance().collection("users").document(UserClient.getUser().getUser_id());
            mDatabase.update("traderposition", UserClient.getUser().getTraderposition())
                    .addOnSuccessListener(aVoid -> {
                        startActivity(new Intent(getApplicationContext(), MapsActivityTrader.class));
                        Log.d(TAG, "POSIZIONE TRADER AGGIORNATA");
                    });

        });

        LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);


        Button setPositionManually = findViewById(R.id.setPositionManually);
        setPositionManually.setOnClickListener(v -> {

            Intent toSetPositionActivityTrader = new Intent(getApplicationContext(), SetPositionActivityTrader.class);
            startActivity(toSetPositionActivityTrader);
        });


        Button setPositionGPS = findViewById(R.id.setPositionGPS);
        setPositionGPS.setOnClickListener(v -> {

            Log.w(TAG,"Bottone premuto");

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
                                    ActivityCompat.requestPermissions(SetShopActivityTrader.this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, MyPermission.PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION));
                }

            }
            permission.getPosition();
        });
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
                                ActivityCompat.requestPermissions(SetShopActivityTrader.this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, MyPermission.PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION));
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
