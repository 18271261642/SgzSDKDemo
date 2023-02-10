package com.sgzapp.sgzsdkdemo;

import com.blala.blalable.BleApplication;
import com.blala.blalable.BleOperateManager;

/**
 * Application
 * Created by Admin
 * Date 2023/2/3
 * @author Admin
 */
public class BaseApplication extends BleApplication {


    private static BaseApplication baseApplication;

    @Override
    public void onCreate() {
        super.onCreate();
        baseApplication = this;
    }


    public static BaseApplication getBaseApplication(){
        return baseApplication;
    }

    /**获取蓝牙数据操作类**/
    public BleOperateManager getBleOperate(){
        return BleOperateManager.getInstance();
    }
}
