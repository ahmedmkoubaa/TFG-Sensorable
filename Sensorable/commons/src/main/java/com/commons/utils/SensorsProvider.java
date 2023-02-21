package com.commons.utils;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.LocationListener;
import android.location.LocationManager;
import android.util.Log;
import android.widget.Toast;

import java.util.List;

public class SensorsProvider {
    private final Context context;

    // This gives us the location
    private LocationManager locationManager;

    // Attributes to use sensors from sensor manager
    private SensorManager sensorManager;

    public SensorsProvider(Context context) {
        this.context = context;

        initializeProviderLocation();
        initializeSensorManager();
    }

    public List<Sensor> getSensorList() {
        initializeSensorManager();
        return sensorManager.getSensorList(Sensor.TYPE_ALL);
    }

    // We're already asking for permissions in the method but the compiler doesn't recognize it
    @SuppressLint("MissingPermission")
    public void subscribeToGps(LocationListener listener) {
        // This verification should be done during onStart() because the system calls
        // this method when the user returns to the activity, which ensures the desired
        // location provider is enabled each time the activity resumes from the stopped state.
        locationManager =
                (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);

        final boolean gpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);

        if (!gpsEnabled) {
            Toast.makeText(context, "Enciende el GPS, por favor", Toast.LENGTH_SHORT).show();
        } else {
            try {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 0, listener);
            } catch (SecurityException e) {
                Toast.makeText(context, "No se facilitaron los permisos de GPS", Toast.LENGTH_SHORT).show();
            }
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

    private void initializeProviderLocation() {
        try {
            locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);

        } catch (Exception e) {
            Log.i("SENSORS_PROVIDER", "ERROR-> " + e.getMessage());

            if (!canAccessLocation()) {
                Toast.makeText(context, "GPS no disponible", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void initializeSensorManager() {
        if (sensorManager == null) {
            sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        }
    }

    // Check if location perms are available
    // Returns true if those permissions are available and false if not
    private boolean canAccessLocation() {
        Log.i("SENSORS_PROVIDER", "checking granted access to location permissions");

        return SensorablePermissions.isGranted(context, Manifest.permission.ACCESS_FINE_LOCATION) &&
                SensorablePermissions.isGranted(context, Manifest.permission.ACCESS_COARSE_LOCATION);
    }


}
