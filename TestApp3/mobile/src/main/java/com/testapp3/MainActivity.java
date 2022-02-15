package com.testapp3;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;
import android.os.Looper;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.commons.SensorsProvider;
import com.google.android.gms.wearable.DataClient;
import com.google.android.gms.wearable.MessageClient;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Wearable;

import java.util.Random;


public class MainActivity extends AppCompatActivity implements MessageClient.OnMessageReceivedListener {
    private static final String[] SENSOR_PERMISSIONS = {
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.INTERNET,
            Manifest.permission.BODY_SENSORS,
            Manifest.permission.ACTIVITY_RECOGNITION
    };

    private final int LOCATION_REQ_CODE = 1;

    private  LocationManager locationManager = null;

    // Attributes to use sensors from sensor manager
    private SensorManager sensorManager;

    private Sensor sensorAccelerometer;
    private Sensor sensorAmbientTemperature;
    private Sensor sensorHumidity;
    private Sensor sensorProximity;
    private Sensor sensorLight;


    private Button userStateSummary;
    private ProgressBar useStateProgressBar;
    private TextView userStateMessage;
    private TextView hearRateText, stepCounterText;

    private SensorsProvider sensorsProvider;

    private Button moreSensorsButton;

    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
//        if (messageEvent.getPath().equals(VOICE_TRANSCRIPTION_MESSAGE_PATH)) {
//            Intent startIntent = new Intent(this, MainActivity.class);
//            startIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//            startIntent.putExtra("VOICE_DATA", messageEvent.getData());
//            startActivity(this, startIntent);
//        }

        Toast.makeText(this, "HE RECIIDO UN MENSAJE", Toast.LENGTH_LONG).show();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initializeAttributesFromUI();

//        Looper myLooper;
//        Wearable.WearableOptions options = new Wearable.WearableOptions.Builder().setLooper(myLooper).build();
        DataClient dataClient = Wearable.getDataClient(this);


        userStateSummary.setClickable(false);
        userStateSummary.setText("EN BUEN ESTADO");

        useStateProgressBar.setMin(0);
        useStateProgressBar.setMax(100);
        useStateProgressBar.setProgress((new Random()).nextInt(100));


        userStateMessage.setText(
                "Te encuentras bien, sigue así. Recuerda hacer ejercicio y tomarte la medicación cuando toque"
        );

