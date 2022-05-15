package com.sensorable.activities;

import android.os.Bundle;
import android.util.Log;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;

import com.commons.database.AdlDao;
import com.commons.database.AdlEntity;
import com.commons.database.AdlRegistryDao;
import com.commons.database.AdlRegistryEntity;
import com.commons.database.DetectedAdlEntity;
import com.sensorable.R;
import com.sensorable.utils.DetectedAdlsAdapter;
import com.sensorable.utils.MobileDatabaseBuilder;
import com.sensorable.utils.SensorableDates;

import java.util.ArrayList;
import java.util.concurrent.ExecutorService;

public class AdlSummaryActivity extends AppCompatActivity {
    private ListView detectedAdlList;
    private ArrayList<DetectedAdlEntity> adlArray;
    private DetectedAdlsAdapter detectedAdlsAdapter;
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
        adlRegistryDao = MobileDatabaseBuilder.getDatabase(this).adlRegistryDao();
        adlDao = MobileDatabaseBuilder.getDatabase(this).adlDao();
        executor = MobileDatabaseBuilder.getExecutor();

        Log.i("DETECTED_ADL", "initialized mobile database");
    }

    private void updateDetectedAdlsFromDatabase() {
        executor.execute(() -> {
            adlArray.clear();
            for (AdlRegistryEntity adlRegistry : adlRegistryDao.getAll()) {
                AdlEntity newAdl = adlDao.getAdlById(adlRegistry.idAdl);
                adlArray.add(
                        new DetectedAdlEntity(
                                newAdl.title,
                                newAdl.description,
                                "from " + SensorableDates.timestampToDate(adlRegistry.startTime) + " to " + SensorableDates.timestampToDate(adlRegistry.endTime),
                                adlRegistry.startTime,
                                adlRegistry.endTime,
                                false
                        ));

            }


            runOnUiThread(() -> detectedAdlsAdapter.notifyDataSetChanged());

            Log.i("DETECTED_ADL", "updated from database");
        });
    }

    private void initializeAttributesFromUI() {
        detectedAdlList = findViewById(R.id.detectedAdlsList);
        detectedAdlList.setDivider(null);
        adlArray = new ArrayList<>();

        detectedAdlsAdapter = new DetectedAdlsAdapter(this, R.layout.detected_adl_layout, adlArray);
        detectedAdlsAdapter.setNotifyOnChange(true);
        detectedAdlList.setAdapter(detectedAdlsAdapter);

        updateDetectedAdlsFromDatabase();
    }
}