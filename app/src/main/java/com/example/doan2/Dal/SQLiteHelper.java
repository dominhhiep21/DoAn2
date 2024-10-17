package com.example.doan2.Dal;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

import com.example.doan2.model.Item;

import java.util.ArrayList;
import java.util.List;

public class SQLiteHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "Data.db";
    private static int DATABASE_VERSION = 1;
    public SQLiteHelper(@Nullable Context context, @Nullable String name, @Nullable SQLiteDatabase.CursorFactory factory, int version) {
        super(context, DATABASE_NAME, factory, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String sql = "CREATE TABLE data ("+
                "id INTEGER PRIMARY KEY AUTOINCREMENT,"+
                "time TEXT,location TEXT,temperature INTEGER," +
                "humidity INTEGER,pump INTEGER)";
        db.execSQL(sql);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    @Override
    public void onOpen(SQLiteDatabase db) {
        super.onOpen(db);
    }

    public List<Item> getAll(){
        List<Item> list = new ArrayList<>();
        SQLiteDatabase sqLiteDatabase = getReadableDatabase();
        String order = "time DESC";
        Cursor rs = sqLiteDatabase.query("data",null,null,null
                ,null,null,order);
        while ((rs!=null)&& rs.moveToNext()){
            int id = rs.getInt(0);
            String time = rs.getString(1);
            String location = rs.getString(2);
            int temp = rs.getInt(3);
            int humi = rs.getInt(4);
            int pump = rs.getInt(5);
            list.add(new Item(id,time,location,temp,humi,pump));
        }
        return list;
    }
    public long addItem(Item i){
        ContentValues contentValues = new ContentValues();
        //contentValues.put("id",i.getId());
        contentValues.put("time",i.getTime());
        contentValues.put("location",i.getLocation());
        contentValues.put("temperature",i.getTemperature());
        contentValues.put("humidity",i.getHumidity());
        contentValues.put("pump",i.getPump());
        SQLiteDatabase db = getWritableDatabase();
        return db.insert("data",null,contentValues);
    }
    public void delete(Item i){
        String sql = "DELETE FROM data WHERE id=?";
        String agrs[]={String.valueOf(i.getId())};
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL(sql,agrs);
    }
    public void deleteAll(Item i){
        String sql = "DELETE FROM data";
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL(sql);
    }
}
