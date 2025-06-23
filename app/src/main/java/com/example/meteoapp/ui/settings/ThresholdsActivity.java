package com.example.meteoapp.ui.settings;

import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.example.meteoapp.BaseActivity;
import com.example.meteoapp.R;
import com.google.android.material.slider.Slider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;
import com.example.meteoapp.BaseActivity;

public class ThresholdsActivity extends BaseActivity {

    private Slider sliderTemp, sliderHumidity;
    private Button saveButton;
    private FirebaseFirestore db;
    private String uid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_thresholds);

        // Legare UI
        sliderTemp = findViewById(R.id.sliderTemperature);
        sliderHumidity = findViewById(R.id.sliderHumidity);
        saveButton = findViewById(R.id.buttonSaveThresholds);

        // Firebase
        db = FirebaseFirestore.getInstance();
        uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // Încarcă pragurile existente
        loadThresholds();

        // La salvare
        saveButton.setOnClickListener(v -> saveThresholds());
    }

    private void loadThresholds() {
        DocumentReference tempRef = db.collection("users")
                .document(uid)
                .collection("thresholds")
                .document("temperature");

        DocumentReference humRef = db.collection("users")
                .document(uid)
                .collection("thresholds")
                .document("humidity");

        tempRef.get().addOnSuccessListener(snapshot -> {
            if (snapshot.exists()) {
                Float val = snapshot.getDouble("thresholdValue").floatValue();
                sliderTemp.setValue(val);
            }
        });

        humRef.get().addOnSuccessListener(snapshot -> {
            if (snapshot.exists()) {
                Float val = snapshot.getDouble("thresholdValue").floatValue();
                sliderHumidity.setValue(val);
            }
        });
    }

    private void saveThresholds() {
        float tempValue = sliderTemp.getValue();
        float humValue = sliderHumidity.getValue();

        Map<String, Object> tempData = new HashMap<>();
        tempData.put("thresholdValue", tempValue);
        tempData.put("sensorType", "temperature");
        tempData.put("condition", ">");
        tempData.put("alarmType", "warning");
        tempData.put("messageTemplate", "Temperatura a depășit pragul!");
        tempData.put("active", true);

        Map<String, Object> humData = new HashMap<>();
        humData.put("thresholdValue", humValue);
        humData.put("sensorType", "humidity");
        humData.put("condition", "<");
        humData.put("alarmType", "warning");
        humData.put("messageTemplate", "Umiditatea a scăzut sub prag.");
        humData.put("active", true);

        db.collection("users").document(uid).collection("thresholds").document("temperature")
                .set(tempData);

        db.collection("users").document(uid).collection("thresholds").document("humidity")
                .set(humData)
                .addOnSuccessListener(aVoid -> Toast.makeText(this, "Praguri salvate", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(this, "Eroare: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }
}

