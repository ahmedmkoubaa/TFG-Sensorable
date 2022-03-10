package com.sensorable.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.ListView;

import com.sensorable.utils.DetectedAdl;
import com.sensorable.utils.DetectedAdlsAdapter;
import com.sensorable.R;

import java.util.ArrayList;
import java.util.Date;

public class AdlSummaryActivity extends AppCompatActivity {

    private ListView detectedAdlList;
    private ArrayList<DetectedAdl> adlArray;
    private DetectedAdlsAdapter detectedAdlsAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_adl_summary);

        initializeAttributesFromUI();
    }

    private void initializeAttributesFromUI() {
        detectedAdlList = (ListView) findViewById(R.id.detectedAdlsList);
        detectedAdlList.setDivider(null);
        adlArray = new ArrayList<>();
        adlArray.add(new DetectedAdl(
                "DESPLAZAMIENTO A LA FARMACIA",
                "Fuiste a la farmacia manoli la cual se encuentra ubicada en la calle alta de cartuja 25",
                "duracion: 12m, distancia: 1km, velocidad: 1km/h",
                new Date()))
        ;

        adlArray.add(new DetectedAdl(
                "DESPLAZAMIENTO AL TRABAJO",
                "Fuiste a la farmacia manoli la cual se encuentra ubicada en la calle alta de cartuja 25",
                "duracion: 12m, distancia: 1km, velocidad: 1km/h",
                new Date()))
        ;

        adlArray.add(new DetectedAdl(
                "DESPLAZAMIENTO A FAMILIARES",
                "Fuiste a la farmacia manoli la cual se encuentra ubicada en la calle alta de cartuja 25",
                "duracion: 12m, distancia: 1km, velocidad: 1km/h",
                new Date()))
        ;

        detectedAdlsAdapter = new DetectedAdlsAdapter(this, R.layout.detected_adl_layout, adlArray);
        detectedAdlsAdapter.setNotifyOnChange(true);
        detectedAdlList.setAdapter(detectedAdlsAdapter);

        /*



        knownLocationsAdapter = new KnownLocationsAdapter(this, R.layout.known_location_layout, locArray);
        knownLocationsList.setAdapter(knownLocationsAdapter);
        * */

    }
}