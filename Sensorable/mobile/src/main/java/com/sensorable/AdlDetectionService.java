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

public class AdlDetectionService extends Service {

    private BroadcastReceiver mobileReceiver;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Toast.makeText(this, "ADL DETECTION SERVICE", Toast.LENGTH_SHORT).show();

        sendMessageToActivity("Hi, I'm ADL service");
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
                Toast.makeText(context, "mobile: received" +
                                intent.getBundleExtra("MOBILE_DATA_COLLECTED")
                                        .getString("MobileMessage"),
                        Toast.LENGTH_SHORT).show();
            }
        };

        LocalBroadcastManager.getInstance(this).registerReceiver(
                mobileReceiver, new IntentFilter("MobileUpdates"));
    }


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}