package com.example.rent_scio1;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.os.Bundle;

import java.util.Objects;

public class TabellaCorseTrader extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tabella_corse_trader);
        initViews();
    }

    private void initViews(){
        Toolbar toolbar_trades_active = findViewById(R.id.toolbar_trader_list);
        setSupportActionBar(toolbar_trades_active);
        Objects.requireNonNull(getSupportActionBar()).setTitle("");
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }


}