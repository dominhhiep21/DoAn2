package com.example.doan2.Mqtt;

import android.content.Context;
import android.os.Handler;
import android.util.Log;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.json.JSONObject;

public class MqttHandler {

    private MqttClient client;
    private int temperature, humidity, rain, light, pressure, pump;
    private OnMessageReceivedListener messageReceivedListener;
    private String brokerUrl;
    private String clientId;
    private int reconnectAttempts = 0;
    private static final int MAX_RECONNECT_ATTEMPTS = 5;
    private Handler handler = new Handler();
    private Runnable reconnectRunnable;

    public interface OnMessageReceivedListener {
        void onMessageReceived(int temperature, int humidity, int rain, int light, int pressure, int pump);
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

            MqttConnectOptions connectOptions = new MqttConnectOptions();
            connectOptions.setCleanSession(true);

            client.connect(connectOptions);
            reconnectAttempts = 0; // Reset số lần thử khi kết nối thành công
            setupCallback();

        } catch (MqttException e) {
            Log.e("MQTT", "Failed to connect. Attempting to reconnect...");
            reconnect();
        }
    }

    private void setupCallback() {
        client.setCallback(new MqttCallbackExtended() {
            @Override
            public void connectComplete(boolean reconnect, String serverURI) {
                if (reconnect) {
                    Log.i("MQTT", "Reconnected to: " + serverURI);
                } else {
                    Log.i("MQTT", "Connected to: " + serverURI);
                }
                reconnectAttempts = 0; // Reset số lần thử kết nối
            }

            @Override
            public void connectionLost(Throwable cause) {
                Log.e("MQTT", "Connection lost. Reconnecting...");
                reconnect();
            }

            @Override
            public void messageArrived(String topic, MqttMessage message) {
                String payload = new String(message.getPayload());
                Log.d("MQTT", "Message received on topic: " + topic + " with payload: " + payload);

                try {
                    JSONObject jsonData = new JSONObject(payload);
                    temperature = jsonData.getInt("temperature");
                    humidity = jsonData.getInt("humidity");
                    rain = jsonData.getInt("rain");
                    light = jsonData.getInt("light");
                    pressure = jsonData.getInt("pressure");
                    pump = jsonData.getInt("pump");

                    if (messageReceivedListener != null) {
                        messageReceivedListener.onMessageReceived(temperature, humidity, rain, light, pressure, pump);
                    }
                } catch (Exception e) {
                    Log.e("MQTT", "Error parsing JSON data: " + e.getMessage());
                }
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {
                // No additional action needed here
            }
        });
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
            if (client.isConnected()) {
                MqttMessage mqttMessage = new MqttMessage(message.getBytes());
                client.publish(topic, mqttMessage);
            } else {
                Log.e("MQTT", "Client is not connected. Unable to publish message.");
            }
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    public void subscribe(String topic) {
        try {
            if (client.isConnected()) {
                client.subscribe(topic);
            } else {
                Log.e("MQTT", "Client is not connected. Unable to subscribe to topic.");
            }
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    private void reconnect() {
        if (reconnectAttempts < MAX_RECONNECT_ATTEMPTS) {
            reconnectAttempts++;
            reconnectRunnable = new Runnable() {
                @Override
                public void run() {
                    try {
                        Log.d("MQTT", "Trying to reconnect...");
                        connect(brokerUrl, clientId);  // Thử kết nối lại
                    } catch (Exception e) {
                        Log.e("MQTT", "Reconnect attempt failed: " + e.getMessage());
                    }
                }
            };
            handler.postDelayed(reconnectRunnable, 5000);  // Thử lại sau 5 giây
        } else {
            Log.e("MQTT", "Max reconnect attempts reached. Could not reconnect.");
        }
    }

    public boolean isConnected() {
        return client != null && client.isConnected();
    }
}
