package com.sensorable.utils;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.sensorable.R;

import java.util.ArrayList;

import static com.sensorable.utils.StepsTimerRecorder.saveTag;

public class StepsRecorderAdapter extends ArrayAdapter<StepsRecord> {
    private final int resource;
    private final Context context;
    private boolean stepsEnabled;

    public StepsRecorderAdapter(@NonNull Context context, int resource, @NonNull ArrayList<StepsRecord> objects, boolean stepsEnabled) {
        super(context, resource, objects);
        this.context = context;
        this.resource = resource;
        this.stepsEnabled = stepsEnabled;
    }


    @Override
    public boolean areAllItemsEnabled() {
        return stepsEnabled ? super.areAllItemsEnabled() : false;
    }

    public void setStepsEnabled(boolean enabled) {
        this.stepsEnabled = enabled;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        StepsRecord record = this.getItem(position);
        String title = record.getTitle();
        int id = record.getId();

        LayoutInflater inflater = LayoutInflater.from(context);
        convertView = inflater.inflate(this.resource, parent, false);

        Button tagStep = convertView.findViewById(R.id.tagStepButton);
        tagStep.setText(title);
        tagStep.setOnClickListener(view -> saveTag(id));

        tagStep.setEnabled(areAllItemsEnabled());

        return convertView;
    }
}
