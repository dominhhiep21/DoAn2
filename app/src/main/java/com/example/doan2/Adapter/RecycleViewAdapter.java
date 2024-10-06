package com.example.doan2.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.doan2.R;
import com.example.doan2.model.Item;

import java.util.ArrayList;
import java.util.List;

public class RecycleViewAdapter extends RecyclerView.Adapter<RecycleViewAdapter.ItemViewHolder> {
    private Context mContext;
    private List<Item> mList;

    public RecycleViewAdapter(Context mContext) {
        this.mContext = mContext;
        this.mList = new ArrayList<>();
    }
    public void setData(List<Item> list){
        this.mList = list;
        notifyDataSetChanged();
    }
    @NonNull
    @Override
    public ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.his_item,parent,false);
        return new ItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ItemViewHolder holder, int position) {
        Item item = mList.get(position);
        if(item==null) return;
        holder.timeItemTv.setText(item.getTime());
        holder.locationItemTv.setText(item.getLocation());
        holder.tempItemTv.setText(item.getTemperature()+"℃");
        holder.humItemTv.setText(item.getHumidity()+"%");
    }

    @Override
    public int getItemCount() {
        if(!mList.isEmpty())
            return mList.size();
        return 0;
    }

    public class ItemViewHolder extends RecyclerView.ViewHolder{
        private TextView timeItemTv,locationItemTv,tempItemTv,humItemTv;
        public ItemViewHolder(@NonNull View itemView) {
            super(itemView);
            timeItemTv = itemView.findViewById(R.id.time_item);
            locationItemTv = itemView.findViewById(R.id.location_item);
            tempItemTv = itemView.findViewById(R.id.temperature_item);
            humItemTv = itemView.findViewById(R.id.humidity_item);
        }
    }
}
