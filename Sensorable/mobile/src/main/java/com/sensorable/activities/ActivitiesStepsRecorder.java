package com.sensorable.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.GridView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.commons.SensorableConstants;
import com.sensorable.R;
import com.sensorable.utils.StepsRecord;
import com.sensorable.utils.StepsRecorderAdapter;

import java.util.ArrayList;

public class ActivitiesStepsRecorder extends AppCompatActivity {
    private GridView steps;
    private Button startButton;
    private Button stopButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_activities_steps_recorder);

        steps = findViewById(R.id.stepsGrid);
        startButton = findViewById(R.id.startActivity);
        stopButton = findViewById(R.id.stopActivity);

        startButton.setOnClickListener(view -> {
            startButton.setVisibility(View.GONE);
            stopButton.setVisibility(View.VISIBLE);
        });

        stopButton.setOnClickListener(view -> {
            stopButton.setVisibility(View.GONE);
            startButton.setVisibility(View.VISIBLE);
        });

        ArrayList<StepsRecord> activitiesArray = new ArrayList<>();

        // TODO: Use activity id to get the steps from the database
        if (savedInstanceState == null && getIntent().getExtras() != null) {
            long activityId = getIntent().getLongExtra(SensorableConstants.ACTIVITY_ID, 0);
            Toast.makeText(this, "received " + activityId, Toast.LENGTH_SHORT).show();
        }

        activitiesArray.add(new StepsRecord(0, "brazo derecho"));
        activitiesArray.add(new StepsRecord(1, "brazo izquierdo"));
        activitiesArray.add(new StepsRecord(2, "abrochar primer botón"));
        activitiesArray.add(new StepsRecord(3, "abrochar segundo botón"));


        StepsRecorderAdapter activitiesRecordAdapter = new StepsRecorderAdapter(this, R.layout.step_record_layout, activitiesArray);
        activitiesRecordAdapter.setNotifyOnChange(true);
        steps.setAdapter(activitiesRecordAdapter);

    }
}