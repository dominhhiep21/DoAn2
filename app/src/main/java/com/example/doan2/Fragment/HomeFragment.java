package com.example.doan2.Fragment;


import static androidx.core.content.ContextCompat.getSystemService;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;


import com.example.doan2.Api.ApiService;
import com.example.doan2.R;
import com.example.doan2.model.WeatherRes;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeFragment extends Fragment implements LocationListener {
    private TextView locationTv,timeTv,statusTv,
            temperatureTv,humidityTv,pressureTv,lightTv,rainTv,pumpTv,windTv;
    private double lat,lon;
    private LocationManager locationManager;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.home_fragment,container,false);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initView(view);
        if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        } else {
            setLocation();
        }
        callApi();
    }

    private void callApi() {
        ApiService.apiService.getData(lat,lon,"cd43023033fba323c69a16922df80e5d").enqueue(new Callback<WeatherRes>() {
            @Override
            public void onResponse(Call<WeatherRes> call, Response<WeatherRes> response) {
                WeatherRes weatherRes = response.body();
                if(weatherRes!=null){
                    windTv.setText(weatherRes.getWind().getSpeed()+" m/s");
                    pressureTv.setText(weatherRes.getMain().getPressure()+" hPa");
                    statusTv.setText(weatherRes.getWeather().get(0).getMain());
                    locationTv.setText(weatherRes.getName());
                }
            }

            @Override
            public void onFailure(Call<WeatherRes> call, Throwable t) {
                Toast.makeText(getContext(), "Call Api failed !!!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setLocation() {
        locationManager = (LocationManager) requireContext().getSystemService(Context.LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 0, this);
    }

    private void initView(View view) {
        locationTv = view.findViewById(R.id.location);
        timeTv = view.findViewById(R.id.time);
        statusTv = view.findViewById(R.id.status);
        temperatureTv = view.findViewById(R.id.temperature);
        humidityTv = view.findViewById(R.id.humiData);
        pressureTv = view.findViewById(R.id.pressData);
        lightTv = view.findViewById(R.id.lightData);
        rainTv = view.findViewById(R.id.rainData);
        pumpTv = view.findViewById(R.id.pumpData);
        windTv = view.findViewById(R.id.windData);
    }

    @Override
    public void onLocationChanged(@NonNull Location location) {
        lat = location.getLatitude();
        lon = location.getLongitude();
    }

    @Override
    public void onLocationChanged(@NonNull List<Location> locations) {
        LocationListener.super.onLocationChanged(locations);
    }

    @Override
    public void onFlushComplete(int requestCode) {
        LocationListener.super.onFlushComplete(requestCode);
    }

    @Override
    public void onProviderEnabled(@NonNull String provider) {
        LocationListener.super.onProviderEnabled(provider);
    }

    @Override
    public void onProviderDisabled(@NonNull String provider) {
        LocationListener.super.onProviderDisabled(provider);
    }
}
