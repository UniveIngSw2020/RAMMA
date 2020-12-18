package com.example.rent_scio1.Client;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.res.ResourcesCompat;

import android.app.Activity;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.rent_scio1.R;

public class SettingsCustomer extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings_customer);

        initViews();
        createListView_Personal_Info();

    }

    public void createListView_Personal_Info(){

        Typeface typeface = ResourcesCompat.getFont(this, R.font.comfortaa_regular);

        TextView tv = new TextView(this);
        tv.setText("INFORMAZIONI PERSONALI");
        tv.setTextSize(25);
        tv.setTextColor(getColor(R.color.teal_200));
        tv.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        tv.setTypeface(typeface);

        String[] items = {"Cambia Nome","Cambia Cognome","Cambia Email", "Cambia Password", "Cambia Numero di telefono", "Cambia il tuo Avatar"};
        ListView listView = findViewById(R.id.listview_settings_personal_info);
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(this,R.layout.format_info_settings, items);

        listView.setAdapter(arrayAdapter);
        listView.addHeaderView(tv);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                switch(position){
                    /*CAMBIA NOME*/
                    case 1:
                        Toast.makeText(getApplicationContext(), "Cambia nome selezionato", Toast.LENGTH_LONG).show();
                        break;
                    /*CAMBIA COGNOME*/
                    case 2:
                        Toast.makeText(getApplicationContext(), "Cambia cognome selezionato", Toast.LENGTH_LONG).show();
                        break;
                    /*CAMBIA EMAIL*/
                    case 3:
                        Toast.makeText(getApplicationContext(), "Cambia email selezionato", Toast.LENGTH_LONG).show();
                        break;
                    /*CAMBIA PASSWORD*/
                    case 4:
                        Toast.makeText(getApplicationContext(), "Cambia password selezionato", Toast.LENGTH_LONG).show();
                        break;
                    /*CAMBIA TELEFONO*/
                    case 5:
                        Toast.makeText(getApplicationContext(), "Cambia numero telefono selezionato", Toast.LENGTH_LONG).show();
                        break;
                    /*CAMBIO AVATAR*/
                    case 6:
                        Toast.makeText(getApplicationContext(), "Cambia avatar selezionato", Toast.LENGTH_LONG).show();
                        break;

                    default:
                }
            }
        });
    }

    public void initViews(){
        Toolbar settings_toolbar = findViewById(R.id.toolbar_settings_customer);
        setSupportActionBar(settings_toolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    //metodo richiamato nell'XML
    public void hideKeyboard(View view) {
        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }
}