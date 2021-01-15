package com.example.rent_scio1.Init;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;
import com.example.rent_scio1.R;

//schermata inziale dell'app, settiamo il comportamento dei due bottoni, oltre a visualizzare il testo di benvenuto.

public class StartActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_start);

        Button login_btn = findViewById(R.id.login_btn);

        login_btn.setOnClickListener(v -> startActivity(new Intent(StartActivity.this, LoginActivity.class)));

        Button register_btn = findViewById(R.id.register_btn);

        register_btn.setOnClickListener(v -> {
            startActivity(new Intent(StartActivity.this, RegisterActivity.class));
        });


    }

}