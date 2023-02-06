package com.commons;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.PowerManager;
import android.provider.Settings;
import android.util.Log;

import static android.content.Context.POWER_SERVICE;

public class SensorablePermissions {
    private static final String[] SENSORABLE_PERMISSIONS = {
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.INTERNET,
            Manifest.permission.BODY_SENSORS,
            Manifest.permission.ACTIVITY_RECOGNITION,
            Manifest.permission.BLUETOOTH,
            Manifest.permission.BLUETOOTH_ADMIN,
            Manifest.permission.BLUETOOTH_CONNECT,
            Manifest.permission.ACCESS_WIFI_STATE,
            Manifest.permission.CHANGE_WIFI_STATE,
            Manifest.permission.ACCESS_NETWORK_STATE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.REQUEST_IGNORE_BATTERY_OPTIMIZATIONS,
            Manifest.permission.WAKE_LOCK,
            Manifest.permission.SCHEDULE_EXACT_ALARM,
            Manifest.permission.SET_ALARM,
            Manifest.permission.RECEIVE_BOOT_COMPLETED,
            Manifest.permission.BLUETOOTH_SCAN
    };

    public static void requestAll(Activity requestingActivity) {
        requestingActivity.requestPermissions(SENSORABLE_PERMISSIONS, SensorableConstants.REQUEST_PERMISSIONS_CODE);
    }

    public static void ignoreBatteryOptimization(Activity requestingActivity) {
        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.M) return;

        PowerManager powerManager = (PowerManager) requestingActivity.getSystemService(POWER_SERVICE);
        boolean hasIgnored = powerManager.isIgnoringBatteryOptimizations(requestingActivity.getPackageName());

        if (!hasIgnored) {
            try {
                @SuppressLint("BatteryLife")
                Intent intent = new Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
                intent.setData(Uri.parse("package:" + requestingActivity.getPackageName()));
                requestingActivity.startActivity(intent);
            } catch (Throwable ignored) {
                Log.i("SENSORABLE_PERMISSIONS", "Caught an error while trying to request ignoring battery optimization");
            }
        }
    }

    public static boolean isGranted(Context context, final String permission) {
        return context.checkCallingPermission(permission) == PackageManager.PERMISSION_GRANTED;
    }

}
