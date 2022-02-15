package com.testapp3;

import android.Manifest;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.wearable.activity.WearableActivity;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends WearableActivity {

    private TextView heartText;
    private SensorManager sensorManager;
//    private SensorsProvider sensorsProvider;

    private static final String[] SENSOR_PERMISSIONS = {
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.INTERNET,
            Manifest.permission.BODY_SENSORS,
            Manifest.permission.ACTIVITY_RECOGNITION
    };

    private final int LOCATION_REQ_CODE = 1;

    private void requestPermissionsAndInform() {
        requestPermissions(SENSOR_PERMISSIONS, LOCATION_REQ_CODE);
        Toast.makeText(this, "Permisos solicitados y aparentemente concedidos", Toast.LENGTH_SHORT).show();
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        heartText = (TextView) findViewById(R.id.heartText);
//        sensorsProvider = new SensorsProvider(this);
    }

    @Override
    protected void onStart() {
        super.onStart();

//        sensorsProvider.subscribeToSensor(Sensor.TYPE_HEART_RATE, new SensorEventListener() {
//            @Override
//            public void onSensorChanged(SensorEvent sensorEvent) {
//                String values = " " + sensorEvent.values[0] + " ppm";
//                heartText.setText(values);
//            }
//
//            @Override
//            public void onAccuracyChanged(Sensor sensor, int i) {
//                Toast.makeText(
//                        MainActivity.this,
//                        i <= 0 ? "Sensor not available" : ("Accuracy value is: " + i) ,
//                        Toast.LENGTH_SHORT
//                ).show();
//            }
//        }, SensorManager.SENSOR_DELAY_FASTEST);
    }
}