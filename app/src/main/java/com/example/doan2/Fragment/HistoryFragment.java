package com.example.doan2.Fragment;


import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.doan2.Adapter.RecycleViewAdapter;
import com.example.doan2.Dal.SQLiteHelper;
import com.example.doan2.R;
import com.example.doan2.model.Item;

import java.util.ArrayList;
import java.util.List;

public class HistoryFragment extends Fragment implements RecycleViewAdapter.ItemListener{
    private RecyclerView recyclerView;
    private RecycleViewAdapter adapter;
    private SQLiteHelper sqLiteHelper;
    private Handler handler = new Handler();
    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            refreshData();
            handler.postDelayed(this, 1000);
        }
    };
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.history_fragment,container,false);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        recyclerView = view.findViewById(R.id.recycleview);
        adapter = new RecycleViewAdapter(getContext());
        sqLiteHelper = new SQLiteHelper(getContext(),"Data.db",null,1);
        adapter.setData(initData());
        LinearLayoutManager manager = new LinearLayoutManager(getContext(),LinearLayoutManager.VERTICAL,false);
        recyclerView.setLayoutManager(manager);
        recyclerView.setAdapter(adapter);

        handler.post(runnable);
    }

    private List<Item> initData() {
        List<Item> items = sqLiteHelper.getAll();
        return items;
    }

    @Override
    public void onItemListener(View view, int position) {

    }

    private void refreshData() {
        List<Item> items = sqLiteHelper.getAll();
        if(items.size() > 12){
            items.remove(items.size()-1);
            sqLiteHelper.delete(items.get(items.size()-1));
        }
        adapter.setData(items);
    }
    @Override
    public void onPause() {
        super.onPause();
        handler.removeCallbacks(runnable);
    }
}
