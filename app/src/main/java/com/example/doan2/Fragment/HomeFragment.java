package com.example.doan2.Fragment;


import static androidx.core.content.ContextCompat.getSystemService;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;


import com.example.doan2.Api.ApiService;
import com.example.doan2.Dal.SQLiteHelper;
import com.example.doan2.Mqtt.MqttHandler;
import com.example.doan2.R;
import com.example.doan2.model.Item;
import com.example.doan2.model.WeatherRes;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeFragment extends Fragment implements LocationListener {
    private TextView locationTv, timeTv, statusTv, temperatureTv, humidityTv, pressureTv, lightTv, rainTv, pumpTv, windTv;
    private ImageView pumpIcon;
    private double lat, lon;
    private LocationManager locationManager;
    private Handler handler;
    private Runnable runnable;
    private static final String BROKER_URL = "tcp://broker.emqx.io:1883";
    private static final String CLIENT_ID = "AndroidClient";
    private MqttHandler mqttHandler;
    private ExecutorService executorService;
    private SQLiteHelper sqLiteHelper;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.home_fragment, container, false);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        executorService = Executors.newSingleThreadExecutor();
        sqLiteHelper = new SQLiteHelper(getContext(), "Data.db", null, 1);
        initView(view);
        if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        } else {
            setTime();
            setLocation();
        }
        connectMQTT();
    }

    private void connectMQTT() {
        mqttHandler = new MqttHandler();
        handler = new Handler();
        runnable = new Runnable() {
            @Override
            public void run() {
                if (!mqttHandler.isConnected()) {
                    Log.e("MQTT", "Connection lost. Reconnecting...");
                    mqttHandler.connect(BROKER_URL, CLIENT_ID);
                    mqttHandler.subscribe("nhom8/sensor");
                }
                handler.postDelayed(this, 5000); // Kiểm tra lại sau 5 giây
            }
        };
        handler.post(runnable);

        pumpIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String currentTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss",
                        Locale.getDefault()).format(new Date());
                if (pumpTv.getText().equals("ON")) {
                    publishMessage("nhom8/sensor", "0");
                    pumpTv.setText("OFF");
                } else if (pumpTv.getText().equals("OFF")) {
                    publishMessage("nhom8/sensor", "1");
                    pumpTv.setText("ON");
                }
                int temp = Integer.parseInt(temperatureTv.getText().toString().replace("℃", ""));
                double humi = Double.parseDouble(humidityTv.getText().toString().replace("%", ""));
                int pumpStatus = 0;
                if (pumpTv.getText().toString().equals("ON")) {
                    pumpStatus = 1;
                } else if (pumpTv.getText().toString().equals("OFF")) {
                    pumpStatus = 0;
                }
                String locat = locationTv.getText().toString();
                try {
                    sqLiteHelper.addItem(new Item(currentTime, locat, temp, humi, pumpStatus));
                } catch (Exception e) {
                    Toast.makeText(getContext(), "Save data failed !!!", Toast.LENGTH_SHORT).show();
                }
            }
        });

        mqttHandler.setOnMessageReceivedListener(new MqttHandler.OnMessageReceivedListener() {
            @Override
            public void onMessageReceived(int temperature, double humidity, int rain, int light, int pressure, int pump) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        temperatureTv.setText(temperature + "℃");
                        humidityTv.setText(humidity + "%");
                        pressureTv.setText((pressure / 1000) + " kPa");
                        if (rain == 0) {
                            rainTv.setText("YES");
                        } else if (rain == 1) {
                            rainTv.setText("NO");
                        }
                        if (light == 0) {
                            lightTv.setText("YES");
                        } else if (light == 1) {
                            lightTv.setText("NO");
                        }
//                        if (pump == 1) {
//                            pumpTv.setText("ON");
//                        } else if (pump == 0) {
//                            pumpTv.setText("OFF");
//                        }
                    }
                });
            }
        });
    }

    private void publishMessage(final String topic, final String message) {
        mqttHandler.publish(topic, message);
    }

    private void subscribeToTopic(String topic) {
        mqttHandler.subscribe(topic);
    }

    private void setTime() {
        handler = new Handler();
        runnable = new Runnable() {
            @Override
            public void run() {
                String currentTime = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault()).format(new Date());
                timeTv.setText("Update at : " + currentTime);
                handler.postDelayed(this, 1000);
            }
        };

        if (handler != null) {
            handler.post(runnable);
        }
    }

    private void callApi() {
        ApiService.apiService.getData(lat, lon, "cd43023033fba323c69a16922df80e5d").enqueue(new Callback<WeatherRes>() {
            @Override
            public void onResponse(Call<WeatherRes> call, Response<WeatherRes> response) {
                WeatherRes weatherRes = response.body();
                if (weatherRes != null) {
                    windTv.setText(weatherRes.getWind().getSpeed() + " m/s");
                    //pressureTv.setText(weatherRes.getMain().getPressure()+" hPa");
                    statusTv.setText(weatherRes.getWeather().get(0).getMain());
                    locationTv.setText(weatherRes.getName());
                    Log.e("Location", "" + weatherRes.getName());
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
        pumpIcon = view.findViewById(R.id.pump_icon);
    }

    @Override
    public void onLocationChanged(@NonNull Location location) {
        lat = location.getLatitude();
        Log.e("Lat:", "" + lat);
        lon = location.getLongitude();
        Log.e("Lon:", "" + lon);
        callApi();
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

    @Override
    public void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(runnable);
        mqttHandler.disconnect();
        executorService.shutdown();
    }
}

