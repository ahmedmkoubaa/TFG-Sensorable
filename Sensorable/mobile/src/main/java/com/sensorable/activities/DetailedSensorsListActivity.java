package com.sensorable.activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.Sensor;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.commons.DeviceType;
import com.commons.SensorTransmissionCoder;
import com.commons.SensorableConstants;
import com.commons.SensorsProvider;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.sensorable.MainActivity;
import com.sensorable.R;

import java.util.ArrayList;

public class DetailedSensorsListActivity extends AppCompatActivity {

    private TextView accelerometerTextView;
    private TextView temperatureTextView;
    private TextView humidityTextView;
    private TextView proximityTextView;
    private TextView lightTextView;
    private Button advancedMenuButton;

    private SensorsProvider sensorsProvider;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detailed_sensors_list);

        initializeAttributtesFromUI();
        initializeSensors();
    }

    private void initializeAttributtesFromUI() {
        accelerometerTextView = findViewById(R.id.acceleromterText);
        temperatureTextView = findViewById(R.id.temperatureText);
        humidityTextView = findViewById(R.id.humidityText);
        proximityTextView = findViewById(R.id.proximityText);
        lightTextView = findViewById(R.id.lightText);
        advancedMenuButton = findViewById(R.id.advancedMenuButton);
        advancedMenuButton.setOnClickListener(v -> {
            startActivity(new Intent(
                    this,
                    AdvancedMenuActivity.class)
            );
        });

        BottomNavigationView bottomNavigation = (BottomNavigationView) findViewById(R.id.bottom_navigation);
        bottomNavigation.setSelectedItemId(R.id.tab_charts);
        bottomNavigation.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();
                switch (id) {
                    case R.id.tab_bluetooth:
                        startActivity(
                                new Intent(DetailedSensorsListActivity.this, BluetoothOptionsActivity.class)
                        );
                        overridePendingTransition(0, 0);

                        return true;

                    case R.id.tab_adls:
                        startActivity(
                                new Intent(DetailedSensorsListActivity.this, AdlSummaryActivity.class)
                        );
                        overridePendingTransition(0, 0);

                        return true;

                    case R.id.tab_locations:
                        startActivity(
                                new Intent(DetailedSensorsListActivity.this, LocationOptionsActivity.class)
                        );
                        overridePendingTransition(0, 0);
                        return true;

                    case R.id.tab_home:

                        startActivity(
                                new Intent(DetailedSensorsListActivity.this, MainActivity.class)
                        );
                        overridePendingTransition(0, 0);
                        return true;
                }

                return true;
            }
        });
    }


    private void initializeSensors() {
        sensorsProvider = new SensorsProvider(this);

        BroadcastReceiver sensorReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Bundle b = intent.getBundleExtra(SensorableConstants.EXTRA_MESSAGE);
                ArrayList<SensorTransmissionCoder.SensorMessage> arrayMessage = b.getParcelableArrayList(SensorableConstants.BROADCAST_MESSAGE);

                try {
                    arrayMessage.forEach(sensorMessage -> {
                        switch (sensorMessage.getDeviceType()) {
                            case DeviceType.MOBILE:
                            case DeviceType.WEAROS:
                            case DeviceType.EMPATICA:

                                switch (sensorMessage.getSensorType()) {

                                    case Sensor.TYPE_AMBIENT_TEMPERATURE:
                                        temperatureTextView.setText(sensorMessage.getValue()[0] + " ºC");
                                        break;

                                    case Sensor.TYPE_PROXIMITY:
                                        proximityTextView.setText(
                                                sensorMessage.getValue()[0] == 0 ? "Cercanía detectada" : "Lejos del teléfono"
                                        );

                                        break;
                                    case Sensor.TYPE_LIGHT:
                                        lightTextView.setText(sensorMessage.getValue()[0] + " lm");

                                        break;

                                    case Sensor.TYPE_LINEAR_ACCELERATION:
                                        accelerometerTextView.setText(
                                                sensorMessage.getValue()[0] + ", " + sensorMessage.getValue()[1] + ", " + sensorMessage.getValue()[2]
                                        );

                                        break;

                                    case Sensor.TYPE_RELATIVE_HUMIDITY:
                                        humidityTextView.setText(String.valueOf(sensorMessage.getValue()[0]));
                                        break;
                                }

                                break;
                        }
                    });
                } catch (NullPointerException e) {
                    Log.e("DETAILED_SENSOR_LIST", "error receiving null sensor array");
                }
            }
        };

        LocalBroadcastManager.getInstance(this).registerReceiver(
                sensorReceiver,
                new IntentFilter(SensorableConstants.SENSORS_PROVIDER_SENDS_SENSORS)
        );

        LocalBroadcastManager.getInstance(this).registerReceiver(
                sensorReceiver,
                new IntentFilter(SensorableConstants.WEAR_SENDS_SENSOR_DATA)
        );

        LocalBroadcastManager.getInstance(this).registerReceiver(
                sensorReceiver,
                new IntentFilter(SensorableConstants.EMPATICA_SENDS_SENSOR_DATA)
        );
    }
}