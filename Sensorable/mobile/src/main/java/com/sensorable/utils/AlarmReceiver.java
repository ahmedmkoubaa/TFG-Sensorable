package com.sensorable.utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import com.commons.database.SensorMessageDao;
import com.commons.database.SensorMessageEntity;
import com.hivemq.client.mqtt.exceptions.MqttClientStateException;

import java.util.List;
import java.util.concurrent.ExecutorService;

public class AlarmReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Toast.makeText(context, "ALARM!!", Toast.LENGTH_SHORT).show();
        Log.i("ALARM_RECEIVER", "A new alarm was received");

        SensorMessageDao sensorMessageDao = MobileDatabaseBuilder.getDatabase(context).sensorMessageDao();
        ExecutorService executorService = MobileDatabaseBuilder.getExecutor();

        MqttHelper.connect();

        executorService.execute(() ->
        {
            List<SensorMessageEntity> content = sensorMessageDao.getAll();
            if (!content.isEmpty()) {

                String payload = "[ ";

                for (int i = 0; i < content.size(); i++) {
                    SensorMessageEntity s = content.get(i);
                    payload += s.toJson() + ",";
                }

                payload = payload.substring(0, payload.length() - 1);
                payload += "]";

                MqttHelper.publish("sensorable/database/sensors/insert", payload.getBytes());
                Log.i("TEST_MQTT", payload);


                sensorMessageDao.deleteAll();
            } else {
                Toast.makeText(context, "TEST_MQTT NOT SENDING, EMPTY", Toast.LENGTH_SHORT).show();

                Log.i("ALARM_RECEIVER", "no rows to back up, see you soon");
            }

        });
    }
}
