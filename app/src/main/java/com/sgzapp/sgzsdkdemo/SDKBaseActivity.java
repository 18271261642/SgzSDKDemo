package com.sgzapp.sgzsdkdemo;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

/**
 * Created by Admin
 * Date 2023/2/4
 * @author Admin
 */
public class SDKBaseActivity extends AppCompatActivity {


    private ProgressDialog dialog;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }



    protected void showDialog(String content){
        if(dialog == null){
            dialog = new ProgressDialog(this);
        }
        dialog.setMessage(content);
        dialog.show();
    }


    protected void dismissDialog(){
        if(isFinishing()){
            return;
        }

        if(dialog != null && dialog.isShowing()){
            dialog.dismiss();
        }
    }
}
