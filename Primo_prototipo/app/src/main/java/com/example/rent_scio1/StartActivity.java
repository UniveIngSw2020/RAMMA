package com.example.rent_scio1;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class StartActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

        Button login_btn = (Button)findViewById(R.id.login_btn);

        login_btn.setOnClickListener(v -> startActivity(new Intent(StartActivity.this, LoginActivity.class)));

        Button register_btn = (Button)findViewById(R.id.register_btn);

        register_btn.setOnClickListener(v -> startActivity(new Intent(StartActivity.this, RegisterActivity.class)));
    }

}