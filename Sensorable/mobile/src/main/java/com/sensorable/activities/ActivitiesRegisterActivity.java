package com.sensorable.activities;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.commons.SensorableConstants;
import com.commons.database.ActivityDao;
import com.commons.database.ActivityEntity;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.sensorable.MainActivity;
import com.sensorable.R;
import com.sensorable.utils.ActivityEntityAdapter;
import com.commons.LoginHelper;
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
    private EditText userCode;
    private Button modifyUserCode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_activities_register);

        intiializeAttributesFromUI();

        initializeMobileDatabase();
        initializeActivitiesToRecord();


        executor.execute(() -> {
            activitiesArray.addAll(activityDao.getAll());
            runOnUiThread(() -> {
                activityEntityAdapter.notifyDataSetChanged();
                activitiesToRecord.setOnItemClickListener((adapterView, view, i, id) -> {
                            startActivity(
                                    new Intent(this, ActivitiesStepsRecorderActivity.class)
                                            .putExtra(SensorableConstants.ACTIVITY_ID, activitiesArray.get(i).id)
                            );
                        }
                );
            });
        });
    }

    private void intiializeAttributesFromUI() {
        userCode = (EditText) findViewById(R.id.userCodeForActivityRegistry);
        modifyUserCode = (Button) findViewById(R.id.modifyUserCode);

        String code = LoginHelper.getUserCode(this);

        if (code == null) {
            userCode.setHint("NO HAY CÓDIGO REGISTRADO");
            userCode.setHintTextColor(Color.RED);
            userCode.setText("");
        } else {
            userCode.setText(code);
            modifyUserCode.setOnClickListener(view -> {
               if (LoginHelper.validateUserCode(code)) {
                   LoginHelper.saveLogin(getApplicationContext(), userCode.getText().toString());
                   Toast.makeText(this, "Código guardado correctamente", Toast.LENGTH_SHORT).show();
               }
            });
        }
    }


    @Override
    protected void onStart() {
        super.onStart();

        BottomNavigationView bottomNavigation = findViewById(R.id.bottom_navigation);
        bottomNavigation.setSelectedItemId(R.id.tab_activities_recorder);
        bottomNavigation.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();
                switch (id) {
//                    case R.id.tab_bluetooth:
//                        startActivity(
//                                new Intent(MainActivity.this, BluetoothOptionsActivity.class)
//                        );
//                        overridePendingTransition(0, 0);
//
//                        return true;

                    case R.id.tab_home:
                        startActivity(
                                new Intent(ActivitiesRegisterActivity.this, MainActivity.class)
                        );
                        overridePendingTransition(0, 0);
                        return true;

                    case R.id.tab_adls:
                        startActivity(
                                new Intent(ActivitiesRegisterActivity.this, AdlSummaryActivity.class)
                        );
                        overridePendingTransition(0, 0);

                        return true;

                    case R.id.tab_locations:
                        startActivity(
                                new Intent(ActivitiesRegisterActivity.this, LocationOptionsActivity.class)
                        );
                        overridePendingTransition(0, 0);
                        return true;

                    case R.id.tab_charts:
                        startActivity(
                                new Intent(ActivitiesRegisterActivity.this, DetailedSensorsListActivity.class)
                        );
                        overridePendingTransition(0, 0);
                        return true;
                }

                return true;
            }
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