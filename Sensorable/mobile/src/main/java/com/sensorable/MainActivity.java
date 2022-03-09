package com.sensorable;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.NetworkInfo;
import android.net.wifi.WpsInfo;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pGroup;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.commons.BluetoothDevicesProvider;
import com.example.commons.DeviceType;
import com.example.commons.SensorTransmissionCoder;
import com.example.commons.SensorsProvider;
import com.google.android.gms.wearable.MessageClient;
import com.google.android.gms.wearable.MessageEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;


public class MainActivity extends AppCompatActivity implements MessageClient.OnMessageReceivedListener {
    private static final String[] SENSOR_PERMISSIONS = {
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.INTERNET,
            Manifest.permission.BODY_SENSORS,
            Manifest.permission.ACTIVITY_RECOGNITION,
            Manifest.permission.BLUETOOTH,
            Manifest.permission.BLUETOOTH_ADMIN,
            Manifest.permission.BLUETOOTH_CONNECT,
            Manifest.permission.ACCESS_WIFI_STATE,
            Manifest.permission.CHANGE_WIFI_STATE
    };

    private final static int REQUEST_PERMISSIONS_CODE = 1;
    private final static int SELECT_DEVICE_REQUEST_CODE = 0;
    private final static int REQUEST_ENABLE_BT = 2;

    private void requestPermissionsAndInform() {
        requestPermissionsAndInform(true);
    }

    private void requestPermissionsAndInform(Boolean inform) {
        this.requestPermissions(SENSOR_PERMISSIONS, REQUEST_PERMISSIONS_CODE);
        if (inform) {
            Toast.makeText(this, "Permisos solicitados y aparentemente concedidos", Toast.LENGTH_SHORT).show();
        }
    }

    private final static int MAX_SENSOR_BUFFER_SIZE = 512;
    private ArrayList<SensorTransmissionCoder.SensorMessage> sensorMessagesBuffer;

    private Button userStateSummary;
    private ProgressBar useStateProgressBar;
    private TextView userStateMessage;
    private TextView hearRateText, stepCounterText;

    private SensorsProvider sensorsProvider;
    private Button moreSensorsButton;

    private WearTransmissionService wearOsService;
    private EmpaticaTransmissionService empaticaService;
    private AdlDetectionService adlDetectionService;

    private BroadcastReceiver wearOsReceiver;
    private BroadcastReceiver empaticaReceiver;
    private BroadcastReceiver infoReceiver;
    private BroadcastReceiver wifiDirectReceiver;

    private BluetoothDevicesProvider bluetoothProvider;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        requestPermissionsAndInform(false);

        initializeAttributesFromUI();
        sensorMessagesBuffer = new ArrayList<>();

//        initializeWearOsTranmissionService();
//        initializeEmpaticaTransmissionService();
        initializeAdlDetectionService();
        initializeBluetoothDetection();
        initializeInfoReceiver();

        initializeWifiDirectDetector();



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


