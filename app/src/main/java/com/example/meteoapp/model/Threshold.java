package com.example.meteoapp.model;
import com.google.firebase.firestore.ServerTimestamp;
import java.util.Date;

public class Threshold {

    private String sensorType; // ex: "temperature"
    private float thresholdValue;
    private String condition;  // ">", "<", ">=", "<="
    private String alarmType;
    private String messageTemplate;
    private boolean active;
    @ServerTimestamp
    private com.google.firebase.Timestamp createdAt;

    @ServerTimestamp
    private com.google.firebase.Timestamp updatedAt;


    // Constructor gol (necesar pentru Firebase)
    public Threshold() {}

    // Getters
    public String getSensorType() {
        return sensorType;
    }

    public float getThresholdValue() {
        return thresholdValue;
    }

    public String getCondition() {
        return condition;
    }

    public String getAlarmType() {
        return alarmType;
    }

    public String getMessageTemplate() {
        return messageTemplate;
    }

    public boolean isActive() {
        return active;
    }

    public com.google.firebase.Timestamp getCreatedAt() {
        return createdAt;
    }

    public com.google.firebase.Timestamp getUpdatedAt() {
        return updatedAt;
    }

    // Setters
    public void setSensorType(String sensorType) {
        this.sensorType = sensorType;
    }

    public void setThresholdValue(float thresholdValue) {
        this.thresholdValue = thresholdValue;
    }

    public void setCondition(String condition) {
        this.condition = condition;
    }

    public void setAlarmType(String alarmType) {
        this.alarmType = alarmType;
    }

    public void setMessageTemplate(String messageTemplate) {
        this.messageTemplate = messageTemplate;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public void setCreatedAt(com.google.firebase.Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public void setUpdatedAt(com.google.firebase.Timestamp updatedAt) {
        this.updatedAt = updatedAt;
    }
}

