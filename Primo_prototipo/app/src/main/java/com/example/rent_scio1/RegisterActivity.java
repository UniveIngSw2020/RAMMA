package com.example.rent_scio1;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import androidx.core.app.ActivityCompat;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.annotations.NotNull;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;


public class RegisterActivity extends AppCompatActivity {

    private static final String TAG = "EmailPassword";

    private Map <String, Object> user = new HashMap<>();


    EditText mName, mSourname, mEmail, mPassword, mPhone, mDate, mPiva;
    Button mRegisterBtn;
    FirebaseAuth mAuth;
    ProgressBar progressBar;
    FirebaseFirestore mStore;
    String userID;
    CheckBox mTrader, mPositionTrader;
    private FusedLocationProviderClient mFusedLocationClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        mName = findViewById(R.id.name);
        mSourname = findViewById(R.id.sourname);
        mEmail = findViewById(R.id.email_register);
        mPassword = findViewById(R.id.password_register);
        mPhone = findViewById(R.id.phone_register);
        mRegisterBtn = findViewById(R.id.confitmregister_btn);

        mAuth = FirebaseAuth.getInstance();
        mStore = FirebaseFirestore.getInstance();
        progressBar = findViewById(R.id.progressBarregister);
        mDate = findViewById(R.id.dateBorn);
        mPiva = findViewById(R.id.piva);
        mTrader = findViewById(R.id.checkTrader);
        mPositionTrader = findViewById(R.id.checkPositionTrader);

        mRegisterBtn.setOnClickListener((View view) -> {
            if(chekForm())
                signIn(mEmail.getText().toString().trim(), mPassword.getText().toString().trim());
        });
        mTrader.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mTrader.isChecked()){
                    mPiva.setVisibility(View.VISIBLE);
                    mPositionTrader.setVisibility(View.VISIBLE);
                }else{
                    mPiva.setVisibility(View.INVISIBLE);
                    mPositionTrader.setVisibility(View.INVISIBLE);
                }
            }
        });


        //QUI C'È IL TASTO PER TORNARE INDIETRO
        Intent intent = getIntent();
        String message = intent.getStringExtra(StartActivity.EXTRA_MESSAGE);

        // Capture the layout's TextView and set the string as its text
        TextView textView = findViewById(R.id.textView2);
        textView.setText(message);
    }
   /*@Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        if(mAuth.getCurrentUser() != null){
            startActivity(new Intent(getApplicationContext(), MapsActivityClient.class));
            finish();
        }
    }*/

    private boolean chekForm(){
        boolean flag = true;
        if(TextUtils.isEmpty(mPassword.getText().toString().trim())){
            mPassword.setError("Password is Required!");
            flag = false;
        }
        if(TextUtils.isEmpty(mName.getText().toString().trim())){
            mName.setError("Name is Required!");
            flag = false;
        }
        if(TextUtils.isEmpty(mSourname.getText().toString().trim())){
            mSourname.setError("Sourname is Required!");
            flag = false;
        }
        if(TextUtils.isEmpty(mDate.getText().toString().trim())){
            mDate.setError("date of Birthday is Required!");
            flag = false;
        }if(TextUtils.isEmpty(mEmail.getText().toString().trim())){
            mEmail.setError("Email is Required!");
            flag = false;
        }if(TextUtils.isEmpty(mPhone.getText().toString().trim())){
            mPhone.setError("Phone is Required!");
            flag = false;
        }
        if(mTrader.isChecked()){
            if(TextUtils.isEmpty(mPiva.getText().toString().trim())){
                mPiva.setError("PIVA is Required!");
                flag = false;
            }
            if(!mPositionTrader.isChecked()){
                mPositionTrader.setError("Position is Required!");
                flag = false;
            }
        }
        return flag;
    }


    private void signIn(String email, String password) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        Log.d(TAG, "signIn:" + email);

        progressBar.setVisibility(View.VISIBLE);

        // [START create_user_with_email]
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NotNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(RegisterActivity.this, "User, Creadted!", Toast.LENGTH_SHORT).show();
                            userID = mAuth.getCurrentUser().getUid();

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
                public void onComplete(@NonNull Task<android.location.Location> task) {
                    if (task.isSuccessful()) {
                        Location location = task.getResult();
                        user.put("traderposition", new GeoPoint(location.getLatitude(), location.getLongitude()));
                        Log.d(TAG, " PPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPOSIZIONE PRESA");
                        storeUser();
                    }else{
                        Log.d(TAG, " EEEEEEEEEEEEEEEEEEEEEEEEEEEEEEERRORE -> POSIZONE NON PRESA");
                    }
                }
            });
        }else{
            user.put("traderposition",null);
            storeUser();
        }
    }


    private void generateStoreUser (){
        user.put("user_id", mAuth.getUid());
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
        if(Objects.equals(user.get("trader"), true)){
            startActivity(new Intent(getApplicationContext(), MapsActivityTrader.class));
        }else{
            startActivity(new Intent(getApplicationContext(), MapsActivityClient.class));
        }
    }

    private void sendEmailVerification() {
        // Disable button

        // Send verification email
        // [START send_email_verification]
        final FirebaseUser user = mAuth.getCurrentUser();
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
                    // [END_EXCLUDE]
                });
        // [END send_email_verification]
    }

}