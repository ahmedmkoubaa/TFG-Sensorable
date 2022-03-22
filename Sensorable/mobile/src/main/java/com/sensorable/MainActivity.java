package com.sensorable;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.commons.DeviceType;
import com.commons.SensorTransmissionCoder;
import com.commons.SensorableConstants;
import com.commons.SensorablePermissions;
import com.commons.SensorsProvider;
import com.commons.database.SensorMessageDao;
import com.commons.database.SensorMessageEntity;
import com.commons.devicesDetection.BluetoothDevicesProvider;
import com.example.commons.devicesDetection.WifiDirectDevicesProvider;
import com.google.android.gms.wearable.MessageClient;
import com.google.android.gms.wearable.MessageEvent;
import com.sensorable.activities.DetailedSensorsListActivity;
import com.sensorable.services.AdlDetectionService;
import com.sensorable.services.BluetoothDetectionService;
import com.sensorable.services.EmpaticaTransmissionService;
import com.sensorable.services.WearTransmissionService;
import com.sensorable.utils.MobileDatabaseBuilder;

import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.ExecutorService;


public class MainActivity extends AppCompatActivity implements MessageClient.OnMessageReceivedListener {

    private ArrayList<SensorTransmissionCoder.SensorMessage> sensorDataBuffer;
    private Button userStateSummary;
    private ProgressBar useStateProgressBar;
    private TextView userStateMessage;
    private TextView hearRateText, stepCounterText;
    private SensorsProvider sensorsProvider;
    private Button moreSensorsButton;

    private WifiDirectDevicesProvider wifiDirectProvider;

    private SensorMessageDao sensorMessageDao;
    private ExecutorService executor;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SensorablePermissions.requestAll(this);

        initializeAttributesFromUI();
        initializeMobileDatabase();


//        initializeWearOsTranmissionService();
//        initializeEmpaticaTransmissionService();
        initializeAdlDetectionService();
//        initializeBluetoothDetection();
        initializeBluetoothDetectionService();
        initializeInfoReceiver();


        initializeWifiDirectDetector();


        // TODO remove this progress bar statements
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

    private void initializeMobileDatabase() {
        sensorMessageDao = MobileDatabaseBuilder.getDatabase(this).sensorMessageDao();
        executor = MobileDatabaseBuilder.getExecutor();
    }

