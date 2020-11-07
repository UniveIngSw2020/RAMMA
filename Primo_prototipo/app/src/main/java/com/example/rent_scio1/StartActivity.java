package com.example.rent_scio1;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class StartActivity extends AppCompatActivity {
    public static final String EXTRA_MESSAGE = "com.example.myfirstapp.MESSAGE";
    private static final String TAG = "LOCATION: START ACTIVITY";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);


        Button login_btn = (Button)findViewById(R.id.login_btn);

        login_btn.setOnClickListener(v -> startActivity(new Intent(StartActivity.this, LoginActivity.class)));

        Button register_btn = (Button)findViewById(R.id.register_btn);

        register_btn.setOnClickListener(v -> {
            Log.d(TAG, "Tasto premutoooooooooooooo oooooooooooooooo ooooooooooooooo ooooooooooooo ");
            startActivity(new Intent(StartActivity.this, RegisterActivity.class));
        });
    }

    /*
    public void sendMessage(View view) {
        Intent intent = new Intent(this, LoginActivity.class);
        TextView textView = (TextView) findViewById(R.id.textView);
        String message = textView.getText().toString();
        intent.putExtra(EXTRA_MESSAGE, message);
        startActivity(intent);
    }*/

}