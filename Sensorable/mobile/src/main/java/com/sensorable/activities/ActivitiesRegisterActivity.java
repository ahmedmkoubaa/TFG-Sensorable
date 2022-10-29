package com.sensorable.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.commons.SensorableConstants;
import com.commons.database.ActivityDao;
import com.commons.database.ActivityEntity;
import com.sensorable.R;
import com.sensorable.utils.ActivityEntityAdapter;
import com.sensorable.utils.MobileDatabase;
import com.sensorable.utils.MobileDatabaseBuilder;

import java.util.ArrayList;
import java.util.concurrent.ExecutorService;

public class ActivitiesRegisterActivity extends AppCompatActivity {

    private ListView activitiesToRecord;
    private ArrayList<ActivityEntity> activitiesArray;
    private ActivityEntityAdapter activityEntityAdapter;
    private ActivityDao activityDao;
    private ExecutorService executor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_activities_register);

        initializeMobileDatabase();
        initializeActivitiesToRecord();


        executor.execute(() -> {
            activitiesArray.addAll(activityDao.getAll());
            runOnUiThread(() -> {
                activityEntityAdapter.notifyDataSetChanged();
                activitiesToRecord.setOnItemClickListener((adapterView, view, i, id) -> {
                            Toast.makeText(ActivitiesRegisterActivity.this, "my position " + i + " id " + id, Toast.LENGTH_LONG).show();
                            startActivity(
                                    new Intent(this, ActivitiesStepsRecorderActivity.class)
                                            .putExtra(SensorableConstants.ACTIVITY_ID, activitiesArray.get(i).id)
                            );
                        }
                );
            });
        });
    }

    private void initializeActivitiesToRecord() {
        activitiesToRecord = findViewById(R.id.activitiesToRecord);
        activitiesToRecord.setDivider(null);
        activitiesArray = new ArrayList<>();
        activityEntityAdapter = new ActivityEntityAdapter(this, R.layout.activities_record_layout, activitiesArray);
        activityEntityAdapter.setNotifyOnChange(true);
        activitiesToRecord.setAdapter(activityEntityAdapter);

    }

    // initialize data structures from the database
    private void initializeMobileDatabase() {
        MobileDatabase database = MobileDatabaseBuilder.getDatabase(this);

        activityDao = database.activityDao();
        executor = MobileDatabaseBuilder.getExecutor();
    }
}