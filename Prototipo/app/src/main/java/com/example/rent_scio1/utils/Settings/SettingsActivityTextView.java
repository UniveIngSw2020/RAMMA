package com.example.rent_scio1.utils.Settings;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.rent_scio1.Client.SettingsCustomer;
import com.example.rent_scio1.R;
import com.example.rent_scio1.Trader.SettingsTrader;
import com.example.rent_scio1.utils.User;
import com.example.rent_scio1.utils.UserClient;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

public class SettingsActivityTextView extends AppCompatActivity {

    private Intent intent;
    private static final String TAG = "SettingsActivityTextView";
    private boolean check = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings_text_view);

        String format="";

        if(UserClient.getUser().getTrader()){
            intent = new Intent(getApplicationContext(), SettingsTrader.class);
        }
        else{
            intent = new Intent(getApplicationContext(), SettingsCustomer.class);
        }
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        //prendo dell'intent cosa devo fare
        String type=getIntent().getStringExtra("type");
        String textType=getIntent().getStringExtra("textType");

        //setto il testo da visualizzare (descrizione)
        TextView textView=findViewById(R.id.text_for_settings);
        textView.setText(textType);

        //setto il testo modificabile
        EditText editText= findViewById(R.id.settings_text_view);

        switch (type){
            case "name":
                format = "Nome";
                editText.setText(UserClient.getUser().getName());
                editText.setInputType(InputType.TYPE_TEXT_VARIATION_PERSON_NAME);
                break;
            case "surname":
                format = "Cognome";
                editText.setText(UserClient.getUser().getSurname());
                editText.setInputType(InputType.TYPE_TEXT_VARIATION_PERSON_NAME);
                break;
            case "phone":
                format = "Numero di telefono";
                editText.setText(UserClient.getUser().getPhone());
                editText.setInputType(InputType.TYPE_CLASS_PHONE);
                break;
            case "shopName":
                format= "Nome del negozio";
                editText.setText(UserClient.getUser().getShopName());
                editText.setInputType(InputType.TYPE_CLASS_TEXT);
                break;
            case "email":
                format = "Email";
                editText.setText(UserClient.getUser().getEmail());
                editText.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
                break;
            case "password":
                format = "Password";
                editText.setHint("Password");
                editText.setTransformationMethod(PasswordTransformationMethod.getInstance());
                break;
        }

        //setto cosa deve fare il tasto conferma
        Button button=findViewById(R.id.settings_text_view_confirm);
        String finalFormat = format;

        FirebaseUser u = FirebaseAuth.getInstance().getCurrentUser();
        AuthCredential credential = EmailAuthProvider.getCredential(UserClient.getUser().getEmail(), UserClient.getUser().getUser_id()); // Current Login Credentials \\

        button.setOnClickListener(v -> {
            //cambio il dato dentro l'oggetto
            switch (type){
                case "name":
                    UserClient.getUser().setName(editText.getText().toString());
                    break;
                case "surname":
                    UserClient.getUser().setSurname(editText.getText().toString());
                    break;
                case "phone":
                    UserClient.getUser().setPhone(editText.getText().toString());
                    break;
                case "shopName":
                    UserClient.getUser().setShopName(editText.getText().toString());
                    break;
                case "email":
                    // Get auth credentials from the user for re-authentication

                    // Prompt the user to re-provide their sign-in credentials
                    u.reauthenticate(credential)
                            .addOnCompleteListener(task -> {
                                Log.d(TAG, "User re-authenticated.");
                                u.updateEmail(editText.getText().toString().trim()).addOnCompleteListener(task1 -> {
                                    if (task1.isSuccessful()) {
                                        UserClient.getUser().setEmail(editText.getText().toString());
                                        Log.e(TAG, "User email address updated.");
                                        check = true;
                                    }else{
                                        Log.e(TAG, "User NOT email address updated.");
                                        check = false;
                                    }
                                });
                            });

                    break;
                case "password":
                    u.reauthenticate(credential)
                            .addOnCompleteListener(task -> u.updatePassword(editText.getText().toString().trim()).addOnCompleteListener(task12 -> {
                                if (task12.isSuccessful()) {
                                    Log.e(TAG, "Password updated");
                                    UserClient.getUser().setUser_id(FirebaseAuth.getInstance().getUid());
                                    check = true;
                                } else {
                                    Log.e(TAG, "Error password not updated");
                                    check = false;
                                }
                            }));


                    break;
            }

            //richiamo metodo che aggiorna il DB
            if(check){
                SettingsUtil.updateAttribute("users", UserClient.getUser().getUser_id(), type, editText.getText().toString(), o -> {
                    Toast.makeText(getApplicationContext(), finalFormat + " cambiato/a correttamente.", Toast.LENGTH_LONG).show();
                    startActivity(intent);
                    finishAffinity();

                });
            }
        });

        initViews();
    }

    public void initViews(){
        Toolbar toolbar_settings_textviews = findViewById(R.id.toolbar_settings_textview);
        setSupportActionBar(toolbar_settings_textviews);
        Objects.requireNonNull(getSupportActionBar()).setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    //metodo richiamato nell'XML
    public void hideKeyboard(View view) {
        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }
}