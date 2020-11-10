package com.example.rent_scio1;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.VerifiedInputEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.example.rent_scio1.utils.Vehicle;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class VehicleListActivityTrader extends AppCompatActivity {

    private static final String TAG="VehicleListActivityTrader";
    private Toolbar toolbar_vehicle_list_java;

    private static final String Intent_newVehicle="Intent_newVehicle";

    private static int maxID=0;

    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    private ArrayList<Vehicle> vehicleArrayList = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vehicle_list_trader);

        db.collection("vehicles")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {

                                vehicleArrayList.add(new Vehicle(document.toObject(Vehicle.class)));

                                Log.d(TAG, document.getId() + " => " + document.getData());
                            }

                            maxID=createTable(vehicleArrayList);
                        } else {
                            Log.w(TAG, "Error getting documents.", task.getException());
                        }
                    }
                });

        Button nuovo=findViewById(R.id.nuovo_veicolo);
        nuovo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent toNewVehicleActivityTrader =new Intent(getApplicationContext(),NewVehicleActivityTrader.class);
                toNewVehicleActivityTrader.putExtra(Intent_newVehicle,maxID);
                startActivity(toNewVehicleActivityTrader);
            }
        });

        initViews();
    }

    public void initViews(){
        toolbar_vehicle_list_java = findViewById(R.id.toolbar_vehicle_list);
        setSupportActionBar(toolbar_vehicle_list_java);
        Objects.requireNonNull(getSupportActionBar()).setTitle("");
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    //ritona l'ID massimo
    private int createTable(ArrayList<Vehicle> vehicles){

        TableLayout table = findViewById(R.id.tabella_veicoli);

        //titoli tabella
        String[] titlesString={"ID ", "Posti a sedere ", "Tipo veicolo ", "Noleggiato? "};
        TableRow titles = new TableRow(VehicleListActivityTrader.this);
        for (int i=0;i<4;i++){
            TextView tv = new TextView(VehicleListActivityTrader.this);
            tv.setText(titlesString[i]);
            tv.setTextColor(Color.BLUE);
            titles.addView(tv);
        }
        table.addView(titles);

        int max=0;

        //dati tabella
        for (Vehicle v : vehicles ) {

            TableRow row = new TableRow(VehicleListActivityTrader.this);


            TextView tv = new TextView(VehicleListActivityTrader.this);
            int ID=v.getID();
            tv.setText(Integer.toString(ID));

            TextView tv1 = new TextView(VehicleListActivityTrader.this);
            tv1.setText(Integer.toString(v.getSeats()));

            TextView tv2 = new TextView(VehicleListActivityTrader.this);
            tv2.setText(v.getVehicleType());

            TextView tv3 = new TextView(VehicleListActivityTrader.this);

            if(v.isRented()){
                tv3.setText("OCCUPATO");
                tv3.setTextColor(Color.RED);
            }
            else{
                tv3.setText("DISPONIBILE");
                tv3.setTextColor(Color.GREEN);
            }

            row.addView(tv);
            row.addView(tv1);
            row.addView(tv2);
            row.addView(tv3);

            table.addView(row);

            if(ID>max){
                max=ID;
            }
        }

        return max;
    }
}