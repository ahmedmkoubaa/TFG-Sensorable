package com.sensorable.utils;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.Room;

import com.commons.SensorableConstants;
import com.commons.database.BluetoothDeviceEntity;
import com.commons.database.BluetoothDeviceDao;
import com.sensorable.R;

import java.util.ArrayList;

public class BluetoothDeviceAdapter extends ArrayAdapter<BluetoothDeviceEntity> {
    private final int resource;
    private BluetoothDeviceDao bluetoothDeviceDao;
    private final Context context;

    public BluetoothDeviceAdapter(@NonNull Context context, int resource, @NonNull ArrayList<BluetoothDeviceEntity> objects) {
        super(context, resource, objects);
        this.context = context;
        this.resource = resource;

        initializeDatabase();
    }

    private void initializeDatabase() {
        MobileDatabase database = Room.databaseBuilder(
                context,
                MobileDatabase.class,
                SensorableConstants.MOBILE_DATABASE_NAME)
                .allowMainThreadQueries()
                .build();

        bluetoothDeviceDao = database.bluetoothDeviceDao();
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        BluetoothDeviceEntity item = this.getItem(position);
        String name = item.deviceName;
        String mac = item.address;
        boolean trusted = item.trusted;

        LayoutInflater inflater = LayoutInflater.from(context);
        convertView = inflater.inflate(this.resource, parent, false);

        TextView deviceName = convertView.findViewById(R.id.deviceName);
        TextView deviceMAC = convertView.findViewById(R.id.deviceMAC);
        Switch deviceTrusted = convertView.findViewById(R.id.deviceTrsusted);
        TextView first = convertView.findViewById(R.id.firstTime);
        TextView last = convertView.findViewById(R.id.lastTime);

        deviceTrusted.setOnCheckedChangeListener((compoundButton, checked) -> {
            item.trusted = checked;
            bluetoothDeviceDao.updateDevice(item);
        });

        deviceName.setText(name);
        deviceMAC.setText(mac);
        deviceTrusted.setChecked(trusted);
        first.setText(Long.toString(item.firstTimestamp));
        last.setText(Long.toString(item.lastTimestamp));

        return convertView;
    }
}
