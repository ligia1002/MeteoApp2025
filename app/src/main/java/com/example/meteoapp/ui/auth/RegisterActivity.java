package com.example.meteoapp.ui.auth;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.Button;
import android.widget.Toast;
import android.content.Intent;

//import com.example.meteoapp.BaseActivity;
import com.google.firebase.messaging.FirebaseMessaging;
import android.util.Log;
import java.util.HashMap;
import java.util.Map;


import androidx.appcompat.app.AppCompatActivity;

import com.example.meteoapp.R;
import com.example.meteoapp.ui.main.MainActivity;
import com.example.meteoapp.model.DevicePreferences;
import com.example.meteoapp.model.Threshold;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Date;

public class RegisterActivity extends AppCompatActivity {
    private EditText emailInput, passwordInput, nicknameInput;
    private Button registerBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        emailInput = findViewById(R.id.emailInput);
        passwordInput = findViewById(R.id.passwordInput);
        nicknameInput = findViewById(R.id.nicknameInput);

        registerBtn = findViewById(R.id.registerBtn);

        registerBtn.setOnClickListener(v -> {
            String email = emailInput.getText().toString().trim();
            String password = passwordInput.getText().toString().trim();
            String[] nickname = new String[1];
            nickname[0] = nicknameInput.getText().toString().trim();

            if (nickname[0].isEmpty()) {
                nickname[0] = "user1002";
            }

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Completează toate câmpurile", Toast.LENGTH_SHORT).show();
                return;
            }

            FirebaseAuth auth = FirebaseAuth.getInstance();
            FirebaseFirestore db = FirebaseFirestore.getInstance();

            auth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            String userId = auth.getCurrentUser().getUid();

                            // ---------- Thresholds ----------
                            String[] sensors = {"temperature", "humidity", "rain", "uv", "pollution"};
                            float[] values = {30f, 40f, 1f, 6f, 50f};
                            String[] conditions = {">", "<", ">", ">", ">"};
                            String[] messages = {
                                    "Temperatura a depășit pragul!",
                                    "Umiditatea a scăzut sub prag.",
                                    "S-a detectat ploaie!",
                                    "Nivel UV ridicat!",
                                    "Nivel ridicat de poluare!"
                            };

                            for (int i = 0; i < sensors.length; i++) {
                                Threshold threshold = new Threshold();
                                threshold.setSensorType(sensors[i]);
                                threshold.setThresholdValue(values[i]);
                                threshold.setCondition(conditions[i]);
                                threshold.setAlarmType("warning");
                                threshold.setMessageTemplate(messages[i]);
                                threshold.setActive(true);
                                //threshold.setCreatedAt(Timestamp.now());
                                //threshold.setUpdatedAt(Timestamp.now());

                                db.collection("users")
                                        .document(userId)
                                        .collection("thresholds")
                                        .document(sensors[i])
                                        .set(threshold);
                            }

                            // ---------- DevicePreferences ----------
                            DevicePreferences prefs = new DevicePreferences();
                            prefs.setNotifyTemperature(true);
                            prefs.setNotifyRain(true);
                            prefs.setNotifyHumidity(true);
                            prefs.setNotifyUv(true);
                            prefs.setNotifyPollution(true);
                            prefs.setCreatedAt(new Date());
                            prefs.setUpdatedAt(new Date());

                            db.collection("users")
                                    .document(userId)
                                    .collection("devicePreferences")
                                    .document("default")
                                    .set(prefs)
                                    .addOnSuccessListener(aVoid -> {
                                        FirebaseMessaging.getInstance().getToken()
                                                .addOnCompleteListener(tokenTask -> {
                                                    if (!tokenTask.isSuccessful()) {
                                                        Log.w("FCM", "Token fetch failed", tokenTask.getException());
                                                        return;
                                                    }

                                                    String token = tokenTask.getResult();
                                                    Log.d("FCM", "Token: " + token);

                                                    db.collection("users")
                                                            .document(userId)
                                                            .update("fcmToken", token)
                                                            .addOnSuccessListener(unused -> Log.d("FCM", "Token salvat cu succes"))
                                                            .addOnFailureListener(e -> Log.e("FCM", "Eroare la salvare token: " + e.getMessage()));
                                                });

                                        Toast.makeText(this, "Cont creat cu succes", Toast.LENGTH_SHORT).show();
                                        startActivity(new Intent(this, MainActivity.class));
                                        finish();
                                    });

                            db.collection("users")
                                    .document(userId)
                                    .set(new HashMap<String, Object>() {{
                                        put("nickname", nickname[0]);
                                    }});
                            Toast.makeText(this, "Cont creat cu succes", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(this, MainActivity.class));
                            finish();
                        } else {
                            Toast.makeText(this, "Eroare: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                        }
                    });
        });
    }
}
