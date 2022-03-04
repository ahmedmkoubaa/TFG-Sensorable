package com.sensorable;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.companion.AssociationRequest;
import android.companion.BluetoothDeviceFilter;
import android.companion.CompanionDeviceManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.commons.DeviceType;
import com.example.commons.SensorTransmissionCoder;
import com.example.commons.SensorsProvider;
import com.google.android.gms.wearable.MessageClient;
import com.google.android.gms.wearable.MessageEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;


public class MainActivity extends AppCompatActivity implements MessageClient.OnMessageReceivedListener {
    private static final String[] SENSOR_PERMISSIONS = {
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.INTERNET,
            Manifest.permission.BODY_SENSORS,
            Manifest.permission.ACTIVITY_RECOGNITION,
            Manifest.permission.BLUETOOTH,
            Manifest.permission.BLUETOOTH_ADMIN,
            Manifest.permission.BLUETOOTH_CONNECT
    };
    private final static int LOCATION_REQ_CODE = 1;
    private final static int SELECT_DEVICE_REQUEST_CODE = 0;

    private void requestPermissionsAndInform() {
        requestPermissionsAndInform(true);
    }
    private void requestPermissionsAndInform(Boolean inform) {
        this.requestPermissions(SENSOR_PERMISSIONS, LOCATION_REQ_CODE);
        if (inform) {
            Toast.makeText(this, "Permisos solicitados y aparentemente concedidos", Toast.LENGTH_SHORT).show();
        }
    }

    private final static int MAX_SENSOR_BUFFER_SIZE = 512;
    private ArrayList<SensorTransmissionCoder.SensorMessage> sensorMessagesBuffer;

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

    private Map<Long, ArrayList<SensorTransmissionCoder.SensorMessage>> collectedData =
            new HashMap<Long, ArrayList<SensorTransmissionCoder.SensorMessage>>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        requestPermissionsAndInform(false);

        initializeAttributesFromUI();
        sensorMessagesBuffer = new ArrayList<>();

//        initializeWearOsTranmissionService();
//        initializeEmpaticaTransmissionService();
        initializeAdlDetectionService();
        initializeBluetoothDetection();

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

