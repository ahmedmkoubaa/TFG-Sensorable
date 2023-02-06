package com.sensorable.utils;

import com.commons.DeviceType;

public class WearosEnvironment {
    private static int DEVICE_TYPE = DeviceType.WEAROS_LEFT_HAND;

    public static void setDeviceType(final int type) {
        DEVICE_TYPE = type;
    }

    public static int getDeviceType() {
        return DEVICE_TYPE;
    }
}
