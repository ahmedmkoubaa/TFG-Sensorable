package com.sensorable.utils;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.commons.SensorableDates;
import com.sensorable.R;

import java.util.ArrayList;


public class ActivitiesRecordAdapter extends ArrayAdapter<ActivitiesRecord> {
    private final int resource;
    private final Context context;

    public ActivitiesRecordAdapter(@NonNull Context context, int resource, @NonNull ArrayList<ActivitiesRecord> objects) {
        super(context, resource, objects);
        this.context = context;
        this.resource = resource;
    }


    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        ActivitiesRecord record = this.getItem(position);
        String title, description;
        title = record.getTitle();
        description = record.getDescription();

        LayoutInflater inflater = LayoutInflater.from(context);
        convertView = inflater.inflate(this.resource, parent, false);

        TextView adlTitle = convertView.findViewById(R.id.activityRecordTitle);
        TextView adlDescription = convertView.findViewById(R.id.activityRecordDescription);

        adlTitle.setText(title);
        adlDescription.setText(description);

        return convertView;
    }
}
