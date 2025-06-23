package com.example.meteoapp.model;

import com.google.firebase.firestore.ServerTimestamp;
import java.util.Date;

public class DevicePreferences {

    private boolean notifyTemperature;
    private boolean notifyRain;
    private boolean notifyPollution;
    private boolean notifyHumidity;
    private boolean notifyUv;

    @ServerTimestamp
    private Date createdAt;

    @ServerTimestamp
    private Date updatedAt;

    public DevicePreferences() {
        // Firestore needs a public no-arg constructor
    }

    // Getters
    public boolean isNotifyTemperature() {
        return notifyTemperature;
    }

    public boolean isNotifyRain() {
        return notifyRain;
    }

    public boolean isNotifyPollution() {
        return notifyPollution;
    }

    public boolean isNotifyUv() {
        return notifyUv;
    }

    public boolean isNotifyHumidity() {
        return notifyHumidity;
    }
    public Date getCreatedAt() {
        return createdAt;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    // Setters
    public void setNotifyTemperature(boolean notifyTemperature) {
        this.notifyTemperature = notifyTemperature;
    }

    public void setNotifyRain(boolean notifyRain) {
        this.notifyRain = notifyRain;
    }

    public void setNotifyPollution(boolean notifyPollution) {
        this.notifyPollution = notifyPollution;
    }
    public void setNotifyHumidity(boolean notifyHumidity) {
        this.notifyHumidity = notifyHumidity;
    }
    public void setNotifyUv(boolean notifyUv) {
        this.notifyUv = notifyUv;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }
}
