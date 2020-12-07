package com.example.rent_scio1.Trader;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.rent_scio1.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class NewVehicleActivityTrader extends AppCompatActivity {

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private static final String TAG="NewVehicleActivityTrader";

    private static final String Intent_newVehicle_maxID="Intent_newVehicle_maxID";
    private static final String Intent_newVehicle_nVehicle="Intent_newVehicle_nVehicle";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_vehicle_trader);

        //creo oggetti testo per poi estrapolare il contenuto
        EditText vehicle_type = findViewById(R.id.tipo_veicolo);
        EditText seats = findViewById(R.id.posti_a_sedere);
        EditText maxSpeed=findViewById(R.id.maxSpeed);




        //aggiungo bottone conferma e il comportamento al click
        Button conferma = findViewById(R.id.Conferma);
        conferma.setOnClickListener(v -> {

            Map<String, Object> newVehicle = new HashMap<>();

            DocumentReference ref = db.collection("vehicles").document();
            String id = ref.getId();
            newVehicle.put("vehicleUID", id);

            newVehicle.put("fk_trader", FirebaseAuth.getInstance().getUid());
            newVehicle.put("vehicleType", vehicle_type.getText().toString());

            Integer seatsInteger=tryParse( seats.getText().toString() );
            if(seatsInteger==null){
                Toast.makeText(getApplicationContext(),"Non è mica un concerto dei metallica! Troppe persone a bordo",Toast.LENGTH_LONG).show();
                return;
            }
            newVehicle.put("seats", seatsInteger);

            newVehicle.put("rented", false);

            Integer maxSpeedKMH=tryParse( maxSpeed.getText().toString() );
            if(maxSpeedKMH==null){
                Toast.makeText(getApplicationContext(),"Va talmente veloce che viaggia nel tempo!",Toast.LENGTH_LONG).show();
                return;
            }
            newVehicle.put("maxSpeedKMH", maxSpeedKMH);

            //aggiungo il veicolo al db
            ref.set(newVehicle).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if(task.isSuccessful()){
                        Log.d(TAG, "DocumentSnapshot added with ID: " + ref.getId());
                    }else{
                        Log.w(TAG, "Error adding document");
                    }
                }
            });
            //db.collection("vehicles").add(newVehicle).addOnSuccessListener(documentReference -> Log.d(TAG, "DocumentSnapshot added with ID: " + documentReference.getId())).addOnFailureListener(e -> Log.w(TAG, "Error adding document", e));

            //ritorno alla tabella dei veicoli
            Intent intent=new Intent(getApplicationContext(),VehicleListActivityTrader.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);


        });

        initViews();
    }

    //metodo richiamato nell'XML
    public void hideKeyboard(View view) {
        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    public static Integer tryParse(String text) {
        try {
            return Integer.parseInt(text);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private void initViews(){
        Toolbar toolbar_new_vehicle_trader = findViewById(R.id.toolbar_new_vehicle);
        setSupportActionBar(toolbar_new_vehicle_trader);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }
}