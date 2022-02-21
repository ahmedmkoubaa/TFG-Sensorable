package com.sensorable;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.example.commons.SensorTransmissionCoder;
import com.google.android.gms.wearable.Channel;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.WearableListenerService;


public class WearTransmissionService extends WearableListenerService {

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        Toast.makeText(this, "WEAR SERVICE", Toast.LENGTH_SHORT).show();
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onMessageReceived(@NonNull MessageEvent messageEvent) {
        super.onMessageReceived(messageEvent);
        SensorTransmissionCoder.SensorMessage message = SensorTransmissionCoder.decodeMessage(messageEvent.getData());
        sendMessageToActivity(message);
    }

    private void sendMessageToActivity(SensorTransmissionCoder.SensorMessage msg) {
        Intent intent = new Intent("SensorDataUpdates");
        // You can also include some extra data.

        Bundle sensorMesssages = new Bundle();
        sensorMesssages.putParcelable("SensorMessage", msg);

        intent.putExtra("WEAR_DATA_COLLECTED", sensorMesssages);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
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
