package com.sensorable.services;

import android.app.ActivityManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;

import com.commons.services.CsvSaverService;
import com.commons.utils.DeviceType;
import com.commons.utils.SensorableConstants;
import com.commons.services.SensorsProviderService;

public class ManagerService extends Service {

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        initializeWearOsTransmissionService();
        initializeEmpaticaTransmissionService();

        initializeSensorsProviderService();
        initializeAdlDetectionService();
        initializeBackUpService();
        initializeCsvSaverSerice();


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
        initializeService(new Intent(this, serviceClass));
    }

    private void initializeService(Intent intent) {
        if (!isMyServiceRunning(intent.getClass())) {
            startService(intent);
        }
    }

    private void initializeWearOsTransmissionService() {
        initializeService(WearTransmissionService.class);
    }

    private void initializeEmpaticaTransmissionService() {
        initializeService(EmpaticaTransmissionService.class);
    }

    private void initializeSensorsProviderService() {
        Intent intent = new Intent(this, SensorsProviderService.class);
        intent.putExtra(SensorableConstants.SENSORS_PROVIDER_DEVICE_TYPE, DeviceType.MOBILE);

        initializeService(intent);
    }

    private void initializeAdlDetectionService() {
        initializeService(AdlDetectionService.class);
    }

    private void initializeBackUpService() {
        initializeService(BackupService.class);
    }

    private void initializeCsvSaverSerice() {
        initializeService(CsvSaverService.class);
    }

    private void initializeRegisterActivitiesService() {
        initializeService(RegisterActivitiesService.class);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}