package com.example.rent_scio1;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.res.ResourcesCompat;

import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.example.rent_scio1.utils.Run;
import com.example.rent_scio1.utils.User;
import com.example.rent_scio1.utils.Vehicle;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;


public class TabellaCorseTrader extends AppCompatActivity {

    private final String TAG="TabellaCorseTrader";
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private TextView warning_empty_table;
    private Toolbar toolbar_trades_active;

    private final ArrayList<Run> runs=new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tabella_corse_trader);

        queryRuns();

        initViews();
    }

    private void initViews(){
        toolbar_trades_active = findViewById(R.id.toolbar_trader_list);
        setSupportActionBar(toolbar_trades_active);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.trade_table_toolbar_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        switch(item.getItemId()){
            case R.id.end_trade:
                Toast.makeText(this, "elimina", Toast.LENGTH_LONG).show();
        }

        return true;
    }

    private void queryRuns(){


        Query getRunsTrader = db.collection("run").whereEqualTo("trader", FirebaseAuth.getInstance().getUid());

        getRunsTrader.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                for (QueryDocumentSnapshot document : task.getResult()) {

                    Run run=new Run(document.toObject(Run.class));

                    queryCustomer(run);

                    runs.add(run);
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

                    queryVehicle(run,u.getName()+" "+u.getSourname());
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

                    updateTime(createRow(user, v.getVehicleType(),run.getTimestamp()),run);

                }

            } else {
                Log.w(TAG, "Error getting documents.", task.getException());
            }
        });

    }

    private void updateTime(TextView timeText, Run run){

        long time=run.getStartTime() + run.getDuration() - Calendar.getInstance().getTime().getTime();

        CountDownTimer timer= new CountDownTimer(time, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                Integer minutes=(int) (millisUntilFinished / 1000) / 60;
                Integer seconds=(int) (millisUntilFinished / 1000) % 60;

                timeText.setText(minutes.toString()+":"+seconds.toString());

                if(minutes<5){
                    timeText.setTextColor(Color.YELLOW);
                }
            }

            @Override
            public void onFinish() {
                timeText.setText("TERMINATO");
                timeText.setTextColor(Color.RED);
            }
        }.start();

    }

    private TextView createRow(String user, String vehicle, Date time){
        Typeface typeface = ResourcesCompat.getFont(this, R.font.comfortaa_regular);

        TableLayout table = findViewById(R.id.tabella_corse);

        TableRow row;
        row = new TableRow(this);
        row.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT, 1.0f));
        row.setBackgroundColor(Color.rgb(3, 50, 73));
        row.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);

        TextView tv;
        tv = new TextView(this);
        tv.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT, 1.0f));
        tv.setText(user);
        tv.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        tv.setTypeface(typeface);
        tv.setTextColor(Color.rgb(113, 152, 241));

        TextView tv1 = new TextView(this);
        tv1.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT, 1.0f));
        tv1.setText(vehicle);
        tv1.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        tv1.setTypeface(typeface);
        tv1.setTextColor(Color.rgb(113, 152, 241));


        TextView tv2 = new TextView(this);
        tv2.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT, 1.0f));

        tv2.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        tv2.setTypeface(typeface);
        tv2.setTextColor(Color.rgb(113, 152, 241));



        row.addView(tv);
        row.addView(tv1);
        row.addView(tv2);


        table.addView(row);

        return tv2;
    }
}