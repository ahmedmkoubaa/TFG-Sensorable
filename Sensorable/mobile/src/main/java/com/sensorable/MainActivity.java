package com.sensorable;

import android.app.Activity;
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

import com.commons.utils.LoginHelper;
import com.commons.utils.SensorTransmissionCoder;
import com.commons.utils.SensorableConstants;
import com.commons.utils.SensorableIntentFilters;
import com.commons.utils.SensorablePermissions;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.sensorable.activities.ActivitiesRegisterActivity;
import com.sensorable.activities.AdlSummaryActivity;
import com.sensorable.activities.DetailedSensorsListActivity;
import com.sensorable.activities.LocationOptionsActivity;
import com.sensorable.activities.LoginActivity;
import com.sensorable.services.BluetoothDetectionService;
import com.sensorable.services.ManagerService;
import com.sensorable.utils.MqttHelper;

import java.util.ArrayList;
import java.util.NoSuchElementException;


public class MainActivity extends AppCompatActivity {
    private final ArrayList<SensorTransmissionCoder.SensorData> sensorDataBuffer = new ArrayList<>();
    private final int stepTarget = 5000;
    private Button userStateSummary;
    private ProgressBar useStateProgressBar;
    private TextView userStateMessage;
    private TextView heartRateText, stepCounterText;

    private BroadcastReceiver displayDataReceiver;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        SensorablePermissions.requestAll(this);
        SensorablePermissions.ignoreBatteryOptimization(this);

        initializeLogin();

        initializeAttributesFromUI();

        initializeSensorDataReceiver();
        initializeInfoReceiver();

        initializeManagerService();

        MqttHelper.connect();
    }


    @Override
    protected void onStart() {
        super.onStart();

        // TODO remove this progress bar statements
        userStateSummary.setClickable(false);


        userStateSummary.setText("\n" + stepTarget);

        useStateProgressBar.setMin(0);
        useStateProgressBar.setMax(stepTarget);


        BottomNavigationView bottomNavigation = findViewById(R.id.bottom_navigation);
        bottomNavigation.setSelectedItemId(R.id.tab_home);
        bottomNavigation.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();
                switch (id) {
                    case R.id.tab_activities_recorder:
                        startActivity(
                                new Intent(MainActivity.this, ActivitiesRegisterActivity.class)
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
        displayDataReceiver =
                new BroadcastReceiver() {
                    @Override
                    public void onReceive(Context context, Intent intent) {
                        Bundle b = intent.getBundleExtra(SensorableConstants.EXTRA_MESSAGE);
                        ArrayList<SensorTransmissionCoder.SensorData> arrayMessage = b.getParcelableArrayList(SensorableConstants.BROADCAST_MESSAGE);


                        // to display the average heart rate, we use streams
                        try {
                            double avgHeartRate = Math.floor(arrayMessage.stream()
                                    .filter(sensor -> sensor.getSensorType() == Sensor.TYPE_HEART_RATE)
                                    .mapToDouble(sensor -> sensor.getValue()[0])
                                    .average()
                                    .orElseThrow(NoSuchElementException::new));

                            heartRateText.setText((avgHeartRate == 0 ? "-" : avgHeartRate) + " ppm");

                            Log.i("DATA RECEIVER", "stream average heart rate" + avgHeartRate);

                        } catch (NoSuchElementException e) {
                            Log.e("DATA RECEIVER", "stream average has failed");
                        }

                        // to show the recorded steps we use the max operation of the stream method
                        try {
                            double recordedSteps = arrayMessage.stream()
                                    .filter(s -> s.getSensorType() == Sensor.TYPE_STEP_COUNTER)
                                    .mapToDouble(s -> s.getValue()[0])
                                    .max()
                                    .orElseThrow(NoSuchElementException::new);

                            stepCounterText.setText(
                                    Math.round((SensorableConstants.DISTANCE_OF_STEP_IN_M * recordedSteps) / 1000) + " Km"
                            );

                            userStateSummary.setText((int) recordedSteps + "\n" + stepTarget);

                        } catch (NoSuchElementException e) {
                            Log.e("DATA RECEIVER", "stream max steps has failed");

                        }
                    }
                };

        LocalBroadcastManager.getInstance(this).
                registerReceiver(displayDataReceiver, SensorableIntentFilters.EMPATICA_SENSORS);

        LocalBroadcastManager.getInstance(this).
                registerReceiver(displayDataReceiver, SensorableIntentFilters.WEAR_SENSORS);

        LocalBroadcastManager.getInstance(this).
                registerReceiver(displayDataReceiver, SensorableIntentFilters.SERVICE_PROVIDER_SENSORS);
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


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        switch (requestCode) {
            case SensorableConstants.REQUEST_ENABLE_BT:
                Log.i("BLUETOOTH_PROVIDER", "on activity result for turn on bluetooth");
                if (resultCode == Activity.RESULT_OK) {
                    startService(new Intent(this, BluetoothDetectionService.class));
                }
                break;
            default:
                Log.i("ON_ACTIVITY_RESULT DEFAULT", "on activity result for companion found device");

                super.onActivityResult(requestCode, resultCode, data);
                break;
        }
    }

    private void initializeManagerService() {
        startService(new Intent(this, ManagerService.class));
    }


    private void initializeAttributesFromUI() {
        userStateSummary = (Button) findViewById(R.id.userStateSummary);
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


    private void initializeLogin() {
        if (!LoginHelper.isLogged(getApplicationContext())) {
            startActivity(new Intent(this, LoginActivity.class));
        }
    }
}