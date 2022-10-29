package com.sensorable.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.GridView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.commons.SensorableConstants;
import com.commons.database.ActivityStepDao;
import com.commons.database.ActivityStepEntity;
import com.commons.database.StepsForActivitiesDao;
import com.commons.database.StepsForActivitiesEntity;
import com.sensorable.R;
import com.sensorable.utils.MobileDatabase;
import com.sensorable.utils.MobileDatabaseBuilder;
import com.sensorable.utils.StepsRecorderAdapter;

import java.util.ArrayList;
import java.util.concurrent.ExecutorService;

import static com.sensorable.utils.StepsTimerRecorder.startRecordinSteps;
import static com.sensorable.utils.StepsTimerRecorder.stopRecordingSteps;

public class ActivitiesStepsRecorderActivity extends AppCompatActivity {
    private GridView stepsGrid;
    private Button startButton;
    private Button stopButton;
    private boolean activityStarted = false;
    private StepsRecorderAdapter activityStepsAdapter;
    private ArrayList<ActivityStepEntity> stepsArray;
    private ActivityStepDao stepsDao;
    private ExecutorService executor;
    private StepsForActivitiesDao stepsForActivitiesDao;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_activities_steps_recorder);

        initializeMobileDatabase();
        initializeStepsToRecord();

        if (savedInstanceState == null && getIntent().getExtras() != null) {
            long activityId = getIntent().getIntExtra(SensorableConstants.ACTIVITY_ID, -1);
            Toast.makeText(this, "received " + activityId, Toast.LENGTH_SHORT).show();


            executor.execute(() -> {
                stepsArray.addAll(stepsDao.getStepsOfActivity(activityId));

//                for (StepsForActivitiesEntity stepForActivity : stepsForActivitiesDao.getAll()) {
//                    if (stepForActivity.idActivity == activityId) {
//                        stepsArray.add(stepsDao.getStepById(stepForActivity.idStep));
//                    }
//                }

                runOnUiThread(() -> activityStepsAdapter.notifyDataSetChanged());
            });

        }
    }

    private void initializeStepsToRecord() {
        stepsGrid = findViewById(R.id.stepsGrid);
        startButton = findViewById(R.id.startActivity);
        stopButton = findViewById(R.id.stopActivity);

        stepsArray = new ArrayList<>();

        activityStepsAdapter =
                new StepsRecorderAdapter(this, R.layout.step_record_layout, stepsArray, activityStarted);
        activityStepsAdapter.setNotifyOnChange(true);
        stepsGrid.setAdapter(activityStepsAdapter);

        startButton.setOnClickListener(view -> {
            startButton.setVisibility(View.GONE);
            stopButton.setVisibility(View.VISIBLE);

            activityStepsAdapter.setStepsEnabled(activityStarted = true);
            activityStepsAdapter.notifyDataSetChanged();
            startRecordinSteps();

        });

        stopButton.setOnClickListener(view -> {
            stopRecordingSteps();
            finish();
        });
    }

    // initialize data structures from the database
    private void initializeMobileDatabase() {
        MobileDatabase database = MobileDatabaseBuilder.getDatabase(this);

        stepsDao = database.activityStepDao();
        stepsForActivitiesDao = database.stepsForActivitiesDao();
        executor = MobileDatabaseBuilder.getExecutor();

    }

    @Override
    public void onBackPressed() {
        if (!activityStarted) {
            super.onBackPressed();
        } else {
            Toast.makeText(this, "Finaliza la actividad para salir", Toast.LENGTH_SHORT).show();
        }
    }
}