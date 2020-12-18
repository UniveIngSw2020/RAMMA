package com.example.rent_scio1.Trader;

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

public class SettingsTrader extends AppCompatActivity {

    private static final String TAG = "SettingsTrader";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings_trader);

        initViews();
        createListView_Personal_Info();

        createListView_Shop_Info();

    }

    public void createListView_Personal_Info(){

        Typeface typeface = ResourcesCompat.getFont(this, R.font.comfortaa_regular);

        TextView tv = new TextView(this);
        tv.setText("INFORMAZIONI PERSONALI");
        tv.setTextSize(25);
        tv.setTextColor(getColor(R.color.teal_200));
        tv.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        tv.setTypeface(typeface);

        String[] items = {"Cambia Nome","Cambia Cognome","Cambia Email", "Cambia Password", "Cambia Numero di telefono"};
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

                    default:
                }
            }
        });
    }

    public void createListView_Shop_Info(){

        Typeface typeface = ResourcesCompat.getFont(this, R.font.comfortaa_regular);

        TextView tv = new TextView(this);
        tv.setText("INFORMAZIONI NEGOZIO");
        tv.setTextSize(25);
        tv.setTextColor(getColor(R.color.teal_200));
        tv.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        tv.setTypeface(typeface);

        String[] items = { "Cambia Nome Negozio", "Cambia la posizione del negozio"};
        ListView listView = findViewById(R.id.listview_settings_shop_info);
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(this,R.layout.format_info_settings, items);

        listView.setAdapter(arrayAdapter);
        listView.addHeaderView(tv);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                switch(position){
                    /*CAMBIA NOME*/
                    case 1:
                        Toast.makeText(getApplicationContext(), "Cambia nome negozio selezionato", Toast.LENGTH_LONG).show();
                        break;
                    /*POSIZIONE NEGOZIO*/
                    case 2:
                        Toast.makeText(getApplicationContext(), "Cambia posizione negozio selezionato", Toast.LENGTH_LONG).show();
                        break;

                    default:
                }
            }
        });
    }

    public void initViews(){
        Toolbar settings_toolbar = findViewById(R.id.toolbar_settings_trader);
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