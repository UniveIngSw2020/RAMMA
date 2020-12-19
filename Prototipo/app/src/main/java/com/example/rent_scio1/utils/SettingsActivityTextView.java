package com.example.rent_scio1.utils;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.example.rent_scio1.Client.SettingsCustomer;
import com.example.rent_scio1.R;
import com.example.rent_scio1.Trader.SettingsTrader;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.tasks.OnSuccessListener;

public class SettingsActivityTextView extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings_text_view);

        User user=UserClient.getUser();

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
                editText.setText(user.getName());
                editText.setInputType(InputType.TYPE_TEXT_VARIATION_PERSON_NAME);
                break;
            case "surname":
                editText.setText(user.getSurname());
                editText.setInputType(InputType.TYPE_TEXT_VARIATION_PERSON_NAME);
                break;
            case "phone":
                editText.setText(user.getPhone());
                editText.setInputType(InputType.TYPE_CLASS_PHONE);
                break;
            case "shopName":
                editText.setText(user.getShopName());
                editText.setInputType(InputType.TYPE_CLASS_TEXT);
                break;
        }

        //setto cosa deve fare il tasto conferma
        Button button=findViewById(R.id.settings_text_view_confirm);
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

                Intent intent;

                if(user.getTrader()){
                    intent = new Intent(getApplicationContext(), SettingsTrader.class);
                }
                else{
                    intent = new Intent(getApplicationContext(), SettingsCustomer.class);
                }
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);

            });
        });
    }
}