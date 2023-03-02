package com.sgzapp.sgzsdkdemo;

import android.Manifest;
import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;

import com.blala.blalable.BleOperateManager;
import com.blala.blalable.BleSpUtils;
import com.blala.blalable.listener.BleConnStatusListener;
import com.blala.blalable.listener.ConnStatusListener;
import com.hjq.permissions.OnPermissionCallback;
import com.hjq.permissions.XXPermissions;
import com.inuker.bluetooth.library.Constants;
import com.inuker.bluetooth.library.search.SearchResult;
import com.inuker.bluetooth.library.search.response.SearchResponse;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

/**
 * search device activity
 * Created by Admin
 * Date 2023/2/3
 *
 * @author Admin
 */
public class ScanDeviceActivity extends SDKBaseActivity implements View.OnClickListener, OnItemClickListener {

    private Button scanBtn;
    private RecyclerView recyclerView;
    private ScanDeviceAdapter scanDeviceAdapter;
    private List<SearchResult> list;

    private BluetoothManager bluetoothManager;
    private BluetoothAdapter bluetoothAdapter;


    private final Handler handler = new Handler(Looper.myLooper()) {
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            if (msg.what == 0x00) {
                BleOperateManager.getInstance().stopScanDevice();
            }
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan_device);

        initViews();


        initBle();

        getPermission();
    }


    private void initViews() {
        scanBtn = findViewById(R.id.scanBtn);
        scanBtn.setOnClickListener(this);
        recyclerView = findViewById(R.id.scanRecyclerView);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.addItemDecoration(new DividerItemDecoration(ScanDeviceActivity.this, DividerItemDecoration.VERTICAL));
        list = new ArrayList<>();
        scanDeviceAdapter = new ScanDeviceAdapter(this, list);
        recyclerView.setAdapter(scanDeviceAdapter);
        scanDeviceAdapter.setOnItemClickListener(this);

    }

    /**
     * The bluetooth is on or off
     */
    private boolean isOpenBluetooth() {
        bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        if (bluetoothManager == null) {
            return false;
        }

        bluetoothAdapter = bluetoothManager.getAdapter();
        if (bluetoothAdapter == null) {
            return false;
        }

        return bluetoothAdapter.isEnabled();

    }


    @SuppressLint("MissingPermission")
    private void initBle() {
        bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        if (bluetoothManager == null)
            return;
        bluetoothAdapter = bluetoothManager.getAdapter();
        if (bluetoothAdapter == null)
            return;
        if (!bluetoothAdapter.isEnabled()){
            openBluetooth();
        }

    }

    /**
     * open the bluetooth
     */
    private void openBluetooth() {
        try {
            // open Bluetooth
            Intent requestBluetoothOn = new Intent(
                    BluetoothAdapter.ACTION_REQUEST_ENABLE);
            // Set the device to be scanned by other Bluetooth devices
            requestBluetoothOn
                    .setAction(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
            // set Bluetooth visible time
            requestBluetoothOn.putExtra(
                    BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION,
                    30 * 1000);
            // open the  Bluetooth
            startActivityForResult(requestBluetoothOn, 1001);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onClick(View view) {
        //start or end scanner device
        if (view.getId() == R.id.scanBtn) {
            startScanDevice();
        }
    }

    /**
     * The location permission is on or off. If it is not open, request the location permission
     */
    private void requestLocationPermission() {
        boolean isPermission = ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
        if (!isPermission) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 0x00);
        }
    }

    /**
     * get location permission back
     *
     * @param requestCode  requestCode
     * @param permissions  permission
     * @param grantResults is successful
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

    }

    //get bluetooth and local permission
    private void getPermission() {
        //判断权限
        boolean isPermission = ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
        if (!isPermission) {
            XXPermissions.with(ScanDeviceActivity.this).permission(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION).request(new OnPermissionCallback() {
                @Override
                public void onGranted(List<String> permissions, boolean all) {

                }
            });
            return;
        }


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            XXPermissions.with(ScanDeviceActivity.this).permission(Manifest.permission.BLUETOOTH_CONNECT,
                    Manifest.permission.BLUETOOTH_SCAN,
                    Manifest.permission.BLUETOOTH_ADVERTISE).request(new OnPermissionCallback() {
                @Override
                public void onGranted(List<String> permissions, boolean all) {

                }
            });

        }
    }


    //start scanner device
    private void startScanDevice() {


        list.clear();
        scanDeviceAdapter.notifyDataSetChanged();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.BLUETOOTH_CONNECT, Manifest.permission.BLUETOOTH_SCAN}, 0x01);
        }

        BleOperateManager.getInstance().scanBleDevice(new SearchResponse() {

            /**
             *
             * search started
             */
            @Override
            public void onSearchStarted() {
                scanBtn.setText("search started");
            }

            /**
             *
             * device founded
             *
             * @param searchResult class
             * public BluetoothDevice device;
             *     public int rssi;
             *     public byte[] scanRecord;
             */
            @Override
            public void onDeviceFounded(SearchResult searchResult) {
                scanBtn.setText("scanner..");
                if (TextUtils.isEmpty(searchResult.getName()) || searchResult.getName().equals("NULL"))
                    return;
                if (!list.contains(searchResult)) {
                    list.add(searchResult);
                    scanDeviceAdapter.notifyDataSetChanged();
                }
            }

            /**
             *
             * search stopped
             */
            @Override
            public void onSearchStopped() {
                scanBtn.setText("scanner stop");
            }

            /**
             *
             * search  canceled
             */
            @Override
            public void onSearchCanceled() {
                scanBtn.setText("scanner complete");
            }
        }, 15 * 1000, 1);
    }

    @Override
    public void onIteClick(int position) {
        String bleName = list.get(position).getName();
        String bleMac = list.get(position).getAddress();
        if (TextUtils.isEmpty(bleMac)) {
            return;
        }
        handler.sendEmptyMessageDelayed(0x00, 100);
        showDialog("connecting..");

        /**
         * set the connect status listener
         */
        BleOperateManager.getInstance().setBleConnStatusListener(new BleConnStatusListener() {
            @Override
            public void onConnectStatusChanged(String mac, int status) {
                if(status == Constants.STATUS_CONNECTED){ //connect successful

                }
                if(status == Constants.STATUS_DISCONNECTED){    //disconnected

                }
            }
        });

        /**
         * connect to device
         */
        BleOperateManager.getInstance().connYakDevice(bleName, bleMac, new ConnStatusListener() {
            @Override
            public void connStatus(int i) {

            }

            /**
             * connection success
             *
             */
            @Override
            public void setNoticeStatus(int i) {

                dismissDialog();
                BleSpUtils.put(ScanDeviceActivity.this, "conn_ble_mac", bleMac);
                finish();
            }
        });
    }
}
