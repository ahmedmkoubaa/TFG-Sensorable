package com.sensorable;

import android.app.Activity;
import android.app.ActivityManager;
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
import com.commons.EmpaticaSensorType;
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
import com.sensorable.services.BackUpService;
import com.sensorable.services.BluetoothDetectionService;
import com.sensorable.services.EmpaticaTransmissionService;
import com.sensorable.services.SensorsProviderService;
import com.sensorable.services.WearTransmissionService;
import com.sensorable.utils.MobileDatabase;
import com.sensorable.utils.MobileDatabaseBuilder;
import com.sensorable.utils.MqttHelper;

import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.ExecutorService;


public class MainActivity extends AppCompatActivity implements MessageClient.OnMessageReceivedListener {

    private final ArrayList<SensorTransmissionCoder.SensorMessage> sensorDataBuffer = new ArrayList<>();
    private Button userStateSummary;
    private ProgressBar useStateProgressBar;
    private TextView userStateMessage;
    private TextView heartRateText, stepCounterText;
    private SensorsProvider sensorsProvider;
    private Button moreSensorsButton;

    private WifiDirectDevicesProvider wifiDirectProvider;

    private SensorMessageDao sensorMessageDao;
    private ExecutorService executor;
    private BroadcastReceiver sensorDataReceiver;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SensorablePermissions.requestAll(this);
        SensorablePermissions.ignoreBatteryOptimization(this);

        MqttHelper.connect();

        initializeAttributesFromUI();

        initializeMobileDatabase();
        initializeSensorDataReceiver();
        initializeInfoReceiver();

        initializeWearOsTranmissionService();
        initializeEmpaticaTransmissionService();

