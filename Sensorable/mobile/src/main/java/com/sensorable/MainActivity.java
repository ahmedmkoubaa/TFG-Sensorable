package com.sensorable;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.Sensor;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
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
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.sensorable.activities.AdlSummaryActivity;
import com.sensorable.activities.BluetoothOptionsActivity;
import com.sensorable.activities.DetailedSensorsListActivity;
import com.sensorable.activities.LocationOptionsActivity;
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
import java.util.concurrent.ExecutorService;


public class MainActivity extends AppCompatActivity implements MessageClient.OnMessageReceivedListener {
    private final ArrayList<SensorTransmissionCoder.SensorMessage> sensorDataBuffer = new ArrayList<>();
    private final int stepTarget = 5000;
    private Button userStateSummary;
    private ProgressBar useStateProgressBar;
    private TextView userStateMessage;
    private TextView heartRateText, stepCounterText;

    private SensorsProvider sensorsProvider;
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


        userStateSummary.setText("\n" + String.valueOf(stepTarget));

        useStateProgressBar.setMin(0);
        useStateProgressBar.setMax(stepTarget);


        BottomNavigationView bottomNavigation = (BottomNavigationView) findViewById(R.id.bottom_navigation);
        bottomNavigation.setSelectedItemId(R.id.tab_home);
        bottomNavigation.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();
                switch (id) {
                    case R.id.tab_bluetooth:
                        startActivity(
                                new Intent(MainActivity.this, BluetoothOptionsActivity.class)
                        );
                        overridePendingTransition(0, 0);

                        return true;

                    case R.id.tab_adls:
                        startActivity(
                                new Intent(MainActivity.this, AdlSummaryActivity.class)
                        );
                        overridePendingTransition(0, 0);

                        return true;

                    case R.id.tab_locations:
                        startActivity(
                                new Intent(MainActivity.this, LocationOptionsActivity.class)
                        );
                        overridePendingTransition(0, 0);
                        return true;

                    case R.id.tab_charts:

                        startActivity(
                                new Intent(MainActivity.this, DetailedSensorsListActivity.class)
                        );
                        overridePendingTransition(0, 0);
                        return true;
                }

                return true;
            }
        });


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
                                case DeviceType.EMPATICA:
                                    switch (sensorMessage.getSensorType()) {
                                        case Sensor.TYPE_STEP_COUNTER:
                                            int steps = Math.round(sensorMessage.getValue()[0]);
                                            double stepsToKm = Math.round((steps * 0.65) / 10) / 100.0;

                                            stepCounterText.setText(stepsToKm + " km");
                                            useStateProgressBar.setProgress(steps);
                                            userStateMessage.setText(getMessageBySteps(steps));
                                            userStateSummary.setText(steps + "\n\n" + stepTarget);
                                            break;

                                        case Sensor.TYPE_HEART_RATE:
                                            int heartRate = Math.round(sensorMessage.getValue()[0]);
                                            String msg = "";

                                            if (heartRate == 0) {
                                                msg = "-";
                                            } else {
                                                msg += heartRate;
                                            }
                                            heartRateText.setText(heartRate == 0 ? "-" : heartRate + " ppm");
                                            break;
                                    }

                                    break;
                            }
                        });
                    }


                };
    }

    private String getMessageBySteps(int steps) {
        String msg = null;
        if (steps < 1000) {
            msg = "Aún te quedan bastantes pasos por hacer, ¡Ánimo!";
        } else if (steps >= 1000 && 3000 < steps) {
            msg = "Has avanzado, sigue caminando y conseguirás tu objetivo rápidamente.";
        } else if (steps >= 3000) {
            msg = "Ya llevas más de la mitad, te queda muy poco. ¡¡Ánimo!!";
        } else {
            msg = "Camina para poder conseguir tu objetivo de pasos, ánimo!";
        }

        return msg;

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

    private void collectReceivedSensorData
            (ArrayList<SensorTransmissionCoder.SensorMessage> arrayMessage) {
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


        }

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

    private void initializeBackUpService() {
        if (!isMyServiceRunning(BackUpService.class)) {
            startService(new Intent(this, BackUpService.class));
        }
    }

    private void initializeEmpaticaTransmissionService() {
        if (!isMyServiceRunning(EmpaticaTransmissionService.class)) {
            startService(new Intent(this, EmpaticaTransmissionService.class));
            // handle messages from our service to this activity
        }

        LocalBroadcastManager.getInstance(MainActivity.this).registerReceiver(
                sensorDataReceiver,
                new IntentFilter(SensorableConstants.EMPATICA_SENDS_SENSOR_DATA));
    }

    private void initializeWearOsTranmissionService() {
        if (!isMyServiceRunning(WearTransmissionService.class)) {
            // start new data transmission service to collect data from wear os
            startService(new Intent(this, WearTransmissionService.class));
        }

        // handle messages from our service to this activity
        LocalBroadcastManager.getInstance(MainActivity.this).registerReceiver(
                sensorDataReceiver,
                new IntentFilter(SensorableConstants.WEAR_SENDS_SENSOR_DATA));
    }

    private void initializeAttributesFromUI() {
        userStateSummary = findViewById(R.id.userStateSummary);
        useStateProgressBar = findViewById(R.id.userStateProgressBar);
        userStateMessage = findViewById(R.id.text);

        heartRateText = findViewById(R.id.hearRateText);
        stepCounterText = findViewById(R.id.stepCounterText);
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
        }

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


    // TODO test me and find my utility if I have any
    @Override
    public void onMessageReceived(@NonNull MessageEvent messageEvent) {
        Toast.makeText(this, "RECIBIDO", Toast.LENGTH_LONG).show();
    }
}