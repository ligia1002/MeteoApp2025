package com.example.meteoapp.repository;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;

import com.example.meteoapp.model.SensorData;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class FirebaseRepository {

    private final DatabaseReference sensorRef;
    private final MutableLiveData<SensorData> latestData = new MutableLiveData<>();

    public FirebaseRepository() {
        sensorRef = FirebaseDatabase.getInstance("https://meteo-app-2025-default-rtdb.europe-west1.firebasedatabase.app/")
                .getReference("SensorData");
        listenLastEntry();
    }

    private void listenLastEntry() {
        sensorRef.limitToLast(1).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot node : snapshot.getChildren()) {
                    SensorData d = node.getValue(SensorData.class);
                    latestData.postValue(d);
                }
            }
            @Override public void onCancelled(@NonNull DatabaseError error) { }
        });
    }

    public MutableLiveData<SensorData> getLatestData() { return latestData; }
}
