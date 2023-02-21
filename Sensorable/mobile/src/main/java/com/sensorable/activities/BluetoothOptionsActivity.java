package com.sensorable.activities;

import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.commons.database.BluetoothDeviceDao;
import com.commons.database.BluetoothDeviceEntity;
import com.commons.database.BluetoothDeviceRegistryDao;
import com.commons.database.BluetoothDeviceRegistryEntity;
import com.commons.devicesDetection.BluetoothDevicesProvider;
import com.commons.utils.SensorableDatabase;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.sensorable.MainActivity;
import com.sensorable.R;
import com.sensorable.utils.BluetoothDeviceInfo;
import com.sensorable.utils.BluetoothDeviceInfoAdapter;
import com.sensorable.utils.MobileDatabaseBuilder;

import java.util.ArrayList;
import java.util.concurrent.ExecutorService;

public class BluetoothOptionsActivity extends AppCompatActivity {

    private ListView bluetoothFoundDevices;
    private BluetoothDevicesProvider bluetoothProvider;
    private BluetoothDeviceInfoAdapter bluetoothDeviceInfoAdapter;
    private ArrayList<BluetoothDeviceInfo> bleArray;

    private BluetoothDeviceRegistryDao bluetoothDeviceRegistryDao;
    private BluetoothDeviceDao bluetoothDeviceDao;
    private ExecutorService executor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetooth_options);

        initializeDatabase();
        initializeAttributesFromUI();
        initializeBluetoothDevicesProvider();
    }

    private void initializeDatabase() {
        SensorableDatabase database = MobileDatabaseBuilder.getDatabase(this);
        bluetoothDeviceDao = database.bluetoothDeviceDao();
        bluetoothDeviceRegistryDao = database.bluetoothDeviceRegistryDao();
        executor = MobileDatabaseBuilder.getExecutor();
    }

    @Override
    protected void onStart() {
        super.onStart();

        bluetoothProvider.startScan(new ScanCallback() {
            @Override
            public void onScanResult(int callbackType, ScanResult result) {
                super.onScanResult(callbackType, result);

/*                BluetoothDeviceEntity searched = bluetoothDeviceDao.findByAddress(result.getDevice().getAddress());
                if (searched == null) {
                    BluetoothDeviceEntity
                            databaseDevice = new BluetoothDeviceEntity();

                    BluetoothDevice device = result.getDevice();
                    databaseDevice.address = device.getAddress();
                    databaseDevice.deviceName = device.getName();
                    databaseDevice.bluetoothDeviceType = device.getBluetoothClass().getDeviceClass();
                    databaseDevice.bondState = device.getBondState();

                    // update database
                    bluetoothDeviceDao.insert(databaseDevice);

                    // update list representation
                    bleArray.add(databaseDevice);

                    // data changed, it's important to notify this event
                    adapter.notifyDataSetChanged();

                    Log.i("BLUETOOTH_SCANNER", "added a new bluetooth device");
                }*/

                // TODO: if the user watches this screen then we want to
                //  show detected devices in real time
                runOnUiThread(() -> updateFromDatabase());

            }
        });
    }

    private void initializeAttributesFromUI() {
        bluetoothFoundDevices = findViewById(R.id.foundDevices);
        BottomNavigationView bottomNavigation = (BottomNavigationView) findViewById(R.id.bottom_navigation);
//        bottomNavigation.setSelectedItemId(R.id.tab_bluetooth);
        bottomNavigation.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();
                switch (id) {
                    case R.id.tab_home:
                        startActivity(
                                new Intent(BluetoothOptionsActivity.this, MainActivity.class)
                        );
                        overridePendingTransition(0, 0);

                        return true;

                    case R.id.tab_adls:
                        startActivity(
                                new Intent(BluetoothOptionsActivity.this, AdlSummaryActivity.class)
                        );
                        overridePendingTransition(0, 0);

                        return true;

                    case R.id.tab_locations:
                        startActivity(
                                new Intent(BluetoothOptionsActivity.this, LocationOptionsActivity.class)
                        );
                        overridePendingTransition(0, 0);
                        return true;

                    case R.id.tab_charts:

                        startActivity(
                                new Intent(BluetoothOptionsActivity.this, DetailedSensorsListActivity.class)
                        );
                        overridePendingTransition(0, 0);
                        return true;
                }

                return true;
            }
        });

        bleArray = new ArrayList<>();

        bluetoothDeviceInfoAdapter = new BluetoothDeviceInfoAdapter(
                this,
                R.layout.bluetooth_devices_layout,
                bleArray
        );

        bluetoothDeviceInfoAdapter.setNotifyOnChange(true);
        bluetoothFoundDevices.setAdapter(bluetoothDeviceInfoAdapter);

        updateFromDatabase();
    }

    private void updateFromDatabase() {
        executor.execute(() -> {
            ArrayList<BluetoothDeviceInfo> bleArrayCopy = new ArrayList<>();

            for (BluetoothDeviceRegistryEntity registry : bluetoothDeviceRegistryDao.getAll()) {
                BluetoothDeviceEntity device = bluetoothDeviceDao.getByAddress(registry.address);

                bleArrayCopy.add(
                        new BluetoothDeviceInfo(
                                device.address,
                                device.deviceName,
                                device.bondState,
                                device.bluetoothDeviceType,
                                device.trusted,
                                registry.start,
                                registry.end)
                );
            }

            runOnUiThread(() -> {
                bleArray.clear();
                bleArray.addAll(bleArrayCopy);
                bluetoothDeviceInfoAdapter.notifyDataSetChanged();
            });
        });
    }

    private void initializeBluetoothDevicesProvider() {
        bluetoothProvider = new BluetoothDevicesProvider(this);
        BluetoothDevicesProvider.enableBluetooth(this);
    }
}