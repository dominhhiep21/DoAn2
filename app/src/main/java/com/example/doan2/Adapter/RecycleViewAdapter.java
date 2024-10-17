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
    private ItemListener itemListener;
    public RecycleViewAdapter(Context mContext) {
        this.mContext = mContext;
        this.mList = new ArrayList<>();
    }
    public void setData(List<Item> list){
        this.mList = list;
        notifyDataSetChanged();
    }
    public void setItemListener(ItemListener itemListener){
        this.itemListener = itemListener;
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
        holder.tempTv.setText("Temp:"+item.getTemperature()+"℃");
        holder.humiTv.setText("Humi:"+item.getHumidity()+"%");
        if(item.getPump()==1){
            holder.pumpTv.setText("Pump:ON");
        } else if (item.getPump()==0) {
            holder.pumpTv.setText("Pump:OFF");
        }
    }

    @Override
    public int getItemCount() {
        if(!mList.isEmpty())
            return mList.size();
        return 0;
    }

    public class ItemViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        private TextView timeItemTv,locationItemTv,tempTv,humiTv,pumpTv;
        public ItemViewHolder(@NonNull View itemView) {
            super(itemView);
            timeItemTv = itemView.findViewById(R.id.time_item);
            locationItemTv = itemView.findViewById(R.id.location_item);
            tempTv = itemView.findViewById(R.id.temp_item);
            humiTv = itemView.findViewById(R.id.humi_item);
            pumpTv = itemView.findViewById(R.id.pump_item);
        }

        @Override
        public void onClick(View v) {
            if(itemListener!=null){
                itemListener.onItemListener(v,getAdapterPosition());
            }
        }
    }
    public interface ItemListener{
        void onItemListener(View view,int position);
    }
}

