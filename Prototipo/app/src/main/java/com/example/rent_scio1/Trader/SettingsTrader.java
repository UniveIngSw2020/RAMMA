package com.example.rent_scio1.Trader;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.widget.TextViewCompat;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.rent_scio1.R;
import com.example.rent_scio1.utils.Settings.SetAvatarActivity;
import com.example.rent_scio1.utils.Settings.SettingsActivityTextView;
import com.example.rent_scio1.utils.UserClient;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

public class SettingsTrader extends AppCompatActivity {

    private Intent intentTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings_trader);

        intentTextView=new Intent(getApplicationContext(), SettingsActivityTextView.class);

        initViews();

        createListViewFinal();
    }

    public void createListViewFinal(){
        AtomicBoolean flag = new AtomicBoolean(true);
        Typeface typeface = ResourcesCompat.getFont(this, R.font.comfortaa_regular);

        String[] items = { "INFORMAZIONI PERSONALI", "Cambia Nome","Cambia Cognome","Cambia Email", "Cambia Password", "Cambia Numero di telefono","INFORMAZIONI NEGOZIO", "Cambia Nome Negozio", "Cambia la posizione del negozio", "Cambia avatar del negozio"};
        ArrayList<String> i = new ArrayList<>(Arrays.asList(items));
        if(UserClient.getUser().getEmail().equals("administrator@rent.it")){
            i.add("Gestione account");
            i.add("hack");
        }
        ListView listView = findViewById(R.id.listview_final);
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(this, R.layout.format_info_settings, i){
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {

                TextView view = (TextView)super.getView(position, convertView, parent);
                if(position == 0 || position == 6 || position == 10){
                    view.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
                    view.setTextColor(getColor(R.color.teal_200));
                    view.setTypeface(typeface);
                    view.setClickable(false);
                    TextViewCompat.setAutoSizeTextTypeWithDefaults(view, TextViewCompat.AUTO_SIZE_TEXT_TYPE_UNIFORM);
                }

                return view;
            }
        };
        listView.setAdapter(arrayAdapter);

        listView.setOnItemClickListener((parent, view, position, id) -> {

            switch(position){
                //CAMBIA NOME
                case 1:
                    flag.set(true);
                    intentTextView.putExtra("type","name");
                    intentTextView.putExtra("textType","Scrivi il tuo nome!");
                    break;
                //CAMBIA COGNOME
                case 2:
                    flag.set(true);
                    intentTextView.putExtra("type","surname");
                    intentTextView.putExtra("textType","Scrivi il tuo cognome!");
                    break;
                //CAMBIA EMAIL
                case 3:
                    flag.set(true);
                    intentTextView.putExtra("type","email");
                    intentTextView.putExtra("textType","Scrivi la tua email");
                    break;
                //CAMBIA PASSWORD
                case 4:
                    flag.set(true);
                    intentTextView.putExtra("type","password");
                    intentTextView.putExtra("textType","Scrivi la tua password");
                    break;
                //CAMBIA TELEFONO
                case 5:
                    flag.set(true);
                    intentTextView.putExtra("type","phone");
                    intentTextView.putExtra("textType","Scrivi il tuo numero di telefono!");
                    break;
                case 7:
                    flag.set(true);
                    intentTextView.putExtra("type","shopName");
                    intentTextView.putExtra("textType","Scrivi il nome del tuo negozio!");
                    break;
                //POSIZIONE NEGOZIO
                case 8:
                    flag.set(false);

                    Intent intent=new Intent(getApplicationContext(),SetPositionActivityTrader.class);
                    intent.putExtra("IMPOSTAZIONI",true);

                    startActivity(intent);
                    break;
                //CAMBIO AVATAR NEGOZIO
                case 9:
                    flag.set(false);
                    startActivity(new Intent(getApplicationContext(), SetAvatarActivity.class));
                    break;
                case 11:
                    flag.set(true);
                    intentTextView.putExtra("type", "Elimina_account");
                    intentTextView.putExtra("textType", "Mail account da eliminare");
                    break;
                default:
                    flag.set(false);
                    break;
            }

            if(flag.get()){
                startActivity(intentTextView);
            }

        });

    }

    public void initViews(){
        Toolbar settings_toolbar = findViewById(R.id.toolbar_settings_trader);
        setSupportActionBar(settings_toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    //metodo richiamato nell'XML
    public void hideKeyboard(View view) {
        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }
}