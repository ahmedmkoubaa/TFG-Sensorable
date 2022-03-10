package com.sensorable.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.commons.SensorsProvider;
import com.google.android.material.textfield.TextInputEditText;
import com.sensorable.R;

import org.osmdroid.events.MapEventsReceiver;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapController;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.MapEventsOverlay;
import org.osmdroid.views.overlay.Marker;

public class AddLocationActivity extends AppCompatActivity {

    private TextInputEditText locationTitle;
    private TextInputEditText locationAddress;
    private TextInputEditText locationTags;

    private MapController mapController;
    private MapView map;
    private Marker mapMarker;
    private Button saveButton;

    private TextView infoError;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_location);

        initializeFieldsFromUI();
        initializeMap();
    }

    private void initializeFieldsFromUI() {
        locationTitle = (TextInputEditText) findViewById(R.id.locationTitleInput);
        locationAddress = (TextInputEditText) findViewById(R.id.locationAddressInput);
        locationTags = (TextInputEditText) findViewById(R.id.locationTagInput);

        saveButton = (Button) findViewById(R.id.saveButton);
        saveButton.setOnClickListener(v-> {
            Toast.makeText(this, "AÑADIDO NUEVO PUNTO", Toast.LENGTH_SHORT).show();

            if (mapMarker.getPosition() != null) {
                GeoPoint p = mapMarker.getPosition();

                setResult(
                        Activity.RESULT_OK,
                        new Intent().
                                putExtra("title", locationTitle.getText().toString()).
                                putExtra("address", locationAddress.getText().toString()).
                                putExtra("tag", locationTags.getText().toString()).
                                putExtra("latitude", "" + p.getLatitude()).
                                putExtra("longitude", "" + p.getLongitude()).
                                putExtra("altitude", "" + p.getAltitude())
                );

                finish();
            } else {
                infoError.setText("POR FAVOR MARCA UN PUNTO DEL MAPA");
                infoError.setVisibility(View.VISIBLE);
//                Toast.makeText(this, "POR FAVOR INDICA UN PUNTO DEL MAPA", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void initializeMap() {
        map = (MapView) findViewById(R.id.mapAdd);
        map.setBuiltInZoomControls(true);
        map.setMultiTouchControls(true);

        initializeMapController();
        initializeMapEvents();
        initializeMarker();
    }

    private void initializeMapEvents() {
        if (map != null) {
            MapEventsReceiver mReceive = new MapEventsReceiver()
            {
                @Override
                public boolean singleTapConfirmedHelper(GeoPoint p)
                {
                    return false;
                }

                @Override
                public boolean longPressHelper(GeoPoint p)
                {
                    setMarker(p);
                    mapMarker.setTitle(locationTitle.getText().toString());
                    mapMarker.setSubDescription(locationAddress.getText().toString());
                    return true;
                }
            };
            MapEventsOverlay evOverlay = new MapEventsOverlay(mReceive);
            map.getOverlays().add(evOverlay);
        } else {
            throw new Error("Using non initialied map in: initializeMapEvents()");
        }
    }

    private void setMarker(GeoPoint point) {
        // this is how to display a position
        mapMarker.setPosition(point);
        mapMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        map.getOverlays().add(mapMarker);
        mapController.setZoom(16);
        mapController.setCenter(point);
    }

    private void initializeMarker() {
        if (map != null) {
            mapMarker = new Marker(map);
        } else {
            throw new Error("Using non initialied map in: initializeMarker()");
        }
    }

    private void initializeMapController() {
        if (map != null) {
            GeoPoint currentPoint = new GeoPoint(37.18817, -3.60667);
            SensorsProvider provider = new SensorsProvider(this);
            mapController = (MapController) map.getController();
            mapController.setZoom(10);
            mapController.setCenter(currentPoint);

            provider.subscribeToGps(new LocationListener() {
                @Override
                public void onLocationChanged(@NonNull Location location) {
                    if (mapController != null && location != null) {
//                        mapController.setCenter(
//                                new GeoPoint(
//                                       location
//                                )
//                        );
                    }
                }

                @Override
                public void onProviderEnabled(@NonNull String provider) {

                }

                @Override
                public void onProviderDisabled(@NonNull String provider) {

                }

                @Override
                public void onStatusChanged(String provider, int status, Bundle extras) {

                }
            });

        } else {
            throw new Error("Using non initialied map in: initializeMapController()");
        }
    }

}