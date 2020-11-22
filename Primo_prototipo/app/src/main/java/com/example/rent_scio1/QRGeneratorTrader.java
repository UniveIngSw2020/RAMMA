package com.example.rent_scio1;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.rent_scio1.utils.UserClient;
import com.google.zxing.WriterException;

import androidmads.library.qrgenearator.QRGContents;
import androidmads.library.qrgenearator.QRGEncoder;


public class QRGeneratorTrader extends AppCompatActivity {

    private static final String ToQRVehicle="QR_code_creation_vehicle";
    private static final String ToQRDuration="QR_code_creation_duration";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_q_r_generator_trader);

        ImageView qrImage = findViewById(R.id.QR_code);

        //ID univoco del veicolo
        final String UID_veicolo = getIntent().getStringExtra(ToQRVehicle);
        final String duration=getIntent().getStringExtra(ToQRDuration);

        
        Log.d("ciao", UserClient.getUser().toString());


        Toast.makeText(this,UserClient.getUser().getUser_id() + " " + UID_veicolo + " " + duration, Toast.LENGTH_LONG).show();



        //Genero QR e ne faccio il display
        QRGEncoder qrgEncoder = new QRGEncoder(UserClient.getUser().getUser_id() + " " + UID_veicolo + " " + duration, null, QRGContents.Type.TEXT,500);

        try {
            // Getting QR-Code as Bitmap
            Bitmap bitmap = qrgEncoder.encodeAsBitmap();
            // Setting Bitmap to ImageView
            qrImage.setImageBitmap(bitmap);
        } catch (WriterException e) {
            Log.v("ciao", e.toString());
        }


        //quando il cliente ha stabile la connessione devo ritornare alla mappa principale

    }
}