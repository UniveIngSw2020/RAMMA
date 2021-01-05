package com.example.rent_scio1.Client;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.widget.TextViewCompat;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import com.example.rent_scio1.R;
import com.example.rent_scio1.utils.Settings.SetAvatarActivity;
import com.example.rent_scio1.utils.Settings.SettingsActivityTextView;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

public class SettingsCustomer extends AppCompatActivity {

    private Intent intentTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings_customer);

        initViews();
        createListView_Personal_Info();
    }

    public void createListView_Personal_Info(){

        Typeface typeface = ResourcesCompat.getFont(this, R.font.comfortaa_regular);

        String[] items = { "INFORMAZIONI PERSONALI", "Cambia Nome","Cambia Cognome","Cambia Email", "Cambia Password", "Cambia Numero di telefono","Cambia il tuo avatar"};
        ListView listView = findViewById(R.id.listview_settings_personal_info);
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(this, R.layout.format_info_settings, items){
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {

                TextView view = (TextView)super.getView(position, convertView, parent);
                if(position == 0){
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
            intentTextView = new Intent(getApplicationContext(), SettingsActivityTextView.class);
            switch(position){
                //CAMBIA NOME
                case 1:
                    intentTextView.putExtra("type","name");
                    intentTextView.putExtra("textType","Scrivi il tuo nome");
                    //startActivity(intentTextView);
                    break;
                //CAMBIA COGNOME
                case 2:
                    intentTextView.putExtra("type","surname");
                    intentTextView.putExtra("textType","Scrivi il tuo cognome");
                    //startActivity(intentTextView);
                    break;
                //CAMBIA EMAIL
                case 3:
                    intentTextView.putExtra("type","email");
                    intentTextView.putExtra("textType","Scrivi la tua email");
                    //startActivity(intentTextView);
                    break;
                //CAMBIA PASSWORD
                case 4:
                    intentTextView.putExtra("type","password");
                    intentTextView.putExtra("textType","Scrivi la tua password");
                    //startActivity(intentTextView);
                    break;
                //CAMBIA TELEFONO
                case 5:
                    intentTextView.putExtra("type","phone");
                    intentTextView.putExtra("textType","Scrivi il tuo numero di telefono");
                    //startActivity(intentTextView);
                    break;
                //CAMBIO AVATAR
                case 6:
                    intentTextView = new Intent(getApplicationContext(), SetAvatarActivity.class);
                    break;
                default:
            }
            startActivity(intentTextView);
        });
    }

    public void initViews(){
        Toolbar settings_toolbar = findViewById(R.id.toolbar_settings_customer);
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