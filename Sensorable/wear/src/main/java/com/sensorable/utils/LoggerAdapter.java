package com.sensorable.utils;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.commons.database.BluetoothDeviceDao;
import com.sensorable.R;

import java.util.ArrayList;
import java.util.concurrent.ExecutorService;

public class LoggerAdapter extends ArrayAdapter<String> {
    private final int resource;
    private final Context context;
    private BluetoothDeviceDao bluetoothDeviceDao;
    private ExecutorService executor;

    public LoggerAdapter(@NonNull Context context, int resource, @NonNull ArrayList<String> objects) {
        super(context, resource, objects);
        this.context = context;
        this.resource = resource;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        String message = this.getItem(position);

        LayoutInflater inflater = LayoutInflater.from(context);
        convertView = inflater.inflate(this.resource, parent, false);

        TextView loggerMessage = convertView.findViewById(R.id.loggerMessageText);
        loggerMessage.setText(message);

        return convertView;
    }
}
