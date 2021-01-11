package com.example.rent_scio1.Client;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.provider.Settings;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.example.rent_scio1.Init.StartActivity;
import com.example.rent_scio1.R;
import com.example.rent_scio1.services.ExitService;
import com.example.rent_scio1.services.MyFirebaseMessagingServices;
import com.example.rent_scio1.utils.Pair;
import com.example.rent_scio1.utils.Run;
import com.example.rent_scio1.utils.User;
import com.example.rent_scio1.utils.UserClient;
import com.example.rent_scio1.utils.Vehicle;
import com.example.rent_scio1.utils.map.MyMapClient;
import com.example.rent_scio1.utils.permissions.MyPermission;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Objects;
import java.util.Random;

public class MapsActivityClient extends AppCompatActivity implements ActivityCompat.OnRequestPermissionsResultCallback, NavigationView.OnNavigationItemSelectedListener {

    private static final String TAG = "MapsActivityClient";
    private static final String ToQR="QR_code_creation";

    private static final int PERMISSIONS_REQUEST_ACCESS_CAMERA = 9004;

    private boolean mCameraPermissionGranted = false;
    private NavigationView navigationView;

    MyMapClient myMapClient;

    private FirebaseAuth mAuth;

    private ScannedBarcodeActivity.Action LastAction;


    private final ArrayList<Pair<User, Pair<Float, Polygon>>> listTrader = new ArrayList<>();
    //private Vehicle v = null;

    //Polygon delimitedArea;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps_client);

