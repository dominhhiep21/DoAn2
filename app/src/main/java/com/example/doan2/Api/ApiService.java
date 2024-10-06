package com.example.doan2.Api;

import com.example.doan2.model.WeatherRes;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;
import retrofit2.http.Query;

/*https://api.openweathermap.org/data/2.5/weather?lat=20.9678407&lon=105.8741382&appid=cd43023033fba323c69a16922df80e5d*/
public interface ApiService {
    Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").create();

    ApiService apiService = new Retrofit.Builder()
            .baseUrl("https://api.openweathermap.org/")
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
            .create(ApiService.class);

    @GET("data/2.5/weather")
    Call<WeatherRes> getData(@Query("lat") double lat,
                             @Query("lon") double lon,
                             @Query("appid") String appid);
}
