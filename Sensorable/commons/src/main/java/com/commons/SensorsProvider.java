package com.commons;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Criteria;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.widget.Toast;

import java.util.List;

public class SensorsProvider {
    private final Activity context;

    // this gives us the location
    private LocationManager locationManager;

    // Attributes to use sensors from sensor manager
    private SensorManager sensorManager;

    public SensorsProvider(Activity context) {
        this.context = context;

        SensorablePermissions.requestAll(context);

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
                    (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);

        } catch (Exception e) {
            if (!canAccessLocation()) {
                Toast.makeText(context, "Necesitamos permisos", Toast.LENGTH_SHORT).show();
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
        } else {
            Toast.makeText(context, "Es necesario obtener los permisos, reinicie la app", Toast.LENGTH_SHORT).show();
        }
    }

    private void initializeSensorManager() {
        if (sensorManager == null) {
            sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        }
    }

    public void subscribeToSensor(int sensorType, SensorEventListener listener, int delay) {
        initializeSensorManager();
        Sensor newSensor = sensorManager.getDefaultSensor(sensorType);
        sensorManager.registerListener(listener, newSensor, delay);
    }

    public void unsubscribeToSensor(SensorEventListener listener) {
        initializeSensorManager();
        sensorManager.unregisterListener(listener);
    }

    @SuppressLint("MissingPermission")
    public void subscribeToGps(LocationListener listener) {
        // Its necessay to do this, we dont need to force calles of this method to call
        // previusly the initialization method
        initializeProviderLocation();

        // This verification should be done during onStart() because the system calls
        // this method when the user returns to the activity, which ensures the desired
        // location provider is enabled each time the activity resumes from the stopped state.
        locationManager =
                (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);


        final boolean gpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);

        if (!gpsEnabled) {
            Toast.makeText(context, "Please turn on location", Toast.LENGTH_SHORT).show();
        } else {
            if (canAccessLocation()) {

                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 0, listener);
            }
        }
    }

    // Check if location perms are available
    // Returns true if those permissions are available and false if not
    private boolean canAccessLocation() {
        return SensorablePermissions.isGranted(context, Manifest.permission.ACCESS_FINE_LOCATION) &&
                SensorablePermissions.isGranted(context, Manifest.permission.ACCESS_COARSE_LOCATION);

    }


}
