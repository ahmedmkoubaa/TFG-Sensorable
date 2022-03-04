package com.example.commons;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Criteria;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;

import java.util.List;

public class SensorsProvider  {
    private static final String[] SENSOR_PERMISSIONS = {
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.INTERNET,
            Manifest.permission.BODY_SENSORS,
            Manifest.permission.ACTIVITY_RECOGNITION

    };




    private final int LOCATION_REQ_CODE = 1;
    private Activity c;

    // this gives us the location
    private LocationManager locationManager;

    // Attributes to use sensors from sensor manager
    private SensorManager sensorManager;

    public SensorsProvider(Activity context) {
        this.c = context;
        requestPermissionsAndInform(false);

        initializeProviderLocation();
        initializeSensorManager();
    }

    public List<Sensor> getSensorList() {
        initializeSensorManager();
        return sensorManager.getSensorList(Sensor.TYPE_ALL);
    }

    private void initializeProviderLocation() {
        try {
            locationManager =
                    (LocationManager) c.getSystemService(Context.LOCATION_SERVICE);

        } catch (Exception e) {
            if (!canAccessLocation()) {
                requestPermissionsAndInform();
            }
        }

        if (locationManager != null && canAccessLocation()) {
            // Check location permissions
            LocationProvider provider =
                    locationManager.getProvider(LocationManager.GPS_PROVIDER);

            // Retrieve a list of location providers that have fine accuracy, no monetary cost, etc
            Criteria criteria = new Criteria();
            criteria.setAccuracy(Criteria.ACCURACY_FINE);
            criteria.setCostAllowed(false);

            String providerName = locationManager.getBestProvider(criteria, true);

            // If no suitable provider is found, null is returned.
            if (providerName != null) {
                Toast.makeText(c, "Tenemos un provider", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(c, "Es necesario obtener los permisos, reinicie la app", Toast.LENGTH_SHORT).show();
        }
    }

    private void initializeSensorManager () {
        if ( sensorManager == null ) {
            sensorManager = (SensorManager) c.getSystemService(Context.SENSOR_SERVICE);
        }
    }

    public void subscribeToSensor (int sensorType, SensorEventListener listener, int delay) {
        initializeSensorManager();
        Sensor newSensor = sensorManager.getDefaultSensor(sensorType);
        sensorManager.registerListener(listener, newSensor, delay);
    }

    public void unsubscribeToSensor(SensorEventListener listener) {
        initializeSensorManager();
        sensorManager.unregisterListener(listener);
    }

    public void subscribeToGps(LocationListener listener) {
        // Its necessay to do this, we dont need to force calles of this method to call
        // previusly the initialization method
        initializeProviderLocation();

        // This verification should be done during onStart() because the system calls
        // this method when the user returns to the activity, which ensures the desired
        // location provider is enabled each time the activity resumes from the stopped state.
        if (!hasLocationManager()) {
            locationManager =
                    (LocationManager) c.getSystemService(Context.LOCATION_SERVICE);
        }

        final boolean gpsEnabled = locationManager.isProviderEnabled(locationManager.GPS_PROVIDER);

        if (!gpsEnabled) {
            Toast.makeText(c, "Please turn on location", Toast.LENGTH_SHORT).show();
        } else {
            if (canAccessLocation()) {
                if (ActivityCompat.checkSelfPermission(c, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                    ActivityCompat.checkSelfPermission(c, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    Toast.makeText(c, "Permisos de ubicación no disponibles", Toast.LENGTH_SHORT).show();
                    requestPermissionsAndInform(false);
                }

                locationManager.requestLocationUpdates(locationManager.GPS_PROVIDER, 1000, 0, listener);
            }
        }
    }

    private boolean hasLocationManager(){
        return locationManager != null;
    }

    // Check if location perms are available
    // Returns true if those permissions are available and false if not
    private boolean canAccessLocation() {
        return (c.checkSelfPermission(
                Manifest.permission.ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED) ||
                (c.checkSelfPermission(
                        Manifest.permission.ACCESS_COARSE_LOCATION) ==
                        PackageManager.PERMISSION_GRANTED);
    }

    private void requestPermissionsAndInform() {
        requestPermissionsAndInform(true);
    }
    private void requestPermissionsAndInform(Boolean inform) {
        c.requestPermissions(SENSOR_PERMISSIONS, LOCATION_REQ_CODE);
        if (inform) {
            Toast.makeText(c, "Permisos solicitados y aparentemente concedidos", Toast.LENGTH_SHORT).show();
        }
    }


}
