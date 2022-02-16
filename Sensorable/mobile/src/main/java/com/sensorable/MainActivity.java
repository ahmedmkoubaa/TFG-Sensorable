package com.sensorable;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;
import android.os.Message;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.commons.SensorsProvider;
import com.google.android.gms.wearable.DataClient;
import com.google.android.gms.wearable.MessageClient;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Wearable;
import com.google.android.gms.wearable.WearableListenerService;

import java.util.Random;


public class MainActivity extends AppCompatActivity implements MessageClient.OnMessageReceivedListener {
    private static final String[] SENSOR_PERMISSIONS = {
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.INTERNET,
            Manifest.permission.BODY_SENSORS,
            Manifest.permission.ACTIVITY_RECOGNITION
    };

    private final int LOCATION_REQ_CODE = 1;

    private  LocationManager locationManager = null;

    // Attributes to use sensors from sensor manager
    private SensorManager sensorManager;

    private Sensor sensorAccelerometer;
    private Sensor sensorAmbientTemperature;
    private Sensor sensorHumidity;
    private Sensor sensorProximity;
    private Sensor sensorLight;


    private Button userStateSummary;
    private ProgressBar useStateProgressBar;
    private TextView userStateMessage;
    private TextView hearRateText, stepCounterText;

    private SensorsProvider sensorsProvider;

    private Button moreSensorsButton;
    private DataTransmissionService service;



    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initializeAttributesFromUI();

//        Looper myLooper;
//        Wearable.WearableOptions options = new Wearable.WearableOptions.Builder().setLooper(myLooper).build();
        DataClient dataClient = Wearable.getDataClient(this);
        service = new DataTransmissionService();
        startService(new Intent(this, DataTransmissionService.class));



        userStateSummary.setClickable(false);
        userStateSummary.setText("EN BUEN ESTADO");

        useStateProgressBar.setMin(0);
        useStateProgressBar.setMax(100);
        useStateProgressBar.setProgress((new Random()).nextInt(100));


        userStateMessage.setText(
                "Te encuentras bien, sigue así. Recuerda hacer ejercicio y tomarte la medicación cuando toque"
        );

        // Summary, progressBar and message will be set using a system valoration
        // this system valoration will be developed in the near future


//        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
//        setActionBar(myToolbar);

//        initializeProviderLocation();
//        initializeSensorAccelerometer();
//        initializeSensorAmbientTemperature();
//        initializeSensorHumidity();
//        initializeSensorProximity();
//        initilizeSensorLight();
//        initializeSensorHeartRate();
//        initializeSensorStepCounter();
        sensorsProvider = new SensorsProvider(this);
    }

    private void initializeAttributesFromUI() {
        userStateSummary = (Button) findViewById(R.id.userStateSummary);
        useStateProgressBar = (ProgressBar) findViewById(R.id.userStateProgressBar);
        userStateMessage = (TextView) findViewById(R.id.userStateMessage);

        hearRateText = (TextView) findViewById(R.id.hearRateText);
        stepCounterText = (TextView) findViewById(R.id.stepCounterText);

        moreSensorsButton = (Button) findViewById(R.id.moreSensorsButton);
        moreSensorsButton = (Button) findViewById(R.id.moreSensorsButton);
        moreSensorsButton.setOnClickListener(v -> {
            Intent intent = new Intent(
                    this, DetailedSensorsList.class);;
            startActivity(intent);
        });
    }


    @Override
    protected void onStart() {
        super.onStart();

//        subscribeToGps();
//        subscribeToSensorAccelerometer();
//        subscribeToSensorAmbientTemperature();
//        subscribeToSensorHumidity();
//        subscribeToSensorProximity();
//        subscribeToSensorLight();
//        subscribeToSensorHeartRate();

       initializeSensors();
    }

    private void initializeSensors() {
        SensorEventListener heartRateListener =  new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent sensorEvent) {
                hearRateText.setText((int) sensorEvent.values[0] + " ppm");
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int i) {
                Toast.makeText(
                        MainActivity.this,
                        i <= 0 ? "Sensor not available" : ("Accuracy value is: " + i) ,
                        Toast.LENGTH_SHORT
                ).show();
            }
        };
        sensorsProvider.subscribeToSensor(Sensor.TYPE_HEART_RATE, heartRateListener, SensorManager.SENSOR_DELAY_NORMAL);

        SensorEventListener stepCounterListener = new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent sensorEvent) {
                stepCounterText.setText((int) sensorEvent.values[0] + " pasos");
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int i) {
                if (i <= 0) {
                    Toast.makeText(
                            MainActivity.this,
                            "Step counter Sensor not available" ,
                            Toast.LENGTH_SHORT
                    ).show();
                }

            }
        };
        sensorsProvider.subscribeToSensor(Sensor.TYPE_STEP_COUNTER, stepCounterListener, SensorManager.SENSOR_DELAY_NORMAL);
    }


    @Override
    public void onMessageReceived(@NonNull MessageEvent messageEvent) {
        Toast.makeText(this, "RECIBIDO", Toast.LENGTH_LONG).show();
    }
}