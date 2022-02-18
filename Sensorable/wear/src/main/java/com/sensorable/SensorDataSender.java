package com.sensorable;

import android.app.Activity;
import android.hardware.Sensor;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.example.commons.SensorDataMessage;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.android.gms.wearable.CapabilityClient;
import com.google.android.gms.wearable.CapabilityInfo;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.Wearable;

import java.nio.charset.StandardCharsets;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SensorDataSender {
    private Activity context;
    private CapabilityInfo capabilityInfo;
    private ExecutorService executorService = Executors.newFixedThreadPool(1);

    private static final String WEAR_DATA_RECEPTION = "wear_data_reception";
    private static final String WEAR_DATA_RECEPTION_MESSAGE_PATH = "/wear_data_reception";

    public SensorDataSender(Activity context) {
        this.context = context;
    }

    public void sendMessage(int sensorType, String value) {
        sendMessage(new SensorDataMessage.SensorMessage(sensorType, value));
    }

    public void sendMessage(final SensorDataMessage.SensorMessage message) {
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                //some code here
                try {
                    capabilityInfo = Tasks.await(
                            Wearable.getCapabilityClient(context).getCapability(
                                    WEAR_DATA_RECEPTION, CapabilityClient.FILTER_REACHABLE));


                } catch (ExecutionException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                String bestNode = pickBestNodeId(capabilityInfo.getNodes());

                if (bestNode != null) {
                    byte[] sensorData = SensorDataMessage.codeMessage(message);

                    Task<Integer> sendTask =
                            Wearable.getMessageClient(context).
                                    sendMessage(bestNode, WEAR_DATA_RECEPTION_MESSAGE_PATH, sensorData);


                    // You can add success and/or failure listeners,
                    // Or you can call Tasks.await() and catch ExecutionException
                    sendTask.addOnSuccessListener(
                            new OnSuccessListener() {
                                @Override
                                public void onSuccess(Object o) {
//                                    Toast.makeText(context, "SUCCESS SEND MESSAGE", Toast.LENGTH_LONG).show();
                                }
                            });


                    sendTask.addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(context, "FAILURE SEND MESSAGE", Toast.LENGTH_LONG).show();
                        }
                    });
                } else {
                    // Unable to retrieve node with transcription capability
                    Toast.makeText(context, "NO NEARBY NODE", Toast.LENGTH_SHORT).show();
                }
            }
        });
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

}
