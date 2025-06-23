package com.example.meteoapp.ui.settings;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.example.meteoapp.BaseActivity;
import com.example.meteoapp.R;
import com.example.meteoapp.databinding.ActivitySettingsBinding;
import com.example.meteoapp.ui.main.FactsActivity;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class SettingsActivity extends BaseActivity {

    private ActivitySettingsBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySettingsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Butonul pentru toggle master (doar demo)
        binding.switchAlarmsEnabled.setOnCheckedChangeListener((b, on) -> {
            String msg = on ? "Alarme activate" : "Alarme dezactivate";
            Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
        });

//        binding.navigationView.setNavigationItemSelectedListener(item -> {
//            int id = item.getItemId();
//            if (id == R.id.nav_news) {
//                Toast.makeText(this, "Se deschid sugestiile noastre...", Toast.LENGTH_SHORT).show();
//                startActivity(new Intent(this, FactsActivity.class));
//            } else if (id == R.id.nav_thresholds) {
//                Toast.makeText(this, "Se încarcă pragurile...", Toast.LENGTH_SHORT).show();
//                startActivity(new Intent(this, ThresholdsActivity.class));
//            }
//            return true;
//        });


    }

}
