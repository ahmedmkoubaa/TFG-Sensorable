package com.sensorable.activities;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.GridView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.commons.LoginHelper;
import com.commons.SensorableConstants;
import com.commons.database.ActivityStepDao;
import com.commons.database.ActivityStepEntity;
import com.commons.database.StepsForActivitiesDao;
import com.commons.database.StepsForActivitiesRegistryDao;
import com.sensorable.R;
import com.sensorable.utils.ActivityStepEntityAdapter;
import com.sensorable.utils.MobileDatabase;
import com.sensorable.utils.MobileDatabaseBuilder;
import com.sensorable.utils.MqttHelper;

import java.util.ArrayList;
import java.util.concurrent.ExecutorService;

import static com.sensorable.utils.StepsTimerRecorder.startRecordingSteps;
import static com.sensorable.utils.StepsTimerRecorder.stopRecordingSteps;

public class ActivitiesStepsRecorderActivity extends AppCompatActivity {
    private GridView stepsGrid;
    private Button startButton;
    private Button stopButton;
    private boolean activityStarted = false;
    private ActivityStepEntityAdapter activityStepsAdapter;
    private ArrayList<ActivityStepEntity> stepsArray;
    private ActivityStepDao stepsDao;
    private ExecutorService executor;
    private StepsForActivitiesDao stepsForActivitiesDao;
    private StepsForActivitiesRegistryDao stepsForActivitiesRegistryDao;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_activities_steps_recorder);

        initializeMobileDatabase();

        if (savedInstanceState == null && getIntent().getExtras() != null) {
            final long activityId = getIntent().getIntExtra(SensorableConstants.ACTIVITY_ID, -1);
            initializeStepsToRecord(activityId);

            executor.execute(() -> {
                stepsArray.addAll(stepsDao.getStepsOfActivity(activityId));
                runOnUiThread(() -> activityStepsAdapter.notifyDataSetChanged());
            });
        } else {
            Log.i("ACTIVITIES STEPS RECORDER", "Didn't receive the activity ID");
        }
    }

    private void initializeStepsToRecord(final long activityId) {
        stepsGrid = findViewById(R.id.stepsGrid);
        startButton = findViewById(R.id.startActivity);
        stopButton = findViewById(R.id.stopActivity);

        stepsArray = new ArrayList<>();

        activityStepsAdapter =
                new ActivityStepEntityAdapter(this, R.layout.step_record_layout, stepsArray, activityId, activityStarted);
        activityStepsAdapter.setNotifyOnChange(true);
        stepsGrid.setAdapter(activityStepsAdapter);

        startButton.setOnClickListener(view -> {
            startButton.setVisibility(View.GONE);
            stopButton.setVisibility(View.VISIBLE);

            activityStepsAdapter.setStepsEnabled(activityStarted = true);
            activityStepsAdapter.notifyDataSetChanged();
            startRecordingSteps(activityId, LoginHelper.getUserCode(getApplicationContext()));

        });

        stopButton.setOnClickListener(view -> {
            stopRecordingSteps(activityId,  LoginHelper.getUserCode(getApplicationContext()));
            executor.execute(() -> {
                // generate the json data structure
                // in lambda expression we can only use final, we need to modify the paylod so this is the reason
                // we use a final array modifying the first element
                final String[] payload = {"["};

                stepsForActivitiesRegistryDao.getAll().forEach(registry -> payload[0] += registry.toJson() + ",");
                payload[0] = payload[0].substring(0, payload[0].length() - 1) + "]";

                // send data via MQTT
                MqttHelper.publish(SensorableConstants.MQTT_ACTIVITIES_INSERT, payload[0].getBytes())
                        .thenAccept(accept -> stepsForActivitiesRegistryDao.deleteAll());
            });

            finish();
        });
    }

    // initialize data structures from the database
    private void initializeMobileDatabase() {
        MobileDatabase database = MobileDatabaseBuilder.getDatabase(this);

        stepsDao = database.activityStepDao();
        stepsForActivitiesDao = database.stepsForActivitiesDao();
        stepsForActivitiesRegistryDao = database.stepsForActivitiesRegistryDao();
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