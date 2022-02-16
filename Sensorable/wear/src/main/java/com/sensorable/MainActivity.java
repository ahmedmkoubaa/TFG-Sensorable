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

import androidx.annotation.NonNull;

import com.example.commons.SensorDataMessage;
import com.example.commons.SensorsProvider;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.android.gms.wearable.CapabilityClient;
import com.google.android.gms.wearable.CapabilityInfo;
import com.google.android.gms.wearable.ChannelClient;
import com.google.android.gms.wearable.DataClient;
import com.google.android.gms.wearable.MessageClient;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.Wearable;

import java.nio.charset.StandardCharsets;

import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class MainActivity extends WearableActivity {

    private TextView heartText;
    private TextView temperatureText;
    private SensorsProvider sensorsProvider;
    private TextView infoText;
    private Button send;

    private static final String WEAR_DATA_RECEPTION = "wear_data_reception";
    private static final String WEAR_DATA_RECEPTION_MESSAGE_PATH = "/wear_data_reception";

    private CapabilityInfo capabilityInfo;
    private SensorDataSender sensorSender;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        heartText = (TextView) findViewById(R.id.heartRateText);
        temperatureText = (TextView) findViewById(R.id.temperatureText);
        infoText = (TextView) findViewById(R.id.infoText);
        send = (Button) findViewById(R.id.buttonSend);

        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sensorSender.sendMessage(Sensor.TYPE_HEART_RATE, "HOLIS PEQUE");
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
                sensorSender.sendMessage(Sensor.TYPE_HEART_RATE, values);
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
                temperatureText.setText(values);
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