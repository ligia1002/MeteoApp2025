package com.example.meteoapp;

import android.app.Application;

import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;

public class MeteoApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        // Initialize Firebase manually with your database URL
//        FirebaseOptions options = new FirebaseOptions.Builder()
//                .setDatabaseUrl("https://meteo-app-2025-default-rtdb.europe-west1.firebasedatabase.app/")
//                .build();
//        try {
//            FirebaseApp.initializeApp(this, options, "MeteoApp");
//        } catch (IllegalStateException e) {
//            // Already initialized
//        }
    }
}
