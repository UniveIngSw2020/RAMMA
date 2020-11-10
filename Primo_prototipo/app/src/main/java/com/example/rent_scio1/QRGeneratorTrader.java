package com.example.rent_scio1;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;

import com.example.rent_scio1.utils.UserClient;
import com.google.zxing.WriterException;

import androidmads.library.qrgenearator.QRGContents;
import androidmads.library.qrgenearator.QRGEncoder;


public class QRGeneratorTrader extends AppCompatActivity {

    private static final String ToQR="QR_code_creation";

    Bitmap bitmap;
    ImageView qrImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_q_r_generator_trader);

        qrImage=findViewById(R.id.QR_code);

        //ID univoco del veicolo
        final int ID_veicolo=getIntent().getIntExtra(ToQR,0);

        //Utente commerciante usare la classe statica UserClient.getID()


        //Genero QR e ne faccio il display
        QRGEncoder qrgEncoder = new QRGEncoder(UserClient.getUser().toString(), null, QRGContents.Type.TEXT,500);

        try {
            // Getting QR-Code as Bitmap
            bitmap = qrgEncoder.encodeAsBitmap();
            // Setting Bitmap to ImageView
            qrImage.setImageBitmap(bitmap);
        } catch (WriterException e) {
            Log.v("ciao", e.toString());
        }

        
    }
}