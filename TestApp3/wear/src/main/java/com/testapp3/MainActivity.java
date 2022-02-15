package com.testapp3;
import android.Manifest;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.wearable.activity.WearableActivity;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.example.commons.SensorsProvider;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.android.gms.wearable.CapabilityClient;
import com.google.android.gms.wearable.CapabilityInfo;
import com.google.android.gms.wearable.DataClient;
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

    private static final String WEAR_DATA_RECEPTION = "wear_data_reception";
    private static final String WEAR_DATA_RECEPTION_MESSAGE_PATH = "/wear_data_reception";

    private CapabilityInfo capabilityInfo;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        heartText = (TextView) findViewById(R.id.heartRateText);
        temperatureText = (TextView) findViewById(R.id.temperatureText);
        infoText = (TextView) findViewById(R.id.infoText);


        sensorsProvider = new SensorsProvider(this);

        // an api for communication?
        DataClient dataclient = Wearable.getDataClient(this);


        ExecutorService executorService = Executors.newFixedThreadPool(1);
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                //some code here
                try {
                    capabilityInfo = Tasks.await(
                            Wearable.getCapabilityClient(MainActivity.this).getCapability(
                                    WEAR_DATA_RECEPTION, CapabilityClient.FILTER_REACHABLE));


                } catch (ExecutionException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                String bestNode = pickBestNodeId(capabilityInfo.getNodes());
                byte[] sensorData = ("HOLA").getBytes(StandardCharsets.US_ASCII);

                if (bestNode != null) {
                    Task<Integer> sendTask =
                            Wearable.getMessageClient(MainActivity.this).sendMessage(
                                    bestNode, WEAR_DATA_RECEPTION_MESSAGE_PATH, sensorData);

                    infoText.setText(capabilityInfo.getNodes().toString());


                    // You can add success and/or failure listeners,
                    // Or you can call Tasks.await() and catch ExecutionException
                    sendTask.addOnSuccessListener(
                            new OnSuccessListener() {
                                @Override
                                public void onSuccess(Object o) {
                                    Toast.makeText(MainActivity.this, "BIEN", Toast.LENGTH_LONG).show();
                                }
                            });


                    sendTask.addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(MainActivity.this, "SALIO JODIDAMENTE MAL", Toast.LENGTH_LONG).show();
                        }
                    });
                } else {
                    // Unable to retrieve node with transcription capability
                    Toast.makeText(MainActivity.this, "NO NODO", Toast.LENGTH_SHORT).show();
                }
            }
        });




    }

    private void callback() {
        Toast.makeText(this, "HECHO", Toast.LENGTH_SHORT).show();
    }

    private String pickBestNodeId(Set<Node> nodes) {
        String bestNodeId = null;
        // Find a nearby node or pick one arbitrarily
        for (Node node : nodes) {
            if (node.isNearby()) {
                return node.getId();
            }
            bestNodeId = node.getId();
        }
        return bestNodeId;
    }


    @Override
    protected void onStart() {
        super.onStart();

        sensorsProvider.subscribeToSensor(Sensor.TYPE_HEART_RATE, new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent sensorEvent) {
                String values = " " + sensorEvent.values[0] + " ppm";
                heartText.setText(values);
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