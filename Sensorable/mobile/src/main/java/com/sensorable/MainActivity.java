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

import com.commons.OperatorType;
import com.commons.SensorTransmissionCoder;
import com.commons.SensorableConstants;
import com.commons.SensorablePermissions;
import com.commons.SensorsProvider;
import com.commons.database.AdlDao;
import com.commons.database.AdlEntity;
import com.commons.database.EventDao;
import com.commons.database.EventEntity;
import com.commons.database.EventForAdlDao;
import com.commons.database.EventForAdlEntity;
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
import com.sensorable.utils.MobileDatabaseBuilder;
import com.sensorable.utils.MqttHelper;

import java.util.ArrayList;
import java.util.List;
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
    private BroadcastReceiver sensorDataReceiver;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SensorablePermissions.requestAll(this);
        SensorablePermissions.ignoreBatteryOptimization(this);

        initializeAttributesFromUI();

        initializeMobileDatabase();
        initializeSensorDataReceiver();

//        initializeWearOsTranmissionService();
//        initializeEmpaticaTransmissionService();
        initializeAdlDetectionService();
        initializeBackUpService();
        initializeBluetoothDetectionService();
        initializeSensorsProviderService();
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

        testMQTT();

        // Summary, progressBar and message will be set using a system valoration
        // this system valoration will be developed in the near future
    }


    @Override
    protected void onStart() {
        super.onStart();
        initializeSensors();
    }

    @Override
    protected void onDestroy() {
        MqttHelper.disconnect();
        super.onDestroy();
    }

    private void testMQTT() {
        Log.i("MQTT_RECEIVE_ADLS", "before connection");
        MqttHelper.connect();

        MqttHelper.subscribe("sensorable/database/adls", mqtt5Publish -> {

            String payload = new String(mqtt5Publish.getPayloadAsBytes());
            String[] tables = payload.split("#");

            Log.i("MQTT_RECEIVE_ADLS", "new content " + payload);

            String adls = tables[0].substring(1, tables[0].length() - 1);
            String events = tables[1].substring(1, tables[1].length() - 1);
            String eventsForAdls = tables[2].substring(1, tables[2].length() - 1);

            final String rowsRegex = "\\}\\{";

            ArrayList<AdlEntity> adlEntities = new ArrayList<AdlEntity>();
            String[] fields;

            // adls
            for (String r : adls.split(rowsRegex)) {
                fields = r.split(SensorableConstants.JSON_FIELDS_SEPARATOR);
                adlEntities.add(new AdlEntity(Integer.parseInt(fields[0]), fields[1], fields[2]));
                Log.i("MQTT_RECEIVE_ADLS", "new row is" + r);
            }

            AdlDao adlDao = MobileDatabaseBuilder.getDatabase(this).adlDao();
            executor.execute(() -> {
                adlDao.insertAll(adlEntities);
            });

            // events
            ArrayList<EventEntity> eventEntities = new ArrayList<>();

            for (String r : events.split(rowsRegex)) {
                fields = r.split(SensorableConstants.JSON_FIELDS_SEPARATOR);


                eventEntities.add(
                        new EventEntity(
                                Integer.parseInt(fields[0]),
                                Integer.parseInt(fields[1]),
                                Integer.parseInt(fields[2]),
                                Integer.parseInt(fields[3]),
                                OperatorType.valueOf(fields[4]),
                                Float.parseFloat(fields[5]),
                                fields[6]
                        )
                );

                Log.i("MQTT_RECEIVE_ADLS", "new row is" + r);
            }

            EventDao eventDao = MobileDatabaseBuilder.getDatabase(this).eventDao();
            executor.execute(() -> {
                eventDao.insertAll(eventEntities);
            });

            // eventsForAdls
            ArrayList<EventForAdlEntity> eventsForAdlsEntities = new ArrayList<>();
            for (String r : eventsForAdls.split(rowsRegex)) {
                fields = r.split(SensorableConstants.JSON_FIELDS_SEPARATOR);
                eventsForAdlsEntities.add(
                        new EventForAdlEntity(
                                Integer.parseInt(fields[0]),
                                Integer.parseInt(fields[1]),
                                Integer.parseInt(fields[2])
                        )
                );

                Log.i("MQTT_RECEIVE_ADLS", "new row is" + r);
            }

            EventForAdlDao eventForAdlDao = MobileDatabaseBuilder.getDatabase(this).eventForAdlDao();
            executor.execute(() -> {
                eventForAdlDao.insertAll(eventsForAdlsEntities);
            });

            executor.execute(() -> {
                List<AdlEntity> adl = adlDao.getAll();
                for (AdlEntity a: adl){
                }
                List<EventEntity> event = eventDao.getAll();
                for (EventEntity a: event){
                }
                List<EventForAdlEntity> eventForAdl = eventForAdlDao.getAll();
                for (EventForAdlEntity a: eventForAdl){
                }
            });
        });


/*
        float[] arr = {1, 2, 3};

        SensorMessageEntity msg = new SensorMessageEntity();
        msg.deviceType = DeviceType.MOBILE;
        msg.sensorType = Sensor.TYPE_HEART_RATE;
        msg.values = Arrays.toString(arr);
        msg.timestamp = new Date().getTime();

        final String myStringMsg = "[ " + msg.toJson() + "]";

        MqttHelper.publish("sensorable/database/sensors/insert", myStringMsg.getBytes());
        Toast.makeText(this, "Sent mqtt message " + myStringMsg, Toast.LENGTH_LONG).show();
*/

    }

    private void initializeSensorDataReceiver() {
        sensorDataReceiver =
                new BroadcastReceiver() {
                    @Override
                    public void onReceive(Context context, Intent intent) {
                        Bundle b = intent.getBundleExtra(SensorableConstants.EXTRA_MESSAGE);
                        ArrayList<SensorTransmissionCoder.SensorMessage> arrayMessage = b.getParcelableArrayList(SensorableConstants.BROADCAST_MESSAGE);
                        collectReceivedSensorData(arrayMessage);
                    }
                };
    }


    private void initializeMobileDatabase() {
        sensorMessageDao = MobileDatabaseBuilder.getDatabase(this).sensorMessageDao();
        executor = MobileDatabaseBuilder.getExecutor();
    }

    private void initializeWifiDirectDetector() {
        wifiDirectProvider = new WifiDirectDevicesProvider(this);
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

    private void initializeBackUpService() {
        startService(new Intent(this, BackUpService.class));

    }

    private void initializeEmpaticaTransmissionService() {
        startService(new Intent(this, EmpaticaTransmissionService.class));

        // handle messages from our service to this activity
        LocalBroadcastManager.getInstance(MainActivity.this).registerReceiver(
                sensorDataReceiver,
                new IntentFilter(SensorableConstants.EMPATICA_SENDS_SENSOR_DATA));
    }

    private void initializeWearOsTranmissionService() {
        // start new data transmission service to collect data from wear os
        startService(new Intent(this, WearTransmissionService.class));

        // handle messages from our service to this activity
        LocalBroadcastManager.getInstance(MainActivity.this).registerReceiver(
                sensorDataReceiver,
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

    private void initializeSensors() {
        sensorsProvider = new SensorsProvider(this);

        sensorsProvider.subscribeToSensor(Sensor.TYPE_HEART_RATE, new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent sensorEvent) {
                hearRateText.setText((int) sensorEvent.values[0] + " ppm");
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