package com.sensorable.activities;

import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.GridView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.commons.LoginHelper;
import com.commons.SensorableConstants;
import com.commons.database.ActivityStepDao;
import com.commons.database.ActivityStepEntity;
import com.commons.database.StepsForActivitiesRegistryDao;
import com.commons.database.StepsForActivitiesRegistryEntity;
import com.sensorable.R;
import com.sensorable.utils.ActivityStepEntityAdapter;
import com.sensorable.utils.MobileDatabase;
import com.sensorable.utils.MobileDatabaseBuilder;
import com.sensorable.utils.MqttHelper;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.stream.Collectors;

public class ActivitiesStepsRecorderActivity extends AppCompatActivity {
    private static StepsForActivitiesRegistryDao stepsForActivitiesRegistryDao;
    private static ExecutorService executor;
    private GridView stepsGrid;
    private Button startButton;
    private Button finishButton;
    private boolean activityStarted = false;
    private ActivityStepEntityAdapter activityStepsAdapter;
    private ArrayList<ActivityStepEntity> stepsArray;
    private ActivityStepDao stepsDao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_activities_steps_recorder);

        initializeMobileDatabase();

        // We got here by navigating passing and id of activity to record, we need to make sure we have the sent ID
        if (savedInstanceState == null && getIntent().getExtras() != null) {
            final long activityId = getIntent().getIntExtra(SensorableConstants.ACTIVITY_ID, -1);
            initializeStepsToRecord(activityId);

            executor.execute(() -> {
                if (notEndedActivity() >= 0) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setTitle("¿Continuar?");
                    builder.setMessage("Hay una actividad previa que no se finalizó ¿Desea continuar con ella?");
                    builder.setPositiveButton("Continuar", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            // Do something when the user confirms
                        }
                    });
                    builder.setNegativeButton("Descartar", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            // Do something when the user cancels
                        }
                    });
                    AlertDialog dialog = builder.create();
                    dialog.show();
                }


                stepsArray.addAll(stepsDao.getStepsOfActivity(activityId));
                runOnUiThread(() -> activityStepsAdapter.notifyDataSetChanged());
            });
        } else {
            Log.i("ACTIVITIES STEPS RECORDER", "Didn't receive the activity ID");
        }
    }

    // It searches in the database if there is any began activity that wasn't yet finished
    // if there is any it returns its Id, in the other case it returns the
    private Long notEndedActivity() {
        // get all remaining initial and ending steps
        final List<StepsForActivitiesRegistryEntity> activities =
                StepsTimerRecorder.getAll().
                        stream().filter(s -> s.idStep < 0) // both startId and finishId are negative
                        .collect(Collectors.toList());

        for (int i = 0; i < activities.size() - 1; i++) {
            if (activities.get(i).idStep == StepsTimerRecorder.startId
                    && activities.get(i + 1).idStep != StepsTimerRecorder.stopId) {
                return activities.get(i).idActivity;
            }
        }

        return -1L;
    }


    private void initializeStepsToRecord(final long activityId) {
        stepsGrid = findViewById(R.id.stepsGrid);
        startButton = findViewById(R.id.startActivity);
        finishButton = findViewById(R.id.stopActivity);

        stepsArray = new ArrayList<>();

        activityStepsAdapter =
                new ActivityStepEntityAdapter(this, R.layout.step_record_layout, stepsArray, activityId, activityStarted);
        activityStepsAdapter.setNotifyOnChange(true);
        stepsGrid.setAdapter(activityStepsAdapter);

        startButton.setOnClickListener(view -> {
            startButton.setVisibility(View.GONE);
            finishButton.setVisibility(View.VISIBLE);

            activityStepsAdapter.setStepsEnabled(activityStarted = true);
            activityStepsAdapter.notifyDataSetChanged();
            StepsTimerRecorder.startRecordingSteps(activityId, LoginHelper.getUserCode(getApplicationContext()));

        });

        // This button finishes the process of recording and sent data to the remote db bia MQTT
        finishButton.setOnClickListener(view -> {
            StepsTimerRecorder.stopRecordingSteps(activityId, LoginHelper.getUserCode(getApplicationContext()));
            executor.execute(() -> {
                // generate the json data structure
                // in lambda expression we can only use final, we need to modify the paylod so this is the reason
                // we use a final array modifying the first element
                final String[] payload = {"["};

                StepsTimerRecorder.getAll().forEach(registry -> payload[0] += registry.toJson() + ",");
                payload[0] = payload[0].substring(0, payload[0].length() - 1) + "]";

                // send data via MQTT
                MqttHelper.publish(SensorableConstants.MQTT_ACTIVITIES_INSERT, payload[0].getBytes())
                /*.thenAccept(accept -> StepsTimerRecorder.deleteAll())*/;
            });

            finish();
        });
    }

    // initialize data structures from the database
    private void initializeMobileDatabase() {
        MobileDatabase database = MobileDatabaseBuilder.getDatabase(this);

        stepsDao = database.activityStepDao();
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

    public static class StepsTimerRecorder {
        // These are reserved ids
        public static final int startId = -1;
        public static final int stopId = -2;


        public static void startRecordingSteps(final long activityId, final String userCode) {
            saveTag(activityId, startId, userCode);
        }

        public static void stopRecordingSteps(final long activityId, final String userCode) {
            saveTag(activityId, stopId, userCode);
        }

        public static void saveTag(long activityId, int stepId, final String userCode) {
            long timestamp = new Date().getTime();

            // get the id of the relation between activity and step and save it into
            executor.execute(() ->
                    {
                        stepsForActivitiesRegistryDao.insert(
                                new StepsForActivitiesRegistryEntity(
                                        activityId, stepId, timestamp, userCode
                                )
                        );
                    }
            );
        }

        public static List<StepsForActivitiesRegistryEntity> getAll() {
            return stepsForActivitiesRegistryDao.getAll();
        }

        public static void deleteAll() {
/*            stepsForActivitiesRegistryDao.deleteAll();*/
        }
    }

}