package com.example.rent_scio1.Trader;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;


import com.example.rent_scio1.R;
import com.example.rent_scio1.services.MyFirebaseMessagingServices;

import com.example.rent_scio1.utils.Run;
import com.example.rent_scio1.utils.User;
import com.example.rent_scio1.utils.UserClient;

import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.zxing.WriterException;
import androidmads.library.qrgenearator.QRGContents;
import androidmads.library.qrgenearator.QRGEncoder;

// Activity di generazione di QR tramite parametri inseriti nell'activity NewRunActivityTrader o quando eliminiamo una corsa.
// Nel secondo caso settiamo il funzionamento del tasto "Forza eliminazione".

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

        //true: eliminazione, false: creazione
        final boolean bol=getIntent().getBooleanExtra("eliminazione",false);

        if(bol){
            Button elimina=findViewById(R.id.forza_delete);
            elimina.setVisibility(View.VISIBLE);
            elimina.setOnClickListener(v -> {
                AlertDialog.Builder builder = new android.app.AlertDialog.Builder(QRGeneratorTrader.this);
                builder.setTitle("Forza eliminazione");
                builder.setMessage("Sei sicuro di voler forzare l'eliminazione di questa corsa? \nATTENZIONE: da usare solo se non si riesce a usare il metodo standard!  ");

                builder.setPositiveButton("Sì", (dialog, id) -> {
                    dialog.dismiss();
                    deleteRunAndNotify(code, getIntent().getStringExtra("IDVEICOLO"), getIntent().getStringExtra("customerID"));


                });
                builder.setNegativeButton("No", (dialog, id) -> dialog.dismiss());
                AlertDialog alert = builder.create();
                alert.show();
            });
        }

        //Genero QR e ne faccio il display
        QRGEncoder qrgEncoder = new QRGEncoder(code, null, QRGContents.Type.TEXT,500);

        try {
            // Getting QR-Code as Bitmap
            Bitmap bitmap = qrgEncoder.encodeAsBitmap();
            // Setting Bitmap to ImageView
            qrImage.setImageBitmap(bitmap);
        } catch (WriterException e) {
            Log.v(TAG, e.toString());
        }


        //quando il cliente ha stabile la connessione devo ritornare alla mappa principale
        redirectMasActivityTrader(code);

    }

    private void deleteRunAndNotify(String runID, String vehicleID, String customerID){

        FirebaseFirestore.getInstance().collection("users").document(customerID).get().addOnSuccessListener(documentSnapshot -> {
            User x = documentSnapshot.toObject(User.class);
            if(x != null && x.getTokens() != null)
                for (String token : x.getTokens())
                    MyFirebaseMessagingServices.sendNotification(getApplicationContext(), token, "Chiusura forzata della corsa", "Riavvia l'applicazione nel dispositivo in uso per ripristinare la schermata principale");

            unlockVehiclebyID(vehicleID);
            deleteRun(runID);
        });

    }

    private void redirectMasActivityTrader(String code){
        FirebaseFirestore.getInstance().collection("run")
                .whereEqualTo("trader", UserClient.getUser().getUser_id())
                .addSnapshotListener((snapshots, e) -> {
                    if (e != null) {
                        Log.w(TAG, "Listen failed.", e);
                        return;
                    }

                    for (DocumentChange dc : snapshots.getDocumentChanges()) {
                        Log.w(TAG, "PRE SWITCH");
                        switch (dc.getType()) {
                            case ADDED:
                                Log.e(TAG, "SWITCH ADD");
                                //ENTRA NELL'ADD QUANDO PREMO IL PULSANTE CONfERMA ELIMINAZIONE SULL'ALERT DI ELIMINAZIONE DEL COMMERCIANTE, QUESTOM UTILIZZANDO LO SWICH dc.getType()
                                if(code.split(" ").length==3 && dc.getDocument().toObject(Run.class).getVehicle().equals(code.split(" ")[1])){
                                    Log.e(TAG, "ADD");
                                    Toast.makeText(QRGeneratorTrader.this, "Corsa Creata con Successo!", Toast.LENGTH_SHORT).show();

                                    finishAffinity();

                                    Intent intent=new Intent(getApplicationContext(), MapsActivityTrader.class);
                                    startActivity(intent);


                                }
                                break;
                            case REMOVED:
                                Log.e(TAG, "SWITCH DELETE");
                                if(code.split(" ").length==1){
                                    Log.e(TAG, "REMOVE");
                                    finishAffinity();

                                    Intent intent=new Intent(getApplicationContext(), MapsActivityTrader.class);
                                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                    startActivity(intent);

                                    Toast.makeText(QRGeneratorTrader.this, "Corsa Terminata con Successo!", Toast.LENGTH_SHORT).show();

                                }
                                break;
                        }
                    }
                });
    }

    private void unlockVehiclebyID(String id){
        DocumentReference mDatabase = FirebaseFirestore.getInstance().collection("vehicles").document(id);
        mDatabase.update("rented", false).addOnSuccessListener(aVoid -> Log.d(TAG, "VEICOLO LIBERATO"));
    }


    private void deleteRun(String PK_run){
        FirebaseFirestore.getInstance().collection("run").document(PK_run)
                .delete()
                .addOnSuccessListener(aVoid -> Log.e(TAG, "DocumentSnapshot successfully deleted!"))
                .addOnFailureListener(e -> Log.e(TAG, "Errore CORSA NON ELIMINATA", e))
                .addOnCompleteListener(task -> {
                    finishAffinity();

                    Intent intent=new Intent(getApplicationContext(), MapsActivityTrader.class);
                    startActivity(intent);

                    Toast.makeText(QRGeneratorTrader.this, "Corsa Terminata con Successo!", Toast.LENGTH_SHORT).show();

                });

        Log.e(TAG,"AREA ELIMINATA");

    }
}