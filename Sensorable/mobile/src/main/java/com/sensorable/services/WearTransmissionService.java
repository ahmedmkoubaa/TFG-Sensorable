package com.sensorable.services;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.commons.utils.SensorTransmissionCoder;
import com.commons.utils.SensorableConstants;
import com.google.android.gms.wearable.Channel;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.WearableListenerService;

import java.util.ArrayList;


public class WearTransmissionService extends WearableListenerService {
    private final ArrayList<SensorTransmissionCoder.SensorData> sensorMessagesBuffer = new ArrayList<>();

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onMessageReceived(@NonNull MessageEvent messageEvent) {
        super.onMessageReceived(messageEvent);

        String receivedSensors = SensorTransmissionCoder.decodeString(
                messageEvent.getData()
        );

        if (!receivedSensors.equals("")) {
            String[] splittedSensors = receivedSensors.split(SensorTransmissionCoder.DATA_SEPARATOR);

            for (String s : splittedSensors) {
                sendMessageToActivity(SensorTransmissionCoder.decodeMessage(s));
            }
        }
    }

    private void sendMessageToActivity(SensorTransmissionCoder.SensorData msg) {

        sensorMessagesBuffer.add(msg);
        if (sensorMessagesBuffer.size() >= SensorableConstants.WEAR_BUFFER_SIZE) {
            Intent intent = new Intent(SensorableConstants.WEAR_SENDS_SENSOR_DATA);
            // You can also include some extra data.

            Bundle wearBundle = new Bundle();
            wearBundle.putParcelableArrayList(SensorableConstants.BROADCAST_MESSAGE, new ArrayList<>(sensorMessagesBuffer));

            intent.putExtra(SensorableConstants.EXTRA_MESSAGE, wearBundle);
            LocalBroadcastManager.getInstance(this).sendBroadcast(intent);

            // reset buffer
            sensorMessagesBuffer.clear();
        }
    }


    @Override
    public void onDataChanged(DataEventBuffer dataEvents) {
        for (DataEvent event : dataEvents) {
            if (event.getType() == DataEvent.TYPE_CHANGED &&
                    event.getDataItem().getUri().getPath().equals("/data")) {
                DataMap dataMap = DataMapItem.fromDataItem(event.getDataItem()).getDataMap();
                String message = dataMap.getString("message");
                Log.d("WEAR_TRANSMISSION_SERVICE", "Message received: " + message);
                // TODO: Process the message
            }
        }
    }

    @Override
    public void onChannelOpened(@NonNull Channel channel) {
        super.onChannelOpened(channel);
    }
}
