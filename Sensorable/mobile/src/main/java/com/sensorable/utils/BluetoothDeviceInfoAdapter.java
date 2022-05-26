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

import com.commons.database.BluetoothDeviceDao;
import com.commons.database.BluetoothDeviceEntity;
import com.sensorable.R;

import java.util.ArrayList;
import java.util.concurrent.ExecutorService;

public class BluetoothDeviceInfoAdapter extends ArrayAdapter<BluetoothDeviceInfo> {
    private final int resource;
    private final Context context;
    private BluetoothDeviceDao bluetoothDeviceDao;
    private ExecutorService executor;

    public BluetoothDeviceInfoAdapter(@NonNull Context context, int resource, @NonNull ArrayList<BluetoothDeviceInfo> objects) {
        super(context, resource, objects);
        this.context = context;
        this.resource = resource;

        initializeDatabase();
    }

    private void initializeDatabase() {
        bluetoothDeviceDao = MobileDatabaseBuilder.getDatabase(context).bluetoothDeviceDao();
        executor = MobileDatabaseBuilder.getExecutor();
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        BluetoothDeviceInfo item = this.getItem(position);
        String name = item.getDeviceName();
        String mac = item.getAddress();
        String firstTiemstamp = SensorableDates.timestampToDate(item.getStart());
        String endTimestamp = SensorableDates.timestampToDate(item.getEnd());
        boolean trusted = item.isTrusted();

        LayoutInflater inflater = LayoutInflater.from(context);
        convertView = inflater.inflate(this.resource, parent, false);

        TextView deviceName = convertView.findViewById(R.id.deviceName);
        TextView deviceMAC = convertView.findViewById(R.id.deviceMAC);
        Switch deviceTrusted = convertView.findViewById(R.id.deviceTrsusted);
        TextView first = convertView.findViewById(R.id.firstTime);
        TextView last = convertView.findViewById(R.id.lastTime);

        deviceTrusted.setOnCheckedChangeListener((compoundButton, checked) -> {
            BluetoothDeviceEntity device = new BluetoothDeviceEntity(item.getAddress(), item.getDeviceName(), item.getBondState(), item.getBluetoothDeviceType(), item.isTrusted());
            device.trusted = checked;
            executor.execute(() -> {
                bluetoothDeviceDao.updateDevice(device);
            });

            notifyDataSetChanged();
        });

        deviceName.setText(name);
        deviceMAC.setText(mac);
        deviceTrusted.setChecked(trusted);

        first.setText(firstTiemstamp);
        last.setText(endTimestamp);

        return convertView;
    }
}