        initializeSensorsProviderService();
        initializeAdlDetectionService();
        initializeBackUpService();
        initializeBluetoothDetectionService();

//        initializeSensors();
//        initializeWifiDirectDetector();

    }


    @Override
    protected void onStart() {
        super.onStart();

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
    }


    private void initializeSensorDataReceiver() {
        sensorDataReceiver =
                new BroadcastReceiver() {
                    @Override
                    public void onReceive(Context context, Intent intent) {
                        Bundle b = intent.getBundleExtra(SensorableConstants.EXTRA_MESSAGE);
                        ArrayList<SensorTransmissionCoder.SensorMessage> arrayMessage = b.getParcelableArrayList(SensorableConstants.BROADCAST_MESSAGE);
                        collectReceivedSensorData(arrayMessage);

                        arrayMessage.forEach(sensorMessage -> {


                            switch (sensorMessage.getDeviceType()) {
                                case DeviceType.MOBILE:
                                case DeviceType.WEAROS:
                                    switch (sensorMessage.getSensorType()) {
                                        case Sensor.TYPE_STEP_COUNTER:
                                            stepCounterText.setText(Math.round(sensorMessage.getValue()[0]) + " pasos");
                                            break;

                                        case Sensor.TYPE_HEART_RATE:
                                            heartRateText.setText(Math.round(sensorMessage.getValue()[0]) + " ppm");
                                            break;

                                        case Sensor.TYPE_LINEAR_ACCELERATION:

//                                            ((TextView) (findViewById(R.id.acceleromterText))).setText(
//                                                    sensorMessage.getValue()[0] + ", " +
//                                                            sensorMessage.getValue()[1] + ", " +
//                                                            sensorMessage.getValue()[2]
//                                            );

                                            break;
                                    }

                                    break;


                                case DeviceType.EMPATICA:
                                    break;


                            }

                            if ((sensorMessage.getDeviceType() == DeviceType.EMPATICA && sensorMessage.getSensorType() == EmpaticaSensorType.IBI)) {
                                heartRateText.setText(Math.round(60 / sensorMessage.getValue()[0]) + " ppm");
                            }

                        });
                    }
                };
    }


    private void initializeMobileDatabase() {
        MobileDatabase database = MobileDatabaseBuilder.getDatabase(this);
        sensorMessageDao = database.sensorMessageDao();
        executor = MobileDatabaseBuilder.getExecutor();
    }

    private void initializeWifiDirectDetector() {
        wifiDirectProvider = new WifiDirectDevicesProvider(this);
    }

    private void initializeBluetoothDetectionService() {
        if (!BluetoothDevicesProvider.isEnabledBluetooth()) {
            BluetoothDevicesProvider.enableBluetooth(this);
        } else {
            if (!isMyServiceRunning(BluetoothDetectionService.class)) {
                startService(new Intent(this, BluetoothDetectionService.class));
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        switch (requestCode) {
            case SensorableConstants.REQUEST_ENABLE_BT:
                Log.i("BLUETOOTH_PROVIDER", "on activity result for turn on bluetooth");
                if (resultCode == Activity.RESULT_OK) {
                    if (!isMyServiceRunning(BluetoothDetectionService.class)) {
                        startService(new Intent(this, BluetoothDetectionService.class));
                    }
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
        // local database storing
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

    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    private void initializeAdlDetectionService() {
        if (!isMyServiceRunning(AdlDetectionService.class)) {
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

    }

    private void initializeBackUpService() {
        if (!isMyServiceRunning(BackUpService.class)) {
            startService(new Intent(this, BackUpService.class));
        }
    }

    private void initializeEmpaticaTransmissionService() {
        if (!isMyServiceRunning(EmpaticaTransmissionService.class)) {
            startService(new Intent(this, EmpaticaTransmissionService.class));
            // handle messages from our service to this activity
            LocalBroadcastManager.getInstance(MainActivity.this).registerReceiver(
                    sensorDataReceiver,
                    new IntentFilter(SensorableConstants.EMPATICA_SENDS_SENSOR_DATA));
        }
    }

    private void initializeWearOsTranmissionService() {
        if (!isMyServiceRunning(WearTransmissionService.class)) {
            // start new data transmission service to collect data from wear os
            startService(new Intent(this, WearTransmissionService.class));

            // handle messages from our service to this activity
            LocalBroadcastManager.getInstance(MainActivity.this).registerReceiver(
                    sensorDataReceiver,
                    new IntentFilter(SensorableConstants.WEAR_SENDS_SENSOR_DATA));
        }
    }

    private void initializeAttributesFromUI() {
        userStateSummary = findViewById(R.id.userStateSummary);
        useStateProgressBar = findViewById(R.id.userStateProgressBar);
        userStateMessage = findViewById(R.id.text);

        heartRateText = findViewById(R.id.hearRateText);
        stepCounterText = findViewById(R.id.stepCounterText);

        moreSensorsButton = findViewById(R.id.moreSensorsButton);
        moreSensorsButton.setOnClickListener(v -> {
            Intent intent = new Intent(
                    this,
                    DetailedSensorsListActivity.class);
            startActivity(intent);
        });
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

    private void initializeSensorsProviderService() {
        if (!isMyServiceRunning(SensorsProviderService.class)) {
            // start new data transmission service to collect data from wear os
            startService(new Intent(this, SensorsProviderService.class));

            // handle sensorMessages from sensors provider service and redirect this messages
            // to the ADL detection service
            // TODO: as you can see is redundant, we don't need to have this information if
            //  the only task we are going to do is redirect it, we can send it directly
            //  between services. So we need to create broadcast receivers in the sensors
            //  provider service
            LocalBroadcastManager.getInstance(MainActivity.this).registerReceiver(
                    sensorDataReceiver,
                    new IntentFilter(SensorableConstants.SENSORS_PROVIDER_SENDS_SENSORS));
        }


    }

    private void initializeSensors() {
        sensorsProvider = new SensorsProvider(this);

        sensorsProvider.subscribeToSensor(Sensor.TYPE_HEART_RATE, new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent sensorEvent) {
                heartRateText.setText((int) sensorEvent.values[0] + " ppm");
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int i) {
            }

        }, SensorManager.SENSOR_DELAY_NORMAL);

        sensorsProvider.subscribeToSensor(Sensor.TYPE_STEP_COUNTER, new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent sensorEvent) {
                stepCounterText.setText((int) sensorEvent.values[0] + " pasos");
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int i) {
            }
        }, SensorManager.SENSOR_DELAY_NORMAL);
    }

    // TODO test me and find my utility if I have any
    @Override
    public void onMessageReceived(@NonNull MessageEvent messageEvent) {
        Toast.makeText(this, "RECIBIDO", Toast.LENGTH_LONG).show();
    }
}