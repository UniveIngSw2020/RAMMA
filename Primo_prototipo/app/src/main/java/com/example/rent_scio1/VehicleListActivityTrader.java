package com.example.rent_scio1;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.res.ResourcesCompat;

import com.example.rent_scio1.utils.Vehicle;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

public class VehicleListActivityTrader extends AppCompatActivity {

    private static final String TAG="VehicleListActivityTrader";

    private static final String Intent_newVehicle_maxID="Intent_newVehicle_maxID";
    private static final String Intent_newVehicle_nVehicle="Intent_newVehicle_nVehicle";
    private static int maxID=0;

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    private final ArrayList<Vehicle> vehicleArrayList = new ArrayList<>();

    private TextView warningEmpty;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vehicle_list_trader);

        warningEmpty=findViewById(R.id.warning_empty_table);
        warningEmpty.setVisibility(View.INVISIBLE);

        Query getVehiclesTrader = db.collection("vehicles").whereEqualTo("fk_trader", FirebaseAuth.getInstance().getUid());

        getVehiclesTrader.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                for (QueryDocumentSnapshot document : task.getResult()) {
                    vehicleArrayList.add(new Vehicle(document.toObject(Vehicle.class)));
                    Log.d(TAG, document.getId() + " => " + document.getData());
                }
                vehicleArrayList.sort( (o1, o2) -> o1.getID()-o2.getID() );
                maxID=createTable(vehicleArrayList);
                if( vehicleArrayList.size()==0 )
                    warningEmpty.setVisibility(View.VISIBLE);
            } else {
                Log.w(TAG, "Error getting documents.", task.getException());
            }
        });
       /* db.collection("vehicles")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {

                            vehicleArrayList.add(new Vehicle(document.toObject(Vehicle.class)));

                            Log.d(TAG, document.getId() + " => " + document.getData());
                        }

                        vehicleArrayList.sort( (o1, o2) -> o1.getID()-o2.getID() );

                        maxID=createTable(vehicleArrayList);

                        if( vehicleArrayList.size()==0 )
                            warningEmpty.setVisibility(View.VISIBLE);

                    } else {
                        Log.w(TAG, "Error getting documents.", task.getException());
                    }
                });*/



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

        AtomicBoolean wasSelected= new AtomicBoolean(false);
        Button elimina=findViewById(R.id.elimina);
        elimina.setOnClickListener(v -> {

            if(vehicleArrayList.size()==0){

                Toast.makeText(getApplicationContext(),"Nulla da eliminare qui",Toast.LENGTH_LONG).show();
                return;
            }

            Button conferma=findViewById(R.id.conferma_eliminazione_veicolo);
            Spinner spinner=findViewById(R.id.seleziona_veicolo_eliminare);

            if(wasSelected.get()){

                //nascondi tasto conferma e spinner
                conferma.setVisibility(View.INVISIBLE);
                spinner.setVisibility(View.INVISIBLE);
                wasSelected.set(false);
            }
            else{

                //mostra tasto conferma e spinner
                conferma.setVisibility(View.VISIBLE);
                spinner.setVisibility(View.VISIBLE);

                //avvia procedura di eliminazione
                creaEliminazione(wasSelected);
                wasSelected.set(true);
            }

        });

        initViews();
    }

    private void creaEliminazione(AtomicBoolean wasSelected){

        //creo copia ArrayList per trasformarlo in ArrayAdapter
        ArrayList<Vehicle> forDataAdapter=new ArrayList<>(vehicleArrayList);
        //forDataAdapter.removeIf(Vehicle::isRented);

        //aggiungo riga di selezione a Arraylist
        forDataAdapter.add(0,new Vehicle());


        //getto lo spinner
        Spinner selezionaVeicolo = findViewById(R.id.seleziona_veicolo_eliminare);

        //adatto i dati da Arraylist di veicolo
        ArrayAdapter<Vehicle> dataAdapter = new ArrayAdapter<Vehicle>(this, R.xml.spinner_item, forDataAdapter ){

            @Override
            public boolean isEnabled(int position){
                // Disable the first item from Spinner
                // First item will be use for hint
                return position != 0;
            }

            @Override
            public View getDropDownView(int position, View convertView, @NotNull ViewGroup parent) {
                View view = super.getDropDownView(position, convertView, parent);
                TextView tv = (TextView) view;
                if(position == 0){
                    // Set the hint text color gray
                    tv.setTextColor(Color.rgb(3,50,73));
                }
                else {
                    tv.setTextColor(Color.rgb(3,50,73));
                }

                return view;
            }
        };

        //aggiungo dati adattati a spinner
        dataAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
        selezionaVeicolo.setAdapter(dataAdapter);


        //dico cosa accade quando seleziono un item
        selezionaVeicolo.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                Vehicle vehicle = (Vehicle) parent.getItemAtPosition(position);

                Button conferma = findViewById(R.id.conferma_eliminazione_veicolo);
                conferma.setOnClickListener(v -> {

                    if( vehicle.getVehicleType()!=null ) {

                        db.collection("vehicles")
                                .get()
                                .addOnCompleteListener(task -> {
                                    if (task.isSuccessful()) {
                                        for (QueryDocumentSnapshot document : task.getResult()) {

                                            if( (new Vehicle(document.toObject(Vehicle.class))).getVehicleUID().equals(vehicle.getVehicleUID())  ){

                                                if( vehicle.isRented() ){
                                                    Toast.makeText(getApplicationContext(),"Non puoi eliminare un veicolo che è attualmente in corsa!",Toast.LENGTH_LONG).show();
                                                    return;
                                                }
                                                //elimina veicolo da DB
                                                eliminazione(document.getId());

                                                //reinizializza tabella
                                                vehicleArrayList.remove(vehicle);
                                                recreateTable(vehicleArrayList);

                                                //reinizializza spinner
                                                creaEliminazione(wasSelected);

                                                //setta visibilità
                                                findViewById(R.id.seleziona_veicolo_eliminare).setVisibility(View.INVISIBLE);
                                                findViewById(R.id.conferma_eliminazione_veicolo).setVisibility(View.INVISIBLE);
                                                wasSelected.set(false);

                                                if( vehicleArrayList.size()==0 )
                                                    warningEmpty.setVisibility(View.VISIBLE);

                                            }

                                            Log.d(TAG, document.getId() + " => " + document.getData());
                                        }
                                    } else {
                                        Log.w(TAG, "Error getting documents.", task.getException());
                                    }
                                });
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

    }

    private void eliminazione(String ID){
        db.collection("vehicles").document(ID)
                .delete()
                .addOnSuccessListener(aVoid -> Log.d(TAG, "DocumentSnapshot successfully deleted!"))
                .addOnFailureListener(e -> Log.w(TAG, "Error deleting document", e));
    }

    private void initViews(){
        Toolbar toolbar_vehicle_list_java = findViewById(R.id.toolbar_vehicle_list);
        setSupportActionBar(toolbar_vehicle_list_java);
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

    private void recreateTable(ArrayList<Vehicle> vehicles){
        TableLayout table = findViewById(R.id.tabella_veicoli);
        table.removeViews(1,vehicles.size()+1);

        maxID=createTable(vehicles);
    }
}