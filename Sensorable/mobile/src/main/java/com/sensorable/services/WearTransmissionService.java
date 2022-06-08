package com.sensorable.services;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

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
    private ArrayList<SensorTransmissionCoder.SensorMessage> sensorMessagesBuffer = new ArrayList<>();;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        Toast.makeText(this, "WEAR OS SERVICE", Toast.LENGTH_SHORT).show();
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onMessageReceived(@NonNull MessageEvent messageEvent) {
        super.onMessageReceived(messageEvent);
        SensorTransmissionCoder.SensorMessage message = SensorTransmissionCoder.decodeMessage(messageEvent.getData());
        sendMessageToActivity(message);
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
        Toast.makeText(this, "DATA CHANGED", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onChannelOpened(@NonNull Channel channel) {
        super.onChannelOpened(channel);
        Toast.makeText(this, "CANAL ABIERTO RECIBIDO", Toast.LENGTH_SHORT).show();
    }
}
