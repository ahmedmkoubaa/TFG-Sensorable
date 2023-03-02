package com.commons.utils;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;

public class SensorableServicesManager {
    public static boolean isMyServiceRunning(Context context, Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    public static void initializeService(Context context, Class<?> serviceClass) {
        initializeService(context, new Intent(context, serviceClass));
    }

    public static void initializeService(Context context, Intent intent) {
        if (!isMyServiceRunning(context, intent.getClass())) {
            context.startService(intent);
        }
    }

}
