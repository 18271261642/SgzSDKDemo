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

import com.blala.blalable.BleSpUtils;
import com.blala.blalable.listener.ConnStatusListener;
import com.hjq.permissions.OnPermissionCallback;
import com.hjq.permissions.XXPermissions;
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
 * Created by Admin
 * Date 2023/2/3
 *
 * @author Admin
 */
public class ScanDeviceActivity extends SDKBaseActivity implements View.OnClickListener,OnItemClickListener {

    private Button scanBtn;
    private RecyclerView recyclerView;
    private ScanDeviceAdapter scanDeviceAdapter;
    private List<SearchResult> list;

    private BluetoothManager bluetoothManager;
    private BluetoothAdapter bluetoothAdapter;


    private final Handler handler = new Handler(Looper.myLooper()){
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            if(msg.what == 0x00){
                BaseApplication.getBaseApplication().getBleOperate().stopScanDevice();
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


    @SuppressLint("MissingPermission")
    private void initBle() {
        bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        if (bluetoothManager == null)
            return;
        bluetoothAdapter = bluetoothManager.getAdapter();
        if (bluetoothAdapter == null)
            return;
        if (!bluetoothAdapter.isEnabled())
            openBletooth();

    }

    private void openBletooth() {
        try {
            // 请求打开 Bluetooth
            Intent requestBluetoothOn = new Intent(
                    BluetoothAdapter.ACTION_REQUEST_ENABLE);
            // 设置 Bluetooth 设备可以被其它 Bluetooth 设备扫描到
            requestBluetoothOn
                    .setAction(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
            // 设置 Bluetooth 设备可见时间
            requestBluetoothOn.putExtra(
                    BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION,
                    30 * 1000);
            // 请求开启 Bluetooth
            startActivityForResult(requestBluetoothOn,
                    1001);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onClick(View view) {
        //开始或停止搜索
        if (view.getId() == R.id.scanBtn) {
            startScanDevice();
        }
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
            XXPermissions.with(ScanDeviceActivity.this).permission(  Manifest.permission.BLUETOOTH_CONNECT,
                    Manifest.permission.BLUETOOTH_SCAN,
                    Manifest.permission.BLUETOOTH_ADVERTISE).request(new OnPermissionCallback() {
                @Override
                public void onGranted(List<String> permissions, boolean all) {

                }
            });

        }
    }


    //开始搜索
    private void startScanDevice() {


        list.clear();
        scanDeviceAdapter.notifyDataSetChanged();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.BLUETOOTH_CONNECT, Manifest.permission.BLUETOOTH_SCAN}, 0x01);
        }
        BaseApplication.getBaseApplication().getBleOperate().scanBleDevice(new SearchResponse() {
            @Override
            public void onSearchStarted() {
                scanBtn.setText("検索を開始");
            }

            @Override
            public void onDeviceFounded(SearchResult searchResult) {
                scanBtn.setText("検索中..");
                if (TextUtils.isEmpty(searchResult.getName()) || searchResult.getName().equals("NULL"))
                    return;
                if (!list.contains(searchResult)) {
                    list.add(searchResult);
                    scanDeviceAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onSearchStopped() {
                scanBtn.setText("検索完了");
            }

            @Override
            public void onSearchCanceled() {
                scanBtn.setText("検索を閉じる");
            }
        }, 15 * 1000, 1);
    }

    @Override
    public void onIteClick(int position) {
        String bleName = list.get(position).getName();
        String bleMac = list.get(position).getAddress();
        if(TextUtils.isEmpty(bleMac)){
            return;
        }
        handler.sendEmptyMessageDelayed(0x00,100);
        showDialog("接続中..");
        BaseApplication.getBaseApplication().getBleOperate().connYakDevice(bleName, bleMac, new ConnStatusListener() {
            @Override
            public void connStatus(int i) {

            }

            @Override
            public void setNoticeStatus(int i) {
                dismissDialog();
                BleSpUtils.put(ScanDeviceActivity.this,"conn_ble_mac",bleMac);
                finish();
            }
        });
    }
}
