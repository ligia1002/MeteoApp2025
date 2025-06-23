package com.example.meteoapp.ui.alarms;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.example.meteoapp.databinding.ActivityAlarmsBinding;

public class AlarmsActivity extends AppCompatActivity {

    private ActivityAlarmsBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAlarmsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.tvPlaceholder.setText("Lista alertelor va apărea aici");
    }
}
