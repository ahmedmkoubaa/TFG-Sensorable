package com.sensorable.activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.hardware.Sensor;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.commons.utils.DeviceType;
import com.commons.utils.SensorTransmissionCoder;
import com.commons.utils.SensorableConstants;
import com.commons.utils.SensorableDates;
import com.commons.utils.SensorableIntentFilters;
import com.commons.utils.SensorsProvider;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.sensorable.MainActivity;
import com.sensorable.R;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;

import im.dacer.androidcharts.BarView;
import im.dacer.androidcharts.LineView;

public class DetailedSensorsListActivity extends AppCompatActivity {
    private final ArrayList<String> heartChartColumns = new ArrayList<>();
    private final ArrayList<Integer> heartchartData = new ArrayList<>();
    private final ArrayList<Integer> barViewData = new ArrayList<>(Arrays.asList(6201, 2510, 4204, 1248, 2589, 0));

    private long lastHeartChartUpdate = 0;
    private TextView accelerometerTextView;
    private TextView temperatureTextView;
    private TextView humidityTextView;
    private TextView proximityTextView;
    private TextView lightTextView;

    private LineView lineView;
    private BarView barView;

    private SensorsProvider sensorsProvider;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detailed_sensors_list);

        initializeAttributtesFromUI();
        initializeSensors();
    }

    private void initializeAttributtesFromUI() {
        accelerometerTextView = findViewById(R.id.acceleromterText);
        temperatureTextView = findViewById(R.id.temperatureText);
        humidityTextView = findViewById(R.id.humidityText);
        proximityTextView = findViewById(R.id.proximityText);
        lightTextView = findViewById(R.id.lightText);

        barView = findViewById(R.id.bar_view);

        barView.setBottomTextList(
                new ArrayList<>(Arrays.asList("09/06", "11/06", "15/06", "16/06", "17/06", "18/06"))
        );


        barView.setDataList(barViewData, 5000);

        lineView = findViewById(R.id.line_view);
        lineView.setDrawDotLine(false); //optional
        lineView.setShowPopup(LineView.SHOW_POPUPS_MAXMIN_ONLY); //optional
        lineView.setColorArray(new int[]{Color.RED});
        lineView.setBottomTextList(heartChartColumns);


        BottomNavigationView bottomNavigation = findViewById(R.id.bottom_navigation);
        bottomNavigation.setSelectedItemId(R.id.tab_charts);
        bottomNavigation.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();
                switch (id) {
                    case R.id.tab_activities_recorder:
                        startActivity(
                                new Intent(DetailedSensorsListActivity.this, ActivitiesRegisterActivity.class)
                        );
                        overridePendingTransition(0, 0);
                        return true;

                    case R.id.tab_adls:
                        startActivity(
                                new Intent(DetailedSensorsListActivity.this, AdlSummaryActivity.class)
                        );
                        overridePendingTransition(0, 0);

                        return true;

                    case R.id.tab_locations:
                        startActivity(
                                new Intent(DetailedSensorsListActivity.this, LocationOptionsActivity.class)
                        );
                        overridePendingTransition(0, 0);
                        return true;

                    case R.id.tab_home:

                        startActivity(
                                new Intent(DetailedSensorsListActivity.this, MainActivity.class)
                        );
                        overridePendingTransition(0, 0);
                        return true;
                }

                return true;
            }
        });
    }


    private void initializeSensors() {
        sensorsProvider = new SensorsProvider(this);

        BroadcastReceiver sensorReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Bundle b = intent.getBundleExtra(SensorableConstants.EXTRA_MESSAGE);
                ArrayList<SensorTransmissionCoder.SensorData> arrayMessage = b.getParcelableArrayList(SensorableConstants.BROADCAST_MESSAGE);

                try {
                    arrayMessage.forEach(sensorMessage -> {
                        switch (sensorMessage.getDeviceType()) {
                            case DeviceType.MOBILE:
                            case DeviceType.WEAROS_RIGHT_HAND:
                            case DeviceType.WEAROS_LEFT_HAND:
                            case DeviceType.EMPATICA:

                                switch (sensorMessage.getSensorType()) {

                                    case Sensor.TYPE_HEART_RATE:
                                        long current = new Date().getTime();
                                        boolean needsUpdate = ((current - lastHeartChartUpdate)) > SensorableConstants.TIME_SINCE_LAST_HEART_CHART_UPDATE;

                                        if (sensorMessage.getValue()[0] > 30 && needsUpdate) {
                                            heartchartData.add(Math.round(sensorMessage.getValue()[0]));
                                            heartChartColumns.add(SensorableDates.timestampToTimeSeconds(sensorMessage.getTimestamp()));

                                            if (heartchartData.size() > 0 && heartChartColumns.size() > 0) {
                                                if (heartChartColumns.size() > 8 || heartChartColumns.size() > 8) {
                                                    heartChartColumns.remove(0);
                                                    heartchartData.remove(0);
                                                }

                                                runOnUiThread(() -> {
                                                    lineView.setBottomTextList(heartChartColumns);
                                                    lineView.setDataList(new ArrayList<>(Arrays.asList(heartchartData)));
                                                });

                                                lastHeartChartUpdate = current;
                                            }
                                        }


                                        break;

                                    case Sensor.TYPE_STEP_COUNTER:
                                        barViewData.remove(barViewData.size() - 1);
                                        barViewData.add(Math.round(sensorMessage.getValue()[0]));

                                        runOnUiThread(() -> {
                                            barView.setDataList(barViewData, 5000);
                                        });
                                        break;

                                    case Sensor.TYPE_AMBIENT_TEMPERATURE:
                                        temperatureTextView.setText(sensorMessage.getValue()[0] + " ºC");
                                        break;

                                    case Sensor.TYPE_PROXIMITY:
                                        proximityTextView.setText(
                                                sensorMessage.getValue()[0] == 0 ? "Cercanía detectada" : "Lejos del teléfono"
                                        );

                                        break;
                                    case Sensor.TYPE_LIGHT:
                                        lightTextView.setText(sensorMessage.getValue()[0] + " lm");

                                        break;

                                    case Sensor.TYPE_LINEAR_ACCELERATION:
                                        accelerometerTextView.setText(
                                                Math.round(sensorMessage.getValue()[0]) + ", " + Math.round(sensorMessage.getValue()[1]) + ", " + Math.round(sensorMessage.getValue()[2])
                                        );

                                        break;

                                    case Sensor.TYPE_RELATIVE_HUMIDITY:
                                        humidityTextView.setText(Math.round((sensorMessage.getValue()[0]) * 100) / 100 + "%");
                                        break;
                                }

                                break;
                        }
                    });
                } catch (NullPointerException e) {
                    Log.e("DETAILED_SENSOR_LIST", "error receiving null sensor array");
                }
            }
        };

        LocalBroadcastManager.getInstance(this).registerReceiver(
                sensorReceiver,
                SensorableIntentFilters.SENSORS_PROVIDER_SENSORS
        );

        LocalBroadcastManager.getInstance(this).registerReceiver(
                sensorReceiver,
                SensorableIntentFilters.WEAR_SENSORS
        );

        LocalBroadcastManager.getInstance(this).registerReceiver(
                sensorReceiver,
                SensorableIntentFilters.EMPATICA_SENSORS
        );
    }
}

