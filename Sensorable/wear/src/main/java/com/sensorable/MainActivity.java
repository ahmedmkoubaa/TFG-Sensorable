package com.sensorable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.wearable.activity.WearableActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.commons.SensorsProvider;
import com.google.android.gms.wearable.CapabilityInfo;


public class MainActivity extends WearableActivity {

    private TextView heartText;
    private TextView lightText;
    private SensorsProvider sensorsProvider;
    private Button send, sendStepCounter;


    private SensorDataSender sensorSender;
    private String lastHeartRateValue = "-1";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        heartText = (TextView) findViewById(R.id.heartRateText);
        lightText = (TextView) findViewById(R.id.temperatureText);
        send = (Button) findViewById(R.id.buttonSendHeartRate);
        sendStepCounter = (Button) findViewById(R.id.buttonSendStepCounter);

        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sensorSender.sendMessage(Sensor.TYPE_HEART_RATE, lastHeartRateValue);
            }
        });

        sendStepCounter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sensorSender.sendMessage(Sensor.TYPE_STEP_COUNTER, "MUCHOS");
            }
        });


        sensorsProvider = new SensorsProvider(this);
        sensorSender = new SensorDataSender(this);

    }

    @Override
    protected void onStart() {
        super.onStart();

        sensorsProvider.subscribeToSensor(Sensor.TYPE_HEART_RATE, new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent sensorEvent) {
                String values = " " + sensorEvent.values[0] + " ppm";
                heartText.setText(values);
                lastHeartRateValue = values;
//                sensorSender.sendMessage(Sensor.TYPE_HEART_RATE, values);
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int i) {
                if (i <= 0) {
                    Toast.makeText(
                            MainActivity.this,
                            "HEART SENSOR NOT AVAILABLE",
                            Toast.LENGTH_SHORT
                    ).show();
                }

            }
        }, SensorManager.SENSOR_DELAY_FASTEST);

        sensorsProvider.subscribeToSensor(Sensor.TYPE_LIGHT, new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent sensorEvent) {
                String values = " " + sensorEvent.values[0] + " lm";
                lightText.setText(values);
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int i) {
                if (i <= 0) {
                    Toast.makeText(
                            MainActivity.this,
                            "AMBIENT TEMPERATURE NOT AVAILABLE",
                            Toast.LENGTH_SHORT
                    ).show();
                }
            }
        }, SensorManager.SENSOR_DELAY_NORMAL);
    }
}