package com.example.rent_scio1.Init;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.rent_scio1.Client.MapsActivityClient;
import com.example.rent_scio1.R;
import com.example.rent_scio1.Trader.SetPositionActivityTrader;
import com.example.rent_scio1.utils.User;
import com.example.rent_scio1.utils.UserClient;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;


public class RegisterActivity extends AppCompatActivity {

    private static final String TAG = "RegisterActivity";
    /*public static final int ERROR_DIALOG_REQUEST = 9001;
    public static final int PERMISSIONS_REQUEST_ENABLE_GPS = 9002;
    public static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 9003;*/

    private Map<String, Object> user = new HashMap<>();

    //widgets
    private EditText mName, mSurname, mEmail, mPassword, mConfirmPasswod, mPhone, mShopname;
    private CheckBox mTrader/*, mPositionTrader*/;
    //private ProgressBar progressBar;

    //vars
    private FirebaseFirestore mStore;
    private String userID;
    //private FusedLocationProviderClient mFusedLocationClient;
    //private boolean mLocationPermissionGranted = false;

    //metodo richiamato nell'XML
    public void hideKeyboard(View view) {
        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        //findViewById(R.id.confitmregister_btn).setOnClickListener(this);

        //mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        //mi getto gli oggetti dall'xml
        mName = findViewById(R.id.name);
        mSurname = findViewById(R.id.surname);
        mEmail = findViewById(R.id.email_register);
        mPassword = findViewById(R.id.password_register);
        mConfirmPasswod = findViewById(R.id.passwordregister_confirm);
        mPhone = findViewById(R.id.phone_register);
        //progressBar = findViewById(R.id.progressBarregister);
        mTrader = findViewById(R.id.check_Trader);
        mShopname = findViewById(R.id.shopName);
        //mPositionTrader = findViewById(R.id.checkPositionTrader);
        mStore = FirebaseFirestore.getInstance();

        //metodo per inizializzare la UI
        initViews();
    }

