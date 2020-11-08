package com.example.rent_scio1;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.rent_scio1.utils.Vehicle;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

public class NewVehicleActivityTrader extends AppCompatActivity {

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private static final String TAG="NewVehicleActivityTrader";

    private static final String Intent_newVehicle="Intent_newVehicle";

    private static int maxID=0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_vehicle_trader);

        //creo oggetti testo per poi estrapolare il contenuto
        EditText vehicle_type = findViewById(R.id.tipo_veicolo);
        EditText seats = findViewById(R.id.posti_a_sedere);


        //prendo l'ID più grande
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            maxID = extras.getInt(Intent_newVehicle);
            maxID++;
        }

        //aggiungo bottone conferma e il comportamento al click
        Button conferma = findViewById(R.id.Conferma);
        conferma.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                Map<String, Object> newVehicle = new HashMap<>();
                newVehicle.put("vehicleType", vehicle_type.getText().toString());
                newVehicle.put("seats", Integer.parseInt(seats.getText().toString()));
                newVehicle.put("ID", maxID);
                newVehicle.put("rented", false);

                //aggiungo il veicolo al db
                db.collection("vehicles")
                        .add(newVehicle)
                        .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                            @Override
                            public void onSuccess(DocumentReference documentReference) {
                                Log.d(TAG, "DocumentSnapshot added with ID: " + documentReference.getId());
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.w(TAG, "Error adding document", e);
                            }
                        });

                //ritorno alla tabella dei veicoli
                startActivity(new Intent(getApplicationContext(), VehicleListActivityTrader.class));
            }
        });
    }
}