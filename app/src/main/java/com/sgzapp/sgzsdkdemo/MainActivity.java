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
import com.blala.blalable.BleOperateManager;
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

        /**
         * set device connected status listener
         */
        BleOperateManager.getInstance().setBleConnStatusListener(new BleConnStatusListener() {
            @Override
            public void onConnectStatusChanged(String s, int i) {
                connStatusTv.setText(i == Constants.STATUS_CONNECTED ? "connected" : "not connected");
            }
        });

        String mac = (String) BleSpUtils.get(this,"conn_ble_mac","");
        if(!TextUtils.isEmpty(mac)){
           // connStatusTv.setText("接続成功");
        }
    }

    private void initData(){

        //register connected status broadcast receiver
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BleConstant.BLE_CONNECTED_ACTION);
        intentFilter.addAction(BleConstant.BLE_DIS_CONNECT_ACTION);
        registerReceiver(broadcastReceiver,intentFilter);

        /**
         * Observe the data measured by the equipment
         */
        BleOperateManager.getInstance().setMeasureDataListener(new OnMeasureDataListener() {

            /**
             *  real step data
             * @param stepValue  step
             * @param distanceValue  distance  m
             * @param kcalValue  calorie  kcal
             */
            @Override
            public void onRealStepData(int stepValue, int distanceValue, int kcalValue) {
                showLogTv.setText("step value: "+stepValue+"\n"+"distance: "+distanceValue+"\n"+"calorie： "+kcalValue);
            }

            /**
             *   measure heart rate value
             * @param heartValue   heart value
             * @param time  measure time
             */
            @Override
            public void onMeasureHeart(int heartValue,long time) {
                showLogTv.setText("heart rate value: "+heartValue);
            }

            /**
             *  measure blood pressure
             * @param sBp  systolic pressure
             * @param disBp   diastolic pressure
             * @param time  measure time
             */
            @Override
            public void onMeasureBp(int sBp,int disBp,long time) {

            }

            /**
             *  measure blood oxygen
             * @param spo2Value     blood oxygen value
             * @param time  measure time
             */
            @Override
            public void onMeasureSpo2(int spo2Value, long time) {
                showLogTv.setText("blood oxygen value: "+spo2Value);
            }

            /**
             *  measure temperature value
             * @param temperatureValue   temperature value need division 10
             */
            @Override
            public void onMeasureTemp(int temperatureValue) {
                float tttT = (float) CalculateUtils.div(temperatureValue,10,1);
                showLogTv.setText("temperature: "+tttT+"℃");
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
            connStatusTv.setText("not disconnect");
            showLogTv.setText("");
        }

        //find device
        if(view.getId() == R.id.findDeviceTv){
            BleOperateManager.getInstance().findTimeBoatDevice();
        }
        //sync time
        if(view.getId() == R.id.syncTimeBtn){
            BleOperateManager.getInstance().syncDeviceTime(new WriteBackDataListener() {
                @Override
                public void backWriteData(byte[] bytes) {
                    showLogTv.setText("sync time to device: "+formatTime());
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

    /**
     * broadcast receiver for device connect status
     */
    private final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            //connected
            if(action.equals(BleConstant.BLE_CONNECTED_ACTION)){
                showLogTv.setText("Connection succeeded ");
            }
            //dis connected
            if(action.equals(BleConstant.BLE_DIS_CONNECT_ACTION)){
                showLogTv.setText("Connection disconnected");
            }
        }
    };
}