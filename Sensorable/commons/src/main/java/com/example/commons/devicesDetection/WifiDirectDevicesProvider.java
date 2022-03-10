package com.example.commons.devicesDetection;

import android.Manifest;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.net.wifi.WpsInfo;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pGroup;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import java.nio.channels.Channel;
import java.util.ArrayList;
import java.util.List;

public class WifiDirectDevicesProvider {
    private AppCompatActivity activity;

    private final IntentFilter wifiDirectIntentFilter = new IntentFilter();
    private WifiP2pManager.Channel channel;
    private WifiP2pManager wifiDirectManager;
    private List<WifiP2pDevice> peers = new ArrayList<WifiP2pDevice>();
    private WifiP2pManager.PeerListListener peerListListener;
    private WifiP2pManager.ConnectionInfoListener connectionListener;
    private BroadcastReceiver wifiDirectReceiver;

    public WifiDirectDevicesProvider(AppCompatActivity activity) {
        this.activity = activity;
        initializeWifiDirectDetector();
    }

    private void initializeWifiDirectDetector() {
        initializeWifiDirectIntentFilter();
        initializeWifiManager();
        initializeWifiDirectReceiver();
        initializePeerListListener();
        initializeWifiDirectChangedStateNotification();
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
        wifiDirectManager = (WifiP2pManager) activity.getSystemService(activity.getApplicationContext().WIFI_P2P_SERVICE);
        channel = wifiDirectManager.initialize(activity, activity.getMainLooper(), new WifiP2pManager.ChannelListener() {
            @Override
            public void onChannelDisconnected() {
                Toast.makeText(activity, "Channel disconnected", Toast.LENGTH_SHORT).show();
                Log.i("WIFI_DIRECT_CHANNEL", "channel disconnected");
            }
        });
    }

    private void wifiDirectAskForGroupPassword() {
        if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
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
        Toast.makeText(activity, "DEFINED WIFI RECEIVER", Toast.LENGTH_SHORT).show();
        Log.i("WIFI_DIRECT_RECEIVER", "Defining receiver");

        wifiDirectReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

                Toast.makeText(activity, "Received something", Toast.LENGTH_SHORT).show();
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
                            if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
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

        LocalBroadcastManager.getInstance(activity)
                .registerReceiver(wifiDirectReceiver, wifiDirectIntentFilter);

    }

    private void wifiDirectDiscoverDevices() {
        if (ActivityCompat.checkSelfPermission(
                activity,
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
                Toast.makeText(activity, "Peers availables", Toast.LENGTH_SHORT).show();

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
        Toast.makeText(activity, "trying to connect to a new peer", Toast.LENGTH_SHORT).show();
        Log.i("WIFI_DIRECT_CONNECTION", "trying to connect to a new peer");

        // Picking the first device found on the network.
        // WARNING: we should choose a desired device from the list and not use only the first
        WifiP2pDevice device = peers.get(0);

        WifiP2pConfig config = new WifiP2pConfig();
        config.deviceAddress = device.deviceAddress;
        config.wps.setup = WpsInfo.PBC;

        if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
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
                Toast.makeText(activity, "Connect failed. Retry.",
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
}


