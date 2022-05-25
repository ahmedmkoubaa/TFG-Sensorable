package com.sensorable.utils;

public class BluetoothDeviceInfo {
    private String address;
    private String deviceName;
    private int bondState;
    private int bluetoothDeviceType;
    private boolean trusted;

    private long start;
    private long end;

    public String getAddress() {
        return address;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public int getBondState() {
        return bondState;
    }

    public int getBluetoothDeviceType() {
        return bluetoothDeviceType;
    }

    public boolean isTrusted() {
        return trusted;
    }

    public long getStart() {
        return start;
    }

    public long getEnd() {
        return end;
    }

    public BluetoothDeviceInfo(String address, String deviceName, int bondState, int bluetoothDeviceType, boolean trusted, long start, long end) {
        this.address = address;
        this.deviceName = deviceName;
        this.bondState = bondState;
        this.bluetoothDeviceType = bluetoothDeviceType;
        this.trusted = trusted;
        this.start = start;
        this.end = end;
    }


}