        sensorsProvider = new SensorsProvider(this);


    }

    private final IntentFilter wifiDirectIntentFilter = new IntentFilter();
    private WifiP2pManager.Channel channel;
    private WifiP2pManager wifiDirectManager;
    private List<WifiP2pDevice> peers = new ArrayList<WifiP2pDevice>();
    private WifiP2pManager.PeerListListener peerListListener;
    private WifiP2pManager.ConnectionInfoListener connectionListener;

    private void initializeWifiDirectDetector() {
        initializeWifiDirectIntentFilter();
        initializeWifiManager();
        initializeWifiDirectReceiver();
        initializePeerListListener();
        wifiDirectDiscoverDevices();
    }

    private void initializeWifiDirectIntentFilter() {
        // Indicates a change in the Wi-Fi P2P status.
        wifiDirectIntentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);

        // Indicates a change in the list of available peers.
        wifiDirectIntentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);

        // Indicates the state of Wi-Fi P2P connectivity has changed.
        wifiDirectIntentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);

        // Indicates this device's details have changed.
        wifiDirectIntentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);
    }

    private void initializeWifiManager() {
        wifiDirectManager = (WifiP2pManager) this.getSystemService(this.getApplicationContext().WIFI_P2P_SERVICE);
        channel = wifiDirectManager.initialize(this, getMainLooper(), new WifiP2pManager.ChannelListener() {
            @Override
            public void onChannelDisconnected() {
                Toast.makeText(MainActivity.this, "Channel disconnected", Toast.LENGTH_SHORT).show();
                Log.i("WIFI_DIRECT_CHANNEL", "channel disconnected");
            }
        });
    }

    private void wifiDirectAskForGroupPassword() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        wifiDirectManager.requestGroupInfo(channel, new WifiP2pManager.GroupInfoListener() {
            @Override
            public void onGroupInfoAvailable(WifiP2pGroup group) {
                String groupPassword = group.getPassphrase();
            }
        });
    }

    private void initializeWifiDirectReceiver() {
        Toast.makeText(this, "DEFINED WIFI RECEIVER", Toast.LENGTH_SHORT).show();
        Log.i("WIFI_DIRECT_RECEIVER", "Defining receiver");

        wifiDirectReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

                Toast.makeText(MainActivity.this, "Received something", Toast.LENGTH_SHORT).show();
                final String TAG = "WIFI_DIRECT_RECEIVER";

                String action = intent.getAction();
                switch (action) {
                    case WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION:
                        // Determine if Wifi P2P mode is enabled or not, alert
                        // the Activity.
                        int state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1);
                        Log.i(TAG, "enabled: " + (state == WifiP2pManager.WIFI_P2P_STATE_ENABLED));
                        Toast.makeText(context, "Wifi direct state has changed", Toast.LENGTH_SHORT).show();
                        break;
                    case WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION:
                        // The peer list has changed! We should probably do something about
                        // that.


                        // Request available peers from the wifi p2p manager. This is an
                        // asynchronous call and the calling activity is notified with a
                        // callback on PeerListListener.onPeersAvailable()
                        if (wifiDirectManager != null) {
                            if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                                return;
                            }
                            wifiDirectManager.requestPeers(channel, peerListListener);
                            connectToAWifiDirectPeer();
                        }

                        Log.i(TAG, "P2P peers changed");
                        Toast.makeText(context, "We've found some peers", Toast.LENGTH_SHORT).show();

                        break;

                    case WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION:
                        // Connection state changed! We should probably do something about
                        // that

                        if (wifiDirectManager == null) {
                            return;
                        }

                        NetworkInfo networkInfo = (NetworkInfo) intent
                                .getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO);

                        if (networkInfo.isConnected()) {

                            // We are connected with the other device, request connection
                            // info to find group owner IP
                            wifiDirectManager.requestConnectionInfo(channel, connectionListener);
                        }

                        Toast.makeText(context, "Connection state has been changed, are we connected?", Toast.LENGTH_SHORT).show();

                        break;

                    case WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION:
                        // Here is a way of updating
                        String extraDevices = intent.getParcelableExtra(
                                WifiP2pManager.EXTRA_WIFI_P2P_DEVICE);

                        Log.i(TAG, extraDevices);
                        break;

                    default:
                        Log.i(TAG, "Non recognized action in receiver");
                        Toast.makeText(context, "Non recognized action", Toast.LENGTH_SHORT).show();
                        break;
                }
            }
        };

        LocalBroadcastManager.getInstance(MainActivity.this)
                .registerReceiver(wifiDirectReceiver, wifiDirectIntentFilter);

    }

    private void wifiDirectDiscoverDevices() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
        ) {
            return;
        }

        wifiDirectManager.discoverPeers(channel, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                // Code for when the discovery initiation is successful goes here.
                // No services have actually been discovered yet, so this method
                // can often be left blank. Code for peer discovery goes in the
                // onReceive method, detailed below.
                Log.i("WIFI_DIRECT_DISCOVERY", "success discovery");
            }

            @Override
            public void onFailure(int reasonCode) {
                // Code for when the discovery initiation fails goes here.
                // Alert the user that something went wrong.

                Log.i("WIFI_DIRECT_DISCOVERY", "failure discovery");
            }
        });
    }

    private void initializePeerListListener() {
        peerListListener = new WifiP2pManager.PeerListListener() {
            @Override
            public void onPeersAvailable(WifiP2pDeviceList peerList) {
                Log.i("WIFI_DIRECT_PEERS_LISTENER", "Peers available");
                Toast.makeText(MainActivity.this, "Peers availables", Toast.LENGTH_SHORT).show();

                List<WifiP2pDevice> refreshedPeers = new ArrayList<>(peerList.getDeviceList());
                if (!refreshedPeers.equals(peers)) {
                    peers.clear();
                    peers.addAll(refreshedPeers);

                    Log.i("WIFI_DIRECT_PEERS_LISTENER", peers.toString());

                    // If an AdapterView is backed by this data, notify it
                    // of the change. For instance, if you have a ListView of
                    // available peers, trigger an update.
                    /*((WiFiPeerListAdapter) getListAdapter()).notifyDataSetChanged();*/

                    // Perform any other updates needed based on the new list of
                    // peers connected to the Wi-Fi P2P network.
                }

                if (peers.size() == 0) {
                    Log.i("WIFI_DIRECT_PEERS_LISTENER", "No devices found");
                    return;
                }
            }
        };
    }

    public void connectToAWifiDirectPeer() {
        Toast.makeText(this, "trying to connect to a new peer", Toast.LENGTH_SHORT).show();
        Log.i("WIFI_DIRECT_CONNECTION", "trying to connect to a new peer");

        // Picking the first device found on the network.
        WifiP2pDevice device = peers.get(0);

        WifiP2pConfig config = new WifiP2pConfig();
        config.deviceAddress = device.deviceAddress;
        config.wps.setup = WpsInfo.PBC;

        if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

//        wifiDirectAskForGroupPassword();

        wifiDirectManager.connect(channel, config, new WifiP2pManager.ActionListener() {

            @Override
            public void onSuccess() {
                // WiFiDirectBroadcastReceiver notifies us. Ignore for now.
                Log.i("WIFI_DIRECT_CONNECT", "conection succeed to a remote peer");
            }

            @Override
            public void onFailure(int reason) {
                Toast.makeText(MainActivity.this, "Connect failed. Retry.",
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void initializeWifiDirectChangedStateNotification() {
        connectionListener = new WifiP2pManager.ConnectionInfoListener() {
            @Override
            public void onConnectionInfoAvailable(WifiP2pInfo info) {
                // String from WifiP2pInfo struct
                String groupOwnerAddress = info.groupOwnerAddress.getHostAddress();

                // After the group negotiation, we can determine the group owner
                // (server).
                if (info.groupFormed && info.isGroupOwner) {
                    // Do whatever tasks are specific to the group owner.
                    // One common case is creating a group owner thread and accepting
                    // incoming connections.

                    Log.i("WIFI_DIRECT_CONNECTION_INFO", "Connection info available has changed, I'm the owner");
                } else if (info.groupFormed) {
                    // The other device acts as the peer (client). In this case,
                    // you'll want to create a peer thread that connects
                    // to the group owner.

                    Log.i("WIFI_DIRECT_CONNECTION_INFO", "Info available I am not the owner");

                }
            }
        };
    }

















//    --------------------------------------------------------------------------------------------

    @Override
    protected void onDestroy() {
        super.onDestroy();

    }


    private void initializeBluetoothDetection() {
        bluetoothProvider = new BluetoothDevicesProvider(this);
        if (!bluetoothProvider.isEnabled()) {
            bluetoothProvider.turnOnBluetooth();
        } else {
            Toast.makeText(this, "BLUETOOTH IS ENABLED", Toast.LENGTH_SHORT).show();
        }

        bluetoothProvider.startScan();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        switch (requestCode) {
            case BluetoothDevicesProvider.SELECT_DEVICE_REQUEST_CODE:
                Log.i("BLUETOOTH_PROVIDER", "on activity result for companion found device");
                bluetoothProvider.onActivityResultCompanionFoundDevice(requestCode, resultCode, data);
                break;

            case BluetoothDevicesProvider.REQUEST_ENABLE_BT:
                Log.i("BLUETOOTH_PROVIDER", "on activity result for turn on bluetooth");
                bluetoothProvider.onActivityResultTurnOnBluetooth(requestCode, resultCode, data);
                break;
            default:
                Log.i("ON_ACTIVITY_RESULT DEFAULT", "on activity result for companion found device");

                super.onActivityResult(requestCode, resultCode, data);
                break;
        }

    }

    private void sendSensorDataToAdlDetectionService(ArrayList<SensorTransmissionCoder.SensorMessage> arrayMessage) {
        sensorMessagesBuffer.addAll(arrayMessage);
        sendSensorDataToAdlDetectionService();
    }
    private void sendSensorDataToAdlDetectionService(SensorTransmissionCoder.SensorMessage msg) {
        sensorMessagesBuffer.add(msg);
        sendSensorDataToAdlDetectionService();

    }

    private void sendSensorDataToAdlDetectionService() {
        if (sensorMessagesBuffer.size() >= MAX_SENSOR_BUFFER_SIZE) {

            Intent intent = new Intent("MOBILE_SENDS_SENSOR_DATA");
            // You can also include some extra data.

            Bundle empaticaBundle = new Bundle();
            empaticaBundle.putParcelableArrayList("MobileMessage", new ArrayList<>(sensorMessagesBuffer));

            intent.putExtra("MOBILE_DATA_COLLECTED", empaticaBundle);
            LocalBroadcastManager.getInstance(this).sendBroadcast(intent);

            // reset buffer
            sensorMessagesBuffer.clear();
        }
    }

    private void initializeAdlDetectionService() {
        adlDetectionService = new AdlDetectionService();
        startService(new Intent(this, AdlDetectionService.class));

        BroadcastReceiver exampleReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Toast.makeText(context, "mobile: received" +
                        intent.getBundleExtra("ADL_DATA_COLLECTED")
                                .getString("AdlMessage"),
                        Toast.LENGTH_SHORT).show();
            }
        };

        LocalBroadcastManager.getInstance(MainActivity.this).registerReceiver(
                exampleReceiver, new IntentFilter("AdlUpdates"));
    }



    private void initializeEmpaticaTransmissionService() {

        empaticaService = new EmpaticaTransmissionService();
        startService(new Intent(this, EmpaticaTransmissionService.class));

        // handle messages from our service to this activity
        empaticaReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Bundle b = intent.getBundleExtra("EMPATICA_DATA_COLLECTED");
                ArrayList<SensorTransmissionCoder.SensorMessage> arrayMessage = b.getParcelableArrayList("EmpaticaMessage");
                sendSensorDataToAdlDetectionService(arrayMessage);
//                Toast.makeText(context, "He recibido " + arrayMessage.size() + " elementos ", Toast.LENGTH_LONG).show();

            }
        };

        LocalBroadcastManager.getInstance(MainActivity.this).registerReceiver(
                empaticaReceiver, new IntentFilter("EmpaticaDataUpdates"));
    }


    private void initializeInfoReceiver() {

        // handle messages from our service to this activity
        infoReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

                String msg = intent.getStringExtra("msg");
                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
            }
        };

        LocalBroadcastManager.getInstance(MainActivity.this).registerReceiver(
                infoReceiver, new IntentFilter("INFO"));
    }

    private void initializeWearOsTranmissionService() {
        // start new data transmission service to collect data from wear os
        wearOsService = new WearTransmissionService();
        startService(new Intent(this, WearTransmissionService.class));

        // handle messages from our service to this activity
        wearOsReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Bundle b = intent.getBundleExtra("WEAR_DATA_COLLECTED");
                SensorTransmissionCoder.SensorMessage message = b.getParcelable("SensorMessage");
                String value;

                switch (message.getSensorType()) {
                    case Sensor.TYPE_HEART_RATE:
                        value = message.getValue()[0] + " ppm";
                        hearRateText.setText(value);
                        break;

                    case Sensor.TYPE_STEP_COUNTER:
                        value = message.getValue()[0] + "steps";
                        stepCounterText.setText(value);
                        break;

                    default:
                        Toast.makeText(context, "Sensor recibido no reconocido", Toast.LENGTH_SHORT).show();
                        break;
                }
            }
        };

        LocalBroadcastManager.getInstance(MainActivity.this).registerReceiver(
                wearOsReceiver, new IntentFilter("SensorDataUpdates"));
    }

    private void initializeAttributesFromUI() {
        userStateSummary = (Button) findViewById(R.id.userStateSummary);
        useStateProgressBar = (ProgressBar) findViewById(R.id.userStateProgressBar);
        userStateMessage = (TextView) findViewById(R.id.text);

        hearRateText = (TextView) findViewById(R.id.hearRateText);
        stepCounterText = (TextView) findViewById(R.id.stepCounterText);

        moreSensorsButton = (Button) findViewById(R.id.moreSensorsButton);
        moreSensorsButton = (Button) findViewById(R.id.moreSensorsButton);
        moreSensorsButton.setOnClickListener(v -> {
            Intent intent = new Intent(
                    this,
                    DetailedSensorsList.class);
            startActivity(intent);
        });
    }


    @Override
    protected void onStart() {
        super.onStart();

       initializeSensors();
    }

    private void initializeSensors() {

        sensorsProvider.subscribeToSensor(Sensor.TYPE_HEART_RATE, new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent sensorEvent) {
                hearRateText.setText((int) sensorEvent.values[0] + " ppm");
                SensorTransmissionCoder.SensorMessage msg =
                        new SensorTransmissionCoder.SensorMessage(
                                DeviceType.MOBILE,
                                Sensor.TYPE_HEART_RATE,
                                sensorEvent.values
                        );

                sendSensorDataToAdlDetectionService(msg);
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int i) {
        /*        Toast.makeText(
                        MainActivity.this,
                        i <= 0 ? "Sensor not available" : ("Accuracy value is: " + i) ,
                        Toast.LENGTH_SHORT
                ).show();*/
            }
        }, SensorManager.SENSOR_DELAY_NORMAL);


        sensorsProvider.subscribeToSensor(Sensor.TYPE_STEP_COUNTER, new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent sensorEvent) {
                stepCounterText.setText((int) sensorEvent.values[0] + " pasos");

                SensorTransmissionCoder.SensorMessage msg =
                        new SensorTransmissionCoder.SensorMessage(
                                DeviceType.MOBILE,
                                Sensor.TYPE_STEP_COUNTER,
                                sensorEvent.values
                        );

                sendSensorDataToAdlDetectionService(msg);
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int i) {
            }
        }, SensorManager.SENSOR_DELAY_NORMAL);


        SensorEventListener transmissionListener = new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent sensorEvent) {
                SensorTransmissionCoder.SensorMessage msg =
                        new SensorTransmissionCoder.SensorMessage(
                                DeviceType.MOBILE,
                                sensorEvent.sensor.getType(),
                                sensorEvent.values
                        );

                sendSensorDataToAdlDetectionService(msg);
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int i) {

            }
        };

        sensorsProvider.subscribeToSensor(Sensor.TYPE_PROXIMITY,  new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent sensorEvent) {
                SensorTransmissionCoder.SensorMessage msg =
                        new SensorTransmissionCoder.SensorMessage(
                                DeviceType.MOBILE,
                                sensorEvent.sensor.getType(),
                                sensorEvent.values
                        );

                sendSensorDataToAdlDetectionService(msg);
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int i) {

            }
        }, SensorManager.SENSOR_DELAY_NORMAL);
        sensorsProvider.subscribeToSensor(Sensor.TYPE_LIGHT, transmissionListener , SensorManager.SENSOR_DELAY_NORMAL);
        sensorsProvider.subscribeToSensor(Sensor.TYPE_ACCELEROMETER, transmissionListener , SensorManager.SENSOR_DELAY_NORMAL);
    }


    @Override
    public void onMessageReceived(@NonNull MessageEvent messageEvent) {
        Toast.makeText(this, "RECIBIDO", Toast.LENGTH_LONG).show();
    }

}