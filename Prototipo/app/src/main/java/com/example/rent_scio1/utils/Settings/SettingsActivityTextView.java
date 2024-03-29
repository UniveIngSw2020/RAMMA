package com.example.rent_scio1.utils.Settings;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.Activity;
import android.app.AlertDialog;
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

import com.example.rent_scio1.Client.MapsActivityClient;
import com.example.rent_scio1.Client.SettingsCustomer;
import com.example.rent_scio1.R;
import com.example.rent_scio1.Trader.MapsActivityTrader;
import com.example.rent_scio1.Trader.SettingsTrader;
import com.example.rent_scio1.utils.User;
import com.example.rent_scio1.utils.UserClient;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Objects;

// Activity di impostazione informazione in forma testuale: qua decidiamo a runtime che cosa visualizzare e permettiamo la modifica caricando il risultato su DB.

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

        //text solo per password
        EditText editText1=findViewById(R.id.settings_text_view2);

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

                editText1.setHint("Conferma password");
                editText1.setTransformationMethod(PasswordTransformationMethod.getInstance());
                editText1.setVisibility(View.VISIBLE);
                break;
            case "Elimina_account":
                format = "elimina account";
                editText.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
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

                    if(UserClient.getUser().getTrader()){
                        intent = new Intent(getApplicationContext(), MapsActivityTrader.class);
                    } else{
                        intent = new Intent(getApplicationContext(), MapsActivityClient.class);
                    }
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

                    break;
                case "email":
                    u.reauthenticate(credential)
                                .addOnCompleteListener(task -> {
                                    Log.d(TAG, "User re-authenticated.");
                                    u.updateEmail(editText.getText().toString().trim()).addOnCompleteListener(task1 -> {
                                        if (task1.isSuccessful()) {
                                            UserClient.getUser().setEmail(editText.getText().toString());
                                            check=true;
                                            Log.e(TAG, "User email address updated.");
                                        } else {
                                            Log.e(TAG, "User NOT email address updated.");
                                            check = false;
                                        }

                                    });
                                });

                    break;
                case "password":
                    check=false;
                    if( editText.getText().toString().equals(editText1.getText().toString()) ){
                        u.reauthenticate(credential)
                                .addOnCompleteListener(task -> u.updatePassword(editText.getText().toString().trim()).addOnCompleteListener(task12 -> {
                                    if (task12.isSuccessful()) {
                                        Log.e(TAG, "Password updated " + editText.getText().toString());
                                        UserClient.getUser().setUser_id(FirebaseAuth.getInstance().getUid());
                                        startActivity(intent);
                                    } else {
                                        Log.e(TAG, "Error password not updated");
                                    }
                                }));
                    } else{
                        Toast.makeText(this,"Le password non corrispondono",Toast.LENGTH_LONG).show();
                    }
                    break;
                case "Elimina_account":
                    check = false;
                    String psw = "123456";
                    AuthCredential hackCredential = EmailAuthProvider.getCredential(editText.getText().toString(), psw.trim()); // Current Login Credentials \\
                    FirebaseAuth.getInstance().signOut();

                    FirebaseAuth.getInstance().signInWithCredential(hackCredential)
                            .addOnSuccessListener(aSuccess1 -> {
                                Log.e(TAG, "Account hackerato");
                                FirebaseUser hack = FirebaseAuth.getInstance().getCurrentUser();
                                assert hack != null;
                                hack.reauthenticate(hackCredential).addOnSuccessListener(aSuccess2 -> {
                                    Log.e(TAG, "Account hack reautenticato");
                                    hack.delete()
                                            .addOnSuccessListener(aSuccess3 -> {
                                                FirebaseFirestore.getInstance().collection("users").whereEqualTo("email", editText.getText().toString()).get().addOnSuccessListener(queryDocumentSnapshots -> {
                                                    String uid = "";
                                                    for (DocumentSnapshot d : queryDocumentSnapshots.getDocuments()) {
                                                        uid = Objects.requireNonNull(d.toObject(User.class)).getUser_id();
                                                    }
                                                    FirebaseFirestore.getInstance().collection("users").document(uid).delete().addOnCompleteListener(task -> {
                                                        FirebaseAuth.getInstance().signOut();
                                                        FirebaseAuth.getInstance().signInWithEmailAndPassword("administrator@rent.it", "admin1")
                                                                .addOnCompleteListener(task1 -> Log.e(TAG, "tornato nell'admin forse"));
                                                        Log.e(TAG, "Account eliminato");
                                                    });

                                                });

                                            })
                                            .addOnFailureListener(error3 -> Log.e(TAG, "Account NON eliminato"));
                                }).addOnFailureListener(error2 -> Log.e(TAG, "account hack non reautenticato"));
                            }).addOnFailureListener(error1 -> {
                                Log.e(TAG, "Account non hackerato");
                            });

                    break;
            }

            //richiamo metodo che aggiorna il DB
            if(check){
                SettingsUtil.updateAttribute("users", UserClient.getUser().getUser_id(), type, editText.getText().toString(), o -> {
                    Toast.makeText(getApplicationContext(), finalFormat + " cambiato/a correttamente.", Toast.LENGTH_LONG).show();
                    startActivity(intent);
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
    public void onBackPressed() {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("ATTENZIONE:");
        builder.setMessage("Sei sicuro di voler uscire dalla schermata senza confermare i cambiamenti?\n");

        builder.setPositiveButton("Sì", (dialog, id) ->{
            dialog.dismiss();
            startActivity(intent);
        });
        builder.setNegativeButton("No", (dialog, id) ->{
            dialog.dismiss();
        });
        builder.create().show();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
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