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
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class RegisterActivity extends AppCompatActivity {

    private static final String TAG = "RegisterActivity";

    private Map<String, Object> user = new HashMap<>();

    //widgets
    private EditText mName, mSurname, mEmail, mPassword, mConfirmPasswod, mPhone, mShopname;
    private CheckBox mTrader;

    //vars
    private FirebaseFirestore mStore;
    private String userID;

    //metodo richiamato nell'XML
    public void hideKeyboard(View view) {
        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        //mi getto gli oggetti dall'xml
        mName = findViewById(R.id.name);
        mSurname = findViewById(R.id.surname);
        mEmail = findViewById(R.id.email_register);
        mPassword = findViewById(R.id.password_register);
        mConfirmPasswod = findViewById(R.id.passwordregister_confirm);
        mPhone = findViewById(R.id.phone_register);
        mTrader = findViewById(R.id.check_Trader);
        mShopname = findViewById(R.id.shopName);
        mStore = FirebaseFirestore.getInstance();

        //metodo per inizializzare la UI
        initViews();
    }

    private void initViews() {

        Toolbar toolbar_regist = findViewById(R.id.toolbar_register);
        setSupportActionBar(toolbar_regist);
        Objects.requireNonNull(getSupportActionBar()).setDisplayShowHomeEnabled(true);
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
        if (TextUtils.isEmpty(mPassword.getText().toString().trim()) || mPassword.getText().length() < 6) {
            mPassword.setError("Immetti una password di almeno 6 caratteri!");
            flag = false;
        }
        if (TextUtils.isEmpty(mName.getText().toString().trim())) {
            mName.setError("Nome eichiesto!");
            flag = false;
        }
        if (TextUtils.isEmpty(mSurname.getText().toString().trim())) {
            mSurname.setError("Cognome richiesto!");
            flag = false;
        }
        if (TextUtils.isEmpty(mEmail.getText().toString().trim())) {
            mEmail.setError("Email richiesta!");
            flag = false;
        }
        if (TextUtils.isEmpty(mPhone.getText().toString().trim())) {
            mPhone.setError("Numero di cellulare richiesto!");
            flag = false;
        }

        if (mTrader.isChecked()) {

            if (TextUtils.isEmpty(mShopname.getText().toString().trim())) {
                mShopname.setError("Nome del negozio richiesto!");
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

        Log.d(TAG, "CREDENZIALIIIIIIIIIIIIIIIIII:        email: " + email + " password: " + password);
        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        userID = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();
                        generateStoreUser();
                    } else {
                        Log.w(TAG, "signInWithEmail:failure", task.getException());
                        Toast.makeText(RegisterActivity.this, "Authentication failed: ." + task.getException(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void generateStoreUser() {

        //inserisci nell'oggetto user le informazioni
        user.put("user_id", FirebaseAuth.getInstance().getUid());
        user.put("name", mName.getText().toString().trim());
        user.put("surname", mSurname.getText().toString().trim());
        user.put("email", mEmail.getText().toString().trim());
        user.put("phone", mPhone.getText().toString().trim());
        user.put("trader", mTrader.isChecked());
        user.put("shopName", mShopname.getText().toString().trim());

        //oggetti che verranno settati durante l'utilizzo dell'app
        user.put("delimited_area", null);
        user.put("traderPosition", null);


        //push dell'oggeto su db
        Log.d(TAG, "signInWithEmail:success");

        DocumentReference documentReference = mStore.collection("users").document(userID);

        documentReference.set(user).addOnSuccessListener(aVoid -> Log.d(TAG, "OOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOnSuccess: user Profile is created for: " + user))
                .addOnFailureListener(e -> System.out.println("onFailure: " + e.toString()));

        Toast.makeText(RegisterActivity.this, "User, Created!", Toast.LENGTH_SHORT).show();

        finishAffinity();

        UserClient.setUser(new User(FirebaseAuth.getInstance().getUid(), mName.getText().toString().trim(), mSurname.getText().toString().trim(), mEmail.getText().toString().trim(), mPhone.getText().toString().trim(), mTrader.isChecked(), mShopname.getText().toString().trim(), null, null, null));

        if (Objects.equals(user.get("trader"), true)) {
            startActivity(new Intent(getApplicationContext(), SetPositionActivityTrader.class));
        } else {
            startActivity(new Intent(getApplicationContext(), MapsActivityClient.class));
        }

    }
}
