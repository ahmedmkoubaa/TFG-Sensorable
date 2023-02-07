package com.sensorable.services;

import android.app.ActivityManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;

import com.sensorable.MainActivity;

public class ManagerService extends Service {

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        initializeWearOsTranmissionService();
        initializeEmpaticaTransmissionService();

        initializeSensorsProviderService();
        initializeAdlDetectionService();
        initializeBackUpService();


        // This isn't useful yet and eventually we'll remove it
        // initializeBluetoothDetectionService();

        initializeRegisterActivitiesService();

        return super.onStartCommand(intent, flags, startId);
    }


    public boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    private void initializeService(Class<?> serviceClass) {
        if (!isMyServiceRunning(serviceClass)) {
            // start new data transmission service to collect data from wear os
            startService(new Intent(this, serviceClass));
        }
    }

    private void initializeWearOsTranmissionService() {
        initializeService(WearTransmissionService.class);
    }

    private void initializeEmpaticaTransmissionService() {
        initializeService(EmpaticaTransmissionService.class);
    }


    private void initializeSensorsProviderService() {
        initializeService(SensorsProviderService.class);
    }

    private void initializeAdlDetectionService() {
        initializeService(AdlDetectionService.class);
    }

    private void initializeBackUpService() {
        initializeService(StorageService.class);
    }

    private void initializeRegisterActivitiesService() {
        initializeService(RegisterActivitiesService.class);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}