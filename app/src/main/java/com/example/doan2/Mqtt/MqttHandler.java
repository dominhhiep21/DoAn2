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
    private int temperature, humidity, rain, light,pressure,pump;
    private OnMessageReceivedListener messageReceivedListener;
    private String brokerUrl;
    private String clientId;
    private int reconnectAttempts = 0;
    private static final int MAX_RECONNECT_ATTEMPTS = 5;

    public interface OnMessageReceivedListener {
        void onMessageReceived(int temperature,int humidity,int rain ,int light,int pressure,int pump);
    }

    public void setOnMessageReceivedListener(OnMessageReceivedListener listener) {
        this.messageReceivedListener = listener;
    }
    public void connect(String brokerUrl, String clientId) {
        this.brokerUrl = brokerUrl;
        this.clientId = clientId;
        try {
            MemoryPersistence persistence = new MemoryPersistence();

            client = new MqttClient(brokerUrl, clientId, persistence);

            // Set up the connection options
            MqttConnectOptions connectOptions = new MqttConnectOptions();
            connectOptions.setCleanSession(true);

            // Connect to the broker
            client.connect(connectOptions);
            callback();
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
    public void reconnect() {
        if (reconnectAttempts < MAX_RECONNECT_ATTEMPTS) {
            reconnectAttempts++;
            try {
                Thread.sleep(2000);  // Đợi 2 giây trước khi thử kết nối lại
                connect(brokerUrl, clientId);  // Thử kết nối lại
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("Max reconnect attempts reached. Could not reconnect.");
        }
    }
}