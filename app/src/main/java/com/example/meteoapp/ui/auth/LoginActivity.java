package com.example.meteoapp.ui.auth;

// importuri necesare
import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import com.example.meteoapp.model.DevicePreferences;

//import com.example.meteoapp.BaseActivity;
import com.example.meteoapp.R;
import com.example.meteoapp.ui.main.MainActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.HashMap;
import java.util.Map;
import java.util.Date;


public class LoginActivity extends AppCompatActivity {

    private EditText etEmail, etPassword;
    private Button btnLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);

        findViewById(R.id.btnCreateAccount).setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
        });

        btnLogin.setOnClickListener(v -> {
            String email = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            String uid3 = FirebaseAuth.getInstance().getCurrentUser().getUid();
                            FirebaseFirestore db3 = FirebaseFirestore.getInstance();

                            // ---------- DevicePreferences ----------
                            DevicePreferences prefs = new DevicePreferences();
                            prefs.setNotifyTemperature(true);
                            prefs.setNotifyRain(true);
                            prefs.setNotifyHumidity(true);
                            prefs.setNotifyUv(true);
                            prefs.setNotifyPollution(true);
                            prefs.setUpdatedAt(new java.util.Date());

                            db3.collection("users")
                                    .document(uid3)
                                    .collection("devicePreferences")
                                    .document("default")
                                    .set(prefs)
                                    .addOnSuccessListener(aVoid -> {
                                        Toast.makeText(this, "Autentificare reușită", Toast.LENGTH_SHORT).show();
                                        startActivity(new Intent(this, MainActivity.class));
                                        finish();
                                    })
                                    .addOnFailureListener(e -> {
                                        Toast.makeText(this, "Autentificat, dar eroare la setarea preferințelor: " + e.getMessage(), Toast.LENGTH_LONG).show();
                                        startActivity(new Intent(this, MainActivity.class));
                                        finish();
                                    });

                        } else {
                            Toast.makeText(this, "Autentificare eșuată: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        });

    }
}
