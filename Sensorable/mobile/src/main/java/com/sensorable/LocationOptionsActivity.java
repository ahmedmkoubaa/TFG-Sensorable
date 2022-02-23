package com.sensorable;


import android.Manifest;
import android.content.Context;

import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;




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
    private Marker mapMarker;
    private ListView knownLocationsList;

    private Button listButton;
    private Button mapButton;



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
        ArrayList<KnownLocation> locArray = new ArrayList<>();
        locArray.add(new KnownLocation("casa de tia pepi", "avenida de la victoria, 43, 3, C, 18013", new GeoPoint(37,0), "FAMILIA"));
        locArray.add(new KnownLocation("casa de tia pepi", "avenida de la victoria, 43, 3, C, 18013", new GeoPoint(37,0), "FAMILIA"));
        locArray.add(new KnownLocation("casa de tia pepi", "avenida de la victoria, 43, 3, C, 18013", new GeoPoint(37,0), "FAMILIA"));
        locArray.add(new KnownLocation("casa de tia pepi", "avenida de la victoria, 43, 3, C, 18013", new GeoPoint(37,0), "FAMILIA"));
        locArray.add(new KnownLocation("casa de tia pepi", "avenida de la victoria, 43, 3, C, 18013", new GeoPoint(37,0), "FAMILIA"));



        ArrayAdapter adapter = new KnownLocationsAdapter(this, R.layout.known_location_layout, locArray);
        knownLocationsList.setAdapter(adapter);

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
        if (map != null) {
            mapMarker = new Marker(map);
        } else {
            throw new Error("Using non initialied map in: initializeMarker()");
        }
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