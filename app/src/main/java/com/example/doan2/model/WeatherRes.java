package com.example.doan2.model;

import java.util.List;

public class WeatherRes {
    private Main main;
    private List<Weather> weather;
    private Wind wind;
    private String name;

    public WeatherRes(Main main, List<Weather> weather, Wind wind, String name) {
        this.main = main;
        this.weather = weather;
        this.wind = wind;
        this.name = name;
    }

    public Main getMain() {
        return main;
    }

    public void setMain(Main main) {
        this.main = main;
    }

    public List<Weather> getWeather() {
        return weather;
    }

    public void setWeather(List<Weather> weather) {
        this.weather = weather;
    }

    public Wind getWind() {
        return wind;
    }

    public void setWind(Wind wind) {
        this.wind = wind;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
