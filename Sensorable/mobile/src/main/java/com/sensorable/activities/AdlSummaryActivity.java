package com.sensorable.activities;

import android.os.Bundle;
import android.util.Log;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;

import com.commons.database.DetectedAdlDao;
import com.commons.database.DetectedAdlEntity;
import com.sensorable.R;
import com.sensorable.utils.DetectedAdlsAdapter;
import com.sensorable.utils.MobileDatabaseBuilder;

import java.util.ArrayList;
import java.util.concurrent.ExecutorService;

public class AdlSummaryActivity extends AppCompatActivity {
    private ListView detectedAdlList;
    private ArrayList<DetectedAdlEntity> adlArray;
    private DetectedAdlsAdapter detectedAdlsAdapter;
    private ExecutorService executor;
    private DetectedAdlDao detectedAdlDao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_adl_summary);

        initializeMobileDatabase();
        initializeAttributesFromUI();
    }

    private void initializeMobileDatabase() {
        detectedAdlDao = MobileDatabaseBuilder.getDatabase(this).detectedAdlDao();
        executor = MobileDatabaseBuilder.getExecutor();

        Log.i("DETECTED_ADL", "initialized mobile database");
    }

    private void updateDetectedAdlsFromDatabase() {
        executor.execute(() -> {
            adlArray.clear();
            adlArray.addAll(detectedAdlDao.getAll());
            detectedAdlsAdapter.notifyDataSetChanged();

            Log.i("DETECTED_ADL", "updated from database");
        });
    }

    private void initializeAttributesFromUI() {
        detectedAdlList = (ListView) findViewById(R.id.detectedAdlsList);
        detectedAdlList.setDivider(null);
        adlArray = new ArrayList<>();

        detectedAdlsAdapter = new DetectedAdlsAdapter(this, R.layout.detected_adl_layout, adlArray);
        detectedAdlsAdapter.setNotifyOnChange(true);
        detectedAdlList.setAdapter(detectedAdlsAdapter);

        updateDetectedAdlsFromDatabase();
    }
}