package com.sensorable;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.IBinder;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.example.commons.SensorTransmissionCoder;

import java.util.ArrayList;

public class AdlDetectionService extends Service {

    private BroadcastReceiver mobileReceiver;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Toast.makeText(this, "ADL DETECTION SERVICE", Toast.LENGTH_SHORT).show();
        initializeMobileReciver();

        return super.onStartCommand(intent, flags, startId);
    }


    private void sendMessageToActivity(String msg) {
        Intent intent = new Intent("AdlUpdates");
        // You can also include some extra data.

        Bundle empaticaBundle = new Bundle();
        empaticaBundle.putString("AdlMessage", msg);

        intent.putExtra("ADL_DATA_COLLECTED", empaticaBundle);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    private void initializeMobileReciver() {
        mobileReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Bundle b = intent.getBundleExtra("MOBILE_DATA_COLLECTED");
                ArrayList<SensorTransmissionCoder.SensorMessage> arrayMessage = b.getParcelableArrayList("MobileMessage");
                Toast.makeText(context, "ADL DETECTOR: " + arrayMessage.size() + " elementos ", Toast.LENGTH_LONG).show();
            }
        };

        LocalBroadcastManager.getInstance(this).registerReceiver(
                mobileReceiver, new IntentFilter("MOBILE_SENDS_SENSOR_DATA"));
    }


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}