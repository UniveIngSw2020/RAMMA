package com.example.rent_scio1;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.rent_scio1.utils.UserClient;
import com.example.rent_scio1.utils.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.Objects;

import static android.text.TextUtils.isEmpty;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener{

    private static final String TAG = "LoginActivity";


    //Firebase
    private FirebaseAuth.AuthStateListener mAuthListener;

    //Widgets
    private EditText mEmail, mPassword;
    private ProgressBar mProgressBar;
    private Toolbar toolbar_act_login;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        mEmail = findViewById(R.id.email_login);
        mPassword = findViewById(R.id.password_login);
        //mLoginBtn = findViewById(R.id.confirmlogin_btn);
        mProgressBar = findViewById(R.id.progressBarlogin);

        setupFirebaseAuth();
        initViews();
        findViewById(R.id.confirmlogin_btn).setOnClickListener(this);
    }

    private void initViews(){
        toolbar_act_login = findViewById(R.id.toolbar_login);
        setSupportActionBar(toolbar_act_login);
        Objects.requireNonNull(getSupportActionBar()).setTitle("");
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }


    private void showDialog(){
        mProgressBar.setVisibility(View.VISIBLE);

    }

    private void hideDialog(){
        if(mProgressBar.getVisibility() == View.VISIBLE){
            mProgressBar.setVisibility(View.INVISIBLE);
        }
    }

    private void setupFirebaseAuth(){
        Log.d(TAG, "setupFirebaseAuth: started.");

        mAuthListener = firebaseAuth -> {
            FirebaseUser user = firebaseAuth.getCurrentUser();
            if (user != null) {
                Log.d(TAG, "onAuthStateChanged:signed_in:" + user.getUid());
                Toast.makeText(LoginActivity.this, "Authenticated with: " + user.getEmail(), Toast.LENGTH_SHORT).show();

                FirebaseFirestore db = FirebaseFirestore.getInstance();
  /*
                Query userquery = db.collection("users").whereEqualTo("user_id", user.getUid());
                userquery.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if(task.isSuccessful()){
                            for(QueryDocumentSnapshot document : task.getResult()){
                                User u = new User(document.toObject(User.class));
                                UserClient.setUser(u);
                                Log.d(TAG, "INFOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOO " + u.toString());
                            }
                        }else{
                            Log.w(TAG, "-----------------------------------------------------------Error getting documents.", task.getException());
                        }

                        if(UserClient.getUser().getTrader()){
                            startActivity(new Intent(getApplicationContext(), MapsActivityTrader.class));
                        }else{
                            startActivity(new Intent(getApplicationContext(), MapsActivityClient.class));
                        }
                    }
                });
 */
                DocumentReference userRef = db.collection("users")
                        .document(user.getUid());

                userRef.get().addOnCompleteListener(task -> {
                    if(task.isSuccessful()){
                        Log.d(TAG, "onComplete: successfully set the user client.");

                        User user1 = task.getResult().toObject(User.class);
                         UserClient.setUser(user1);

                        if (user1 != null) {

                            if(user1.getTrader()){
                                startActivity(new Intent(getApplicationContext(), MapsActivityTrader.class));
                            }else{
                                startActivity(new Intent(getApplicationContext(), MapsActivityClient.class));
                            }
                        }
                    }
                });

                /*Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();*/

            } else {
                // User is signed out
                Log.d(TAG, "onAuthStateChanged:signed_out");
            }
            // ...
        };
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
        //check if the fields are filled out
        if(!isEmpty(mEmail.getText().toString().trim())
                && !isEmpty(mPassword.getText().toString().trim())){
            Log.d(TAG, "onClick: attempting to authenticate.");

            showDialog();

            FirebaseAuth.getInstance().signInWithEmailAndPassword(mEmail.getText().toString().trim(),
                    mPassword.getText().toString().trim())
                    .addOnCompleteListener(task -> hideDialog()).addOnFailureListener(e -> {
                        Toast.makeText(LoginActivity.this, "Authentication Failed", Toast.LENGTH_SHORT).show();
                        hideDialog();
                    });
        }else{
            check();
            Toast.makeText(LoginActivity.this, "You didn't fill in all the fields.", Toast.LENGTH_SHORT).show();
        }
    }

    private void check (){
        if(TextUtils.isEmpty(mEmail.getText().toString().trim())){ mEmail.setError("Email is Required!"); }
        if(TextUtils.isEmpty(mPassword.getText().toString().trim())){ mPassword.setError("Password is Required!"); }
    }


    /*private void signIn(String email, String password) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        Log.d(TAG, "signIn:" + email);



        progressBar.setVisibility(View.VISIBLE);

        // [START sign_in_with_email]
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithEmail:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            Toast.makeText(LoginActivity.this, "Authentication Successes.", Toast.LENGTH_SHORT).show();


                            Query userquery = db.collection("users").whereEqualTo("user_id", mAuth.getCurrentUser().getUid());
                            userquery.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                    if(task.isSuccessful()){
                                        for(QueryDocumentSnapshot document : task.getResult()){
                                            u = new User(document.toObject(User.class));

                                            Log.d(TAG, "INFOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOO " + u.toString());
                                        }
                                    }else{
                                        Log.w(TAG, "-----------------------------------------------------------Error getting documents.", task.getException());
                                    }

                                    if(u.getTrader()){
                                        startActivity(new Intent(getApplicationContext(), MapsActivityTrader.class));
                                    }else{
                                        startActivity(new Intent(getApplicationContext(), MapsActivityClient.class));
                                    }
                                }
                            });




                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithEmail:failure", task.getException());
                            System.out.println("signInWithEmail:failure" + task.getException());
                            Toast.makeText(LoginActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        }

                        progressBar.setVisibility(View.INVISIBLE);
                    }
                });
    }*/


    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.confirmlogin_btn) {
            signIn();
        }
    }
}
