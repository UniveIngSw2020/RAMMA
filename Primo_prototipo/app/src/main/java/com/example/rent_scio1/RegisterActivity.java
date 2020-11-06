package com.example.rent_scio1;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

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

import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {

    private static final String TAG = "EmailPassword";

    EditText mName, mSourname, mEmail, mPassword, mPhone, mDate, mPiva;
    Button mRegisterBtn;
    FirebaseAuth mAuth;
    ProgressBar progressBar;
    FirebaseFirestore mStore;
    String userID;
    CheckBox mTrader;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

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

        mRegisterBtn.setOnClickListener((View view) -> {
            if(chekForm())
                signIn(mEmail.getText().toString().trim(), mPassword.getText().toString().trim());
        });
        mTrader.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mTrader.isChecked()){
                    mPiva.setVisibility(View.VISIBLE);
                }else{
                    mPiva.setVisibility(View.INVISIBLE);
                }
            }
        });
    }
    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        if(mAuth.getCurrentUser() != null){
            startActivity(new Intent(getApplicationContext(), MapsActivityClient.class));
            finish();
        }
    }

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
        }
        return flag;
    }


    private void signIn(String email, String password) {
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
                            DocumentReference documentReference = mStore.collection("users").document(userID);
                            Map<String, Object> user = generateUser();

                            Log.d(TAG, "signInWithEmail:success");

                            documentReference.set(user).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    //Log.d(TAG, "onSuccess: user Profile is created for: " + userID);
                                    System.out.println("onSuccess: user Profile is created for: " + userID);
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    System.out.println("onFaiulure: "+ e.toString());
                                }
                            });
                            /*mStore.collection("users").add(u).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                @Override
                                public void onSuccess(DocumentReference documentReference) {
                                    Log.d(TAG, "DocumentSnapshot added with ID: " + documentReference.getId());
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Log.w(TAG, "Error adding document", e);
                                }
                            });*/




                            //DocumentReference documentReference = mStore.collection("users").document(userID);
                            //sendEmailVerification();
                            /*documentReference.set(u).addOnSuccessListener(aVoid -> {
                                Log.d(TAG, "onSuccess: user Profile is created for: " + userID);
                                System.out.println("onSuccess: user Profile is created for: " + userID);
                            }).addOnFailureListener(e -> System.out.println("onFaiulure: "+ e.toString()));*/
                            //TODO: VERIFICARE SE è UN CLIENTE O COMMERICANTE
                            for(Map.Entry<String, Object> entry : user.entrySet()) {
                                String key = entry.getKey();
                                Object value = entry.getValue();
                                if(key.equals("trader")){
                                    if(value.equals(true))
                                        startActivity(new Intent(getApplicationContext(), MapsActivityTrader.class));
                                    else
                                        startActivity(new Intent(getApplicationContext(), MapsActivityClient.class));
                                }

                            }

                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithEmail:failure", task.getException());
                            Toast.makeText(RegisterActivity.this, "Authentication failed.", Toast.LENGTH_SHORT).show();
                            // [START_EXCLUDE]
                            // [END_EXCLUDE]
                        }

                        progressBar.setVisibility(View.INVISIBLE);
                        // [END_EXCLUDE]
                    }
                });
        // [END sign_in_with_email]
    }

    private Map<String, Object> generateUser (){
        Map <String, Object> user = new HashMap<>();
        user.put("name", mName.getText().toString().trim());
        user.put("sourname", mSourname.getText().toString().trim());
        user.put("email", mEmail.getText().toString().trim());
        user.put("born", mDate.getText().toString().trim());
        user.put("phone", mPhone.getText().toString().trim());
        user.put("piva", mPiva.getText().toString().trim());
        user.put("trader", mTrader.isChecked());
        return user;
    }

    private void sendEmailVerification() {
        // Disable button

        // Send verification email
        // [START send_email_verification]
        final FirebaseUser user = mAuth.getCurrentUser();
        user.sendEmailVerification()
                .addOnCompleteListener(this, new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        // [START_EXCLUDE]
                        // Re-enable button

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
                    }
                });
        // [END send_email_verification]
    }

}