package com.sgzapp.sgzsdkdemo;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.inuker.bluetooth.library.search.SearchResult;

import java.util.Arrays;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

/**
 * Created by Admin
 *
 * @author Admin
 */
public class ScanDeviceAdapter extends RecyclerView.Adapter<ScanDeviceAdapter.ScanDeviceViewHolder>{


    private Context mContext;
    private List<SearchResult> deviceList;

    private OnItemClickListener onItemClickListener;

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    public ScanDeviceAdapter(Context mContext, List<SearchResult> deviceList) {
        this.mContext = mContext;
        this.deviceList = deviceList;
    }

    @NonNull
    @Override
    public ScanDeviceViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_scan_device,viewGroup,false);
        return new ScanDeviceViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ScanDeviceViewHolder scanDeviceViewHolder, int i) {
        scanDeviceViewHolder.nameTv.setText(deviceList.get(i).getName());
        scanDeviceViewHolder.macTv.setText(deviceList.get(i).getAddress());
        scanDeviceViewHolder.rebtTv.setText(Arrays.toString(deviceList.get(i).getScanRecord()));
        scanDeviceViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(onItemClickListener != null){
                    int position = scanDeviceViewHolder.getLayoutPosition();
                    onItemClickListener.onIteClick(position);
                }
            }
        });
    }
	

    @Override
    public int getItemCount() {
        return deviceList.size();
    }

    class ScanDeviceViewHolder extends RecyclerView.ViewHolder{

        TextView nameTv,macTv,rebtTv;


        public ScanDeviceViewHolder(@NonNull View itemView) {
            super(itemView);
            nameTv = itemView.findViewById(R.id.itemBleNameTv);
            macTv = itemView.findViewById(R.id.itemBleMacTv);
            rebtTv = itemView.findViewById(R.id.rebtTv);
        }
    }




}
