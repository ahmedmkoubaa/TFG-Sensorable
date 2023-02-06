package com.sensorable.services;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import com.commons.DeviceType;
import com.commons.LoginHelper;
import com.commons.SensorTransmissionCoder;
import com.commons.SensorableConstants;
import com.commons.database.SensorMessageDao;
import com.commons.database.SensorMessageEntity;
import com.sensorable.utils.MobileDatabase;
import com.sensorable.utils.MobileDatabaseBuilder;

import java.util.ArrayList;
import java.util.concurrent.ExecutorService;

public class ManagerService extends Service {


    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}