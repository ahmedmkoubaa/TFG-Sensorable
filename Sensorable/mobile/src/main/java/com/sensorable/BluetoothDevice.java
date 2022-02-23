package com.sensorable;

public class BluetoothDevice {
    private String name;
    private String mac;
    private boolean trusted;

    public BluetoothDevice(String deviceName, String deviceMac, boolean deviceTrusted) {
        name = deviceName;
        mac = deviceMac;
        trusted = deviceTrusted;
    }

    public String getName() {
        return name;
    }

    public String getMac() {
        return mac;
    }

    public boolean getTrusted() {
        return trusted;
    }
}