    private void initializeWifiDirectDetector() {
        wifiDirectProvider = new WifiDirectDevicesProvider(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private void initializeBluetoothDetectionService() {
        if (!BluetoothDevicesProvider.isEnabledBluetooth()) {
            BluetoothDevicesProvider.enableBluetooth(this);
        } else {
            startService(new Intent(this, BluetoothDetectionService.class));
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        switch (requestCode) {
            case SensorableConstants.REQUEST_ENABLE_BT:
                Log.i("BLUETOOTH_PROVIDER", "on activity result for turn on bluetooth");
                if (resultCode == Activity.RESULT_OK) {
                    startService(new Intent(this, BluetoothDetectionService.class));
                    Toast.makeText(this, "Bluetooth was turned on", Toast.LENGTH_SHORT).show();
                }
                break;
            default:
                Log.i("ON_ACTIVITY_RESULT DEFAULT", "on activity result for companion found device");

                super.onActivityResult(requestCode, resultCode, data);
                break;
        }
    }

    private void collectReceivedSensorData(ArrayList<SensorTransmissionCoder.SensorMessage> arrayMessage) {
        executor.execute(() -> {
            ArrayList<SensorMessageEntity> sensorMessageEntities = new ArrayList<>();
            for (SensorTransmissionCoder.SensorMessage s : arrayMessage) {
                sensorMessageEntities.add(s.toSensorDataMessage());
            }
            sensorMessageDao.insertAll(sensorMessageEntities);
        });

        sensorDataBuffer.addAll(arrayMessage);
        sendSensorDataToAdlDetectionService();
    }

    private void collectReceivedSensorData(SensorTransmissionCoder.SensorMessage msg) {
        executor.execute(() -> {
            sensorMessageDao.insert(msg.toSensorDataMessage());
        });

        sensorDataBuffer.add(msg);
        sendSensorDataToAdlDetectionService();
    }

    private void sendSensorDataToAdlDetectionService() {
        if (sensorDataBuffer.size() >= SensorableConstants.COLLECTED_SENSOR_DATA_SIZE) {
            Intent intent = new Intent(SensorableConstants.MOBILE_SENDS_SENSOR_DATA);
            // You can also include some extra data.

            Bundle adlBundle = new Bundle();
            adlBundle.putParcelableArrayList(SensorableConstants.BROADCAST_MESSAGE, new ArrayList<>(sensorDataBuffer));

            intent.putExtra(SensorableConstants.EXTRA_MESSAGE, adlBundle);
            LocalBroadcastManager.getInstance(this).sendBroadcast(intent);

            // reset buffer
            sensorDataBuffer.clear();
        }
    }

    private void initializeAdlDetectionService() {
        sensorDataBuffer = new ArrayList<>();

        startService(new Intent(this, AdlDetectionService.class));

        LocalBroadcastManager.getInstance(MainActivity.this).registerReceiver(
                new BroadcastReceiver() {
                    @Override
                    public void onReceive(Context context, Intent intent) {
                        Toast.makeText(context, "mobile: received" +
                                        intent.getBundleExtra(SensorableConstants.EXTRA_MESSAGE)
                                                .getString(SensorableConstants.BROADCAST_MESSAGE),
                                Toast.LENGTH_SHORT).show();
                    }
                }, new IntentFilter(SensorableConstants.ADL_UPDATE));
    }

    private void initializeEmpaticaTransmissionService() {
        startService(new Intent(this, EmpaticaTransmissionService.class));

        // handle messages from our service to this activity
        LocalBroadcastManager.getInstance(MainActivity.this).registerReceiver(
                new BroadcastReceiver() {
                    @Override
                    public void onReceive(Context context, Intent intent) {
                        Bundle b = intent.getBundleExtra(SensorableConstants.EXTRA_MESSAGE);
                        ArrayList<SensorTransmissionCoder.SensorMessage> arrayMessage = b.getParcelableArrayList(SensorableConstants.BROADCAST_MESSAGE);
                        collectReceivedSensorData(arrayMessage);
                    }
                }, new IntentFilter(SensorableConstants.EMPATICA_SENDS_SENSOR_DATA));
    }

    private void initializeInfoReceiver() {
        // handle messages from our service to this activity
        LocalBroadcastManager.getInstance(MainActivity.this).registerReceiver(
                new BroadcastReceiver() {
                    @Override
                    public void onReceive(Context context, Intent intent) {

                        String msg = intent.getStringExtra(SensorableConstants.EXTRA_MESSAGE);
                        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
                    }
                }, new IntentFilter(SensorableConstants.SERVICE_SENDS_INFO));
    }

    private void initializeWearOsTranmissionService() {
        // start new data transmission service to collect data from wear os
        startService(new Intent(this, WearTransmissionService.class));

        // handle messages from our service to this activity
        LocalBroadcastManager.getInstance(MainActivity.this).registerReceiver(
                new BroadcastReceiver() {
                    @Override
                    public void onReceive(Context context, Intent intent) {
                        Bundle b = intent.getBundleExtra(SensorableConstants.EXTRA_MESSAGE);
                        ArrayList<SensorTransmissionCoder.SensorMessage> arrayMessage = b.getParcelableArrayList(SensorableConstants.BROADCAST_MESSAGE);
                        collectReceivedSensorData(arrayMessage);
                    }
                },
                new IntentFilter(SensorableConstants.WEAR_SENDS_SENSOR_DATA));
    }

    private void initializeAttributesFromUI() {
        userStateSummary = findViewById(R.id.userStateSummary);
        useStateProgressBar = findViewById(R.id.userStateProgressBar);
        userStateMessage = findViewById(R.id.text);

        hearRateText = findViewById(R.id.hearRateText);
        stepCounterText = findViewById(R.id.stepCounterText);

        moreSensorsButton = findViewById(R.id.moreSensorsButton);
        moreSensorsButton.setOnClickListener(v -> {
            Intent intent = new Intent(
                    this,
                    DetailedSensorsListActivity.class);
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

                collectReceivedSensorData(msg);
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int i) {
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

                collectReceivedSensorData(msg);
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

                collectReceivedSensorData(msg);
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int i) {

            }
        };

        sensorsProvider.subscribeToSensor(Sensor.TYPE_PROXIMITY, transmissionListener, SensorManager.SENSOR_DELAY_NORMAL);
        sensorsProvider.subscribeToSensor(Sensor.TYPE_LIGHT, transmissionListener, SensorManager.SENSOR_DELAY_NORMAL);
        sensorsProvider.subscribeToSensor(Sensor.TYPE_ACCELEROMETER, transmissionListener, SensorManager.SENSOR_DELAY_NORMAL);
    }

    // TODO test me and find my utility if I have any
    @Override
    public void onMessageReceived(@NonNull MessageEvent messageEvent) {
        Toast.makeText(this, "RECIBIDO", Toast.LENGTH_LONG).show();
    }
}