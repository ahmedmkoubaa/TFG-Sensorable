package com.sensorable.activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.commons.SensorablePermissions;
import com.commons.SensorsProvider;
import com.commons.database.KnownLocationDao;
import com.commons.database.KnownLocationEntity;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationBarView;
import com.sensorable.MainActivity;
import com.sensorable.R;
import com.sensorable.utils.KnownLocationsAdapter;
import com.sensorable.utils.MobileDatabase;
import com.sensorable.utils.MobileDatabaseBuilder;

import org.osmdroid.config.Configuration;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapController;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;

import java.util.ArrayList;
import java.util.concurrent.ExecutorService;

public class LocationOptionsActivity extends AppCompatActivity {
    private boolean isMarkedCurrentLocation;
    private MapController mapController;
    private MapView map;

    private ListView knownLocationsList;

    private Button listButton;
    private Button mapButton;

    private FloatingActionButton addLocationButton;
    private ArrayList<KnownLocationEntity> locArray;
    private KnownLocationsAdapter knownLocationsAdapter;

    private ActivityResultLauncher<Intent> activityLauncher;
    private KnownLocationDao knownLocationDao;
    private ExecutorService executorService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SensorablePermissions.requestAll(this);

        // TODO remove this, test it and find out if it's necessary
        //load/initialize the osmdroid configuration, this can be done
        Context ctx = getApplicationContext();
        Configuration.getInstance().load(ctx, PreferenceManager.getDefaultSharedPreferences(ctx));

        //inflate and create the map
        setContentView(R.layout.activity_location_options);

        initializeAttributesFromUI();

        initializeMobileDatabase();

        initializeMap();

        initializeKnownLocationList();

        initializeSwitchButtons();

        initializeAddLocationButton();

        initializeActivityLauncher();
    }

    private void initializeAttributesFromUI() {
        BottomNavigationView bottomNavigation = (BottomNavigationView) findViewById(R.id.bottom_navigation);
        bottomNavigation.setSelectedItemId(R.id.tab_locations);
        bottomNavigation.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();
                switch (id) {
                    case R.id.tab_bluetooth:
                        startActivity(
                                new Intent(LocationOptionsActivity.this, BluetoothOptionsActivity.class)
                        );
                        overridePendingTransition(0, 0);
                        finish();

                        return true;

                    case R.id.tab_adls:
                        startActivity(
                                new Intent(LocationOptionsActivity.this, AdlSummaryActivity.class)
                        );
                        overridePendingTransition(0, 0);
                        finish();

                        return true;

                    case R.id.tab_home:
                        startActivity(
                                new Intent(LocationOptionsActivity.this, MainActivity.class)
                        );
                        overridePendingTransition(0, 0);
                        finish();
                        return true;

                    case R.id.tab_charts:

                        startActivity(
                                new Intent(LocationOptionsActivity.this, DetailedSensorsListActivity.class)
                        );
                        overridePendingTransition(0, 0);
                        finish();
                        return true;
                }

                return true;
            }
        });
    }

    private void initializeMobileDatabase() {
        MobileDatabase database = MobileDatabaseBuilder.getDatabase(this);
        knownLocationDao = database.knownLocationDao();
        executorService = MobileDatabaseBuilder.getExecutor();
    }

    private void initializeActivityLauncher() {
        // You can do the assignment inside onAttach or onCreate, i.e, before the activity is displayed
        activityLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        Log.i("KNOWN_LOCATION_RESULT", "Received a new result");

                        if (result.getResultCode() == Activity.RESULT_OK) {
                            updateLocationsFromDatabase();
                            Log.i("KNOWN_LOCATION_RESULT", "Upadting bro");
                        }
                    }
                });
    }

    private void initializeAddLocationButton() {
        addLocationButton = findViewById(R.id.addLocationButton);
        addLocationButton.setOnClickListener(v -> {
            openAddLocationActivity();
        });
    }

    public void openAddLocationActivity() {
        Intent intent = new Intent(this, AddLocationActivity.class);
        activityLauncher.launch(intent);
    }

    private void initializeSwitchButtons() {
        listButton = findViewById(R.id.listButton);

        listButton.setOnClickListener(View -> {
            knownLocationsList.setVisibility(android.view.View.VISIBLE);
            map.setVisibility(android.view.View.GONE);
            listButton.setBackgroundColor(Color.GREEN);
            mapButton.setBackgroundColor(Color.RED);
        });

        mapButton = findViewById(R.id.mapButton);
        mapButton.setOnClickListener(view -> {
            knownLocationsList.setVisibility(View.GONE);
            map.setVisibility(View.VISIBLE);
            listButton.setBackgroundColor(Color.RED);
            mapButton.setBackgroundColor(Color.GREEN);
        });

        map.setVisibility(View.GONE);
        knownLocationsList.setVisibility(View.VISIBLE);
    }

    private void updateLocationsFromDatabase() {
        executorService.execute(() -> {
            locArray.clear();
            locArray.addAll(knownLocationDao.getAll());

            Log.i("KNOWN_LOCATION_ENTITY", "Query has been made succesfully, no errors");


            for (KnownLocationEntity k : locArray) {
                setMarker(new GeoPoint( k.latitude,  k.longitude,  k.altitude), k.title, k.address);
            }
        });

        knownLocationsAdapter.notifyDataSetChanged();
    }

    private void initializeKnownLocationList() {
        knownLocationsList = findViewById(R.id.knownLocationsList);
        locArray = new ArrayList<>();
        knownLocationsAdapter = new KnownLocationsAdapter(this, R.layout.known_location_layout, locArray);
        knownLocationsList.setAdapter(knownLocationsAdapter);
        knownLocationsAdapter.setNotifyOnChange(true);

        updateLocationsFromDatabase();
    }

    private void initializeMapController() {
        if (map != null) {
            SensorsProvider sensor = new SensorsProvider(this);
            sensor.subscribeToGps(new LocationListener() {
                @Override
                public void onProviderEnabled(@NonNull String provider) {
                    Toast.makeText(LocationOptionsActivity.this, "Provider is enabled", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onProviderDisabled(@NonNull String provider) {
                    Toast.makeText(LocationOptionsActivity.this, "Provider is disabled", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onStatusChanged(String provider, int status, Bundle extras) {
                }

                @Override
                public void onLocationChanged(@NonNull Location location) {
                    if (mapController != null && !isMarkedCurrentLocation) {
                        GeoPoint g = new GeoPoint(location);
                        mapController.setCenter(g);
                        mapController.setZoom(16);
                        isMarkedCurrentLocation = true;
                        setMarker(g);

                        Log.i("KNOWN_LOCATION", "Set current location because update");
                    }
                }
            });
            mapController = (MapController) map.getController();
        } else {
            throw new Error("Using non initialied map in: initializeMapController()");
        }
    }

    private void initializeMap() {
        map = findViewById(R.id.map);
        map.setBuiltInZoomControls(true);
        map.setMultiTouchControls(true);
        isMarkedCurrentLocation = false;

        initializeMapController();
    }

    private void setMarker(GeoPoint point) {
        setMarker(point, "", "");
    }
    private void setMarker(GeoPoint point, String title, String description) {
        // this is how to display a position
        Marker marker = new Marker(map);
        marker.setPosition(point);
        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        marker.setTitle(title);
        marker.setSubDescription(description);
        map.getOverlays().add(marker);

    }
}