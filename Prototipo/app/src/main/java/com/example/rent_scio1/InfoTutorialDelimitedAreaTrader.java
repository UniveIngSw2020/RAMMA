package com.example.rent_scio1;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.os.Bundle;

public class InfoTutorialDelimitedAreaTrader extends AppCompatActivity {

    private Toolbar info_tutorial;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info_tutorial_delimited_area_trader);

        initViews();
    }

    private void initViews(){
        info_tutorial = findViewById(R.id.toolbar_info_tutorial_);
        setSupportActionBar(info_tutorial);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }
}