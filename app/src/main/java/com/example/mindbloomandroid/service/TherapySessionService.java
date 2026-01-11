package com.example.mindbloomandroid.service;



import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database. DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase. database.FirebaseDatabase;
import com.google.firebase.database. ValueEventListener;
import com. example.mindbloomandroid.model.TherapySession;

import java.util.ArrayList;
import java.util.List;

public class TherapySessionService {
    private DatabaseReference therapySessionsRef;

    public TherapySessionService() {
        therapySessionsRef = FirebaseDatabase.getInstance().getReference("therapy_sessions");
    }


    public void createTherapySession(TherapySession session, OnCompleteListener listener) {
        String sessionId = therapySessionsRef.push().getKey();
        if (sessionId != null) {
            session.setSessionId(sessionId);
            session.setCreatedAt(System.currentTimeMillis());

            android.util.Log.d("TherapySessionService", "💾 Creating therapy session...");
            android.util.Log.d("TherapySessionService", "   Session ID: " + sessionId);
            android.util.Log.d("TherapySessionService", "   Client: " + session.getClientName());
            android.util.Log.d("TherapySessionService", "   Instructor ID: " + session.getInstructorId());
            android.util.Log.d("TherapySessionService", "   Zoom Link: " + (session.getZoomLink() != null ? session.getZoomLink() : "NULL"));
            android.util.Log.d("TherapySessionService", "   Status: " + session.getStatus());
            android.util.Log.d("TherapySessionService", "   Date: " + new java.util.Date(session.getSessionDate()));

            therapySessionsRef.child(sessionId).setValue(session)
                    .addOnSuccessListener(aVoid -> {
                        android.util.Log.d("TherapySessionService", "✅ Therapy session created successfully!");
                        listener.onSuccess();
                    })
                    .addOnFailureListener(e -> {
                        android.util.Log.e("TherapySessionService", "❌ Failed to create session: " + e.getMessage());
                        listener.onError(e.getMessage());
                    });
        } else {
            android.util.Log.e("TherapySessionService", "❌ Failed to generate session ID");
            listener.onError("Failed to generate session ID");
        }
    }

    public void getScheduledSessionsForUser(String userId, OnSessionsLoadedListener listener) {
        android.util.Log.d("TherapySessionService", "📖 Loading scheduled sessions for user: " + userId);

        // Use addValueEventListener for REAL-TIME updates
        therapySessionsRef.orderByChild("clientId").equalTo(userId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        List<TherapySession> sessions = new ArrayList<>();
                        long currentTime = System.currentTimeMillis();

                        android.util.Log.d("TherapySessionService", "📥 Received " + dataSnapshot.getChildrenCount() + " therapy sessions (REAL-TIME)");

                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            TherapySession session = snapshot.getValue(TherapySession.class);
                            if (session != null &&
                                    "SCHEDULED".equals(session.getStatus()) &&
                                    session.getSessionDate() >= currentTime) {
                                session.setSessionId(snapshot.getKey());
                                sessions.add(session);
                                android.util.Log.d("TherapySessionService", "   ✅ Session ID: " + snapshot.getKey() + ", Zoom: " + (session.getZoomLink() != null ? "YES" : "NO"));
                            }
                        }

                        android.util.Log.d("TherapySessionService", "✅ Loaded " + sessions.size() + " scheduled sessions (REAL-TIME UPDATE)");
                        listener.onSessionsLoaded(sessions);
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                        android.util.Log.e("TherapySessionService", "❌ Error loading sessions: " + error.getMessage());
                        listener.onError(error.getMessage());
                    }
                });
    }


    public void getScheduledSessionsForInstructor(String instructorId, OnSessionsLoadedListener listener) {
        therapySessionsRef.orderByChild("instructorId").equalTo(instructorId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        List<TherapySession> sessions = new ArrayList<>();
                        long currentTime = System.currentTimeMillis();

                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            TherapySession session = snapshot.getValue(TherapySession.class);
                            if (session != null &&
                                    "SCHEDULED".equals(session.getStatus()) &&
                                    session.getSessionDate() >= currentTime) {
                                session.setSessionId(snapshot.getKey());
                                sessions.add(session);
                            }
                        }
                        listener.onSessionsLoaded(sessions);
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                        listener.onError(error.getMessage());
                    }
                });
    }


    public void getInstructorWeeklySessions(String instructorId, long weekStartTime, OnSessionsLoadedListener listener) {
        android.util.Log.d("TherapySessionService", "📅 Loading weekly sessions for instructor: " + instructorId);
        android.util.Log.d("TherapySessionService", "   Week start: " + new java.util.Date(weekStartTime));

        therapySessionsRef.orderByChild("instructorId").equalTo(instructorId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        List<TherapySession> sessions = new ArrayList<>();
                        long weekEndTime = weekStartTime + (7 * 24 * 60 * 60 * 1000); // Add 7 days

                        android.util.Log.d("TherapySessionService", "📥 Received " + dataSnapshot.getChildrenCount() + " sessions from Firebase");

                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            TherapySession session = snapshot.getValue(TherapySession.class);
                            if (session != null &&
                                    "SCHEDULED".equals(session.getStatus()) &&
                                    session.getSessionDate() >= weekStartTime &&
                                    session.getSessionDate() < weekEndTime) {
                                session.setSessionId(snapshot.getKey());
                                sessions.add(session);

                                android.util.Log.d("TherapySessionService", "   ✅ Session: " + session.getClientName());
                                android.util.Log.d("TherapySessionService", "      Zoom Link: " + (session.getZoomLink() != null && !session.getZoomLink().isEmpty() ? session.getZoomLink() : "⚠️ NO ZOOM LINK"));
                            }
                        }

                        android.util.Log.d("TherapySessionService", "✅ Loaded " + sessions.size() + " weekly sessions");
                        listener.onSessionsLoaded(sessions);
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                        android.util.Log.e("TherapySessionService", "❌ Error: " + error.getMessage());
                        listener.onError(error.getMessage());
                    }
                });
    }


    public void updateSessionStatus(String sessionId, String status, OnCompleteListener listener) {
        therapySessionsRef.child(sessionId).child("status").setValue(status)
                .addOnSuccessListener(aVoid -> listener.onSuccess())
                .addOnFailureListener(e -> listener.onError(e.getMessage()));
    }


    public void cancelSession(String sessionId, OnCompleteListener listener) {
        updateSessionStatus(sessionId, "CANCELLED", listener);
    }

    // Interfaces
    public interface OnCompleteListener {
        void onSuccess();
        void onError(String error);
    }

    public interface OnSessionsLoadedListener {
        void onSessionsLoaded(List<TherapySession> sessions);
        void onError(String error);
    }
}