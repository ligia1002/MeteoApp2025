#include <WiFi.h>
#include <HTTPClient.h>
#include <DHT.h>
#include <Wire.h>
#include <Adafruit_Sensor.h>
#include <Adafruit_BMP085_U.h>
#include <NTPClient.h>
#include <WiFiUdp.h>
#include <time.h>

#define DHTPIN       D3
#define DHTTYPE      DHT11
DHT dht(DHTPIN, DHTTYPE);

#define UV_SENSOR    A0
#define RAIN_ANALOG  A1
#define RAIN_DIGITAL D4
#define MQ4_PIN      A2

const char* ssid     = "bt@gov.ia";
const char* password = "23112012";

const char* firebaseHost = "https://meteo-app-2025-default-rtdb.europe-west1.firebasedatabase.app/SensorData.json";

// Inițializare BMP180
Adafruit_BMP085_Unified bmp = Adafruit_BMP085_Unified(10085);

// Obiecte NTP
WiFiUDP ntpUDP;
NTPClient timeClient(ntpUDP, "pool.ntp.org", 3600, 60000); // offset +1h în secunde, update la 60s

void setup() {
  Serial.begin(115200);
  delay(2000);
  dht.begin();

  if (!bmp.begin()) {
    Serial.println(" BMP180 read failure – presiune indisponibilă");
  }

  pinMode(UV_SENSOR,    INPUT);
  pinMode(RAIN_ANALOG,  INPUT);
  pinMode(RAIN_DIGITAL, INPUT);
  pinMode(MQ4_PIN,      INPUT);

  Serial.print("Connecting to Wi-Fi");
  WiFi.begin(ssid, password);
  while (WiFi.status() != WL_CONNECTED) {
    delay(500); Serial.print(".");
  }
  Serial.println("\n Wi-Fi connected!");

  timeClient.begin();
  configTime(10800, 0, "pool.ntp.org");  // Fus orar UTC+1, fără DST
}

void loop() {
  timeClient.update();

  float temp   = dht.readTemperature();
  float hum    = dht.readHumidity();
  int   uvRaw  = analogRead(UV_SENSOR);
  float uvIndex = (uvRaw * 3.3 / 4095.0) * 10.0;

  int rainAnalog  = analogRead(RAIN_ANALOG);
  int rainDigital = digitalRead(RAIN_DIGITAL);
  int mq4Value    = analogRead(MQ4_PIN);

  float pressure = -1;
  sensors_event_t event;
  bmp.getEvent(&event);
  if (event.pressure) {
    pressure = event.pressure;
  }

  if (isnan(temp) || isnan(hum)) {
    Serial.println(" DHT11 read failure – skipping this cycle");
    delay(2000);
    return;
  }

  // Obținem data + ora din sistemul ESP32
  // Obținem data + ora din sistemul ESP32
  struct tm timeinfo;
  if (!getLocalTime(&timeinfo)) {
    Serial.println(" Failed to get time");
    return;
  }

  char timeString[25];
  strftime(timeString, sizeof(timeString), "%b %d %Y %H:%M:%S", &timeinfo);
  String timestamp(timeString);

  Serial.println(" Sending data to Firebase...");

  if (WiFi.status() == WL_CONNECTED) {
    HTTPClient http;
    http.begin(firebaseHost);
    http.addHeader("Content-Type", "application/json");

    String payload = "{";
    payload += "\"temperature\":" + String(temp) + ",";
    payload += "\"humidity\":" + String(hum) + ",";
    payload += "\"uvIndex\":" + String(uvIndex) + ",";
    payload += "\"rainDetected\":" + String(rainDigital == 0 ? "true" : "false") + ",";
    payload += "\"rainfall\":" + String(rainAnalog) + ",";
    payload += "\"pollutionLevel\":" + String(mq4Value) + ",";
    if (pressure > 0) {
      payload += "\"pressure\":" + String(pressure) + ",";
    }
    payload += "\"timestamp\":\"" + timestamp + "\"";
    payload += "}";

    int code = http.POST(payload);
    Serial.print("Firebase response code: "); Serial.println(code);
    Serial.print("Payload: "); Serial.println(payload);

    http.end();
  }

  delay(60000);
}
