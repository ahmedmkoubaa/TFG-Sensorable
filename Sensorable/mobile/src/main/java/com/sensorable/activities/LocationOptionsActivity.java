package com.sensorable.activities;


import android.Manifest;
import android.app.Activity;
import android.content.Context;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;


import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.sensorable.utils.KnownLocation;
import com.sensorable.utils.KnownLocationsAdapter;
import com.sensorable.R;

import org.osmdroid.config.Configuration;

import org.osmdroid.events.MapEventsReceiver;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapController;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.*;

import java.util.ArrayList;

public class LocationOptionsActivity extends AppCompatActivity {
    private static final String[] SENSOR_PERMISSIONS = {
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.INTERNET,
            Manifest.permission.BODY_SENSORS,
            Manifest.permission.ACTIVITY_RECOGNITION,
            Manifest.permission.ACCESS_NETWORK_STATE,
            Manifest.permission.ACCESS_WIFI_STATE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    private static final int LOCATION_REQ_CODE = 1;


    private MapController mapController;
    private MapView map;

    private ListView knownLocationsList;

    private Button listButton;
    private Button mapButton;

    private FloatingActionButton addLocationButton;
    private ArrayList<KnownLocation> locArray;
    private ArrayAdapter knownLocationsAdapter;

    private ActivityResultLauncher<Intent> activityLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location_options);

        requestPermissionsAndInform(true);

        //load/initialize the osmdroid configuration, this can be done
        Context ctx = getApplicationContext();
        Configuration.getInstance().load(ctx, PreferenceManager.getDefaultSharedPreferences(ctx));
        //setting this before the layout is inflated is a good idea
        //it 'should' ensure that the map has a writable location for the map cache, even without permissions
        //if no tiles are displayed, you can try overriding the cache path using Configuration.getInstance().setCachePath
        //see also StorageUtils
        //note, the load method also sets the HTTP User Agent to your application's package name, abusing osm's tile servers will get you banned based on this string

        //inflate and create the map
        setContentView(R.layout.activity_location_options);
        initializeMap();

        initializeKnownLocationList();

        initializeSwitchButtons();

        initializeAddLocationButton();

