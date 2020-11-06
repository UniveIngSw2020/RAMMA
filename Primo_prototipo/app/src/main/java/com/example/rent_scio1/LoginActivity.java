package com.example.rent_scio1;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.rent_scio1.utils.Users;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

public class LoginActivity extends AppCompatActivity{

    private static final String TAG = "EmailPassword";
    EditText mEmail, mPassword;
    Button mLoginBtn;
    ProgressBar progressBar;
    private Users u = new Users();


    // [START declare_auth]
    private FirebaseAuth mAuth;
    // [END declare_auth]

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        mEmail = findViewById(R.id.email_login);
        mPassword = findViewById(R.id.password_login);
        mLoginBtn = findViewById(R.id.confirmlogin_btn);
        progressBar = findViewById(R.id.progressBarlogin);



        // Buttons

        // [START initialize_auth]
        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();
        // [END initialize_auth]

        mLoginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = mEmail.getText().toString().trim();
                String password = mPassword.getText().toString().trim();

                if(TextUtils.isEmpty(email)){
                    mEmail.setError("Email is Required!");
                    return;
                }

                if(TextUtils.isEmpty(password)){
                    mPassword.setError("Password is Required!");
                    return;
                }

                if(password.length() < 6){
                    mPassword.setError("Password must be >= 6 characters");
                    return;
                }

