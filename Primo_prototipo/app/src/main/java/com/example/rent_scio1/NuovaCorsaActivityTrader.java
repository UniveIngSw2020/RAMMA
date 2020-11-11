package com.example.rent_scio1;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.rent_scio1.utils.Vehicle;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Objects;

public class NuovaCorsaActivityTrader extends AppCompatActivity {

    private static final String TAG="NuovaCorsaActivityTrader";

    private static final String ToQR="QR_code_creation";
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nuova_corsa_trader);

        Spinner selezionaVeicoli = findViewById(R.id.veicoli_disponibili);

        ArrayList<Vehicle> veicoliDisponibili = new ArrayList<>();

        //aggiungo un elemento per il "titolo" dello spinner
        veicoliDisponibili.add(0, new Vehicle());

        query(veicoliDisponibili,selezionaVeicoli);

        initViews();
    }

    private void query(ArrayList<Vehicle> veicoliDisponibili, Spinner spinner){

        db.collection("vehicles")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {

                            Vehicle v = new Vehicle(document.toObject(Vehicle.class));

                            if (!v.isRented())
                                veicoliDisponibili.add(v);

                            Log.d(TAG, document.getId() + " => " + document.getData());
                        }
                        spinnerAdd(veicoliDisponibili , spinner);

                    } else {
                        Log.w(TAG, "Error getting documents.", task.getException());
                    }
                });
    }



    private void spinnerAdd(ArrayList<Vehicle> veicoliDisponibili, Spinner selezionaVeicoli){

        // Spinner click listener
        selezionaVeicoli.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                Vehicle vehicle = (Vehicle) parent.getItemAtPosition(position);

                Button conferma = findViewById(R.id.conferma_veicolo);
                conferma.setOnClickListener(v -> {

                    if(vehicle.getVehicleType()!=null) {
                        Intent intent = new Intent(getApplicationContext(), QRGeneratorTrader.class);
                        intent.putExtra(ToQR, vehicle.getID());

                        startActivity(intent);
                    }
                    else {
                        Toast.makeText(getApplicationContext(),"Seleziona un veicolo prima!!!!! ",Toast.LENGTH_LONG).show();
                    }
                });

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        // Creating adapter for spinner
        ArrayAdapter<Vehicle> dataAdapter = new ArrayAdapter<Vehicle>(this, R.layout.support_simple_spinner_dropdown_item, veicoliDisponibili){

                @Override
                public boolean isEnabled(int position){
                    // Disable the first item from Spinner
                    // First item will be use for hint
                    return position != 0;
                }

                @Override
                public View getDropDownView(int position, View convertView, ViewGroup parent) {
                    View view = super.getDropDownView(position, convertView, parent);
                    TextView tv = (TextView) view;
                    if(position == 0){
                        // Set the hint text color gray
                        tv.setTextColor(Color.GRAY);
                    }
                    else {
                        tv.setTextColor(Color.BLACK);
                    }

                    return view;
                }
        };

        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        selezionaVeicoli.setAdapter(dataAdapter);


    }

    private void initViews(){
        Toolbar toolbar_new_corsa_java = findViewById(R.id.toolbar_new_corsa);
        setSupportActionBar(toolbar_new_corsa_java);
        Objects.requireNonNull(getSupportActionBar()).setTitle("");
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }
}