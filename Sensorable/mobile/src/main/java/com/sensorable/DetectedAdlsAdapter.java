package com.sensorable;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;


public class DetectedAdlsAdapter extends ArrayAdapter<DetectedAdl> {
    private final int resource;
    private Context context;

    private boolean buttonStates[];

    public DetectedAdlsAdapter(@NonNull Context context, int resource, @NonNull ArrayList<DetectedAdl> objects) {
        super(context, resource, objects);
        this.context = context;
        this.resource = resource;

        this.buttonStates = new boolean[objects.size()];
        for (boolean b: buttonStates) {
            b = false;
        }
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        DetectedAdl adl = this.getItem(position);

        String title, description, stats, timestamp;
        title = adl.getTitle();
        description = adl.getDescription();
        stats = adl.getStats();
        timestamp = adl.getTimestamp().toString();


        LayoutInflater inflater = LayoutInflater.from(context);
        convertView = inflater.inflate(this.resource, parent, false);

        TextView adlTitle = (TextView) convertView.findViewById(R.id.adlTitle);
        TextView adlDescription = (TextView) convertView.findViewById(R.id.adlDescription);
        TextView adlStats = (TextView )convertView.findViewById(R.id.adlStats);
        TextView adlTimestamp = (TextView )convertView.findViewById(R.id.adlTimestamp);

        Button seeStats = (Button) convertView.findViewById(R.id.seeStatsButton);
        seeStats.setOnClickListener(v-> {
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