package com.example.meteoapp.ui.main;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.example.meteoapp.model.SensorData;
import com.example.meteoapp.repository.FirebaseRepository;

public class MainViewModel extends ViewModel {

    private final FirebaseRepository repo = new FirebaseRepository();

    public LiveData<SensorData> getData() {
        return repo.getLatestData();
    }
}
