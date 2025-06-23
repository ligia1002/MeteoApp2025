package com.example.meteoapp.ui.main;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import androidx.appcompat.content.res.AppCompatResources;
import android.graphics.drawable.Drawable;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.data.ScatterDataSet;
import com.github.mikephil.charting.data.ScatterData;
import com.github.mikephil.charting.charts.ScatterChart;
import com.github.mikephil.charting.data.CombinedData;

import androidx.annotation.NonNull;
import com.github.mikephil.charting.data.Entry;
import java.util.ArrayList;
import java.util.List;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.charts.CombinedChart;

import androidx.annotation.Nullable;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.firestore.SetOptions;
import com.bumptech.glide.Glide;
import java.util.Collections;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.MenuItem;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import android.net.Uri;
import android.widget.ImageView;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import com.github.mikephil.charting.formatter.ValueFormatter;

import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import androidx.appcompat.app.AppCompatActivity;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import android.graphics.Color;


import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.lifecycle.ViewModelProvider;
import android.widget.Switch;
import com.example.meteoapp.R;
import com.example.meteoapp.databinding.ActivityMainBinding;
import com.example.meteoapp.model.SensorData;
import com.example.meteoapp.repository.FirebaseRepository;
import com.example.meteoapp.ui.auth.LoginActivity;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessaging;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.net.URL;
import java.net.HttpURLConnection;
import java.io.OutputStream;
import com.example.meteoapp.ui.settings.ThresholdsActivity;
import com.example.meteoapp.ui.main.FactsActivity;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.example.meteoapp.BaseActivity;

import android.app.AlertDialog;
import android.widget.EditText;
import android.text.InputType;

public class MainActivity extends BaseActivity {

    private ActivityMainBinding binding;
    private MainViewModel viewModel;
    private static final int PICK_IMAGE_REQUEST = 101;
    private Uri imageUri;

    private DrawerLayout drawerLayout;
    private Toolbar toolbar;
    private NavigationView navigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        com.google.firebase.FirebaseApp.initializeApp(this);

