package com.example.rent_scio1.Init;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.rent_scio1.Client.MapsActivityClient;
import com.example.rent_scio1.Trader.MapsActivityTrader;
import com.example.rent_scio1.R;
import com.example.rent_scio1.utils.UserClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.FirebaseDatabase;

public class StartActivity extends AppCompatActivity {
    public static final String EXTRA_MESSAGE = "com.example.myfirstapp.MESSAGE";
    private static final String TAG = "StartActivity";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if(UserClient.getUser()!=null){

            if(UserClient.getUser().getTrader()){
                startActivity(new Intent(getApplicationContext(), MapsActivityTrader.class));
            }
            else{
                startActivity(new Intent(getApplicationContext(), MapsActivityClient.class));
            }

            finishAffinity();
        }
        else{

            setContentView(R.layout.activity_start);

            Button login_btn = (Button)findViewById(R.id.login_btn);

            login_btn.setOnClickListener(v -> startActivity(new Intent(StartActivity.this, LoginActivity.class)));

            Button register_btn = (Button)findViewById(R.id.register_btn);

            register_btn.setOnClickListener(v -> {
                Log.d(TAG, "Tasto premutoooooooooooooo oooooooooooooooo ooooooooooooooo ooooooooooooo ");
                startActivity(new Intent(StartActivity.this, RegisterActivity.class));
            });
        }

    }

}