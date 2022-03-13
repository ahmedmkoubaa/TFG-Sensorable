package com.sensorable.utils;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.commons.database.DetectedAdlEntity;
import com.sensorable.R;

import java.util.ArrayList;


public class DetectedAdlsAdapter extends ArrayAdapter<DetectedAdlEntity> {
    private final int resource;
    private final Context context;

    private final boolean[] buttonStates;

    public DetectedAdlsAdapter(@NonNull Context context, int resource, @NonNull ArrayList<DetectedAdlEntity> objects) {
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
        DetectedAdlEntity adl = this.getItem(position);

        String title, description, stats, timestamp;
        title = adl.title;
        description = adl.description;
        stats = adl.stats;
        timestamp = Long.toString(adl.timestamp);

        LayoutInflater inflater = LayoutInflater.from(context);
        convertView = inflater.inflate(this.resource, parent, false);

        TextView adlTitle = convertView.findViewById(R.id.adlTitle);
        TextView adlDescription = convertView.findViewById(R.id.adlDescription);
        TextView adlStats = convertView.findViewById(R.id.adlStats);
        TextView adlTimestamp = convertView.findViewById(R.id.adlTimestamp);

        Button seeStats = convertView.findViewById(R.id.seeStatsButton);
        seeStats.setOnClickListener(v -> {
            if (buttonStates[position]) {
                seeStats.setText("VER ESTADÍSTICAS DETALLADAS");
                adlStats.setVisibility(View.GONE);


            } else {
                seeStats.setText("VER MENOS");
                adlStats.setVisibility(View.VISIBLE);
            }

            buttonStates[position] = !buttonStates[position];
        });


        adlTitle.setText(title);
        adlDescription.setText(description);
        adlStats.setText(stats);
        adlTimestamp.setText(timestamp);

        return convertView;
    }
}
