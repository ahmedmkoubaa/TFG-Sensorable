package com.sensorable.utils;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.commons.database.ActivityStepEntity;
import com.sensorable.R;

import java.util.ArrayList;

import static com.sensorable.utils.StepsTimerRecorder.saveTag;

public class ActivityStepEntityAdapter extends ArrayAdapter<ActivityStepEntity> {
    private final int resource;
    private final Context context;
    private boolean stepsEnabled;
    private final long idActivity;

    public ActivityStepEntityAdapter(@NonNull Context context, int resource, @NonNull ArrayList<ActivityStepEntity> objects, final long idActivity, boolean stepsEnabled) {
        super(context, resource, objects);
        this.context = context;
        this.resource = resource;
        this.stepsEnabled = stepsEnabled;
        this.idActivity = idActivity;
    }


    @Override
    public boolean areAllItemsEnabled() {
        return stepsEnabled && super.areAllItemsEnabled();
    }

    public void setStepsEnabled(boolean enabled) {
        this.stepsEnabled = enabled;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        ActivityStepEntity activityStep = this.getItem(position);
        String title = activityStep.getTitle();
        int id = activityStep.getId();

        LayoutInflater inflater = LayoutInflater.from(context);
        convertView = inflater.inflate(this.resource, parent, false);

        Button tagStep = convertView.findViewById(R.id.tagStepButton);
        tagStep.setText(title);
        tagStep.setOnClickListener(view -> saveTag(idActivity, id));

        tagStep.setEnabled(areAllItemsEnabled());

        return convertView;
    }
}
