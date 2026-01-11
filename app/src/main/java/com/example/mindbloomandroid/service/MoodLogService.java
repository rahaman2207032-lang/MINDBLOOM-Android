package com.example.mindbloomandroid.service;



import com.google.firebase.database.DataSnapshot;
import com.google. firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com. google.firebase.database.FirebaseDatabase;
import com.google. firebase.database.ValueEventListener;
import com.example.mindbloomandroid.model.MoodLog;

import java.util.ArrayList;
import java.util.List;

public class MoodLogService {
    private DatabaseReference moodLogsRef;

    public MoodLogService() {
        moodLogsRef = FirebaseDatabase.getInstance().getReference("mood_logs");
    }


    public void saveMoodLog(MoodLog moodLog, OnCompleteListener listener) {
        String moodLogId = moodLogsRef.push().getKey();
        if (moodLogId != null) {
            moodLog.setMoodLogId(moodLogId);
            moodLog.setLogDate(System.currentTimeMillis());

            android.util.Log.d("MoodLogService", "📝 Saving mood log:");
            android.util.Log.d("MoodLogService", "   ID: " + moodLogId);
            android.util.Log.d("MoodLogService", "   UserID: " + moodLog.getUserId());
            android.util.Log.d("MoodLogService", "   Rating: " + moodLog.getMoodRating());
            android.util.Log.d("MoodLogService", "   LogDate: " + moodLog.getLogDate());
            android.util.Log.d("MoodLogService", "   Notes: " + (moodLog.getNotes() != null ? moodLog.getNotes().substring(0, Math.min(20, moodLog.getNotes().length())) + "..." : "none"));

            moodLogsRef.child(moodLogId).setValue(moodLog)
                    .addOnSuccessListener(aVoid -> {
                        android.util.Log.d("MoodLogService", "✅ Mood log saved successfully");
                        android.util.Log.d("MoodLogService", "📍 Saved at path: mood_logs/" + moodLogId);
                        android.util.Log.d("MoodLogService", "💡 To load: query mood_logs where userId='" + moodLog.getUserId() + "'");
                        listener.onSuccess();
                    })
                    .addOnFailureListener(e -> {
                        android.util.Log.e("MoodLogService", "❌ Failed to save mood log: " + e.getMessage());
                        listener.onError(e.getMessage());
                    });
        } else {
            android.util.Log.e("MoodLogService", "❌ Failed to generate mood log ID");
            listener.onError("Failed to generate mood log ID");
        }
    }


    public void getUserMoodLogs(String userId, OnMoodLogsLoadedListener listener) {
        android.util.Log.d("MoodLogService", "📖 Loading mood logs for user: " + userId);
        android.util.Log.d("MoodLogService", "📍 Querying: mood_logs.orderByChild('userId').equalTo('" + userId + "')");

        // Use addValueEventListener for REAL-TIME updates (not addListenerForSingleValueEvent)
        moodLogsRef.orderByChild("userId").equalTo(userId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        List<MoodLog> moodLogs = new ArrayList<>();
                        android.util.Log.d("MoodLogService", "📥 Received " + dataSnapshot.getChildrenCount() + " mood logs from Firebase (REAL-TIME)");

                        int successCount = 0;
                        int nullCount = 0;

                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            android.util.Log.d("MoodLogService", "   🔍 Processing mood log ID: " + snapshot.getKey());

                            MoodLog moodLog = snapshot.getValue(MoodLog.class);
                            if (moodLog != null) {
                                moodLog.setMoodLogId(snapshot.getKey());
                                moodLogs.add(moodLog);
                                successCount++;
                                android.util.Log.d("MoodLogService", "   ✅ Loaded: Rating=" + moodLog.getMoodRating() + ", Date=" + moodLog.getLogDate());
                            } else {
                                nullCount++;
                                android.util.Log.w("MoodLogService", "   ⚠️ Null mood log at: " + snapshot.getKey());
                            }
                        }

                        android.util.Log.d("MoodLogService", "✅ Successfully loaded " + successCount + " mood logs (REAL-TIME UPDATE)");
                        if (nullCount > 0) {
                            android.util.Log.w("MoodLogService", "⚠️ " + nullCount + " entries were null (data structure issue?)");
                        }

                        if (moodLogs.isEmpty()) {
                            android.util.Log.i("MoodLogService", "ℹ️ No mood logs found for userId: " + userId);
                            android.util.Log.i("MoodLogService", "💡 Check Firebase Console: Does mood_logs have entries with userId='" + userId + "'?");
                        }

                        listener.onMoodLogsLoaded(moodLogs);
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                        android.util.Log.e("MoodLogService", "❌ Error loading mood logs: " + error.getMessage());
                        android.util.Log.e("MoodLogService", "❌ Error code: " + error.getCode());
                        android.util.Log.e("MoodLogService", "❌ Error details: " + error.getDetails());
                        listener.onError(error.getMessage());
                    }
                });
    }


    public void getLatestMoodLog(String userId, OnMoodLogLoadedListener listener) {
        moodLogsRef.orderByChild("userId").equalTo(userId)
                .limitToLast(1)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        for (DataSnapshot snapshot : dataSnapshot. getChildren()) {
                            MoodLog moodLog = snapshot. getValue(MoodLog.class);
                            if (moodLog != null) {
                                moodLog.setMoodLogId(snapshot.getKey());
                                listener.onMoodLogLoaded(moodLog);
                                return;
                            }
                        }
                        listener.onError("No mood log found");
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                        listener. onError(error.getMessage());
                    }
                });
    }


    public void getAverageMood(String userId, OnAverageMoodListener listener) {
        getUserMoodLogs(userId, new OnMoodLogsLoadedListener() {
            @Override
            public void onMoodLogsLoaded(List<MoodLog> moodLogs) {
                if (moodLogs.isEmpty()) {
                    listener.onAverageMoodCalculated(0.0);
                    return;
                }

                double sum = 0;
                for (MoodLog log : moodLogs) {
                    sum += log.getMoodRating();
                }
                double average = sum / moodLogs.size();
                listener.onAverageMoodCalculated(average);
            }

            @Override
            public void onError(String error) {
                listener.onError(error);
            }
        });
    }
    
    public void deleteMoodLog(String moodLogId, OnCompleteListener listener) {
        if (moodLogId == null || moodLogId.isEmpty()) {
            listener.onError("Invalid mood log ID");
            return;
        }
        
        moodLogsRef.child(moodLogId).removeValue()
            .addOnSuccessListener(aVoid -> listener.onSuccess())
            .addOnFailureListener(e -> listener.onError(e.getMessage()));
    }

    // Interfaces
    public interface OnCompleteListener {
        void onSuccess();
        void onError(String error);
    }

    public interface OnMoodLogLoadedListener {
        void onMoodLogLoaded(MoodLog moodLog);
        void onError(String error);
    }

    public interface OnMoodLogsLoadedListener {
        void onMoodLogsLoaded(List<MoodLog> moodLogs);
        void onError(String error);
    }

    public interface OnAverageMoodListener {
        void onAverageMoodCalculated(double average);
        void onError(String error);
    }
}