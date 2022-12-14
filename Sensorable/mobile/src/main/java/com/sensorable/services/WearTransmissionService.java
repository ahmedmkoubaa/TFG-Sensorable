package com.sensorable.services;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.commons.SensorTransmissionCoder;
import com.commons.SensorableConstants;
import com.google.android.gms.wearable.Channel;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.WearableListenerService;

import java.util.ArrayList;


public class WearTransmissionService extends WearableListenerService {
    private final ArrayList<SensorTransmissionCoder.SensorMessage> sensorMessagesBuffer = new ArrayList<>();

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
            String[] splitedSensors = receivedSensors.split(SensorTransmissionCoder.DATA_SEPARATOR);

            for (String s : splitedSensors) {
                sendMessageToActivity(SensorTransmissionCoder.decodeMessage(s));
            }
        }
    }

    private void sendMessageToActivity(SensorTransmissionCoder.SensorMessage msg) {

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
    public void onDataChanged(@NonNull DataEventBuffer dataEventBuffer) {
        super.onDataChanged(dataEventBuffer);
    }

    @Override
    public void onChannelOpened(@NonNull Channel channel) {
        super.onChannelOpened(channel);
    }
}
