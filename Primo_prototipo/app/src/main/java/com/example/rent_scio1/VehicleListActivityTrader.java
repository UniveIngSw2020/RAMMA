package com.example.rent_scio1;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.os.Bundle;
import android.util.Log;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.example.rent_scio1.utils.Vehicle;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class VehicleListActivityTrader extends AppCompatActivity {

    private static final String TAG="VehicleListActivityTrader";

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

                            createTable(vehicleArrayList);
                        } else {
                            Log.w(TAG, "Error getting documents.", task.getException());
                        }
                    }
                });
    }

    private void createTable(ArrayList<Vehicle> vehicles){

        TableLayout table = findViewById(R.id.tabella_veicoli);

        for (Vehicle v : vehicles ) {

            TableRow row = new TableRow(VehicleListActivityTrader.this);


            TextView tv = new TextView(VehicleListActivityTrader.this);
            tv.setText(Integer.toString(v.getID()));

            TextView tv1 = new TextView(VehicleListActivityTrader.this);
            tv1.setText(Integer.toString(v.getSeats()));

            TextView tv2 = new TextView(VehicleListActivityTrader.this);
            tv2.setText(v.getVehicleType());

            TextView tv3 = new TextView(VehicleListActivityTrader.this);

            if(v.isRented()){
                tv3.setText("OCCUPATO");
            }
            else{
                tv3.setText("DISPONIBILE");
            }

            row.addView(tv);
            row.addView(tv1);
            row.addView(tv2);
            row.addView(tv3);

            table.addView(row);
        }

    }
}