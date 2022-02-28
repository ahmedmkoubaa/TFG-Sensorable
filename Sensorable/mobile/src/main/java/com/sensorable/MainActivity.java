package com.sensorable;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.IBinder;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.empatica.empalink.ConnectionNotAllowedException;
import com.empatica.empalink.EmpaDeviceManager;
import com.empatica.empalink.EmpaticaDevice;
import com.empatica.empalink.config.EmpaSensorType;
import com.empatica.empalink.config.EmpaStatus;
import com.empatica.empalink.delegate.EmpaDataDelegate;
import com.empatica.empalink.delegate.EmpaStatusDelegate;
import com.example.commons.DeviceType;
import com.example.commons.EmpaticaSensorType;
import com.example.commons.SensorTransmissionCoder;
import com.example.commons.SensorsProvider;
import com.google.android.gms.wearable.MessageClient;
import com.google.android.gms.wearable.MessageEvent;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class MainActivity extends AppCompatActivity implements MessageClient.OnMessageReceivedListener {
    private static final String[] SENSOR_PERMISSIONS = {
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.INTERNET,
            Manifest.permission.BODY_SENSORS,
            Manifest.permission.ACTIVITY_RECOGNITION,
            Manifest.permission.BLUETOOTH,
            Manifest.permission.BLUETOOTH_ADMIN
    };
    private final static int LOCATION_REQ_CODE = 1;

    private void requestPermissionsAndInform() {
        requestPermissionsAndInform(true);
    }
    private void requestPermissionsAndInform(Boolean inform) {
        this.requestPermissions(SENSOR_PERMISSIONS, LOCATION_REQ_CODE);
        if (inform) {
            Toast.makeText(this, "Permisos solicitados y aparentemente concedidos", Toast.LENGTH_SHORT).show();
        }
    }

    private Button userStateSummary;
    private ProgressBar useStateProgressBar;
    private TextView userStateMessage;
    private TextView hearRateText, stepCounterText;

    private SensorsProvider sensorsProvider;
    private Button moreSensorsButton;

    private WearTransmissionService wearOsService;
    private EmpaticaTransmissionService empaticaService;
    private BroadcastReceiver wearOsReceiver;
    private BroadcastReceiver empaticaReceiver;
    private BroadcastReceiver infoReceiver;
    private AdlDetectionService adlDetectionService;

    private EmpaDeviceManager deviceManager;
    private static final String EMPATICA_API_KEY = "e910f7a73ce74dbd99b774b9f6010ab5";
    private ExecutorService executorService = Executors.newFixedThreadPool(1);

    private Map<Long, ArrayList<SensorTransmissionCoder.SensorMessage>> collectedData =
            new HashMap<Long, ArrayList<SensorTransmissionCoder.SensorMessage>>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        requestPermissionsAndInform(true);

        initializeAttributesFromUI();
//        initializeWearOsTranmissionService();
        initializeEmpaticaTransmissionService();
//        initializeAdlDetectionService();

        initializeInfoReceiver();


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


        sensorsProvider = new SensorsProvider(this);


    }


    private void sendMessageToActivity(String msg) {
        Intent intent = new Intent("MobileUpdates");
        // You can also include some extra data.

        Bundle empaticaBundle = new Bundle();
        empaticaBundle.putString("MobileMessage", msg);

        intent.putExtra("MOBILE_DATA_COLLECTED", empaticaBundle);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    private void initializeAdlDetectionService() {
        adlDetectionService = new AdlDetectionService();
        startService(new Intent(this, AdlDetectionService.class));

        BroadcastReceiver exampleReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Toast.makeText(context, "mobile: received" +
                        intent.getBundleExtra("ADL_DATA_COLLECTED")
                                .getString("AdlMessage"),
                        Toast.LENGTH_SHORT).show();

                sendMessageToActivity("Hi I'm mobile");
            }
        };

        LocalBroadcastManager.getInstance(MainActivity.this).registerReceiver(
                exampleReceiver, new IntentFilter("AdlUpdates"));
    }



    private void initializeEmpaticaTransmissionService() {

        empaticaService = new EmpaticaTransmissionService();
        startService(new Intent(this, EmpaticaTransmissionService.class));

        // handle messages from our service to this activity
        empaticaReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Bundle b = intent.getBundleExtra("EMPATICA_DATA_COLLECTED");
                SensorTransmissionCoder.SensorMessage message = b.getParcelable("EmpaticaMessage");
                String s = "Recibido " + message.toString();


//                hearRateText.setText(" " + message.getTimestamp());

                Toast.makeText(context, s, Toast.LENGTH_SHORT).show();
            }
        };

        LocalBroadcastManager.getInstance(MainActivity.this).registerReceiver(
                empaticaReceiver, new IntentFilter("EmpaticaDataUpdates"));
    }


    private void initializeInfoReceiver() {

        // handle messages from our service to this activity
        infoReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

                String msg = intent.getStringExtra("msg");
                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
            }
        };

        LocalBroadcastManager.getInstance(MainActivity.this).registerReceiver(
                infoReceiver, new IntentFilter("INFO"));
    }

    private void initializeWearOsTranmissionService() {
        // start new data transmission service to collect data from wear os
        wearOsService = new WearTransmissionService();
        startService(new Intent(this, WearTransmissionService.class));

        // handle messages from our service to this activity
        wearOsReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Bundle b = intent.getBundleExtra("WEAR_DATA_COLLECTED");
                SensorTransmissionCoder.SensorMessage message = b.getParcelable("SensorMessage");
                String value;

                switch (message.getSensorType()) {
                    case Sensor.TYPE_HEART_RATE:
                        value = message.getValue()[0] + " ppm";
                        hearRateText.setText(value);
                        break;

                    case Sensor.TYPE_STEP_COUNTER:
                        value = message.getValue()[0] + "steps";
                        stepCounterText.setText(value);
                        break;

                    default:
                        Toast.makeText(context, "Sensor recibido no reconocido", Toast.LENGTH_SHORT).show();
                        break;
                }
            }
        };

        LocalBroadcastManager.getInstance(MainActivity.this).registerReceiver(
                wearOsReceiver, new IntentFilter("SensorDataUpdates"));
    }

    private void initializeAttributesFromUI() {
        userStateSummary = (Button) findViewById(R.id.userStateSummary);
        useStateProgressBar = (ProgressBar) findViewById(R.id.userStateProgressBar);
        userStateMessage = (TextView) findViewById(R.id.text);

        hearRateText = (TextView) findViewById(R.id.hearRateText);
        stepCounterText = (TextView) findViewById(R.id.stepCounterText);

        moreSensorsButton = (Button) findViewById(R.id.moreSensorsButton);
        moreSensorsButton = (Button) findViewById(R.id.moreSensorsButton);
        moreSensorsButton.setOnClickListener(v -> {
            Intent intent = new Intent(
                    this,
                    DetailedSensorsList.class);
            startActivity(intent);
        });
    }


    @Override
    protected void onStart() {
        super.onStart();

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