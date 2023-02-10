package com.sgzapp.sgzsdkdemo;

import androidx.appcompat.app.AppCompatActivity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.blala.blalable.BleConstant;
import com.blala.blalable.BleSpUtils;
import com.blala.blalable.Utils;
import com.blala.blalable.listener.BleConnStatusListener;
import com.blala.blalable.listener.OnCommBackDataListener;
import com.blala.blalable.listener.OnMeasureDataListener;
import com.blala.blalable.listener.WriteBackDataListener;
import com.inuker.bluetooth.library.Constants;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;


/**
 * @author Admin
 */
public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "MainActivity";

    private Button getDeviceBtn,disconnectBtn;

    /**findDevice**/
    private Button findDeviceTv;

    /**logtv**/
    private TextView showLogTv;
    /**sync time**/
    private Button syncTimeBtn;
    /** connected status**/
    private TextView connStatusTv;


    private final DecimalFormat decimalFormat = new DecimalFormat("#.#");
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews();

        initData();
    }


    @Override
    protected void onResume() {
        super.onResume();

        BaseApplication.getBaseApplication().getBleOperate().setBleConnStatusListener(new BleConnStatusListener() {
            @Override
            public void onConnectStatusChanged(String s, int i) {
                Log.e(TAG,"------连接状态="+s+" "+i);
                connStatusTv.setText(i == Constants.STATUS_CONNECTED ? "接続成功" : "接続切断");
            }
        });

        String mac = (String) BleSpUtils.get(this,"conn_ble_mac","");
        if(!TextUtils.isEmpty(mac)){
           // connStatusTv.setText("接続成功");
        }
    }

    private void initData(){

        //register connected status broadcastreceiver
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BleConstant.BLE_CONNECTED_ACTION);
        intentFilter.addAction(BleConstant.BLE_DIS_CONNECT_ACTION);
        registerReceiver(broadcastReceiver,intentFilter);


        BaseApplication.getBaseApplication().getBleOperate().setMeasureDataListener(new OnMeasureDataListener() {
            @Override
            public void onRealStepData(int i, int i1, int i2) {
                showLogTv.setText("ステップ数: "+i+"\n"+"きょり: "+i1+"\n"+"カロリー： "+i2);
            }

            @Override
            public void onMeasureHeart(int i, long l) {
                showLogTv.setText("心拍数: "+i);
            }

            @Override
            public void onMeasureBp(int i, int i1, long l) {

            }

            @Override
            public void onMeasureSpo2(int i, long l) {
                showLogTv.setText("血中酸素: "+i);
            }

            @Override
            public void onMeasureTemp(int i) {
                float tttT = (float) CalculateUtils.div(i,10,1);
                showLogTv.setText("体温: "+tttT+"℃");
            }
        });
    }

    private void initViews(){
        connStatusTv = findViewById(R.id.connStatusTv);
        syncTimeBtn = findViewById(R.id.syncTimeBtn);
        findDeviceTv = findViewById(R.id.findDeviceTv);
        showLogTv = findViewById(R.id.showLogTv);
        getDeviceBtn = findViewById(R.id.getDeviceBtn);
        disconnectBtn = findViewById(R.id.disconnectBtn);

        getDeviceBtn.setOnClickListener(this);
        disconnectBtn.setOnClickListener(this);
        findDeviceTv.setOnClickListener(this);
        syncTimeBtn.setOnClickListener(this);

    }

    @Override
    public void onClick(View view) {
        //scan device
        if(view.getId() == R.id.getDeviceBtn){
            startActivity(new Intent(MainActivity.this,ScanDeviceActivity.class));
        }

        //disconnected
        if(view.getId() == R.id.disconnectBtn){
            BleSpUtils.put(this,"conn_ble_mac","");
            BaseApplication.getBaseApplication().getBleOperate().disConnYakDevice();
            connStatusTv.setText("接続が切断されました");
            showLogTv.setText("");
        }

        //find device
        if(view.getId() == R.id.findDeviceTv){
            BaseApplication.getBaseApplication().getBleOperate().findTimeBoatDevice();
        }
        //sync time
        if(view.getId() == R.id.syncTimeBtn){
            BaseApplication.getBaseApplication().getBleOperate().syncDeviceTime(new WriteBackDataListener() {
                @Override
                public void backWriteData(byte[] bytes) {
                    showLogTv.setText("時間が同期されました: "+formatTime());
                }
            });
        }
    }


    private String formatTime(){
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA);
        return simpleDateFormat.format(new Date());

    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        BleSpUtils.put(this,"conn_ble_mac","");
        unregisterReceiver(broadcastReceiver);
    }

    private final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            //connected
            if(action.equals(BleConstant.BLE_CONNECTED_ACTION)){
                showLogTv.setText("デバイスが接続されています ");
            }
            //dis connected
            if(action.equals(BleConstant.BLE_DIS_CONNECT_ACTION)){
                showLogTv.setText("デバイスが接続されていません");
            }
        }
    };
}