package com.commons;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;

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
    };

    public static void requestAll(Activity requestingActivity) {
        requestingActivity.requestPermissions(SENSORABLE_PERMISSIONS, SensorableConstants.REQUEST_PERMISSIONS_CODE);
    }

    public static boolean isGranted(Context context, final String permission) {
        return context.checkCallingPermission(permission) == PackageManager.PERMISSION_GRANTED;
    }

}
