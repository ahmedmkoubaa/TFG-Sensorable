package com.sensorable.utils;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.commons.LoginHelper;
import com.commons.database.ActivityStepEntity;
import com.sensorable.R;
import com.sensorable.activities.ActivitiesStepsRecorderActivity;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.stream.Collectors;

public class ActivityStepEntityAdapter extends ArrayAdapter<ActivityStepEntity> {
    private final int resource;
    private final Context context;
    private final long idActivity;
    private boolean stepsEnabled;

    final private ArrayList<Integer> clickedArrays = new ArrayList<>();

    public ActivityStepEntityAdapter(@NonNull Context context, int resource, @NonNull ArrayList<ActivityStepEntity> objects, final long idActivity, boolean stepsEnabled) {
        super(context, resource, objects);
        this.context = context;
        this.resource = resource;
        this.stepsEnabled = stepsEnabled;
        this.idActivity = idActivity;

        clickedArrays.addAll(ActivitiesStepsRecorderActivity.StepsTimerRecorder.getClickedStepsByActivityId(idActivity));
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
        LayoutInflater inflater = LayoutInflater.from(context);
        convertView = inflater.inflate(this.resource, parent, false);

        ActivityStepEntity activityStep = this.getItem(position);
        String title = activityStep.getTitle();
        int currentStepId = activityStep.getId();

        Button tagStep = convertView.findViewById(R.id.tagStepButton);
        tagStep.setText(title);

        if (clickedArrays.contains(currentStepId)) {
            tagStep.setBackgroundColor(Color.GRAY);
        }

        tagStep.setOnClickListener(view -> {
            tagStep.setBackgroundColor(Color.GRAY);
            clickedArrays.add(currentStepId);
            ActivitiesStepsRecorderActivity.StepsTimerRecorder.saveTag(idActivity, currentStepId, LoginHelper.getUserCode(context));
        });

        tagStep.setEnabled(areAllItemsEnabled());

        return convertView;
    }
}
