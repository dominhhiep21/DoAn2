package com.example.doan2.model;

public class Item {
    private int id;
    private String time;
    private String location;
    private int temperature;
    private int humidity;
    private int pump;
    public Item() {
    }

    public Item(String time, String location, int temperature, int humidity, int pump) {
        this.time = time;
        this.location = location;
        this.temperature = temperature;
        this.humidity = humidity;
        this.pump = pump;
    }

    public Item(int id, String time, String location, int temperature, int humidity, int pump) {
        this.id = id;
        this.time = time;
        this.location = location;
        this.temperature = temperature;
        this.humidity = humidity;
        this.pump = pump;
    }

    public int getPump() {
        return pump;
    }

    public void setPump(int pump) {
        this.pump = pump;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public int getTemperature() {
        return temperature;
    }

    public void setTemperature(int temperature) {
        this.temperature = temperature;
    }

    public int getHumidity() {
        return humidity;
    }

    public void setHumidity(int humidity) {
        this.humidity = humidity;
    }
}
