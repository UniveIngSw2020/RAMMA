package com.example.rent_scio1.utils.Settings;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
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

import java.util.Objects;

public class SettingsActivityTextView extends AppCompatActivity {

    private Intent intent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings_text_view);

        String format="";
        User user= UserClient.getUser();

        if(user.getTrader()){
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
                editText.setText(user.getName());
                editText.setInputType(InputType.TYPE_TEXT_VARIATION_PERSON_NAME);
                break;
            case "surname":
                format = "Cognome";
                editText.setText(user.getSurname());
                editText.setInputType(InputType.TYPE_TEXT_VARIATION_PERSON_NAME);
                break;
            case "phone":
                format = "Numero di telefono";
                editText.setText(user.getPhone());
                editText.setInputType(InputType.TYPE_CLASS_PHONE);
                break;
            case "shopName":
                format= "Nome del negozio";
                editText.setText(user.getShopName());
                editText.setInputType(InputType.TYPE_CLASS_TEXT);
                break;
        }

        //setto cosa deve fare il tasto conferma
        Button button=findViewById(R.id.settings_text_view_confirm);
        String finalFormat = format;
        button.setOnClickListener(v -> {
            //cambio il dato dentro l'oggetto
            switch (type){
                case "name":
                    user.setName(editText.getText().toString());
                    break;
                case "surname":
                    user.setSurname(editText.getText().toString());
                    break;
                case "phone":
                    user.setPhone(editText.getText().toString());
                    break;
                case "shopName":
                    user.setShopName(editText.getText().toString());
                    break;
            }

            //richiamo metodo che aggiorna il DB
            SettingsUtil.updateAttribute("users", user.getUser_id(), type, editText.getText().toString(), o -> {
                Toast.makeText(getApplicationContext(), finalFormat + " cambiato correttamente.", Toast.LENGTH_LONG).show();
                startActivity(intent);

            });
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
            //this.finish();
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