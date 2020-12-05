package com.example.rent_scio1;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.res.ResourcesCompat;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.example.rent_scio1.utils.Run;
import com.example.rent_scio1.utils.User;
import com.example.rent_scio1.utils.UserClient;
import com.example.rent_scio1.utils.Vehicle;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;


import java.util.Calendar;


public class RunTableTrader extends AppCompatActivity {

    private final String TAG="TabellaCorseTrader";
    private static final String ToQR="QR_code_creation";
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private TextView warning_empty_table;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tabella_corse_trader);

        warning_empty_table = findViewById(R.id.corse_attive);
        warning_empty_table.setVisibility(View.INVISIBLE);

        queryRuns();
        initViews();
    }

    private void initViews(){
        Toolbar toolbar_trades_active = findViewById(R.id.toolbar_trader_list);
        setSupportActionBar(toolbar_trades_active);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void queryRuns(){

        Query getRunsTrader = db.collection("run").whereEqualTo("trader", UserClient.getUser().getUser_id());

        getRunsTrader.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                boolean isEmpty=true;
                for (QueryDocumentSnapshot document : task.getResult()) {

                    Run run=new Run(document.toObject(Run.class));

                    queryCustomer(run);
                    isEmpty=false;
                }

                if(isEmpty){
                    warning_empty_table = findViewById(R.id.corse_attive);
                    warning_empty_table.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    private void queryCustomer(Run run){

        Query getCliente = db.collection("users").whereEqualTo("user_id", run.getUser() );

        getCliente.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {

                for (QueryDocumentSnapshot document : task.getResult()) {

                    User u=new User(document.toObject(User.class));

                    String customer=u.getName()+" "+u.getSourname();

                    if(customer.length()>8){
                        customer=u.getName()+"\n"+u.getSourname();
                    }

                    queryVehicle(run,customer);
                }

            } else {
                Log.w(TAG, "Error getting documents.", task.getException());
            }
        });

    }

    private void queryVehicle(Run run, String user){

        Query getCliente = db.collection("vehicles").whereEqualTo("vehicleUID", run.getVehicle() );

        getCliente.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {

                for (QueryDocumentSnapshot document : task.getResult()) {

                    Vehicle v=new Vehicle(document.toObject(Vehicle.class));

                    updateTime(createRow(user, v.getVehicleType(), run),run);

                }

            } else {
                Log.w(TAG, "Error getting documents.", task.getException());
            }
        });

    }

    private void updateTime(TextView timeText, Run run){

        long time=run.getStartTime() + run.getDuration() - Calendar.getInstance().getTime().getTime();

        new CountDownTimer(time, 1000) {
            @SuppressLint("DefaultLocale")
            @Override
            public void onTick(long millisUntilFinished) {
                int minutes=(int) (millisUntilFinished / 1000) / 60;
                int seconds=(int) (millisUntilFinished / 1000) % 60;

                timeText.setText(String.format("%d:%d", minutes, seconds));

                if(minutes<5){
                    timeText.setTextColor(Color.YELLOW);
                }
            }

            @SuppressLint("SetTextI18n")
            @Override
            public void onFinish() {
                timeText.setText("TERMINATO");
                timeText.setTextColor(Color.rgb(236, 124, 124));
            }
        }.start();

    }

    @SuppressLint("SetTextI18n")
    private TextView createRow(String user, String vehicle, Run run){
        Typeface typeface = ResourcesCompat.getFont(this, R.font.comfortaa_regular);

        TableLayout table = findViewById(R.id.tabella_corse);

        TableRow row;
        row = new TableRow(this);
        row.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT, 1.0f));
        row.setBackgroundColor(Color.rgb(3, 50, 73));
        row.setPadding(0,5,0,0);
        row.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);

        TextView tv;
        tv = new TextView(RunTableTrader.this);
        tv.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT, 1.0f));
        tv.setText(user);
        tv.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        tv.setTypeface(typeface);
        tv.setTextColor(Color.rgb(113, 152, 241));

        TextView tv1 = new TextView(RunTableTrader.this);
        tv1.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT, 1.0f));
        tv1.setText(vehicle);
        tv1.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        tv1.setTypeface(typeface);
        tv1.setTextColor(Color.rgb(113, 152, 241));


        TextView tv2 = new TextView(RunTableTrader.this);
        tv2.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT, 1.0f));
        tv2.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        tv2.setTypeface(typeface);
        tv2.setTextColor(Color.rgb(113, 152, 241));


        Button delete=new Button(RunTableTrader.this);
        delete.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT, 1.0f));
        delete.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        delete.setTypeface(typeface);
        delete.setPadding(0,1,0,0);
        delete.setBackgroundResource(R.drawable.rounded_button);
        delete.setTextSize(18);
        delete.setTextColor(getColor(R.color.back));
        delete.setText("ELIMINA");

        delete.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(RunTableTrader.this);
            builder.setTitle("Elimina");
            builder.setMessage("Sei sicuro di voler eliminare definitivamente questa corsa?");

            //builder.setIcon(R.drawable.ic_launcher);
            builder.setPositiveButton("Sì", (dialog, id) -> {
                dialog.dismiss();
                //deleteRun(run.getRunUID());
                //unlockVehiclebyID(run.getVehicle());
                //Intent intent=new Intent(getApplicationContext(), RunTableTrader.class);
                //intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

                Intent intent=new Intent(getApplicationContext(), QRGeneratorTrader.class);
                intent.putExtra(ToQR, run.getRunUID());
                startActivity(intent);
            });
            builder.setNegativeButton("No", (dialog, id) -> dialog.dismiss());
            builder.setNegativeButton("No", (dialog, id) -> dialog.dismiss());
            AlertDialog alert = builder.create();
            alert.show();
        });

        row.addView(tv);
        row.addView(tv1);
        row.addView(tv2);
        row.addView(delete);


        table.addView(row);

        //setti invisible
        warning_empty_table.setVisibility(View.INVISIBLE);

        return tv2;
    }

    /*private void deleteRun(String PK_run){
        db.collection("run").document(PK_run)
                .delete()
                .addOnSuccessListener(aVoid ->

                        Log.e(TAG, "DocumentSnapshot successfully DELETEEEEEEEEEEEEEED!"))
                .addOnFailureListener(e -> Log.e(TAG, "ERRRRRRRROREEEEEEEEEE CORSA NON ELIMINATA", e));
    }

    private void unlockVehiclebyID(String id){
        DocumentReference mDatabase = FirebaseFirestore.getInstance().collection("vehicles").document(id);
        mDatabase.update("rented", false).addOnSuccessListener(aVoid -> {
            Log.d(TAG, "VEICOLO LIBERATO");
        });
    }*/
}