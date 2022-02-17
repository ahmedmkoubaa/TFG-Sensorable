package com.sensorable;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.example.commons.SensorDataMessage;
import com.google.android.gms.wearable.Channel;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.WearableListenerService;


public class DataTransmissionService extends WearableListenerService {

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        Toast.makeText(this, "SERVICIO LANZADO", Toast.LENGTH_SHORT).show();
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onMessageReceived(@NonNull MessageEvent messageEvent) {
        super.onMessageReceived(messageEvent);
        SensorDataMessage.SensorMessage message = SensorDataMessage.decodeMessage(messageEvent.getData());

        sendMessageToActivity(message);
//        Toast.makeText(this, "RECIBIDO (t:" + message.getSensorType() + ", v:" + message.getValue() + ")", Toast.LENGTH_LONG).show();
    }

    private void sendMessageToActivity(SensorDataMessage.SensorMessage msg) {
        Intent intent = new Intent("SensorDataUpdates");
        // You can also include some extra data.

        Bundle sensorMesssages = new Bundle();
        sensorMesssages.putParcelable("SensorMessage", msg);

        intent.putExtra("WEAR_OS_COLLECTED_DATA", sensorMesssages);
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