        initializeActivityLauncher();

    }

    private void initializeActivityLauncher() {
        // You can do the assignment inside onAttach or onCreate, i.e, before the activity is displayed
        activityLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        if (result.getResultCode() == Activity.RESULT_OK) {
                            // There are no request codes
                            Intent data = result.getData();

                            String title, address, tag;
                            title = data.getStringExtra("title");
                            address = data.getStringExtra("address");
                            tag = data.getStringExtra("tag");

                            String latitude = data.getStringExtra("latitude");
                            String longitude = data.getStringExtra("longitude");
                            String altitude = data.getStringExtra("latitude");

                            GeoPoint point = new GeoPoint(Float.parseFloat(latitude), Float.parseFloat(longitude), Float.parseFloat(altitude));


                            knownLocationsAdapter.add(new KnownLocation(title, address, point, tag));
                            knownLocationsAdapter.setNotifyOnChange(true);

                            for (KnownLocation k: locArray) {
                                setMarker(k.getPoint());
                            }

                        }
                    }
                });
    }

    private void initializeAddLocationButton() {
        addLocationButton = (FloatingActionButton) findViewById(R.id.addLocationButton);
        addLocationButton.setOnClickListener(v-> {
            openAddLocationActivity();
        });
    }



    public void openAddLocationActivity() {
        Intent intent = new Intent(this, AddLocationActivity.class);
        activityLauncher.launch(intent);
    }

    private void initializeSwitchButtons() {
        listButton = (Button) findViewById(R.id.listButton);

        listButton.setOnClickListener(View -> {
            Toast.makeText(this, "selected list", Toast.LENGTH_SHORT).show();
            knownLocationsList.setVisibility(View.VISIBLE);
            map.setVisibility(View.GONE);
            listButton.setBackgroundColor(Color.GREEN);
            mapButton.setBackgroundColor(Color.RED);
        });

        mapButton = (Button) findViewById(R.id.mapButton);
        mapButton.setOnClickListener(view -> {
            Toast.makeText(this, "selected map", Toast.LENGTH_SHORT).show();
            knownLocationsList.setVisibility(View.GONE);
            map.setVisibility(View.VISIBLE);
            listButton.setBackgroundColor(Color.RED);
            mapButton.setBackgroundColor(Color.GREEN);
        });

        map.setVisibility(View.GONE);
        knownLocationsList.setVisibility(View.VISIBLE);
    }

    private void initializeKnownLocationList() {

        knownLocationsList = (ListView) findViewById(R.id.knownLocationsList);
        locArray = new ArrayList<>();
        locArray.add(new KnownLocation("casa de tia pepi", "avenida de la victoria, 43, 3, C, 18013", new GeoPoint(37,0), "FAMILIA"));
        locArray.add(new KnownLocation("casa de tia pepi", "avenida de la victoria, 43, 3, C, 18013", new GeoPoint(37,0), "FAMILIA"));
        locArray.add(new KnownLocation("casa de tia pepi", "avenida de la victoria, 43, 3, C, 18013", new GeoPoint(37,0), "FAMILIA"));
        locArray.add(new KnownLocation("casa de tia pepi", "avenida de la victoria, 43, 3, C, 18013", new GeoPoint(37,0), "FAMILIA"));
        locArray.add(new KnownLocation("casa de tia pepi", "avenida de la victoria, 43, 3, C, 18013", new GeoPoint(37,0), "FAMILIA"));



        knownLocationsAdapter = new KnownLocationsAdapter(this, R.layout.known_location_layout, locArray);
        knownLocationsList.setAdapter(knownLocationsAdapter);

//        knownLocationsList = (ListView) findViewById(R.id.knownLocationsList);
//        ArrayList<BluetoothDevice> bleArray = new ArrayList<BluetoothDevice>();
//        bleArray.add(new BluetoothDevice("carmenito", "00:14:25:FF", false));
//        bleArray.add(new BluetoothDevice("carmenito", "00:14:25:FF", true));
//        bleArray.add(new BluetoothDevice("carmenito", "00:14:25:FF", false));
//        bleArray.add(new BluetoothDevice("carmenito", "00:14:25:FF", true));
//        bleArray.add(new BluetoothDevice("carmenito", "00:14:25:FF", false));
//
//
//        ArrayAdapter adapter = new BluetoothDeviceAdapter(this, R.layout.bluetooth_devices_layout, bleArray);
//        knownLocationsList.setAdapter(adapter);
    }

    private void initializeMarker() {

    }

    private void initializeMapController() {
        if (map != null) {
            GeoPoint currentPoint = new GeoPoint(37.18817, -3.60667);
            mapController = (MapController) map.getController();
            mapController.setCenter(currentPoint);
            mapController.setZoom(10);
        } else {
            throw new Error("Using non initialied map in: initializeMapController()");
        }
    }

    private void initializeMap() {
        map = (MapView) findViewById(R.id.map);
        map.setBuiltInZoomControls(true);
        map.setMultiTouchControls(true);

        initializeMapController();
        initializeMapEvents();

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
        Marker marker = new Marker(map);
        marker.setPosition(point);
        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        map.getOverlays().add(marker);
        mapController.setZoom(16);
        mapController.setCenter(point);
    }

    public void onResume(){
        super.onResume();
        //this will refresh the osmdroid configuration on resuming.
        //if you make changes to the configuration, use
        //SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        //Configuration.getInstance().load(this, PreferenceManager.getDefaultSharedPreferences(this));
//        map.onResume(); //needed for compass, my location overlays, v6.0.0 and up
    }

    public void onPause(){
        super.onPause();
        //this will refresh the osmdroid configuration on resuming.
        //if you make changes to the configuration, use
        //SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        //Configuration.getInstance().save(this, prefs);
//        map.onPause();  //needed for compass, my location overlays, v6.0.0 and up
    }

    private void requestPermissionsAndInform() {
        requestPermissionsAndInform(true);
    }
    private void requestPermissionsAndInform(Boolean inform) {
        this.requestPermissions(SENSOR_PERMISSIONS, LOCATION_REQ_CODE);
        if (inform) {
            Toast.makeText(this, "Permisos solicitados y aparentemente concedidos", Toast.LENGTH_SHORT).show();
        }
    }


}