package com.example.mindbloomandroid.service;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.example.mindbloomandroid.model.JournalEntry;

import java.util.ArrayList;
import java.util.List;


public class JournalService {
    private DatabaseReference journalRef;

    public JournalService() {
        journalRef = FirebaseDatabase.getInstance().getReference("journal_entries");
    }


    public void createJournal(JournalEntry entry, OnCompleteListener listener) {
        String journalId = journalRef.push().getKey();
        if (journalId != null) {
            entry.setJournalId(journalId);
            
            android.util.Log.d("JournalService", "📝 Creating journal - ID: " + journalId + ", UserID: " + entry.getUserId());

            journalRef.child(journalId).setValue(entry)
                    .addOnSuccessListener(aVoid -> {
                        android.util.Log.d("JournalService", "✅ Journal saved successfully - ID: " + journalId);
                        listener.onSuccess(journalId);
                    })
                    .addOnFailureListener(e -> {
                        android.util.Log.e("JournalService", "❌ Failed to save journal: " + e.getMessage());
                        listener.onFailure(e.getMessage());
                    });
        } else {
            android.util.Log.e("JournalService", "❌ Failed to generate journal ID");
            listener.onFailure("Failed to generate journal ID");
        }
    }


    public void getJournalByUserId(String userId, OnJournalLoadedListener listener) {
        android.util.Log.d("JournalService", "📖 Loading journal for user: " + userId);

        journalRef.orderByChild("userId").equalTo(userId)
                .limitToLast(1)  // Get most recent entry
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        android.util.Log.d("JournalService", "📥 Received " + dataSnapshot.getChildrenCount() + " journal entries");

                        if (dataSnapshot.exists()) {
                            for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                JournalEntry journal = snapshot.getValue(JournalEntry.class);
                                if (journal != null) {
                                    journal.setJournalId(snapshot.getKey());
                                    android.util.Log.d("JournalService", "✅ Journal loaded - ID: " + snapshot.getKey());
                                    listener.onSuccess(journal);
                                    return;
                                }
                            }
                        }
                        // No journal found - return null
                        android.util.Log.d("JournalService", "ℹ️ No journal found for user");
                        listener.onSuccess(null);
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                        android.util.Log.e("JournalService", "❌ Error loading journal: " + error.getMessage());
                        listener.onFailure(error.getMessage());
                    }
                });
    }


    public void updateJournal(String journalId, JournalEntry entry, OnCompleteListener listener) {
        if (journalId != null) {
            journalRef.child(journalId).setValue(entry)
                    .addOnSuccessListener(aVoid -> listener.onSuccess("Journal updated"))
                    .addOnFailureListener(e -> listener.onFailure(e.getMessage()));
        } else {
            listener.onFailure("Journal ID is null");
        }
    }


    public void deleteJournal(String journalId, OnCompleteListener listener) {
        journalRef.child(journalId).removeValue()
                .addOnSuccessListener(aVoid -> listener.onSuccess("Journal deleted"))
                .addOnFailureListener(e -> listener.onFailure(e.getMessage()));
    }


    public void getAllJournalsByUserId(String userId, OnJournalsLoadedListener listener) {
        journalRef.orderByChild("userId").equalTo(userId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        List<JournalEntry> journals = new ArrayList<>();
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            JournalEntry journal = snapshot.getValue(JournalEntry.class);
                            if (journal != null) {
                                journal.setJournalId(snapshot.getKey());
                                journals.add(journal);
                            }
                        }
                        listener.onSuccess(journals);
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                        listener.onFailure(error.getMessage());
                    }
                });
    }

    // Interfaces
    public interface OnCompleteListener {
        void onSuccess(String message);
        void onFailure(String error);
    }

    public interface OnJournalLoadedListener {
        void onSuccess(JournalEntry journal);
        void onFailure(String error);
    }

    public interface OnJournalsLoadedListener {
        void onSuccess(List<JournalEntry> journals);
        void onFailure(String error);
    }
}