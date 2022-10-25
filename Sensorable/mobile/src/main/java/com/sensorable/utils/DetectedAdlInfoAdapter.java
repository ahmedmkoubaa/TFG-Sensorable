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


public class DetectedAdlInfoAdapter extends ArrayAdapter<DetectedAdlInfo> {
    private final int resource;
    private final Context context;

    private final boolean[] buttonStates;

    public DetectedAdlInfoAdapter(@NonNull Context context, int resource, @NonNull ArrayList<DetectedAdlInfo> objects) {
        super(context, resource, objects);
        this.context = context;
        this.resource = resource;

        this.buttonStates = new boolean[objects.size()];
        for (boolean b : buttonStates) {
            b = false;
        }
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        DetectedAdlInfo adl = this.getItem(position);
        String title, description, stats, timestamp;
        title = adl.getTitle();
        description = adl.getDescription();
        stats = "Desde " +
                SensorableDates.timestampToDate(adl.getStartTime()) +
                " hasta " +
                SensorableDates.timestampToDate(adl.getEndTime());

        timestamp = SensorableDates.timestampToDate(adl.getStartTime());

        LayoutInflater inflater = LayoutInflater.from(context);
        convertView = inflater.inflate(this.resource, parent, false);

        TextView adlTitle = convertView.findViewById(R.id.adlRecordTitle);
        TextView adlDescription = convertView.findViewById(R.id.adlDescription);
        TextView adlStats = convertView.findViewById(R.id.adlStats);
        TextView adlTimestamp = convertView.findViewById(R.id.adlTimestamp);
        CheckBox accompanied = convertView.findViewById(R.id.accompaniedCheckBox);

        Button seeStats = convertView.findViewById(R.id.seeStatsButton);
        seeStats.setOnClickListener(v -> {
            if (adlStats.getVisibility() == View.GONE) {
                seeStats.setText("VER MENOS");
                adlStats.setVisibility(View.VISIBLE);
            } else {
                seeStats.setText("VER ESTADÍSTICAS DETALLADAS");
                adlStats.setVisibility(View.GONE);
            }
        });


        adlTitle.setText(title);
        adlDescription.setText(description);
        adlStats.setText(stats);
        adlTimestamp.setText(timestamp);
        accompanied.setChecked(adl.getAccompanied());

        return convertView;
    }
}
