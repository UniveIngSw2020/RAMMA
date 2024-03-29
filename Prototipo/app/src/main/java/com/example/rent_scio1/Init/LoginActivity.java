package com.example.rent_scio1.Init;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.rent_scio1.Client.MapsActivityClient;
import com.example.rent_scio1.R;
import com.example.rent_scio1.Trader.MapsActivityTrader;
import com.example.rent_scio1.Trader.SetPositionActivityTrader;
import com.example.rent_scio1.services.ExitService;
import com.example.rent_scio1.services.MyLocationService;
import com.example.rent_scio1.utils.Run;
import com.example.rent_scio1.utils.User;
import com.example.rent_scio1.utils.UserClient;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Objects;

import static android.text.TextUtils.isEmpty;


//Activity di login, controlliamo se Email e password inserite corrispondono a un account registrato sul DB.

public class LoginActivity extends AppCompatActivity implements View.OnClickListener{

    private static final String TAG = "LoginActivity";


    //Firebase
    private FirebaseAuth.AuthStateListener mAuthListener;

    //Widgets
    private EditText mEmail, mPassword;
    private ProgressBar mProgressBar;
    private CheckBox mCheckBox;
    private SharedPreferences.Editor loginPrefsEditor;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        mEmail = findViewById(R.id.email_login);
        mPassword = findViewById(R.id.password_login);
        mProgressBar = findViewById(R.id.progressBarlogin);
        mCheckBox = findViewById(R.id.checkBoxLogin);

        SharedPreferences loginPreferences = getSharedPreferences("loginPrefs", MODE_PRIVATE);
        loginPrefsEditor = loginPreferences.edit();

        setupFirebaseAuth();
        initViews();
        findViewById(R.id.confirmlogin_btn).setOnClickListener(this);