        // Summary, progressBar and message will be set using a system valoration
        // this system valoration will be developed in the near future


//        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
//        setActionBar(myToolbar);

//        initializeProviderLocation();
//        initializeSensorAccelerometer();
//        initializeSensorAmbientTemperature();
//        initializeSensorHumidity();
//        initializeSensorProximity();
//        initilizeSensorLight();
//        initializeSensorHeartRate();
//        initializeSensorStepCounter();
        sensorsProvider = new SensorsProvider(this);
    }

    private void initializeAttributesFromUI() {
        userStateSummary = (Button) findViewById(R.id.userStateSummary);
        useStateProgressBar = (ProgressBar) findViewById(R.id.userStateProgressBar);
        userStateMessage = (TextView) findViewById(R.id.userStateMessage);

        hearRateText = (TextView) findViewById(R.id.hearRateText);
        stepCounterText = (TextView) findViewById(R.id.stepCounterText);

        moreSensorsButton = (Button) findViewById(R.id.moreSensorsButton);
        moreSensorsButton = (Button) findViewById(R.id.moreSensorsButton);
        moreSensorsButton.setOnClickListener(v -> {
            Intent intent = new Intent(
                    this, DetailedSensorsList.class);;
            startActivity(intent);
        });
    }


    @Override
    protected void onStart() {
        super.onStart();

//        subscribeToGps();
//        subscribeToSensorAccelerometer();
//        subscribeToSensorAmbientTemperature();
//        subscribeToSensorHumidity();
//        subscribeToSensorProximity();
//        subscribeToSensorLight();
//        subscribeToSensorHeartRate();

       initializeSensors();
    }

    private void initializeSensors() {
        SensorEventListener heartRateListener =  new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent sensorEvent) {
                hearRateText.setText((int) sensorEvent.values[0] + " ppm");
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int i) {
                Toast.makeText(
                        MainActivity.this,
                        i <= 0 ? "Sensor not available" : ("Accuracy value is: " + i) ,
                        Toast.LENGTH_SHORT
                ).show();
            }
        };
        sensorsProvider.subscribeToSensor(Sensor.TYPE_HEART_RATE, heartRateListener, SensorManager.SENSOR_DELAY_NORMAL);

        SensorEventListener stepCounterListener = new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent sensorEvent) {
                stepCounterText.setText((int) sensorEvent.values[0] + " pasos");
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int i) {
                if (i <= 0) {
                    Toast.makeText(
                            MainActivity.this,
                            "Step counter Sensor not available" ,
                            Toast.LENGTH_SHORT
                    ).show();
                }

            }
        };
        sensorsProvider.subscribeToSensor(Sensor.TYPE_STEP_COUNTER, stepCounterListener, SensorManager.SENSOR_DELAY_NORMAL);
    }


    private void requestPermissionsAndInform() {
        requestPermissions(SENSOR_PERMISSIONS, LOCATION_REQ_CODE);
        Toast.makeText(this, "Permisos solicitados y aparentemente concedidos", Toast.LENGTH_SHORT).show();
    }

    //-------------------------------------------------------------------------------------//
    // SENSORES
    //-------------------------------------------------------------------------------------//

    //-------------------------------------------------------------//
    // GPS

    private void initializeProviderLocation() {

        try {
            locationManager =
                    (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

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
                Toast.makeText(this, "Tenemos un provider", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "Es necesario obtener los permisos, reinicie la app", Toast.LENGTH_SHORT).show();
        }
    }



    private void subscribeToGps() {
        // This verification should be done during onStart() because the system calls
        // this method when the user returns to the activity, which ensures the desired
        // location provider is enabled each time the activity resumes from the stopped state.
        if (hasLocationSensor()) {
            final boolean gpsEnabled = locationManager.isProviderEnabled(locationManager.GPS_PROVIDER);

            if (!gpsEnabled) {
                Toast.makeText(this, "Please turn on location", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "GPS correctamente habilitado", Toast.LENGTH_SHORT).show();
                LocationListener locationListener = new LocationListener() {
                    @Override
                    public void onLocationChanged(@NonNull Location location) {
                        Toast.makeText(MainActivity.this, "Location is " + location, Toast.LENGTH_SHORT).show();
                    }
                };

                if (canAccessLocation()) {
                    if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        // TODO: Consider calling
                        //    ActivityCompat#requestPermissions
                        return;
                    }
                    locationManager.requestLocationUpdates(locationManager.GPS_PROVIDER, 0, 0, locationListener);
                }

            }
        }
    }

    // Check if location perms are available
    // Returns true if those permissions are available and false if not
    private boolean canAccessLocation() {
        return (checkSelfPermission(
                    Manifest.permission.ACCESS_FINE_LOCATION) ==
                    PackageManager.PERMISSION_GRANTED) ||
                (checkSelfPermission(
                    Manifest.permission.ACCESS_COARSE_LOCATION) ==
                    PackageManager.PERMISSION_GRANTED);
    }

    private boolean hasLocationSensor(){
        return locationManager != null;
    }

    //-------------------------------------------------------------//
    // SENSOR ACELEROMETRO

    private void initializeSensorAccelerometer() {
        if ( sensorManager == null ) {
            sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        }

        sensorAccelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
    }

    private void subscribeToSensorAccelerometer () {
        SensorEventListener listener = new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent sensorEvent) {
                String values = "(" +
                        sensorEvent.values[0] + ", " +
                        sensorEvent.values[1] + ", " +
                        sensorEvent.values[2] +
                        ")";
                Toast.makeText(MainActivity.this, "new values are: " +
                        values,
                        Toast.LENGTH_SHORT
                ).show();
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int i) {
                Toast.makeText(MainActivity.this, "accuracy has changed: " +
                        i,
                        Toast.LENGTH_SHORT
                ).show();
            }
        };

        sensorManager.registerListener(listener, sensorAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
    }

    //-------------------------------------------------------------//
    // AMBIENT TEMPERATURE SENSOR

    // This sensor may not work in some devices due to hardware restrictions
    private void initializeSensorAmbientTemperature() {
        if ( sensorManager == null ) {
            sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        }

        sensorAmbientTemperature = sensorManager.getDefaultSensor(Sensor.TYPE_AMBIENT_TEMPERATURE);
    }

    private void subscribeToSensorAmbientTemperature () {
        if (sensorAmbientTemperature != null) {
            SensorEventListener listener = new SensorEventListener() {
                @Override
                public void onSensorChanged(SensorEvent sensorEvent) {
                    String values = sensorEvent.values[0] + "ºC";
                    Toast.makeText(MainActivity.this, "new temperature values are: " +
                            values,
                            Toast.LENGTH_SHORT
                    ).show();
                }

                @Override
                public void onAccuracyChanged(Sensor sensor, int i) {
                    Toast.makeText(MainActivity.this,
                            "Accuracy has changed in ambient temperature sensor: " +
                            sensor.getResolution(),
                            Toast.LENGTH_SHORT
                    ).show();
                }
            };

            sensorManager.registerListener(listener, sensorAmbientTemperature, SensorManager.SENSOR_DELAY_NORMAL);
        } else {
            Toast.makeText(this,
                    "This device doesn't include an ambient temperature sensor or not recognized",
                    Toast.LENGTH_SHORT).show();
        }
    }

    //-------------------------------------------------------------//
    // HUMIDITY SENSOR

    private void initializeSensorHumidity() {
        if ( sensorManager == null ) {
            sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        }

        sensorHumidity = sensorManager.getDefaultSensor(Sensor.TYPE_RELATIVE_HUMIDITY);
    }

    private void subscribeToSensorHumidity () {
        if (sensorHumidity != null) {
            SensorEventListener listener = new SensorEventListener() {
                @Override
                public void onSensorChanged(SensorEvent sensorEvent) {
                    String values = "(" + sensorEvent.values[0] + ")";
                    Toast.makeText(MainActivity.this, "new humidity values are: " +
                            values,
                            Toast.LENGTH_SHORT
                    ).show();
                }

                @Override
                public void onAccuracyChanged(Sensor sensor, int i) {
                    Toast.makeText(MainActivity.this, "accuracy has changed: " +
                            sensor.getResolution(),
                            Toast.LENGTH_SHORT
                    ).show();
                }
            };

            sensorManager.registerListener(listener, sensorHumidity, SensorManager.SENSOR_DELAY_NORMAL);
        } else {
            Toast.makeText(
                    this,
                    "Sensor not recognized or doesn't exist in your device",
                    Toast.LENGTH_SHORT
            ).show();
        }
    }

    //-------------------------------------------------------------//
    // PROXIMITY SENSOR
    private void initializeSensorProximity() {
        if ( sensorManager == null ) {
            sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        }

        sensorProximity = sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);
    }

    private void subscribeToSensorProximity () {
        if (sensorProximity != null) {
            SensorEventListener listener = new SensorEventListener() {
                @Override
                public void onSensorChanged(SensorEvent sensorEvent) {
                    String values = "(" + sensorEvent.values[0] + ")";
                    Toast.makeText(MainActivity.this, "new proximity values are: " +
                            values,
                            Toast.LENGTH_SHORT
                    ).show();
                }

                @Override
                public void onAccuracyChanged(Sensor sensor, int i) {
                    Toast.makeText(MainActivity.this, "accuracy has changed: " +
                            sensor.getResolution(),
                            Toast.LENGTH_SHORT
                    ).show();
                }
            };

            sensorManager.registerListener(listener, sensorProximity, SensorManager.SENSOR_DELAY_NORMAL);
        } else {
            Toast.makeText(this, "Proximity sensor not recognized", Toast.LENGTH_SHORT).show();
        }
    }

    //-------------------------------------------------------------//
    // LIGHT SENSOR
    private void initilizeSensorLight() {
        if ( sensorManager == null ) {
            sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        }

        sensorLight = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
    }

    private void subscribeToSensorLight () {
        if (sensorLight != null) {
            SensorEventListener listener = new SensorEventListener() {
                @Override
                public void onSensorChanged(SensorEvent sensorEvent) {
                    String values = "(" + sensorEvent.values[0] + ")";
                    Toast.makeText(MainActivity.this, "new light values are: " +
                            values,
                            Toast.LENGTH_SHORT
                    ).show();
                }

                @Override
                public void onAccuracyChanged(Sensor sensor, int i) {
                    Toast.makeText(MainActivity.this, "accuracy has changed: " +
                            sensor.getResolution(),
                            Toast.LENGTH_SHORT
                    ).show();
                }
            };

            sensorManager.registerListener(listener, sensorLight, SensorManager.SENSOR_DELAY_NORMAL);
        } else {
            Toast.makeText(this, "Light sensor not recognized", Toast.LENGTH_SHORT).show();
        }
    }
}