                /*progressBar.setVisibility(View.VISIBLE);

                // authenticete the user

                mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()){
                            Toast.makeText(LoginActivity.this, "Logged in Successfully!", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(getApplicationContext(), MapsActivity.class));
                        }else{
                            Toast.makeText(LoginActivity.this, "Error: "+ task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            progressBar.setVisibility(View.GONE);
                        }
                    }
                });*/
                signIn(email,password);
            }
        });

        //QUI C'È IL TASTO PER TORNARE INDIETRO
        Intent intent = getIntent();
        String message = intent.getStringExtra(StartActivity.EXTRA_MESSAGE);

        // Capture the layout's TextView and set the string as its text
        TextView textView = findViewById(R.id.textView);
        textView.setText(message);
    }

    // [START on_start_check_user]
    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        if(mAuth.getCurrentUser() != null){
            startActivity(new Intent(getApplicationContext(), MapsActivityClient.class));
            finish();
        }
    }
    // [END on_start_check_user]


    private void signIn(String email, String password) {
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


                            //TODO: VERIFICARE SE è UN CLIENTE O COMMERICANTE
                            /*db.collection("users")
                                    .get()
                                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                        @Override
                                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                            if (task.isSuccessful()) {
                                               for (QueryDocumentSnapshot document : task.getResult()) {
                                                   document.getId()
                                                    if(document.get("piva").equals(false))
                                                        startActivity(new Intent(getApplicationContext(), MapsActivityClient.class));
                                                    else
                                                        startActivity(new Intent(getApplicationContext(), MapsActivityTrader.class));
                                                    Log.d(TAG, "\n\n\n\nINFOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOO " + document.getData());
                                                }
                                                startActivity(new Intent(getApplicationContext(), MapsActivityClient.class));
                                            } else {
                                                Log.w(TAG, "Error getting documents.", task.getException());
                                            }
                                        }
                                    });*/

                            Query userquery = db.collection("users").whereEqualTo("user_id", mAuth.getCurrentUser().getUid());
                            userquery.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                    if(task.isSuccessful()){
                                        for(QueryDocumentSnapshot document : task.getResult()){
                                            u = new Users(document.toObject(Users.class));
                                            Log.d(TAG, "DOCUMENTTTTTTT " + document);
                                            Log.d(TAG, "INFOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOO " + u.toString());
                                        }
                                        Log.d(TAG, "INFOOOOOOOOO");
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


                            /*for(Users a : users){
                                if(a.getEmail().equals(user.getEmail()))
                                    if(a.getTrader())
                                        startActivity(new Intent(getApplicationContext(), MapsActivityTrader.class));
                                    else
                                        startActivity(new Intent(getApplicationContext(), MapsActivityClient.class));
                            }*/




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
    }

    /*private void signOut() {
        mAuth.signOut();
    }*/

    /*private void sendEmailVerification() {
        // Disable button
        mBinding.verifyEmailButton.setEnabled(false);

        // Send verification email
        // [START send_email_verification]
        final FirebaseUser user = mAuth.getCurrentUser();
        user.sendEmailVerification()
                .addOnCompleteListener(this, new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        // [START_EXCLUDE]
                        // Re-enable button
                        mBinding.verifyEmailButton.setEnabled(true);

                        if (task.isSuccessful()) {
                            Toast.makeText(LoginActivity.this,
                                    "Verification email sent to " + user.getEmail(),
                                    Toast.LENGTH_SHORT).show();
                        } else {
                            Log.e(TAG, "sendEmailVerification", task.getException());
                            Toast.makeText(LoginActivity.this,
                                    "Failed to send verification email.",
                                    Toast.LENGTH_SHORT).show();
                        }
                        // [END_EXCLUDE]
                    }
                });
        // [END send_email_verification]
    }*/

    /*private void reload() {
        mAuth.getCurrentUser().reload().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    updateUI(mAuth.getCurrentUser());
                    Toast.makeText(LoginActivity.this,
                            "Reload successful!",
                            Toast.LENGTH_SHORT).show();
                } else {
                    Log.e(TAG, "reload", task.getException());
                    Toast.makeText(LoginActivity.this,
                            "Failed to reload user.",
                            Toast.LENGTH_SHORT).show();
                }
            }
        });
    }*/

    /*private boolean validateForm() {
        boolean valid = true;

        String email = mBinding.fieldEmail.getText().toString();
        if (TextUtils.isEmpty(email)) {
            mBinding.fieldEmail.setError("Required.");
            valid = false;
        } else {
            mBinding.fieldEmail.setError(null);
        }

        String password = mBinding.fieldPassword.getText().toString();
        if (TextUtils.isEmpty(password)) {
            mBinding.fieldPassword.setError("Required.");
            valid = false;
        } else {
            mBinding.fieldPassword.setError(null);
        }

        return valid;
    }*/

    /*private void updateUI(FirebaseUser user) {
        hideProgressBar();
        if (user != null) {
            mBinding.status.setText(getString(R.string.emailpassword_status_fmt,
                    user.getEmail(), user.isEmailVerified()));
            mBinding.detail.setText(getString(R.string.firebase_status_fmt, user.getUid()));

            mBinding.emailPasswordButtons.setVisibility(View.GONE);
            mBinding.emailPasswordFields.setVisibility(View.GONE);
            mBinding.signedInButtons.setVisibility(View.VISIBLE);

            if (user.isEmailVerified()) {
                mBinding.verifyEmailButton.setVisibility(View.GONE);
            } else {
                mBinding.verifyEmailButton.setVisibility(View.VISIBLE);
            }
        } else {
            mBinding.status.setText(R.string.signed_out);
            mBinding.detail.setText(null);

            mBinding.emailPasswordButtons.setVisibility(View.VISIBLE);
            mBinding.emailPasswordFields.setVisibility(View.VISIBLE);
            mBinding.signedInButtons.setVisibility(View.GONE);
        }
    }*/

   /* private void checkForMultiFactorFailure(Exception e) {
        // Multi-factor authentication with SMS is currently only available for
        // Google Cloud Identity Platform projects. For more information:
        // https://cloud.google.com/identity-platform/docs/android/mfa
        if (e instanceof FirebaseAuthMultiFactorException) {
            Log.w(TAG, "multiFactorFailure", e);
            Intent intent = new Intent();
            MultiFactorResolver resolver = ((FirebaseAuthMultiFactorException) e).getResolver();
            intent.putExtra("EXTRA_MFA_RESOLVER", resolver);
            setResult(MultiFactorActivity.RESULT_NEEDS_MFA_SIGN_IN, intent);
            finish();
        }
    }*/

}
