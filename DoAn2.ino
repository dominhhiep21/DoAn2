
#include <WiFi.h>
#include <PubSubClient.h>
#include <ArduinoJson.h>
#include <DHT.h>
#include <Wire.h>
#include <Adafruit_BMP085.h>
                                                                                                                                                                                                                                           
// GPIO cho các cảm biến và relay
#define camBienMua 23
#define camBienAnhSang 5
#define RELAY_PIN 18  // Chân điều khiển relay

// WiFi
const char *ssid = "Lam Hiep";            // Tên mạng WiFi của bạn
const char *password = "Lamhiep@321";  // Mật khẩu WiFi của bạn

// MQTT Broker
const char *mqtt_broker = "broker.emqx.io";
const char *topic = "esp32/led";  // Đổi tên topic nếu cần
const char *mqtt_relay_topic = "esp32/relay";  // MQTT topic cho relay
const char *mqtt_username = "emqx";
const char *mqtt_password = "public";
const int mqtt_port = 1883;

// DHT sensor setup
#define DHTPIN 15     // DHT sensor is connected to GPIO 15
#define DHTTYPE DHT11 // DHT 11
DHT dht(DHTPIN, DHTTYPE);  // Initialize DHT sensor
int relay;
// BMP180 sensor setup
Adafruit_BMP085 bmp;
float seaLevelPressure = 101325.0; // Mực nước biển chuẩn

WiFiClient espClient;
PubSubClient client(espClient);

void setup() {
  // Khởi động cổng serial
  Serial.begin(9600);
  delay(1000);  // Delay để ổn định

  // Kết nối WiFi
  WiFi.begin(ssid, password);
  while (WiFi.status() != WL_CONNECTED) {
    delay(500);
    Serial.println("Connecting to WiFi...");
  }
  Serial.println("Connected to the WiFi network");

  // Cài đặt các chân cảm biến và relay
  pinMode(camBienAnhSang, INPUT);
  pinMode(camBienMua, INPUT);
  pinMode(RELAY_PIN, OUTPUT);  // Relay pin là OUTPUT
  digitalWrite(RELAY_PIN, LOW);  // Tắt relay ban đầu

  // DHT sensor initialization
  dht.begin();

  // BMP180 initialization
  if (!bmp.begin()) {
    Serial.println("Không tìm thấy cảm biến BMP180, vui lòng kiểm tra kết nối!");
    while (1) {}
  }
  Serial.println("Cảm biến BMP180 đã sẵn sàng.");

  // Kết nối đến MQTT broker
  client.setServer(mqtt_broker, mqtt_port);
  client.setCallback(callback);
  while (!client.connected()) {
    String client_id = "esp32-client-";  // Đổi từ esp8266 sang esp32
    client_id += String(WiFi.macAddress());
    Serial.printf("The client %s connects to the public MQTT broker\n", client_id.c_str());
    if (client.connect(client_id.c_str(), mqtt_username, mqtt_password)) {
      Serial.println("Public EMQX MQTT broker connected");
    } else {
      Serial.print("Failed with state ");
      Serial.print(client.state());
      delay(2000);
    }
  }

  // Subscribe thêm topic cho relay
  client.subscribe(mqtt_relay_topic);
}

// Hàm callback khi nhận được message từ MQTT
void callback(char *topic, byte *payload, unsigned int length) {
  Serial.print("Message arrived in topic: ");
  Serial.println(topic);
  Serial.print("Message: ");
  String message;
  for (int i = 0; i < length; i++) {
    message += (char)payload[i];  // Chuyển đổi byte sang string
  }
Serial.println(message);

  // Điều khiển relay dựa trên message nhận được
  if (String(topic) == mqtt_relay_topic) {
    if (message == "1") {
      digitalWrite(RELAY_PIN, HIGH); 
      relay = 1; // Bật relay
      Serial.println("Relay ON");
    } else if (message == "0") {
      digitalWrite(RELAY_PIN, LOW);   // Tắt relay
      Serial.println("Relay OFF");
      relay = 0;
    }
  }
}

void loop() {
  client.loop();  // Đảm bảo MQTT client vẫn hoạt động

  // Đọc dữ liệu từ DHT sensor
  float h = dht.readHumidity();    // Đọc độ ẩm
  float t = dht.readTemperature(); // Đọc nhiệt độ tính bằng Celsius
  float f = dht.readTemperature(true);

  // Kiểm tra xem các giá trị có hợp lệ không
  if (isnan(h) || isnan(t) || isnan(f)) {
    Serial.println("Failed to read from DHT sensor!");
    return;
  }

  int mua = digitalRead(camBienMua);
  int anhsang = digitalRead(camBienAnhSang);

  // Đọc nhiệt độ từ cảm biến BMP180
  float temperatureBMP = bmp.readTemperature();

  // Đọc áp suất từ cảm biến BMP180
  float pressure = bmp.readPressure();

  // Hiệu chỉnh áp suất dựa trên áp suất tại mực nước biển thực tế
  float adjustedPressure = pressure * (seaLevelPressure / 101325.0);

  // In kết quả ra Serial Monitor
  Serial.print("Humidity: ");
  Serial.print(h);
  Serial.print(" %\t");
  Serial.print("Temperature: ");
  Serial.print(t);
  Serial.print(" *C ");
  Serial.print(f);
  Serial.print(" *F\t");
  Serial.print("  Heat index: ");
  float hic = dht.computeHeatIndex(t, h, false);
  Serial.print(hic);
  Serial.print(" *C ");
  Serial.println();

  Serial.print("Mưa: ");
  Serial.println(mua);
  Serial.print("Ánh sáng: ");
  Serial.println(anhsang);

  Serial.print("Nhiệt độ BMP180: ");
  Serial.print(temperatureBMP);
  Serial.println(" °C");

  Serial.print("Áp suất: ");
  Serial.print(adjustedPressure);
  Serial.println(" Pa");

  // Tạo dữ liệu JSON để gửi
  DynamicJsonDocument data(512);
  data["humidity"] = h;
  data["temperature"] = t;
  data["rain"] = mua;
  data["light"] = anhsang;
  data["bmp_temperature"] = temperatureBMP;
  data["pressure"] = adjustedPressure;
  data["pump"] = relay;
  char json_string[512];
  serializeJson(data, json_string);  // Serialize JSON
  Serial.println(json_string);

  // Publish dữ liệu JSON lên topic
  client.publish(topic, json_string, false);

  delay(200);  // Delay để gửi dữ liệu đều đặn
}  