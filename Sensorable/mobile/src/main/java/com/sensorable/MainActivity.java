package com.sensorable;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.NetworkInfo;
import android.net.wifi.WpsInfo;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pGroup;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.commons.BluetoothDevicesProvider;
import com.example.commons.DeviceType;
import com.example.commons.SensorTransmissionCoder;
import com.example.commons.SensorsProvider;
import com.example.commons.devicesDetection.WifiDirectDevicesProvider;
import com.google.android.gms.wearable.MessageClient;
import com.google.android.gms.wearable.MessageEvent;

import java.util.ArrayList;
import java.util.List;
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
            Manifest.permission.BLUETOOTH_CONNECT,
            Manifest.permission.ACCESS_WIFI_STATE,
            Manifest.permission.CHANGE_WIFI_STATE
    };

    private final static int REQUEST_PERMISSIONS_CODE = 1;
    private final static int SELECT_DEVICE_REQUEST_CODE = 0;
    private final static int REQUEST_ENABLE_BT = 2;

    private void requestPermissionsAndInform() {
        requestPermissionsAndInform(true);
    }

    private void requestPermissionsAndInform(Boolean inform) {
        this.requestPermissions(SENSOR_PERMISSIONS, REQUEST_PERMISSIONS_CODE);
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
    private AdlDetectionService adlDetectionService;

    private BroadcastReceiver wearOsReceiver;
    private BroadcastReceiver empaticaReceiver;
    private BroadcastReceiver infoReceiver;
    private BroadcastReceiver wifiDirectReceiver;

    private BluetoothDevicesProvider bluetoothProvider;
    private WifiDirectDevicesProvider wifiDirectProvider;


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

        initializeWifiDirectDetector();



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

    private final IntentFilter wifiDirectIntentFilter = new IntentFilter();
    private WifiP2pManager.Channel channel;
    private WifiP2pManager wifiDirectManager;
    private List<WifiP2pDevice> peers = new ArrayList<WifiP2pDevice>();
    private WifiP2pManager.PeerListListener peerListListener;
    private WifiP2pManager.ConnectionInfoListener connectionListener;

    private void initializeWifiDirectDetector() {
        wifiDirectProvider = new WifiDirectDevicesProvider(this);
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();

    }


    private void initializeBluetoothDetection() {
        bluetoothProvider = new BluetoothDevicesProvider(this);
        if (!bluetoothProvider.isEnabled()) {
            bluetoothProvider.turnOnBluetooth();
        } else {
            Toast.makeText(this, "BLUETOOTH IS ENABLED", Toast.LENGTH_SHORT).show();
        }

        bluetoothProvider.startScan();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        switch (requestCode) {
            case BluetoothDevicesProvider.SELECT_DEVICE_REQUEST_CODE:
                Log.i("BLUETOOTH_PROVIDER", "on activity result for companion found device");
                bluetoothProvider.onActivityResultCompanionFoundDevice(requestCode, resultCode, data);
                break;

            case BluetoothDevicesProvider.REQUEST_ENABLE_BT:
                Log.i("BLUETOOTH_PROVIDER", "on activity result for turn on bluetooth");
                bluetoothProvider.onActivityResultTurnOnBluetooth(requestCode, resultCode, data);
                break;
            default:
                Log.i("ON_ACTIVITY_RESULT DEFAULT", "on activity result for companion found device");

                super.onActivityResult(requestCode, resultCode, data);
                break;
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