package com.example.mindbloomandroid.service;


import com.google.firebase.database.DataSnapshot;
import com.google. firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com. google.firebase.database.FirebaseDatabase;
import com.google. firebase.database.ValueEventListener;
import com.example.mindbloomandroid.model.SleepEntry;

import java.util.ArrayList;
import java.util.List;

public class SleepTrackerService {
    private DatabaseReference sleepEntriesRef;

    public SleepTrackerService() {
        sleepEntriesRef = FirebaseDatabase.getInstance().getReference("sleep_entries");
    }


    public void saveSleepEntry(SleepEntry entry, OnCompleteListener listener) {
        String entryId = sleepEntriesRef.push().getKey();
        if (entryId != null) {
            entry.setSleepEntryId(entryId);
            entry.setCreatedAt(System.currentTimeMillis());

            android.util.Log.d("SleepService", "📝 Saving sleep entry - ID: " + entryId + ", Duration: " + entry.getSleepDurationHours() + " hrs, UserID: " + entry.getUserId());

            sleepEntriesRef.child(entryId).setValue(entry)
                    .addOnSuccessListener(aVoid -> {
                        android.util.Log.d("SleepService", "✅ Sleep entry saved successfully - ID: " + entryId);
                        listener.onSuccess();
                    })
                    .addOnFailureListener(e -> {
                        android.util.Log.e("SleepService", "❌ Failed to save sleep entry: " + e.getMessage());
                        listener.onError(e.getMessage());
                    });
        } else {
            android.util.Log.e("SleepService", "❌ Failed to generate sleep entry ID");
            listener.onError("Failed to generate sleep entry ID");
        }
    }

    public void getUserSleepEntries(String userId, OnSleepEntriesLoadedListener listener) {
        android.util.Log.d("SleepService", "📖 Loading sleep entries for user: " + userId);

        // Use addValueEventListener for REAL-TIME updates (not addListenerForSingleValueEvent)
        sleepEntriesRef.orderByChild("userId").equalTo(userId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        List<SleepEntry> entries = new ArrayList<>();
                        android.util.Log.d("SleepService", "📥 Received " + dataSnapshot.getChildrenCount() + " sleep entries (REAL-TIME)");

                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            SleepEntry entry = snapshot.getValue(SleepEntry.class);
                            if (entry != null) {
                                entry.setSleepEntryId(snapshot.getKey());
                                entries.add(entry);
                            }
                        }
                        android.util.Log.d("SleepService", "✅ Loaded " + entries.size() + " sleep entries (REAL-TIME UPDATE)");
                        listener.onSleepEntriesLoaded(entries);
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                        android.util.Log.e("SleepService", "❌ Error loading sleep entries: " + error.getMessage());
                        listener.onError(error.getMessage());
                    }
                });
    }


    public void getLatestSleepEntry(String userId, OnSleepEntryLoadedListener listener) {
        sleepEntriesRef. orderByChild("userId").equalTo(userId)
                .limitToLast(1)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            SleepEntry entry = snapshot.getValue(SleepEntry.class);
                            if (entry != null) {
                                entry.setSleepEntryId(snapshot.getKey());
                                listener.onSleepEntryLoaded(entry);
                                return;
                            }
                        }
                        listener.onError("No sleep entry found");
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                        listener.onError(error.getMessage());
                    }
                });
    }


    public void deleteSleepEntry(String entryId, OnCompleteListener listener) {
        sleepEntriesRef.child(entryId).removeValue()
                .addOnSuccessListener(aVoid -> listener.onSuccess())
                .addOnFailureListener(e -> listener.onError(e. getMessage()));
    }

    // Interfaces
    public interface OnCompleteListener {
        void onSuccess();
        void onError(String error);
    }

    public interface OnSleepEntryLoadedListener {
        void onSleepEntryLoaded(SleepEntry entry);
        void onError(String error);
    }

    public interface OnSleepEntriesLoadedListener {
        void onSleepEntriesLoaded(List<SleepEntry> entries);
        void onError(String error);
    }
}