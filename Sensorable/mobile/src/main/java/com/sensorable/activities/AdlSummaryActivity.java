package com.sensorable.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.commons.database.AdlDao;
import com.commons.database.AdlEntity;
import com.commons.database.AdlRegistryDao;
import com.commons.database.AdlRegistryEntity;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.sensorable.MainActivity;
import com.sensorable.R;
import com.sensorable.utils.DetectedAdlInfo;
import com.sensorable.utils.DetectedAdlInfoAdapter;
import com.sensorable.utils.MobileDatabase;
import com.sensorable.utils.MobileDatabaseBuilder;

import java.util.ArrayList;
import java.util.concurrent.ExecutorService;

public class AdlSummaryActivity extends AppCompatActivity {
    private ListView detectedAdlList;
    private ArrayList<DetectedAdlInfo> adlArray;
    private DetectedAdlInfoAdapter detectedAdlInfoAdapter;
    private ExecutorService executor;
    private AdlRegistryDao adlRegistryDao;
    private AdlDao adlDao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_adl_summary);

        initializeMobileDatabase();
        initializeAttributesFromUI();
    }

    private void initializeMobileDatabase() {
        MobileDatabase database = MobileDatabaseBuilder.getDatabase(this);
        adlRegistryDao = database.adlRegistryDao();
        adlDao = database.adlDao();
        executor = MobileDatabaseBuilder.getExecutor();

        Log.i("DETECTED_ADL", "initialized mobile database");
    }

    private void updateDetectedAdlsFromDatabase() {
        executor.execute(() -> {
            ArrayList<DetectedAdlInfo> adlArrayCopy = new ArrayList<>();

            for (AdlRegistryEntity adlRegistry : adlRegistryDao.getAll()) {
                AdlEntity newAdl = adlDao.getAdlById(adlRegistry.idAdl);
                adlArrayCopy.add(
                        new DetectedAdlInfo(
                                adlRegistry.idAdl,
                                newAdl.title,
                                newAdl.description,
                                adlRegistry.startTime,
                                adlRegistry.endTime,
                                false
                        ));
            }

            Log.i("DETECTED_ADL", "updated from database");

            runOnUiThread(() -> {
                adlArray.clear();
                adlArray.addAll(adlArrayCopy);
                detectedAdlInfoAdapter.notifyDataSetChanged();
            });
        });
    }

    private void initializeAttributesFromUI() {
        BottomNavigationView bottomNavigation = findViewById(R.id.bottom_navigation);
        bottomNavigation.setSelectedItemId(R.id.tab_adls);
        bottomNavigation.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();
                switch (id) {
                    case R.id.tab_activities_recorder:
                        startActivity(
                                new Intent(AdlSummaryActivity.this, ActivitiesRegisterActivity.class)
                        );
                        overridePendingTransition(0, 0);
                        return true;


                    case R.id.tab_locations:
                        startActivity(
                                new Intent(AdlSummaryActivity.this, LocationOptionsActivity.class)
                        );
                        overridePendingTransition(0, 0);
                        return true;

                    case R.id.tab_home:
                        startActivity(
                                new Intent(AdlSummaryActivity.this, MainActivity.class)
                        );
                        overridePendingTransition(0, 0);

                        return true;

                    case R.id.tab_charts:

                        startActivity(
                                new Intent(AdlSummaryActivity.this, DetailedSensorsListActivity.class)
                        );
                        overridePendingTransition(0, 0);
                        return true;

                }

                return true;
            }
        });

        detectedAdlList = findViewById(R.id.detectedAdlsList);
        detectedAdlList.setDivider(null);
        adlArray = new ArrayList<>();

        detectedAdlInfoAdapter = new DetectedAdlInfoAdapter(this, R.layout.detected_adl_layout, adlArray);
        detectedAdlInfoAdapter.setNotifyOnChange(true);
        detectedAdlList.setAdapter(detectedAdlInfoAdapter);

        updateDetectedAdlsFromDatabase();
    }
}