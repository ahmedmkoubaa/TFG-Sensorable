package com.sensorable.activities;

import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.commons.SensorsProvider;
import com.sensorable.R;

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
        sensorsProvider = new SensorsProvider(this);
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
    }

    @Override
    protected void onStart() {
        super.onStart();
        initializeSensors();
    }

    private void initializeSensors() {


        sensorsProvider.subscribeToSensor(Sensor.TYPE_AMBIENT_TEMPERATURE, new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent sensorEvent) {
                temperatureTextView.setText(sensorEvent.values[0] + " ºC");
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int i) {
            }
        }, SensorManager.SENSOR_DELAY_NORMAL);

        sensorsProvider.subscribeToSensor(Sensor.TYPE_RELATIVE_HUMIDITY, new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent sensorEvent) {
                String values = sensorEvent.values[0] + " %";
                humidityTextView.setText(values);
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int i) {

            }
        }, SensorManager.SENSOR_DELAY_NORMAL);

        sensorsProvider.subscribeToSensor(Sensor.TYPE_PROXIMITY, new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent sensorEvent) {
                String values = sensorEvent.values[0] + " cm";
                proximityTextView.setText(values);
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int i) {

            }
        }, SensorManager.SENSOR_DELAY_NORMAL);

        sensorsProvider.subscribeToSensor(Sensor.TYPE_LIGHT, new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent sensorEvent) {
                String values = sensorEvent.values[0] + " lm";
                lightTextView.setText(values);
                Log.i("SENSORS", "LIGHT CHANGED " + sensorEvent.values[0]);
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int i) {

            }
        }, SensorManager.SENSOR_DELAY_NORMAL);

        Toast.makeText(this, "Sensor inicializados correctamente", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
    }
}