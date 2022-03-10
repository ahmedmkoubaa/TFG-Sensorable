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

import com.commons.SensorsProvider;


public class MainActivity extends WearableActivity {

    private TextView heartText;
    private TextView lightText;
    private SensorsProvider sensorsProvider;
    private Button send, sendStepCounter;


    private WearSensorDataSender sensorSender;
    private float[] lastHeartRateValue;

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
                float value[] = new float[1];
                value[0] = 12312;
                Toast.makeText(MainActivity.this, "enviando step counter", Toast.LENGTH_LONG).show();
                sensorSender.sendMessage(Sensor.TYPE_STEP_COUNTER, value);
            }
        });


        sensorsProvider = new SensorsProvider(this);
        sensorSender = new WearSensorDataSender(this);

    }

    @Override
    protected void onStart() {
        super.onStart();

        sensorsProvider.subscribeToSensor(Sensor.TYPE_HEART_RATE, new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent sensorEvent) {
                String values = " " + sensorEvent.values[0] + " ppm";
                heartText.setText(values);
                lastHeartRateValue = sensorEvent.values;
                sensorSender.sendMessage(Sensor.TYPE_HEART_RATE, sensorEvent.values);
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