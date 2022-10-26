package com.sensorable.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.commons.SensorableConstants;
import com.sensorable.R;
import com.sensorable.utils.ActivitiesRecord;
import com.sensorable.utils.ActivitiesRecordAdapter;

import java.util.ArrayList;

public class ActivitiesRegisterActivity extends AppCompatActivity {
    private ListView activitiesToRecord;
    private ArrayList<ActivitiesRecord> activitiesArray;
    private ActivitiesRecordAdapter activitiesRecordAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_activities_register);

        activitiesToRecord = findViewById(R.id.activitiesToRecord);
        activitiesToRecord.setDivider(null);
        activitiesArray = new ArrayList<>();

        activitiesArray.add(new ActivitiesRecord(0, "Vestir camisa", "Vestir una camisa con 5 más de 2 botones y una chaquetea"));
        activitiesArray.add(new ActivitiesRecord(1, "Ponerse los zapatos", "Ponerse zapatos con cordones, atarlos y demás."));
        activitiesArray.add(new ActivitiesRecord(2, "Vestir bata", "Vestir una bata ancha y cómoda con solo 2 botones"));


        activitiesRecordAdapter = new ActivitiesRecordAdapter(this, R.layout.activities_record_layout, activitiesArray);
        activitiesRecordAdapter.setNotifyOnChange(true);
        activitiesToRecord.setAdapter(activitiesRecordAdapter);

        activitiesToRecord.setOnItemClickListener((adapterView, view, i, id) -> {
                    Toast.makeText(ActivitiesRegisterActivity.this, "my position " + i + " id " + id, Toast.LENGTH_LONG).show();
                    startActivity(
                            new Intent(this, ActivitiesStepsRecorder.class)
                                    .putExtra(SensorableConstants.ACTIVITY_ID, id)
                    );
                }
        );
    }
}