        boolean saveLogin = loginPreferences.getBoolean("saveLogin", false);
        if (saveLogin) {
            mEmail.setText(loginPreferences.getString("username", ""));
            mPassword.setText(loginPreferences.getString("password", ""));
            mCheckBox.setChecked(true);
        }
    }

    public void hideKeyboard(View view) {
        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(this.getCurrentFocus().getWindowToken(), 0);
    }

    private void initViews(){
        Toolbar toolbar_act_login = findViewById(R.id.toolbar_login);
        setSupportActionBar(toolbar_act_login);
        Objects.requireNonNull(getSupportActionBar()).setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }


    private void showDialog(){
        mEmail.setVisibility(View.VISIBLE);
        mPassword.setVisibility(View.VISIBLE);
        findViewById(R.id.confirmlogin_btn).setVisibility(View.VISIBLE);
        findViewById(R.id.textViewLogin).setVisibility(View.VISIBLE);
        findViewById(R.id.checkBoxLogin).setVisibility(View.VISIBLE);
    }

    private void hideDialog(){
        mEmail.setVisibility(View.INVISIBLE);
        mPassword.setVisibility(View.INVISIBLE);
        findViewById(R.id.confirmlogin_btn).setVisibility(View.INVISIBLE);
        findViewById(R.id.textViewLogin).setVisibility(View.INVISIBLE);
        findViewById(R.id.checkBoxLogin).setVisibility(View.INVISIBLE);
        findViewById(R.id.progressBarLoadLogin).setVisibility(View.INVISIBLE);
        if(mProgressBar.getVisibility() == View.VISIBLE){
            findViewById(R.id.checkBoxLogin).setVisibility(View.INVISIBLE);
            mProgressBar.setVisibility(View.INVISIBLE);
        }
    }

    private void setupFirebaseAuth(){
        Log.d(TAG, "setupFirebaseAuth: started.");

        mAuthListener = firebaseAuth -> {
            FirebaseUser user = firebaseAuth.getCurrentUser();

            if (user != null) {
                if(mProgressBar.getVisibility() == View.INVISIBLE){
                    findViewById(R.id.progressBarLoadLogin).setVisibility(View.VISIBLE);
                    findViewById(R.id.checkBoxLogin).setVisibility(View.INVISIBLE);
                }

                Log.d(TAG, "onAuthStateChanged:signed_in:" + user.getUid());



                FirebaseFirestore db = FirebaseFirestore.getInstance();
                DocumentReference userRef = db.collection("users")
                        .document(user.getUid());

                userRef.get().addOnCompleteListener(task -> {
                    if(task.isSuccessful()){
                        Log.d(TAG, "onComplete: successfully set the user client.");

                        User user1 = task.getResult().toObject(User.class);
                        UserClient.setUser(user1);
                        UserClient.setRun(null);
                        if(user1 != null) {
                            db.collection("run").whereEqualTo("user", user1.getUser_id()).get()
                                    .addOnSuccessListener(queryDocumentSnapshots -> {
                                        for (DocumentSnapshot d : queryDocumentSnapshots.getDocuments()) {
                                            Log.e(TAG, "C'è UNA CORSA SOLA");
                                            UserClient.setRun(d.toObject(Run.class));
                                            startLocationService(true);
                                        }
                                    })
                                    .addOnCompleteListener(task1 -> {
                                        Toast.makeText(LoginActivity.this, "Autenticato come: " + user.getEmail(), Toast.LENGTH_SHORT).show();

                                        if (user1.getTrader()) {
                                            if (user1.getTraderPosition() == null)
                                                startActivity(new Intent(getApplicationContext(), SetPositionActivityTrader.class));
                                            else
                                                startActivity(new Intent(getApplicationContext(), MapsActivityTrader.class));
                                        } else
                                            startActivity(new Intent(getApplicationContext(), MapsActivityClient.class));

                                        hideDialog();
                                        finishAffinity();
                                    });
                        }else{
                            Log.e(TAG, "L'account è stato eliminato per qualche ragione");
                        }
                    }
                });

            } else {
                Log.d(TAG, "onAuthStateChanged:signed_out");
                showDialog();
            }
        };
    }


    private boolean isLocationServiceRunning() {
        ActivityManager manager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {

            if ("com.example.rent_scio1.services.MyLocationService".equals(service.service.getClassName())) {
                Log.d(TAG, "isLocationServiceRunning: location service is already running.");
                return true;
            }
        }
        Log.e(TAG, "isLocationServiceRunning: location service is not running.");
        return false;
    }

    private void startLocationService(Boolean rawValue) {
        if (!isLocationServiceRunning()) {
            Log.e(TAG, "RUN");
            Intent serviceIntent = new Intent(this, MyLocationService.class);

            serviceIntent.putExtra(TAG, rawValue);

            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {

                LoginActivity.this.startForegroundService(serviceIntent);
            } else {
                startService(serviceIntent);
            }
        }
    }


    @Override
    public void onStart() {
        super.onStart();
        FirebaseAuth.getInstance().addAuthStateListener(mAuthListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            FirebaseAuth.getInstance().removeAuthStateListener(mAuthListener);
        }
    }

    private void signIn(){
        //controlla che i campi non siano vuoti
        if(!isEmpty(mEmail.getText().toString().trim())
                && !isEmpty(mPassword.getText().toString().trim())){
            Log.d(TAG, "onClick: attempting to authenticate.");

            mProgressBar.setVisibility(View.VISIBLE);
            findViewById(R.id.progressBarLoadLogin).setVisibility(View.INVISIBLE);

            FirebaseAuth.getInstance().signInWithEmailAndPassword(mEmail.getText().toString().trim(), mPassword.getText().toString().trim())
                    .addOnFailureListener(e -> {
                        Toast.makeText(LoginActivity.this, "Autenticazione Fallita!", Toast.LENGTH_SHORT).show();
                        showDialog();
                        if(mProgressBar.getVisibility() == View.VISIBLE){
                            mProgressBar.setVisibility(View.INVISIBLE);
                        }
                    })
                    .addOnSuccessListener(authResult -> {


                        boolean check = mCheckBox.isChecked();
                        if (check) {
                            loginPrefsEditor.putBoolean("saveLogin", true);
                            loginPrefsEditor.putString("username", mEmail.getText().toString().trim());
                            loginPrefsEditor.putString("password", mPassword.getText().toString().trim());
                        }else{
                            loginPrefsEditor.clear();
                        }
                        loginPrefsEditor.commit();
                    });
        }else{
            check();
            Toast.makeText(LoginActivity.this, "Non hai completato i campi!", Toast.LENGTH_SHORT).show();
        }
    }

    private void check (){
        if(TextUtils.isEmpty(mEmail.getText().toString().trim())){ mEmail.setError("Email Richiesta!"); }
        if(TextUtils.isEmpty(mPassword.getText().toString().trim())){ mPassword.setError("Password Richiesta"); }
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.confirmlogin_btn) {
            signIn();
        }
    }
}
