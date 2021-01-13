package com.example.rent_scio1.Trader;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.example.rent_scio1.Client.MapsActivityClient;
import com.example.rent_scio1.Init.StartActivity;
import com.example.rent_scio1.R;
import com.example.rent_scio1.services.ExitService;
import com.example.rent_scio1.services.MyFirebaseMessagingServices;
import com.example.rent_scio1.utils.UserClient;
import com.example.rent_scio1.utils.map.MyMapTrader;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessaging;

public class MapsActivityTrader extends AppCompatActivity implements ActivityCompat.OnRequestPermissionsResultCallback, NavigationView.OnNavigationItemSelectedListener {



    private FirebaseAuth mAuth;
    //private static final String TAG = "MapsActivityTrader";
    public Context thisContext;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps_trader);

        mAuth = FirebaseAuth.getInstance();

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.mapDelimiter);
        mapFragment.getMapAsync(new MyMapTrader(MapsActivityTrader.this));
        thisContext = MapsActivityTrader.this;
        startService(new Intent(MapsActivityTrader.this, MyFirebaseMessagingServices.class));
        startService(new Intent(MapsActivityTrader.this, ExitService.class));
    }

    @Override
    protected void onResume() {
        super.onResume();
        initViews();
    }

    private void initViews(){
        NavigationView navigationView = findViewById(R.id.navigationView_Map_Trader);
        TextView textView = (TextView) navigationView.getHeaderView(0).findViewById(R.id.text_email_trader);
        textView.setText(mAuth.getCurrentUser().getEmail());
        DrawerLayout drawer_map_trader = (DrawerLayout) findViewById(R.id.drawer_map_trader1);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_map_trader);
        setSupportActionBar(toolbar);
        navigationView.setNavigationItemSelectedListener(this);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer_map_trader, toolbar, R.string.drawer_open, R.string.drawer_close);
        drawer_map_trader.addDrawerListener(toggle);
        toggle.syncState();
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.logout:

                logout();

                //finishAffinity();
                break;
            case R.id.nuova_corsa:
                startActivity(new Intent(getApplicationContext(), NewRunActivityTrader.class));
                break;
            case R.id.Parco_mezzi:
                startActivity(new Intent(getApplicationContext(), VehicleListActivityTrader.class));
                break;
            case R.id.area_limited:
                startActivity(new Intent(getApplicationContext(), DelimitedAreaActivityTrader.class));
                break;
            case R.id.tabella_corse:
                startActivity(new Intent(getApplicationContext(), RunTableTrader.class));
                break;
            case R.id.impostazioni_trader:
                startActivity(new Intent(getApplicationContext(), SettingsTrader.class));
                break;
        }
        return true;
    }

    public void logout(){
        final String TAG = "deleteCurrentToken";
        Log.e(TAG, "CIAO CIAO TOKEN");
        if(UserClient.getUser() != null && UserClient.getUser().getTokens() != null){
            FirebaseMessaging.getInstance().getToken().addOnSuccessListener(s -> {
                UserClient.getUser().getTokens().remove(s);
                DocumentReference mDatabase = FirebaseFirestore.getInstance().collection("users").document(UserClient.getUser().getUser_id());
                mDatabase.update("tokens", UserClient.getUser().getTokens())
                        .addOnSuccessListener(aVoid -> Log.d(TAG, "Token rimosso correttamente"))
                        .addOnFailureListener(error -> Log.e(TAG, "Errore nella rimozione del token"))
                        .addOnCompleteListener(complete -> {
                            FirebaseAuth.getInstance().signOut();
                            getSharedPreferences("loginPrefs", MODE_PRIVATE).edit().clear().apply();
                            UserClient.setUser(null);
                            finishAffinity();
                            startActivity(new Intent(getApplicationContext(), StartActivity.class));
                            Log.d(TAG, "terminato tentativo di rimozione token");
                        });
            });
        }
    }

}