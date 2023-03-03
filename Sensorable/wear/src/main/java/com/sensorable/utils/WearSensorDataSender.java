package com.sensorable.utils;

import android.content.Context;

import androidx.annotation.NonNull;

import com.commons.utils.SensorTransmissionCoder;
import com.commons.utils.SensorableConstants;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.android.gms.wearable.CapabilityClient;
import com.google.android.gms.wearable.CapabilityInfo;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.Wearable;


import java.util.ArrayList;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class WearSensorDataSender {
    private final Context context;
    private static final String WEAR_DATA_RECEPTION = "wear_data_reception";
    private static final String WEAR_DATA_RECEPTION_MESSAGE_PATH = "/wear_data_reception";
    private final ExecutorService executorThread = Executors.newFixedThreadPool(1);
    private final ArrayList<SensorTransmissionCoder.SensorData> sensorsBuffer = new ArrayList<>();

    public WearSensorDataSender(Context context) {
        this.context = context;
    }

    public void sendMessage(int device_type, int sensorType, float[] value) {
        sendMessage(new SensorTransmissionCoder.SensorData(device_type, sensorType, value));
    }

    public void sendMessage(final SensorTransmissionCoder.SensorData message) {
        sensorsBuffer.add(message);
        transmitData();
    }

    public void sendMessage(final ArrayList<SensorTransmissionCoder.SensorData> sensorArray) {
        sensorsBuffer.addAll(sensorArray);
        transmitData();
    }

    private void transmitData() {
        if (sensorsBuffer.size() == SensorableConstants.WEAR_SENDING_BUFFER_SIZE) {
            final ArrayList<SensorTransmissionCoder.SensorData> sensorsBackUpBuffer = new ArrayList<>(sensorsBuffer);
            sensorsBuffer.removeAll(sensorsBackUpBuffer);

            String sensorsToSend = "";
            for (SensorTransmissionCoder.SensorData s : sensorsBackUpBuffer) {
                sensorsToSend += s.toString() + SensorTransmissionCoder.DATA_SEPARATOR;
            }

            final String finalSensorsToSend = sensorsToSend;
            executorThread.execute(new Runnable() {
                @Override
                public void run() {
                    CapabilityInfo capabilityInfo = null;
                    String bestNode = null;

                    try {
                        capabilityInfo = Tasks.await(Wearable
                                .getCapabilityClient(context)
                                .getCapability(WEAR_DATA_RECEPTION, CapabilityClient.FILTER_REACHABLE)
                        );
                    } catch (ExecutionException | InterruptedException e) {
                        e.printStackTrace();
                    }

                    if (capabilityInfo != null) {
                        bestNode = pickBestNodeId(capabilityInfo.getNodes());
                    }

                    if (bestNode != null) {
                        Task<Integer> sendTask =
                                Wearable.getMessageClient(context).
                                        sendMessage(
                                                bestNode,
                                                WEAR_DATA_RECEPTION_MESSAGE_PATH,
                                                SensorTransmissionCoder.codeString(finalSensorsToSend)
                                        );


                        // You can add success and/or failure listeners,
                        // Or you can call Tasks.await() and catch ExecutionException
                        sendTask.addOnSuccessListener(
                                new OnSuccessListener() {
                                    @Override
                                    public void onSuccess(Object o) {
                                        SensorableLogger.log("Successfully sent sensors to android");
                                    }
                                });


                        sendTask.addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                SensorableLogger.log("Failed to send sensors");
                                sensorsBuffer.addAll(0, sensorsBackUpBuffer);
                            }
                        });
                    } else {
                        // Unable to retrieve node with transcription capability
                        SensorableLogger.log("NO NEARBY NODE");
                    }
                }
            });
        }
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
