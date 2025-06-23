package com.example.meteoapp.model;

public class SensorData {
    private double temperature;
    private int humidity;
    private int pollution;
    private double uvIndex;
    private boolean rainDetected;
    private String timestamp;
    private int pollutionLevel;
    private float rainfall;
    private double pressure;
    public SensorData() {} // Required for Firebase

    public double getTemperature() { return temperature; }
    public int getHumidity() { return humidity; }
    public int getPollution() { return pollution; }
    public double getUvIndex() { return uvIndex; }
    public boolean isRainDetected() { return rainDetected; }
    public void setUvIndex(double uvIndex) {
        this.uvIndex = uvIndex;
    }
    public String getTimestamp() { return timestamp; }
    public int getPollutionLevel() {
        return pollutionLevel;
    }

    public double getPressure() {
        return pressure;
    }
    public void setPressure(double pressure) {
        this.pressure = pressure;
    }


    public float getRainfall() {
        return rainfall;
    }

}
