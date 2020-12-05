package com.example.rent_scio1.Trader;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.rent_scio1.R;
import com.example.rent_scio1.utils.UserClient;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.zxing.WriterException;

import androidmads.library.qrgenearator.QRGContents;
import androidmads.library.qrgenearator.QRGEncoder;


public class QRGeneratorTrader extends AppCompatActivity {

    private static final String ToQR="QR_code_creation";
    private static final String TAG="QRGeneratorTrader";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_q_r_generator_trader);

        ImageView qrImage = findViewById(R.id.QR_code);

        //ID univoco
        final String code = getIntent().getStringExtra(ToQR);


        
        Log.d("ciao", UserClient.getUser().toString());


        Toast.makeText(this, code, Toast.LENGTH_LONG).show();



        //Genero QR e ne faccio il display
        QRGEncoder qrgEncoder = new QRGEncoder(code, null, QRGContents.Type.TEXT,500);

        try {
            // Getting QR-Code as Bitmap
            Bitmap bitmap = qrgEncoder.encodeAsBitmap();
            // Setting Bitmap to ImageView
            qrImage.setImageBitmap(bitmap);
        } catch (WriterException e) {
            Log.v("ciao", e.toString());
        }


        //quando il cliente ha stabile la connessione devo ritornare alla mappa principale


        redirectMasActivityTrader(code);

    }

    private void redirectMasActivityTrader(String code){
        FirebaseFirestore.getInstance().collection("run")
                .whereEqualTo("trader", UserClient.getUser().getUser_id())
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot snapshots,
                                        @Nullable FirebaseFirestoreException e) {
                        if (e != null) {
                            Log.w(TAG, "Listen failed.", e);
                            return;
                        }

                        for (DocumentChange dc : snapshots.getDocumentChanges()) {

                            switch (dc.getType()) {
                                case ADDED:
                                    //if(code.split(" ").length==3){
                                        startActivity(new Intent(getApplicationContext(), MapsActivityTrader.class));
                                        Toast.makeText(QRGeneratorTrader.this, "Corsa Creaata con Successo!", Toast.LENGTH_SHORT).show();
                                    //}
                                    break;
                                case REMOVED:
                                    //if(code.split(" ").length==1){
                                        startActivity(new Intent(getApplicationContext(), MapsActivityTrader.class));
                                        Toast.makeText(QRGeneratorTrader.this, "Corsa Terminata con Successo!", Toast.LENGTH_SHORT).show();
                                    //}
                                    break;
                            }
                        }
                    }
                });

    }
}