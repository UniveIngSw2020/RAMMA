package com.example.rent_scio1;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.res.ResourcesCompat;
import androidx.transition.Slide;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.example.rent_scio1.utils.Vehicle;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Objects;

public class VehicleListActivityTrader extends AppCompatActivity {

    private static final String TAG="VehicleListActivityTrader";

    private Toolbar toolbar_vehicle_list_java;

    private static final String Intent_newVehicle_maxID="Intent_newVehicle_maxID";
    private static final String Intent_newVehicle_nVehicle="Intent_newVehicle_nVehicle";
    private static int maxID=0;

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    private final ArrayList<Vehicle> vehicleArrayList = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vehicle_list_trader);

        db.collection("vehicles")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {

                            vehicleArrayList.add(new Vehicle(document.toObject(Vehicle.class)));

                            Log.d(TAG, document.getId() + " => " + document.getData());
                        }

                        vehicleArrayList.sort( (o1, o2) -> o1.getID()-o2.getID() );

                        maxID=createTable(vehicleArrayList);


                    } else {
                        Log.w(TAG, "Error getting documents.", task.getException());
                    }
                });


        Button nuovo=findViewById(R.id.nuovo_veicolo);
        nuovo.setOnClickListener(v -> {
            if(vehicleArrayList.size()>=Vehicle.maxVehicles){
                Toast.makeText(getApplicationContext(),"ATTENZIONE: non puoi inserire più di 10 veicoli",Toast.LENGTH_LONG).show();
            }
            else{
                Intent toNewVehicleActivityTrader =new Intent(getApplicationContext(),NewVehicleActivityTrader.class);
                toNewVehicleActivityTrader.putExtra(Intent_newVehicle_maxID,maxID);
                toNewVehicleActivityTrader.putExtra(Intent_newVehicle_nVehicle,vehicleArrayList.size());
                startActivity(toNewVehicleActivityTrader);
            }
        });

        Button elimina=findViewById(R.id.elimina);
        elimina.setOnClickListener(v -> {

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
        Typeface typeface = ResourcesCompat.getFont(this, R.font.comfortaa_regular);

        TableLayout table = findViewById(R.id.tabella_veicoli);
        /*ID, Posti a sedere, Tipo veicolo, Disponibilità*/

        int max=0;

        //dati tabella
        for (Vehicle v : vehicles ) {

            TableRow row;
            row = new TableRow(this);
            row.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT, 1.0f));
            row.setBackgroundColor(Color.rgb(3, 50, 73));
            row.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);

            TextView tv;
            /*tv = findViewById(R.id.textview_dyna);*/
            tv = new TextView(VehicleListActivityTrader.this);
            tv.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT, 1.0f));
            int ID = v.getID();
            tv.setText(Integer.toString(ID));
            tv.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
            tv.setTypeface(typeface);
            tv.setTextColor(Color.rgb(113, 152, 241));

            TextView tv1 = new TextView(VehicleListActivityTrader.this);
            tv1.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT, 1.0f));
            tv1.setText(Integer.toString(v.getSeats()));
            tv1.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
            tv1.setTypeface(typeface);
            tv1.setTextColor(Color.rgb(113, 152, 241));

            TextView tv2 = new TextView(VehicleListActivityTrader.this);
            tv2.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT, 1.0f));
            tv2.setText(v.getVehicleType());
            tv2.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
            tv2.setTypeface(typeface);
            tv2.setTextColor(Color.rgb(113, 152, 241));

            TextView tv3 = new TextView(VehicleListActivityTrader.this);
            tv3.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT, 1.0f));
            tv3.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
            tv3.setTypeface(typeface);


            if (v.isRented()) {
                tv3.setText("OCCUPATO");
                tv3.setTextColor(Color.rgb(236, 124, 124));
            } else {
                tv3.setText("DISPONIBILE");
                tv3.setTextColor(Color.rgb(94, 214, 121));
            }

            row.addView(tv);
            row.addView(tv1);
            row.addView(tv2);
            row.addView(tv3);

            table.addView(row);

            if (ID > max) {
                max = ID;
            }


        }

        return max;
    }
}