    private void initializeBluetoothDetection() {
        CompanionDeviceManager deviceManager =
                (CompanionDeviceManager) getSystemService(
                        Context.COMPANION_DEVICE_SERVICE
                );


        BluetoothDeviceFilter deviceFilter =
                new BluetoothDeviceFilter.Builder().build();

        AssociationRequest pairingRequest = new AssociationRequest.Builder()
                .addDeviceFilter(deviceFilter)
                .setSingleDevice(false)
                .build();

        deviceManager.associate(pairingRequest,
                new CompanionDeviceManager.Callback() {
                    @Override
                    public void onDeviceFound(IntentSender chooserLauncher) {
                        Log.i("BLUETOOTH_DETECTOR", "found a device");
                        try {
                            startIntentSenderForResult(chooserLauncher,
                                    SELECT_DEVICE_REQUEST_CODE, null, 0, 0, 0);
                        } catch (IntentSender.SendIntentException e) {
                            // failed to send the intent
                        }
                    }

                    @Override
                    public void onFailure(CharSequence error) {
                        // handle failure to find the companion device
                        Log.i("BLUETOOTH_DETECTOR", "error trying to found a device");
                    }
                }, null);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == SELECT_DEVICE_REQUEST_CODE) {
            Toast.makeText(this, "Found a device", Toast.LENGTH_SHORT).show();
            if (resultCode == Activity.RESULT_OK && data != null) {
                Toast.makeText(this, "It went all well", Toast.LENGTH_SHORT).show();

                BluetoothDevice deviceToPair = data.getParcelableExtra(
                        CompanionDeviceManager.EXTRA_DEVICE
                );

                if (deviceToPair != null) {
                    Toast.makeText(this, "We can do a bond", Toast.LENGTH_SHORT).show();

                    deviceToPair.createBond();
                    // ... Continue interacting with the paired device.
                }
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void sendSensorDataToAdlDetectionService(ArrayList<SensorTransmissionCoder.SensorMessage> arrayMessage) {
        sensorMessagesBuffer.addAll(arrayMessage);
        sendSensorDataToAdlDetectionService();
    }
    private void sendSensorDataToAdlDetectionService(SensorTransmissionCoder.SensorMessage msg) {
        sensorMessagesBuffer.add(msg);
        sendSensorDataToAdlDetectionService();

    }

    private void sendSensorDataToAdlDetectionService() {
        if (sensorMessagesBuffer.size() >= MAX_SENSOR_BUFFER_SIZE) {

            Intent intent = new Intent("MOBILE_SENDS_SENSOR_DATA");
            // You can also include some extra data.

            Bundle empaticaBundle = new Bundle();
            empaticaBundle.putParcelableArrayList("MobileMessage", new ArrayList<>(sensorMessagesBuffer));

            intent.putExtra("MOBILE_DATA_COLLECTED", empaticaBundle);
            LocalBroadcastManager.getInstance(this).sendBroadcast(intent);

            // reset buffer
            sensorMessagesBuffer.clear();
        }
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
                ArrayList<SensorTransmissionCoder.SensorMessage> arrayMessage = b.getParcelableArrayList("EmpaticaMessage");
                sendSensorDataToAdlDetectionService(arrayMessage);
//                Toast.makeText(context, "He recibido " + arrayMessage.size() + " elementos ", Toast.LENGTH_LONG).show();

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

        sensorsProvider.subscribeToSensor(Sensor.TYPE_HEART_RATE, new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent sensorEvent) {
                hearRateText.setText((int) sensorEvent.values[0] + " ppm");
                SensorTransmissionCoder.SensorMessage msg =
                        new SensorTransmissionCoder.SensorMessage(
                                DeviceType.MOBILE,
                                Sensor.TYPE_HEART_RATE,
                                sensorEvent.values
                        );

                sendSensorDataToAdlDetectionService(msg);
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int i) {
        /*        Toast.makeText(
                        MainActivity.this,
                        i <= 0 ? "Sensor not available" : ("Accuracy value is: " + i) ,
                        Toast.LENGTH_SHORT
                ).show();*/
            }
        }, SensorManager.SENSOR_DELAY_NORMAL);


        sensorsProvider.subscribeToSensor(Sensor.TYPE_STEP_COUNTER, new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent sensorEvent) {
                stepCounterText.setText((int) sensorEvent.values[0] + " pasos");

                SensorTransmissionCoder.SensorMessage msg =
                        new SensorTransmissionCoder.SensorMessage(
                                DeviceType.MOBILE,
                                Sensor.TYPE_STEP_COUNTER,
                                sensorEvent.values
                        );

                sendSensorDataToAdlDetectionService(msg);
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int i) {
            }
        }, SensorManager.SENSOR_DELAY_NORMAL);


        SensorEventListener transmissionListener = new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent sensorEvent) {
                SensorTransmissionCoder.SensorMessage msg =
                        new SensorTransmissionCoder.SensorMessage(
                                DeviceType.MOBILE,
                                sensorEvent.sensor.getType(),
                                sensorEvent.values
                        );

                sendSensorDataToAdlDetectionService(msg);
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int i) {

            }
        };

        sensorsProvider.subscribeToSensor(Sensor.TYPE_PROXIMITY,  new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent sensorEvent) {
                SensorTransmissionCoder.SensorMessage msg =
                        new SensorTransmissionCoder.SensorMessage(
                                DeviceType.MOBILE,
                                sensorEvent.sensor.getType(),
                                sensorEvent.values
                        );

                sendSensorDataToAdlDetectionService(msg);
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int i) {

            }
        }, SensorManager.SENSOR_DELAY_NORMAL);
        sensorsProvider.subscribeToSensor(Sensor.TYPE_LIGHT, transmissionListener , SensorManager.SENSOR_DELAY_NORMAL);
        sensorsProvider.subscribeToSensor(Sensor.TYPE_ACCELEROMETER, transmissionListener , SensorManager.SENSOR_DELAY_NORMAL);
    }


    @Override
    public void onMessageReceived(@NonNull MessageEvent messageEvent) {
        Toast.makeText(this, "RECIBIDO", Toast.LENGTH_LONG).show();
    }

}