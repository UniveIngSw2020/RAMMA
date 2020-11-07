package com.example.rent_scio1;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.os.Bundle;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

public class TableActivityTrader extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_table_trader);

        createTable(45,45);

    }

    private void createTable(int rowNumber, int columnNumber){

        TableLayout table = new TableLayout(this);

        for (int i=0; i < rowNumber; i++) {

            TableRow row = new TableRow(TableActivityTrader.this);

            for (int j=0; j < columnNumber; j++) {
                TextView tv = new TextView(TableActivityTrader.this);
                tv.setText("ciao");
                row.addView(tv);
            }

            table.addView(row);
        }

        ConstraintLayout mainLayout = findViewById(R.id.main);
        mainLayout.addView(table);
    }
}