package com.example.rent_scio1.Trader;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.res.ResourcesCompat;
import com.example.rent_scio1.R;
import com.example.rent_scio1.utils.Vehicle;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;

public class VehicleListActivityTrader extends AppCompatActivity {

    private static final String TAG="VehicleListActivityTrader";


    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    private final ArrayList<Vehicle> vehicleArrayList = new ArrayList<>();

    private TextView warningEmpty;
    Toolbar toolbar_vehicle_list_java;


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
                vehicleArrayList.sort( (o1, o2) -> o1.getSeats()-o2.getSeats() );

                createTable(vehicleArrayList);

                if( vehicleArrayList.size()==0 )
                    warningEmpty.setVisibility(View.VISIBLE);

            } else {
                Log.w(TAG, "Error getting documents.", task.getException());
            }
        });

        initViews();
        toolbar_vehicle_list_java = findViewById(R.id.toolbar_vehicle_list);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_new_vehicle_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.add_one_vehicle) {
            if(vehicleArrayList.size()>=Vehicle.maxVehicles){
                Toast.makeText(getApplicationContext(),"ATTENZIONE: non puoi inserire più di 10 veicoli",Toast.LENGTH_LONG).show();
            }
            else{
                Intent toNewVehicleActivityTrader =new Intent(getApplicationContext(),NewVehicleActivityTrader.class);
                startActivity(toNewVehicleActivityTrader);
            }
            return true;
        } else
            return super.onOptionsItemSelected(item);
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
    private void createTable(ArrayList<Vehicle> vehicles){
        Typeface typeface = ResourcesCompat.getFont(this, R.font.comfortaa_regular);

        TableLayout table = findViewById(R.id.tabella_veicoli);

        /*Posti a sedere, Tipo veicolo, Disponibilità, Elimina*/

        //dati tabella
        for (Vehicle v : vehicles ) {

            TableRow row;
            row = new TableRow(this);
            row.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT, 1.0f));
            row.setBackgroundColor(Color.rgb(3, 50, 73));
            row.setPadding(0,8,0,0);
            row.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);


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

            Button delete=new Button(VehicleListActivityTrader.this);
            delete.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT, 1.0f));
            delete.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
            delete.setTypeface(typeface);
            delete.setPadding(0,1,0,0);
            delete.setBackgroundResource(R.drawable.rounded_button);
            delete.setTextSize(18);
            delete.setTextColor(getColor(R.color.back));
            delete.setText("ELIMINA");

            delete.setOnClickListener(view -> {
                if(v.isRented()){
                    Toast.makeText(getApplicationContext(),"Non puoi eliminare un veicolo occupato",Toast.LENGTH_LONG).show();
                }
                else{
                    AlertDialog.Builder builder = new AlertDialog.Builder(VehicleListActivityTrader.this);
                    builder.setTitle("Conferma eliminazione");
                    builder.setMessage("Sei sicuro di voler eliminare definitivamente questo veicolo?");

                    builder.setPositiveButton("Sì", (dialog, id) -> {
                        dialog.dismiss();
                        eliminazione(v.getVehicleUID());

                        Intent intent=new Intent(getApplicationContext(),VehicleListActivityTrader.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);
                    });
                    builder.setNegativeButton("No", (dialog, id) -> dialog.dismiss());
                    AlertDialog alert = builder.create();
                    alert.show();
                }
            });

            row.addView(tv2);
            row.addView(tv1);
            row.addView(tv3);
            row.addView(delete);

            table.addView(row);

        }

    }
}