        mAuth = FirebaseAuth.getInstance();
        Log.d(TAG, "CLIENTEEEEEEEEEOOOOOOOOOOOOOOOOOO ");
        createTable();
        getListTrader();
    }

    @Override
    protected void onResume() {

        super.onResume();
        Log.e(TAG, "sono nel resume ");
        initViews();
        startService(new Intent(this, MyFirebaseMessagingServices.class));
        startService(new Intent(this, ExitService.class));

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

    private void getListTrader(){
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        Query getTrader;

        if(UserClient.getRun() != null){
            getTrader = db.collection("users").whereEqualTo("user_id", UserClient.getRun().getTrader());
        }else{
            getTrader = db.collection("users").whereEqualTo("trader", true);
        }


        getTrader.get().addOnSuccessListener(queryDocumentSnapshots -> {
            for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                User u=new User(document.toObject(User.class));
                if(u.getTraderPosition()!=null){
                    Random rnd = new Random();
                    listTrader.add(new Pair<>(u, new Pair<>((float) rnd.nextInt(360), null)));
                }

            }

            /*if(UserClient.getRun() != null){
                Query getVehicle= db.collection("vehicles").whereEqualTo("user_id", UserClient.getRun().getVehicle());

                getVehicle.get().addOnSuccessListener(queryDocumentSnapshots1 -> {
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        v = new Vehicle(document.toObject(Vehicle.class));
                    }
                    openMap();
                });
            }else{
                openMap();
            }*/
            openMap();
        });
    }

    private void openMap(){
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.mapDelimiter);

        LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        assert mapFragment != null;

        myMapClient=new MyMapClient(MapsActivityClient.this,manager,(dialog, which) -> {

            Intent enableGpsIntent = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            startActivityForResult(enableGpsIntent, MyPermission.PERMISSIONS_REQUEST_ENABLE_GPS);
        }, listTrader);
        Log.e(TAG, "aziono la mappa ");
        mapFragment.getMapAsync(myMapClient);
    }

    private void updateTime(TextView timeText,TextView speedText, Run run){

        long time=run.getStartTime() + run.getDuration() - Calendar.getInstance().getTime().getTime();

        new CountDownTimer(time, 100) {
            @SuppressLint({"DefaultLocale", "SetTextI18n"})
            @Override
            public void onTick(long millisUntilFinished) {

                int minutes=(int) (millisUntilFinished / 1000) / 60;
                int seconds=(int) (millisUntilFinished / 1000) % 60;
                int speed=run.getSpeed();

                if(minutes>=60){
                    int hours=minutes/60;
                    minutes=minutes-(hours*60);

                    String hoursText=""+hours;
                    if(hours<10){

                        hoursText="0"+hoursText;
                    }


                    String minutesText=""+minutes;
                    if(minutes<10){

                        minutesText="0"+minutesText;
                    }

                    String secondText=""+seconds;
                    if(seconds<10){

                        secondText="0"+seconds;
                    }

                    timeText.setText(String.format("%s:%s:%s",hoursText, minutesText, secondText));
                }
                else{

                    String minutesText=""+minutes;
                    if(minutes<10){

                        minutesText="0"+minutesText;
                    }

                    String secondText=""+seconds;
                    if(seconds<10){

                        secondText="0"+seconds;
                    }

                    timeText.setText(String.format("%s:%s", minutesText, secondText));
                }

                speedText.setText(speed+" km/h");

            }

            @SuppressLint("SetTextI18n")
            @Override
            public void onFinish() {
                timeText.setText("TERMINATO");
                speedText.setText("-");
                timeText.setTextColor(Color.rgb(236, 124, 124));
            }
        }.start();

    }

    private void createTable() {
        Typeface typeface = ResourcesCompat.getFont(this, R.font.comfortaa_regular);

        TableLayout table = findViewById(R.id.gridview_maps_client);

            TableRow row;
            row = new TableRow(this);
            row.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT, 1.0f));
            row.setBackgroundColor(getColor(R.color.text));
            row.setPadding(0, 8, 0, 0);
            row.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);


            TextView tv1 = new TextView(MapsActivityClient.this);
            tv1.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT, 1.0f));
            tv1.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
            tv1.setTypeface(typeface);
            tv1.setTextColor(getColor(R.color.back));


            TextView tv2 = new TextView(MapsActivityClient.this);
            tv2.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT, 1.0f));
            tv2.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
            tv2.setTypeface(typeface);
            tv2.setTextColor(getColor(R.color.back));


            row.addView(tv1);
            row.addView(tv2);

            table.addView(row);

            if(UserClient.getRun() == null){
                tv1.setText("-"/*ci metto un trattino ad indicare che la corsa non è attiva*/);
                tv2.setText("-"/*ci metto un trattino ad indicare che la corsa non è attiva*/);
            }
            else{
                updateTime(tv1,tv2,UserClient.getRun());
            }
    }

    private void initViews(){
        navigationView = findViewById(R.id.navigationView_Map_Client);
        TextView textView = navigationView.getHeaderView(0).findViewById(R.id.text_email_client);
        textView.setText(Objects.requireNonNull(mAuth.getCurrentUser()).getEmail());


        DrawerLayout drawer_map_client= findViewById(R.id.drawer_map_client1);
        Toolbar toolbar = findViewById(R.id.toolbar_map_client);
        setSupportActionBar(toolbar);
        navigationView.setNavigationItemSelectedListener(this);
        if(UserClient.getRun() != null){
            navigationView.getMenu().findItem(R.id.Assistenza).setVisible(true);
            navigationView.getMenu().findItem(R.id.end_run).setVisible(true);
            navigationView.getMenu().findItem(R.id.go_back_shop).setVisible(true);
            navigationView.getMenu().findItem(R.id.nuova_corsa_client).setVisible(false);
            navigationView.getMenu().findItem(R.id.logout_client).setVisible(false);
            Log.e(TAG, UserClient.getRun().toString());
        }
        else{
            navigationView.getMenu().findItem(R.id.Assistenza).setVisible(false);
            navigationView.getMenu().findItem(R.id.go_back_shop).setVisible(false);
            navigationView.getMenu().findItem(R.id.end_run).setVisible(false);
            navigationView.getMenu().findItem(R.id.nuova_corsa_client).setVisible(true);
            navigationView.getMenu().findItem(R.id.logout_client).setVisible(true);
            Log.e(TAG, "sono entrato nel ramo else");
        }

        if(UserClient.getUser()!=null)
            textView.setText(UserClient.getUser().getEmail());

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer_map_client , toolbar, R.string.drawer_open, R.string.drawer_close);
        drawer_map_client.addDrawerListener(toggle);
        toggle.syncState();

    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {

        switch (item.getItemId()){
            case R.id.logout_client:
                if(UserClient.getRun() == null) {
                    logout();
                }else {
                    Toast.makeText(this, "Non puoi scappare, termina la corsa prima", Toast.LENGTH_LONG).show();
                    Log.e(TAG, "Non puoi scappare, termina la corsa prima");
                }
                break;
            case R.id.nuova_corsa_client:


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

            case R.id.impostazioni_client:
                startActivity(new Intent(getApplicationContext(), SettingsCustomer.class));
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

    private void returnShop() {
        String uri = "http://maps.google.com/maps?saddr=" + UserClient.getRun().getGeoPoint().getLatitude() +
                "," + UserClient.getRun().getGeoPoint().getLongitude() + "&daddr=" + UserClient.getTrader().getTraderPosition().getLatitude() +
                "," + UserClient.getTrader().getTraderPosition().getLongitude();
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
        startActivity(intent);
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

    public void logout(){
        final String TAG = "deleteCurrentToken";
        Log.e(TAG, "LOGOUT TOKEN");
        if(UserClient.getRun() == null && UserClient.getUser() != null && UserClient.getUser().getTokens() != null){
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
    //Mancano poche linee
    //ce la si sta facendo
    //manca sempre meno
    //ci siamo quasi
    //tutto questo
    //perchè sono depresso
    //dai manca poco
    //sempre meno
    //ultimi sforzi
    //poi smetto lo giuro
    //si vede il traguardo
    //TODO: CAZZO SI (x500)
}