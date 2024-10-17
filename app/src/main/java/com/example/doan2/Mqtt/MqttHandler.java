package com.example.doan2.Mqtt;

import android.content.Context;
import android.os.Handler;
import android.util.Log;

import com.example.doan2.Dal.SQLiteHelper;
import com.example.doan2.model.Item;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MqttHandler {

    private MqttClient client;
    private SQLiteHelper sqLiteHelper;
    private Handler handler = new Handler();
    private int temperature, humidity, rain, light,pressure,pump;
    private OnMessageReceivedListener messageReceivedListener;

    public MqttHandler(Context context) {
        sqLiteHelper = new SQLiteHelper(context,"Data.db",null,1);
    }

    public interface OnMessageReceivedListener {
        void onMessageReceived(int temperature,int humidity,int rain ,int light,int pressure,int pump);
    }

    public void setOnMessageReceivedListener(OnMessageReceivedListener listener) {
        this.messageReceivedListener = listener;
    }
    public void connect(String brokerUrl, String clientId) {
        try {
            // Set up the persistence layer
            MemoryPersistence persistence = new MemoryPersistence();

            // Initialize the MQTT client
            client = new MqttClient(brokerUrl, clientId, persistence);

            // Set up the connection options
            MqttConnectOptions connectOptions = new MqttConnectOptions();
            connectOptions.setCleanSession(true);

            // Connect to the broker
            client.connect(connectOptions);
            callback();
            //startSavingData();
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    public void disconnect() {
        try {
            client.disconnect();
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    public void publish(String topic, String message) {
        try {
            MqttMessage mqttMessage = new MqttMessage(message.getBytes());
            client.publish(topic, mqttMessage);
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    public void subscribe(String topic) {
        try {
            client.subscribe(topic);
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    public void callback(){
        client.setCallback(new MqttCallbackExtended() {
            @Override
            public void connectComplete(boolean reconnect, String serverURI) {

            }

            @Override
            public void connectionLost(Throwable cause) {

            }

            @Override
            public void messageArrived(String topic, MqttMessage message) throws Exception {
                String payload = new String(message.getPayload());

                try {
                    // Phân tích dữ liệu thành JSON
                    JSONObject jsonData = new JSONObject(payload);

                    temperature = jsonData.getInt("temperature");
                    humidity = jsonData.getInt("humidity");
                    rain = jsonData.getInt("rain");
                    light = jsonData.getInt("light");
                    pressure = jsonData.getInt("pressure");
                    pump = jsonData.getInt("pump");
                    if (messageReceivedListener != null) {
                        messageReceivedListener.onMessageReceived(temperature,humidity,rain,light,pressure,pump);
                    }


                } catch (Exception e) {
                    // Xử lý lỗi phân tích JSON
                    e.printStackTrace();
                    System.out.println("Error parsing JSON data.");
                }
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {

            }
        });
    }
    private void startSavingData() {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                // Lấy thời gian hiện tại và chuyển thành chuỗi
                String timestamp = getCurrentTimestamp();

                // Lưu dữ liệu vào SQLite cùng với thời gian
                sqLiteHelper.addItem(new Item(timestamp,"Hanoi",temperature,humidity,pump));

                // Lặp lại mỗi 5 phút
                handler.postDelayed(this, 60 * 1000); // 5 phút
            }
        }, 60 * 1000); // 5 phút
    }
    private String getCurrentTimestamp() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        return sdf.format(new Date());
    }
}