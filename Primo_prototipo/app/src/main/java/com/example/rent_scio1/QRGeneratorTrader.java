package com.example.rent_scio1;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;

public class QRGeneratorTrader extends AppCompatActivity {

    private static final String ToQR="QR_code_creation";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_q_r_generator_trader);

        //ID univoco del veicolo
        final int ID=getIntent().getIntExtra(ToQR,0);

        
    }
}