    private void initViews() {

        Toolbar toolbar_regist = findViewById(R.id.toolbar_register);
        setSupportActionBar(toolbar_regist);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //setto il listener per il bottone di conferma
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_activity_register);
        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            switch (item.getItemId()) {
                case R.id.confitmregister_btn:

                    if (chekForm())
                        signIn(mEmail.getText().toString().trim(), mPassword.getText().toString().trim());

                    break;
            }
            return true;
        });

        //checkbox commerciante
        mTrader.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (mTrader.isChecked()) {

                //mPositionTrader.setVisibility(View.VISIBLE);
                mShopname.setVisibility(View.VISIBLE);
            } else {
                mShopname.setVisibility(View.INVISIBLE);
                //mPositionTrader.setVisibility(View.INVISIBLE);
            }
        });
    }


    private boolean chekForm() {
        boolean flag = true;
        if (TextUtils.isEmpty(mPassword.getText().toString().trim())) {
            mPassword.setError("Password Richesta!");
            flag = false;
        }
        if (TextUtils.isEmpty(mName.getText().toString().trim())) {
            mName.setError("Nome Richeisto!");
            flag = false;
        }
        if (TextUtils.isEmpty(mSurname.getText().toString().trim())) {
            mSurname.setError("Cognome Richiesto!");
            flag = false;
        }
        if (TextUtils.isEmpty(mEmail.getText().toString().trim())) {
            mEmail.setError("Email Richeista!");
            flag = false;
        }
        if (TextUtils.isEmpty(mPhone.getText().toString().trim())) {
            mPhone.setError("Numero di Cellulare Richiesto!");
            flag = false;
        }

        if (mTrader.isChecked()) {
            /*if (!mPositionTrader.isChecked()) {
                mPositionTrader.setError("Posizione Richesta!");
                flag = false;
            }*/

            if (TextUtils.isEmpty(mShopname.getText().toString().trim())) {
                mShopname.setError("Nome del Negozio Richiesto!");
                flag = false;
            }
        }

        if (!mPassword.getText().toString().trim().equals(mConfirmPasswod.getText().toString().trim())) {
            mConfirmPasswod.setError("Entrambe le Passowrd devono essere uguali!");
            flag = false;
        }
        return flag;
    }

    private void signIn(String email, String password) {
        Log.d(TAG, "signIn:" + email);

        //progressBar.setVisibility(View.VISIBLE);

        Log.d(TAG, "CREDENZIALIIIIIIIIIIIIIIIIII:        email: " + email + " password: " + password);
        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        userID = FirebaseAuth.getInstance().getCurrentUser().getUid();
                        generateStoreUser();
                    } else {
                        Log.w(TAG, "signInWithEmail:failure", task.getException());
                        Toast.makeText(RegisterActivity.this, "Authentication failed: ." + task.getException(), Toast.LENGTH_SHORT).show();
                    }
                    //progressBar.setVisibility(View.INVISIBLE);
                });
    }

    private void generateStoreUser (){

        //inserisci nell'oggetto user le informazioni
        user.put("user_id", FirebaseAuth.getInstance().getUid());
        user.put("name", mName.getText().toString().trim());
        user.put("surname", mSurname.getText().toString().trim());
        user.put("email", mEmail.getText().toString().trim());
        user.put("phone", mPhone.getText().toString().trim());
        user.put("trader", mTrader.isChecked());
        user.put("shopName", mShopname.getText().toString().trim());

        //oggetti che verranno settati durante l'utilizzo dell'app
        user.put("delimited_area", null );
        user.put("traderPosition",null);


        //push dell'oggeto su db
        Log.d(TAG, "signInWithEmail:success");

        DocumentReference documentReference = mStore.collection("users").document(userID);

        documentReference.set(user).addOnSuccessListener(aVoid -> Log.d(TAG,"OOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOnSuccess: user Profile is created for: " + user))
                .addOnFailureListener(e -> System.out.println("onFailure: "+ e.toString()));

        Toast.makeText(RegisterActivity.this, "User, Created!", Toast.LENGTH_SHORT).show();

        finishAffinity();

        UserClient.setUser(new User( FirebaseAuth.getInstance().getUid(), mName.getText().toString().trim(),mSurname.getText().toString().trim(),mEmail.getText().toString().trim(),mPhone.getText().toString().trim(), mTrader.isChecked(),mShopname.getText().toString().trim(),null, null,  null));

        if(Objects.equals(user.get("trader"), true)){
            startActivity(new Intent(getApplicationContext(), SetPositionActivityTrader.class));
            //startActivity(new Intent(getApplicationContext(), MapsActivityTrader.class));
        }else{
            startActivity(new Intent(getApplicationContext(), MapsActivityClient.class));
        }

        //getPosition();
    }

    /*
    private void getPosition() {
        if (mPositionTrader.isChecked()) {
            Log.d(TAG, "getLastKnownLocation: called.");*/
            /*if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "getLastKnownLocation: IIIIIIIIIIIIIIIFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF.");
                return;
            }*//*
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                Log.d(TAG, "getLastKnownLocation: IIIIIIIIIIIIIIIFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF.");
                return;
            }
            mFusedLocationClient.getCurrentLocation(LocationRequest.PRIORITY_HIGH_ACCURACY, new CancellationToken() {
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
            }).addOnCompleteListener(new OnCompleteListener<Location>() {
                @Override
                public void onComplete(@NonNull Task<Location> task) {
                    if (task.isSuccessful()) {
                        Location location = task.getResult();
                        Log.d(TAG, String.valueOf(location));
                        Log.d(TAG, "POSIZIONE: " + location.toString());
                        user.put("traderposition", new GeoPoint(location.getLatitude(), location.getLongitude()));
                        Log.d(TAG, " REGISTERRRRRRRR POSIZIONE PRESA");
                        storeUser();
                    } else {
                        Log.d(TAG, " REGISTERRRRRRRR EEEEEEEEEEEEEERRORE -> POSIZIONE NON PRESA");
                    }
                }
            });*/
            /*mFusedLocationClient.getLastLocation().addOnCompleteListener(new OnCompleteListener<android.location.Location>() {
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
                    if (task.isSuccessful()) {

                    }else{
                        Log.d(TAG, " REGISTERRRRRRRR EEEEEEEEEEEEEERRORE -> POSIZONE NON PRESA");
                    }
                }
            });*//*
        }else{
            user.put("traderposition",null);
            //storeUser();
        }
    }*/

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

    /*
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

    private void getLocationPermission() {

        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mLocationPermissionGranted = true;
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }
    }*/

        /*
        mPositionTrader.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(mPositionTrader.isChecked()){
                    checkMapServices();
                }
            }
        });*/
    }


    /*
    @Override
    protected void onResume() {
        super.onResume();
        if(checkMapServices()){
            if(!mLocationPermissionGranted){
                getLocationPermission();
            }
        }
    }
*/

    /*
    private boolean checkMapServices(){
        if(isServicesOK()){
            return isMapsEnabled();
        }
        return false;
    }*/

    /*
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
    }*/

    /*
    public boolean isMapsEnabled(){
        final LocationManager manager = (LocationManager) getSystemService( Context.LOCATION_SERVICE );

        if ( !manager.isProviderEnabled( LocationManager.GPS_PROVIDER ) ) {
            buildAlertMessageNoGps();
            return false;
        }
        return true;
    }*/

    /*
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


    }*/