        // Verificare autentificare
        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        // Cerere permisiune notificari pentru Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.POST_NOTIFICATIONS},
                        1001);
            }
        }

        // Obtine token-ul FCM (pentru testare sau backend)
        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        String token = task.getResult();
                        String uid2 = FirebaseAuth.getInstance().getCurrentUser().getUid();
                        sendTokenToBackend(uid2, token);
                    }
                });

        // Initializare UI
        SwitchMaterial switchMaster = binding.navigationView.getHeaderView(0).findViewById(R.id.switchAlarmsEnabled);
        SwitchMaterial switchRain = binding.navigationView.getHeaderView(0).findViewById(R.id.switchRain);
        SwitchMaterial switchTemp = binding.navigationView.getHeaderView(0).findViewById(R.id.switchTemp);
        SwitchMaterial switchHumidity = binding.navigationView.getHeaderView(0).findViewById(R.id.switchHumidity);
        SwitchMaterial switchPollution = binding.navigationView.getHeaderView(0).findViewById(R.id.switchPollution);
        SwitchMaterial switchUv = binding.navigationView.getHeaderView(0).findViewById(R.id.switchUv);

        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("users")
                .document(uid)
                .collection("devicePreferences")
                .document("default")
                .get()
                .addOnSuccessListener(snapshot -> {
                    if (snapshot.exists()) {
                        boolean rain = snapshot.getBoolean("notifyRain") != null && snapshot.getBoolean("notifyRain");
                        boolean temp = snapshot.getBoolean("notifyTemperature") != null && snapshot.getBoolean("notifyTemperature");
                        boolean hum  = snapshot.getBoolean("notifyHumidity") != null && snapshot.getBoolean("notifyHumidity");
                        boolean pol  = snapshot.getBoolean("notifyPollution") != null && snapshot.getBoolean("notifyPollution");
                        boolean uv   = snapshot.getBoolean("notifyUv") != null && snapshot.getBoolean("notifyUv");

                        boolean allEnabled = rain || temp || hum || pol || uv;

                        switchMaster.setChecked(allEnabled);
                        switchRain.setChecked(rain);
                        switchTemp.setChecked(temp);
                        switchHumidity.setChecked(hum);
                        switchPollution.setChecked(pol);
                        switchUv.setChecked(uv);

                        enableAlarmSwitches(allEnabled,
                                switchRain, switchTemp, switchHumidity, switchPollution, switchUv);
                    }
                });

        switchMaster.setOnCheckedChangeListener((btn, isChecked) -> {
                    enableAlarmSwitches(isChecked, switchRain, switchTemp, switchHumidity, switchPollution, switchUv);
                });
        switchRain.setOnCheckedChangeListener((buttonView, isChecked) ->
                updateDevicePreferencesInFirestore(
                        isChecked,
                        switchTemp.isChecked(),
                        switchHumidity.isChecked(),
                        switchPollution.isChecked(),
                        switchUv.isChecked()
                )
        );
        switchTemp.setOnCheckedChangeListener((buttonView, isChecked) ->
        updateDevicePreferencesInFirestore(
                switchRain.isChecked(),
                isChecked,
                switchHumidity.isChecked(),
                switchPollution.isChecked(),
                switchUv.isChecked()
        )
        );
        switchHumidity.setOnCheckedChangeListener((buttonView, isChecked) ->
                updateDevicePreferencesInFirestore(
                        switchRain.isChecked(),
                        switchTemp.isChecked(),
                        isChecked,
                        switchPollution.isChecked(),
                        switchUv.isChecked()
                )
        );
        switchPollution.setOnCheckedChangeListener((buttonView, isChecked) ->
                updateDevicePreferencesInFirestore(
                        switchRain.isChecked(),
                        switchTemp.isChecked(),
                        switchHumidity.isChecked(),
                        isChecked,
                        switchUv.isChecked()
                )
        );
        switchUv.setOnCheckedChangeListener((buttonView, isChecked) ->
                updateDevicePreferencesInFirestore(
                        switchRain.isChecked(),
                        switchTemp.isChecked(),
                        switchHumidity.isChecked(),
                        switchPollution.isChecked(),
                        isChecked
                )
        );
        switchMaster.setOnCheckedChangeListener((btn, isChecked) -> {
            enableAlarmSwitches(isChecked, switchRain, switchTemp, switchHumidity, switchPollution, switchUv);

            if (!isChecked) {
                updateDevicePreferencesInFirestore(false, false, false, false, false);
            }
        });

        setSupportActionBar(binding.toolbar);
        NavigationView navView = binding.navigationView;
        View headerView = navView.getHeaderView(0);
        TextView emailTextView = headerView.findViewById(R.id.emailTextView);
        String email = FirebaseAuth.getInstance().getCurrentUser().getEmail();
        emailTextView.setText(email);

        String dataAzi = new SimpleDateFormat("EEEE, d MMMM yyyy", new Locale("ro"))
                .format(new Date());
        getSupportActionBar().setTitle(dataAzi);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(android.R.drawable.ic_menu_sort_by_size);

        viewModel = new ViewModelProvider(this).get(MainViewModel.class);
        viewModel.getData().observe(this, this::updateUI);

        binding.navigationView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_news) {
                Toast.makeText(this, "Se deschid sugestiile noastre...", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(this, FactsActivity.class));
            } else if (id == R.id.nav_thresholds) {
                Toast.makeText(this, "Se încarcă pragurile...", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(this, ThresholdsActivity.class));
            }
            binding.drawerLayout.closeDrawer(binding.navigationView);
            return true;
        });

        View logoutFooter = getLayoutInflater().inflate(R.layout.logout_footer, binding.navigationView, false);
        binding.navigationView.addView(logoutFooter);

        TextView logoutTextView = logoutFooter.findViewById(R.id.nav_logout);
        logoutTextView.setOnClickListener(v -> {
            String uid2 = FirebaseAuth.getInstance().getCurrentUser().getUid();
            FirebaseFirestore db2 = FirebaseFirestore.getInstance();

            // Dezactivează notificările
            Map<String, Object> updates = new HashMap<>();
            updates.put("notifyRain", false);
            updates.put("notifyTemperature", false);
            updates.put("notifyHumidity", false);
            updates.put("notifyPollution", false);
            updates.put("notifyUv", false);
            updates.put("updatedAt", new Date());

            db2.collection("users")
                    .document(uid2)
                    .collection("devicePreferences")
                    .document("default")
                    .update(updates)
                    .addOnCompleteListener(task -> {
                        // Continuă cu sign out doar după actualizare
                        FirebaseAuth.getInstance().signOut();
                        startActivity(new Intent(MainActivity.this, LoginActivity.class));
                        finish();
                    });
        });
        ImageView profileImage = binding.navigationView.getHeaderView(0).findViewById(R.id.profileImage);
        profileImage.setOnClickListener(v -> openImagePicker());
        loadProfileImageIfExists();
        loadNicknameIfExists();
        loadUvDataFromRealtimeDb();

    }
    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.setType("image/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();

            // Afișează în UI
            ImageView profileImage = binding.navigationView.getHeaderView(0).findViewById(R.id.profileImage);
            profileImage.setImageURI(imageUri);

            // Încarcă în Firebase
            uploadProfileImage(imageUri);
        }
    }


    private void enableAlarmSwitches(boolean enabled, SwitchMaterial... switches)
    {
            for (SwitchMaterial s : switches) {
                s.setEnabled(enabled);
                if (!enabled) {
                    s.setChecked(false);
                }
            }
        }
        private void sendTokenToBackend(String uid, String token) {
        new Thread(() -> {
            try {
                URL url = new URL("http://10.0.2.2:3000/registerToken"); // localhost în emulator
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json; utf-8");
                conn.setDoOutput(true);

                String jsonInput = String.format("{\"uid\":\"%s\", \"token\":\"%s\"}", new Object[]{uid, token});

                try (OutputStream os = conn.getOutputStream()) {
                    byte[] input = jsonInput.getBytes("utf-8");
                    os.write(input, 0, input.length);
                }

                int code = conn.getResponseCode();
                Log.d("FCM_SYNC", "Răspuns server: " + code);
            } catch (Exception e) {
                Log.e("FCM_SYNC", "Eroare trimitere token", e);
            }
        }).start();
    }
    private void updateUI(SensorData data) {
        if (data != null) {
            int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
            boolean isNight = (hour < 6 || hour >= 21);

            binding.tvWeatherMain.setText(data.isRainDetected() ? "Ploaie" : "Senin");
            binding.tvTemp.setText(data.getTemperature() + " °C");

            if (data.isRainDetected() && isNight) {
                binding.bgImage.setImageResource(R.drawable.bg_rain_night);
                binding.weatherAnimation.setAnimation("rain_animation.json");
            } else if (data.isRainDetected()) {
                binding.bgImage.setImageResource(R.drawable.bg_rain);
                binding.weatherAnimation.setAnimation("rain_animation.json");
            } else if (isNight) {
                binding.bgImage.setImageResource(R.drawable.bg_clear_night);
                binding.weatherAnimation.setAnimation("moon_animation.json");
            } else {
                binding.bgImage.setImageResource(R.drawable.bg_sky);
                binding.weatherAnimation.setAnimation("sun_animation.json");
            }
            binding.weatherAnimation.playAnimation();
            String pressure = String.format(Locale.getDefault(), "%.2f hPa", data.getPressure());
            String humidityLevel = data.getHumidity() + "% - " + interpretHumidity(data.getHumidity());
            String aqi = data.getPollutionLevel() + " AQI - " + interpretAQI(data.getPollutionLevel());
            String uv = data.getUvIndex() + " - " + interpretUV(data.getUvIndex());
            String rain = data.getRainfall() + " mm - " + interpretRain(data.getRainfall());
            binding.tvHumidityValue.setText(data.getHumidity() + " %");
            binding.tvHumidityInterpretation.setText(interpretHumidity(data.getHumidity()));
            binding.tvPollutionValue.setText(data.getPollutionLevel() + " AQI");
            binding.tvPollutionInterpretation.setText(interpretAQI(data.getPollutionLevel()));
            binding.tvPressureValue.setText(pressure);
            binding.tvPressureInterpretation.setText(interpretPressure(data.getPressure()));
            int uvPercent = calculateUVPercentage(data.getUvIndex());
            binding.tvUvValue.setText(uvPercent + "%");
            binding.tvUvInterpretation.setText(interpretUV(data.getUvIndex()));
            int rainPercent = calculateRainPercentage(data.getRainfall());
            binding.tvRainValue.setText(rainPercent + "%");
            binding.tvRainInterpretation.setText(interpretRain(data.getRainfall()));
        }
    }
    private void uploadProfileImage(Uri uri) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null || uri == null) return;

        try {
            String base64 = resizeAndConvertToBase64(uri);
            saveProfileBase64ToFirestore(base64);
            loadProfileImageIfExists();
            Toast.makeText(this, "Imagine salvată!", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Log.e("IMG_CONVERT", "Eroare imagine: " + e.getMessage(), e);
            Toast.makeText(this, "Eroare imagine: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
    private String resizeAndConvertToBase64(Uri uri) throws Exception {
        InputStream imageStream = getContentResolver().openInputStream(uri);
        Bitmap original = BitmapFactory.decodeStream(imageStream);
        Bitmap resized = Bitmap.createScaledBitmap(original, 50, 50, true);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        resized.compress(Bitmap.CompressFormat.PNG, 100, baos);
        byte[] imageBytes = baos.toByteArray();

        return Base64.encodeToString(imageBytes, Base64.DEFAULT);
    }

    private void saveProfileBase64ToFirestore(String base64) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;

        Map<String, Object> data = new HashMap<>();
        data.put("imageBase64", base64);
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("users")
                .document(user.getUid())
                .set(data, SetOptions.merge())
                .addOnSuccessListener(aVoid -> Log.d("FIRESTORE", "Poza salvată"))
                .addOnFailureListener(e -> Log.e("FIRESTORE_ERR", "Eșec salvare: " + e.getMessage()));
    }
    private void loadProfileImageIfExists() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;

        FirebaseFirestore.getInstance().collection("users")
                .document(user.getUid())
                .get()
                .addOnSuccessListener(snapshot -> {
                    if (snapshot.exists()) {
                        String base64 = snapshot.getString("imageBase64");
                        if (base64 != null) {
                            byte[] imageBytes = Base64.decode(base64, Base64.DEFAULT);
                            Bitmap bmp = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
                            ImageView profileImage = binding.navigationView.getHeaderView(0).findViewById(R.id.profileImage);
                            profileImage.setImageBitmap(bmp);
                        }
                    }
                });


    }
    private void loadNicknameIfExists() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;

        FirebaseFirestore.getInstance().collection("users")
                .document(user.getUid())
                .get()
                .addOnSuccessListener(snapshot -> {
                    if (snapshot.exists()) {
                        String nickname = snapshot.getString("nickname");
                        TextView nicknameTextView = binding.navigationView.getHeaderView(0).findViewById(R.id.nicknameTextView);
                        if (nickname != null && !nickname.isEmpty()) {
                            nicknameTextView.setText(nickname);
                        } else {
                            nicknameTextView.setText("Fără nickname");
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("FIRESTORE", "Eroare la obținerea nickname-ului: " + e.getMessage());
                });
    }

    private void saveNicknameToFirestore(String nickname) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;

        FirebaseFirestore.getInstance()
                .collection("users")
                .document(user.getUid())
                .set(Collections.singletonMap("nickname", nickname), SetOptions.merge())
                .addOnSuccessListener(aVoid ->
                        Log.d("FIRESTORE", "Nickname salvat cu succes.")
                )
                .addOnFailureListener(e ->
                        Log.e("FIRESTORE", "Eroare salvare nickname: " + e.getMessage())
                );
    }


    private void updateDevicePreferencesInFirestore(
            boolean notifyRain,
            boolean notifyTemp,
            boolean notifyHumidity,
            boolean notifyPollution,
            boolean notifyUv
    ) {
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("users")
                .document(uid)
                .collection("devicePreferences")
                .document("default")
                .update(
                        "notifyRain", notifyRain,
                        "notifyTemperature", notifyTemp,
                        "notifyHumidity", notifyHumidity,
                        "notifyPollution", notifyPollution,
                        "notifyUv", notifyUv,
                        "updatedAt", new Date()
                )
                .addOnSuccessListener(unused ->
                        Log.d("FIRESTORE", "Preferințele au fost salvate.")
                )
                .addOnFailureListener(e ->
                        Log.e("FIRESTORE", "Eroare la salvarea preferințelor: " + e.getMessage())
                );
    }


    private String interpretHumidity(int humidity) {
        if (humidity < 30) return "Aer uscat";
        if (humidity < 60) return "Umiditate normală";
        return "Umiditate ridicată";
    }
    private String interpretPressure(double pressure) {
        if (pressure < 1000) return "Presiune scăzută";
        if (pressure <= 1020) return "Presiune normală";
        return "Presiune ridicată";
    }

    private int calculateRainPercentage(float mm) {
        final float MAX = 4000f;
        if (mm >= MAX) return 0;          // complet uscat
        if (mm <= 2000f) return 100;          // apă maximă
        return Math.round(((MAX - mm) / MAX) * 100);
    }


    private int calculateUVPercentage(double uvIndex) {
        final double MAX_UV = 11.0;
        if (uvIndex <= 0) return 0;
        if (uvIndex >= MAX_UV) return 100;
        return (int) Math.round((uvIndex / MAX_UV) * 100);
    }

    private String interpretAQI(int value) {
        if (value <= 50) return "Calitate excelentă";
        if (value <= 100) return "Acceptabilă";
        if (value <= 150) return "Nesănătos pt. sensibili";
        if (value <= 200) return "Nesănătos";
        if (value <= 300) return "Foarte nesănătos";
        return "Calitate a aerului periculosa";
    }

    private String interpretUV(double uv) {
        if (uv < 3) return "Risc UV scăzut";
        if (uv < 6) return "Risc UV moderat";
        if (uv < 8) return "Risc UV ridicat";
        return "Risc UV extrem";
    }

    private String interpretRain(float mm) {
        if (mm >= 4000) return "Fără precipitații";
        if (mm > 3000) return "Ploaie ușoară";
        if (mm > 2000) return "Ploaie moderată";
        return "Ploaie abundentă";
    }

    @Override
    public boolean onSupportNavigateUp() {
        binding.drawerLayout.openDrawer(binding.navigationView);
        return true;
    }

    public void onNicknameClick(View view) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Editează nickname-ul");

        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        input.setHint("Introdu un nou nickname");
        builder.setView(input);

        builder.setPositiveButton("Salvează", (dialog, which) -> {
            String newNickname = input.getText().toString().trim();
            if (!newNickname.isEmpty()) {
                FirebaseFirestore.getInstance()
                        .collection("users")
                        .document(user.getUid())
                        .update("nickname", newNickname)
                        .addOnSuccessListener(aVoid -> {
                            TextView nicknameTextView = binding.navigationView.getHeaderView(0).findViewById(R.id.nicknameTextView);
                            nicknameTextView.setText(newNickname);
                            Toast.makeText(this, "Nickname actualizat!", Toast.LENGTH_SHORT).show();
                        })
                        .addOnFailureListener(e -> Toast.makeText(this, "Eroare: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }
        });

        builder.setNegativeButton("Anulează", (dialog, which) -> dialog.cancel());
        builder.show();
    }


    private void loadUvDataFromRealtimeDb() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("SensorData");

        ref.limitToLast(10).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<Entry> entries = new ArrayList<>();
                int index = 0;

                Log.d("UV_INDEX_CHECK", "Total copii: " + snapshot.getChildrenCount());

                for (DataSnapshot child : snapshot.getChildren()) {
                    Map<String, Object> data = (Map<String, Object>) child.getValue();

                    if (data != null) {
                        Object uvValue = data.get("uvIndex"); // sau "UVIndex"
                        if (uvValue == null) uvValue = data.get("UVIndex");

                        if (uvValue instanceof Number) {
                            float uv = ((Number) uvValue).floatValue();
                            if(uv<=8)
                            entries.add(new Entry(index++, uv));
                            else
                                entries.add(new Entry(index++, 8));
                        } else {
                            Log.w("UV_INDEX_CHECK", "Valoare UV lipsă sau invalidă pentru nod: " + child.getKey());
                        }
                    }
                }

                Log.d("UV_INDEX_CHECK", "Valori UV extrase: " + entries.size());

                if (!entries.isEmpty()) {
                    drawUvChart(entries, -1); // sau folosește predictionIndex dacă ai
                } else {
                    Log.w("UV_GRAPH", "Nu s-au găsit valori UV.");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("UV_GRAPH", "Eroare Firebase: " + error.getMessage());
            }
        });
    }
    private List<String> generateFakeTimestamps(int count) {
        List<String> labels = new ArrayList<>();
        Calendar cal = Calendar.getInstance();

        for (int i = 0; i < count; i++) {
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
            labels.add(0, sdf.format(cal.getTime())); // adăugăm la început pentru invers
            cal.add(Calendar.MINUTE, -5);
        }

        return labels;
    }
    private void drawUvChart(List<Entry> entries, int predictionIndex) {
        CombinedChart chart = findViewById(R.id.uvChart);

        LineDataSet lineSet = new LineDataSet(entries, "UV");
        lineSet.setColor(0xFF6ad2f7);
        lineSet.setDrawCircles(false);
        lineSet.setLineWidth(6f);
        lineSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        lineSet.setDrawValues(false);

        List<Entry> greenDots = new ArrayList<>();
        List<Entry> yellowDots = new ArrayList<>();
        List<Entry> redDots = new ArrayList<>();

        for (Entry e : entries) {
            float y = e.getY();
            if (y < 2f) greenDots.add(e);
            else if (y < 6f) yellowDots.add(e);
            else redDots.add(e);
        }

        ScatterDataSet greenSet = new ScatterDataSet(greenDots, "");
        greenSet.setColor(Color.GREEN);
        greenSet.setScatterShape(ScatterChart.ScatterShape.CIRCLE);
        greenSet.setScatterShapeSize(25f);
        greenSet.setDrawValues(false);

        ScatterDataSet yellowSet = new ScatterDataSet(yellowDots, "");
        yellowSet.setColor(Color.YELLOW);
        yellowSet.setScatterShape(ScatterChart.ScatterShape.CIRCLE);
        yellowSet.setScatterShapeSize(32f);
        yellowSet.setDrawValues(false);

        ScatterDataSet redSet = new ScatterDataSet(redDots, "");
        redSet.setColor(Color.RED);
        redSet.setScatterShape(ScatterChart.ScatterShape.CIRCLE);
        redSet.setScatterShapeSize(40f);
        redSet.setDrawValues(false);

        // 3. Axa OY
        YAxis yAxis = chart.getAxisLeft();
        yAxis.setAxisMinimum(-1.0f);
        yAxis.setAxisMaximum(8f);
        yAxis.setTextColor(0xffffffff);
        chart.getAxisRight().setEnabled(false);

        // 4. Axa OX
        XAxis xAxis = chart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f);
        xAxis.setTextColor(0xffffffff);
        xAxis.setDrawGridLines(false);
        xAxis.setValueFormatter(new ValueFormatter() {
            @Override
            public String getAxisLabel(float value, AxisBase axis) {
                try {
                    int minutesAgo = (entries.size() - 1) - (int) value;
                    Calendar cal = Calendar.getInstance();
                    cal.add(Calendar.MINUTE, -minutesAgo * 85);
                    return new SimpleDateFormat("HH:mm", Locale.getDefault()).format(cal.getTime());
                } catch (Exception e) {
                    return "";
                }
            }
        });

        // 5. Asamblare
        LineData lineData = new LineData(lineSet);
        ScatterData scatterData = new ScatterData(greenSet, yellowSet, redSet);

        CombinedData combinedData = new CombinedData();
        combinedData.setData(lineData);      // linia albastră
        combinedData.setData(scatterData);   // bulinele colorate

        chart.setData(combinedData);


        chart.getDescription().setText("Radiații UV");
        chart.getDescription().setTextSize(12f);
        chart.setDrawGridBackground(false);
        chart.setDrawBorders(false);
        chart.invalidate();
    }




}
