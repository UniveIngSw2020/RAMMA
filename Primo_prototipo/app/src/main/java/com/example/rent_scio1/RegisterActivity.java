package com.example.rent_scio1;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.annotations.NotNull;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;


public class RegisterActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "RegisterActivity";
    public static final int ERROR_DIALOG_REQUEST = 9001;
    public static final int PERMISSIONS_REQUEST_ENABLE_GPS = 9002;
    public static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 9003;

    private Map <String, Object> user = new HashMap<>();

    //widgets
    private EditText mName, mSourname, mEmail, mPassword, mConfirmPasswod, mPhone, mDate, mPiva;
    private CheckBox mTrader, mPositionTrader;
    private ProgressBar progressBar;
    private Toolbar toolbar_regist;
    private boolean mLocationPermissionGranted = false;

    //vars
    private FirebaseFirestore mStore;
    private String userID;
    private FusedLocationProviderClient mFusedLocationClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        findViewById(R.id.confitmregister_btn).setOnClickListener(this);
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        mName = findViewById(R.id.name);
        mSourname = findViewById(R.id.sourname);
        mEmail = findViewById(R.id.email_register);
        mPassword = findViewById(R.id.password_register);
        mConfirmPasswod = findViewById(R.id.passwordregister_confirm);
        mPhone = findViewById(R.id.phone_register);
        progressBar = findViewById(R.id.progressBarregister);
        mDate = findViewById(R.id.dateBorn);
        mPiva = findViewById(R.id.piva);
        mTrader = findViewById(R.id.check_Trader);
        mPositionTrader = findViewById(R.id.checkPositionTrader);
        mStore = FirebaseFirestore.getInstance();
        initViews();
        checkTraderRegister();
    }

    private void initViews(){
        toolbar_regist = findViewById(R.id.toolbar_register);
        setSupportActionBar(toolbar_regist);
        Objects.requireNonNull(getSupportActionBar()).setTitle("");
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }



    private boolean chekForm(){
        boolean flag = true;
        if(TextUtils.isEmpty(mPassword.getText().toString().trim())){
            mPassword.setError("Password Richesta!");
            flag = false;
        }if(TextUtils.isEmpty(mName.getText().toString().trim())){
            mName.setError("Nome Richeisto!");
            flag = false;
        }if(TextUtils.isEmpty(mSourname.getText().toString().trim())){
            mSourname.setError("Cognome Richiesto!");
            flag = false;
        }if(TextUtils.isEmpty(mDate.getText().toString().trim())){
            mDate.setError("Data di Nascita  Richiesto!");
            flag = false;
        }if(TextUtils.isEmpty(mEmail.getText().toString().trim())){
            mEmail.setError("Email Richeista!");
            flag = false;
        }if(TextUtils.isEmpty(mPhone.getText().toString().trim())){
            mPhone.setError("Numero di Cellulare Richiesto!");
            flag = false;
        }if(mTrader.isChecked()){
            if(TextUtils.isEmpty(mPiva.getText().toString().trim())){
                mPiva.setError("Partita IVA Richesta!");
                flag = false;
            }
            if(!mPositionTrader.isChecked()){
                mPositionTrader.setError("Posizione Richesta!");
                flag = false;
            }
        }
        if(!mPassword.getText().toString().trim().equals(mConfirmPasswod.getText().toString().trim())){
            mConfirmPasswod.setError("Entrambe le Passowrd devono essere Uguali!");
            flag = false;
        }
        return flag;
    }


    private void signIn(String email, String password) {
        Log.d(TAG, "signIn:" + email);

        progressBar.setVisibility(View.VISIBLE);

        Log.d(TAG, "CREDENZIALIIIIIIIIIIIIIIIIII:        email: " + email + " password: " +password);
        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NotNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            generateStoreUser();


                        } else {
                            Log.w(TAG, "signInWithEmail:failure", task.getException());
                            Toast.makeText(RegisterActivity.this, "Authentication failed: ." + task.getException(), Toast.LENGTH_SHORT).show();
                        }
                        progressBar.setVisibility(View.INVISIBLE);
                    }
                });
    }

    private void getPosition () {
        if(mPositionTrader.isChecked()) {
            Log.d(TAG, "getLastKnownLocation: called.");
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            mFusedLocationClient.getLastLocation().addOnCompleteListener(new OnCompleteListener<android.location.Location>() {
                @Override
                public void onComplete(@NonNull Task<Location> task) {
                    task.addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.d(TAG, " FAILUREEEEEEEEEEEE -> POSIZONE NON PRESA");
                        }
                    });
                    task.addOnSuccessListener(new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            Log.d(TAG, String.valueOf(location));
                            Log.d(TAG, "POSIZIONE: " + location.toString());
                            user.put("traderposition", new GeoPoint(location.getLatitude(), location.getLongitude()));
                            Log.d(TAG, " REGISTERRRRRRRR POSZIONE PRESA");
                            storeUser();
                        }
                    });
                    /*if (task.isSuccessful()) {

                    }else{
                        Log.d(TAG, " REGISTERRRRRRRR EEEEEEEEEEEEEERRORE -> POSIZONE NON PRESA");
                    }*/
                }
            });
        }else{
            user.put("traderposition",null);
            storeUser();
        }
    }

    private void generateStoreUser (){
        user.put("user_id", FirebaseAuth.getInstance().getUid());
        user.put("name", mName.getText().toString().trim());
        user.put("sourname", mSourname.getText().toString().trim());
        user.put("email", mEmail.getText().toString().trim());
        user.put("born", mDate.getText().toString().trim());
        user.put("phone", mPhone.getText().toString().trim());
        user.put("piva", mPiva.getText().toString().trim());
        user.put("trader", mTrader.isChecked());
        getPosition();
    }

    private void storeUser () {
        Log.d(TAG, "signInWithEmail:success");
        DocumentReference documentReference = mStore.collection("users").document(userID);
        documentReference.set(user).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Log.d(TAG,"OOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOnSuccess: user Profile is created for: " + user);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                System.out.println("onFaiulure: "+ e.toString());
            }
        });

        Toast.makeText(RegisterActivity.this, "User, Creadted!", Toast.LENGTH_SHORT).show();
        userID = FirebaseAuth.getInstance().getCurrentUser().getUid();

        if(Objects.equals(user.get("trader"), true)){
            startActivity(new Intent(getApplicationContext(), MapsActivityTrader.class));
        }else{
            startActivity(new Intent(getApplicationContext(), MapsActivityClient.class));
        }
    }

    /*private void sendEmailVerification() {
        final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        user.sendEmailVerification()
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(RegisterActivity.this,
                                "Verification email sent to " + user.getEmail(),
                                Toast.LENGTH_SHORT).show();
                    } else {
                        Log.e(TAG, "sendEmailVerification", task.getException());
                        Toast.makeText(RegisterActivity.this,
                                "Failed to send verification email.",
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }*/

    @Override
    public void onClick(View view) {
        Log.d(TAG,"VIEWWWWWWWWWWW " + view.getId());
        if (view.getId() == R.id.confitmregister_btn) {
            if (chekForm())
                signIn(mEmail.getText().toString().trim(), mPassword.getText().toString().trim());
        }
    }

    private void getLocationPermission() {
        /*
         * Request location permission, so that we can get the location of the
         * device. The result of the permission request is handled by a callback,
         * onRequestPermissionsResult.
         */
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mLocationPermissionGranted = true;
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }
    }

    private void checkTraderRegister (){
        mTrader.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(mTrader.isChecked()){
                    mPiva.setVisibility(View.VISIBLE);
                    mPositionTrader.setVisibility(View.VISIBLE);
                }else{
                    mPiva.setVisibility(View.INVISIBLE);
                    mPositionTrader.setVisibility(View.INVISIBLE);
                }
            }
        });

        mPositionTrader.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(mPositionTrader.isChecked()){
                    checkMapServices();
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG, "onActivityResult: called.");
        switch (requestCode) {
            case PERMISSIONS_REQUEST_ENABLE_GPS: {
                if(!mLocationPermissionGranted){
                    getLocationPermission();
                }
            }
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        if(checkMapServices()){
            if(!mLocationPermissionGranted){
                getLocationPermission();
            }
        }
    }



    private boolean checkMapServices(){
        if(isServicesOK()){
            if(isMapsEnabled()){
                return true;
            }
        }
        return false;
    }

    public boolean isServicesOK(){
        Log.d(TAG, "isServicesOK: checking google services version");

        int available = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(RegisterActivity.this);

        if(available == ConnectionResult.SUCCESS){
            //everything is fine and the user can make map requests
            Log.d(TAG, "isServicesOK: Google Play Services is working");
            return true;
        }
        else if(GoogleApiAvailability.getInstance().isUserResolvableError(available)){
            //an error occured but we can resolve it
            Log.d(TAG, "isServicesOK: an error occured but we can fix it");
            Dialog dialog = GoogleApiAvailability.getInstance().getErrorDialog(RegisterActivity.this, available, ERROR_DIALOG_REQUEST);
            dialog.show();
        }else{
            Toast.makeText(this, "You can't make map requests", Toast.LENGTH_SHORT).show();
        }
        return false;
    }

    public boolean isMapsEnabled(){
        final LocationManager manager = (LocationManager) getSystemService( Context.LOCATION_SERVICE );

        if ( !manager.isProviderEnabled( LocationManager.GPS_PROVIDER ) ) {
            buildAlertMessageNoGps();
            return false;
        }
        return true;
    }

    private void buildAlertMessageNoGps() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("This application requires GPS to work properly, do you want to enable it?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, final int id) {
                        Intent enableGpsIntent = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        startActivityForResult(enableGpsIntent, PERMISSIONS_REQUEST_ENABLE_GPS);

                    }
                });
        final AlertDialog alert = builder.create();
        alert.show();
    }
}