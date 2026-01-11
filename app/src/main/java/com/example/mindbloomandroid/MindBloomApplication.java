package com.example.mindbloomandroid;

import android.app.Application;
import com.google.firebase.FirebaseApp;
import com.google.firebase.database.FirebaseDatabase;

/**
 * Custom Application class for Firebase initialization
 * This ensures Firebase is properly configured before any activity starts
 */
public class MindBloomApplication extends Application {

    private static boolean isPersistenceEnabled = false;

    @Override
    public void onCreate() {
        super.onCreate();

        // Initialize Firebase
        try {
            FirebaseApp.initializeApp(this);

            // Enable Firebase Realtime Database persistence (offline support)
            // Can only be called once per app lifecycle
            if (!isPersistenceEnabled) {
                try {
                    FirebaseDatabase database = FirebaseDatabase.getInstance();
                    database.setPersistenceEnabled(true);
                    isPersistenceEnabled = true;
                    android.util.Log.d("MindBloomApp", "✅ Firebase persistence enabled");
                } catch (Exception e) {
                    android.util.Log.w("MindBloomApp", "⚠️ Persistence already enabled: " + e.getMessage());
                }
            }

            android.util.Log.d("MindBloomApp", "✅ Firebase initialized successfully");

            // Test database connection
            FirebaseDatabase.getInstance().getReference(".info/connected")
                .addValueEventListener(new com.google.firebase.database.ValueEventListener() {
                    @Override
                    public void onDataChange(com.google.firebase.database.DataSnapshot snapshot) {
                        boolean connected = snapshot.getValue(Boolean.class);
                        if (connected) {
                            android.util.Log.d("MindBloomApp", "✅ Connected to Firebase Database");
                        } else {
                            android.util.Log.w("MindBloomApp", "⚠️ Not connected to Firebase Database");
                        }
                    }

                    @Override
                    public void onCancelled(com.google.firebase.database.DatabaseError error) {
                        android.util.Log.e("MindBloomApp", "❌ Firebase connection listener failed: " + error.getMessage());
                    }
                });

        } catch (Exception e) {
            android.util.Log.e("MindBloomApp", "❌ Error initializing Firebase: " + e.getMessage(), e);
        }
    }
}

