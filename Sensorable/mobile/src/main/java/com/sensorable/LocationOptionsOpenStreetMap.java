package com.sensorable;


import android.Manifest;
import android.content.Context;

import android.os.Bundle;
import android.preference.PreferenceManager;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;


import org.osmdroid.config.Configuration;

import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapController;
import org.osmdroid.views.MapView;

public class LocationOptionsOpenStreetMap extends AppCompatActivity {

    private MapView myOpenMap;
    private MapController mapController;

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
    private MapView map;


    private void requestPermissionsAndInform() {
        requestPermissionsAndInform(true);
    }
    private void requestPermissionsAndInform(Boolean inform) {
        this.requestPermissions(SENSOR_PERMISSIONS, LOCATION_REQ_CODE);
        if (inform) {
            Toast.makeText(this, "Permisos solicitados y aparentemente concedidos", Toast.LENGTH_SHORT).show();
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location_options_open_street_map);

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
        setContentView(R.layout.activity_location_options_open_street_map);
        GeoPoint madrid = new GeoPoint(37.18817, -3.60667);

        map = (MapView) findViewById(R.id.map);
        map.setBuiltInZoomControls(true);
        mapController = (MapController) map.getController();
        mapController.setCenter(madrid);
        mapController.setZoom(6);
        map.setMultiTouchControls(true);
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

}