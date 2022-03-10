package com.sensorable.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.LinearLayout;

import com.sensorable.R;

public class AdvancedMenuActivity extends AppCompatActivity {

    private LinearLayout bluetoothLayout;
    private LinearLayout locationLayout;
    private LinearLayout adlsLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_advanced_menu);

        bluetoothLayout = (LinearLayout) findViewById(R.id.bluetoothOptions);
        bluetoothLayout.setOnClickListener(v -> {
            startActivity(new Intent(
                    this,
                    BluetoothOptionsActivity.class)
            );
        });

        adlsLayout = (LinearLayout) findViewById(R.id.adlOptions);
        adlsLayout.setOnClickListener(v -> {
            startActivity(new Intent(
                    this, AdlSummaryActivity.class)
            );
        });

        locationLayout = (LinearLayout) findViewById(R.id.locationOptions);
        locationLayout.setOnClickListener(v -> {
            startActivity(new Intent(
                    this,
                    LocationOptionsActivity.class)
            );
        });

    }
}