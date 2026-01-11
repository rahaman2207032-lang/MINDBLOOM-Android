package com.example.mindbloomandroid.service;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.example.mindbloomandroid.model.ClientOverview;
import com.example.mindbloomandroid.model.Instructor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InstructorService {
    private DatabaseReference instructorsRef;  // Changed from usersRef
    private DatabaseReference usersRef;
    private DatabaseReference sessionRequestsRef;

    public InstructorService() {
        instructorsRef = FirebaseDatabase.getInstance().getReference("instructors");
        usersRef = FirebaseDatabase.getInstance().getReference("users");
        sessionRequestsRef = FirebaseDatabase.getInstance().getReference("session_requests");
    }


    public void getInstructorProfile(String instructorId, OnInstructorLoadedListener listener) {
        instructorsRef.child(instructorId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            Instructor instructor = dataSnapshot.getValue(Instructor.class);
                            if (instructor != null) {
                                listener.onInstructorLoaded(instructor);
                            } else {
                                listener.onError("Failed to parse instructor data");
                            }
                        } else {
                            listener.onError("Instructor not found");
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                        listener.onError(error.getMessage());
                    }
                });
    }


    public void updateInstructorProfile(Instructor instructor, OnUpdateCompleteListener listener) {
        if (instructor.getInstructorId() == null) {
            listener.onError("Instructor ID is required");
            return;
        }

        instructorsRef.child(instructor.getInstructorId())
                .setValue(instructor)
                .addOnSuccessListener(aVoid -> listener.onSuccess("Profile updated successfully"))
                .addOnFailureListener(e -> listener.onError(e.getMessage()));
    }


    public void getDashboardStats(String instructorId, OnStatsLoadedListener listener) {
        Map<String, Integer> stats = new HashMap<>();
        stats.put("pendingRequests", 0);
        stats.put("todaySessions", 0);
        stats.put("totalSessions", 0);
        stats.put("totalClients", 0);
        stats.put("availableSlots", 0);

        // Count pending session requests
        sessionRequestsRef.orderByChild("instructorId").equalTo(instructorId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        int pendingCount = 0;

                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            String status = snapshot.child("status").getValue(String.class);
                            if ("PENDING".equals(status)) {
                                pendingCount++;
                            }
                        }

                        stats.put("pendingRequests", pendingCount);

                        // Now count therapy sessions and unique clients
                        DatabaseReference sessionsRef = FirebaseDatabase.getInstance().getReference("therapy_sessions");
                        sessionsRef.orderByChild("instructorId").equalTo(instructorId)
                                .addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot sessionSnapshot) {
                                        int totalSessionCount = 0;
                                        int todaySessionCount = 0;
                                        java.util.Set<String> uniqueClients = new java.util.HashSet<>();

                                        long todayStart = getTodayStartTimestamp();
                                        long todayEnd = getTodayEndTimestamp();

                                        for (DataSnapshot snapshot : sessionSnapshot.getChildren()) {
                                            totalSessionCount++;
                                            
                                            // Count unique clients
                                            String clientId = snapshot.child("clientId").getValue(String.class);
                                            if (clientId != null) {
                                                uniqueClients.add(clientId);
                                            }

                                            // Count today's sessions
                                            Long sessionDate = snapshot.child("sessionDate").getValue(Long.class);
                                            String sessionStatus = snapshot.child("status").getValue(String.class);
                                            if (sessionDate != null && sessionDate >= todayStart && sessionDate <= todayEnd) {
                                                if ("SCHEDULED".equals(sessionStatus)) {
                                                    todaySessionCount++;
                                                }
                                            }
                                        }

                                        stats.put("totalSessions", totalSessionCount);
                                        stats.put("todaySessions", todaySessionCount);
                                        stats.put("totalClients", uniqueClients.size());

                                        // Calculate available slots (assuming 8 hour workday with 1-hour sessions)
                                        // Available slots = max daily sessions - today's scheduled sessions
                                        int maxDailySlots = 8; // 8 sessions per day (8am-5pm with 1-hour lunch)
                                        int availableToday = Math.max(0, maxDailySlots - todaySessionCount);
                                        stats.put("availableSlots", availableToday);

                                        listener.onStatsLoaded(stats);
                                    }

                                    @Override
                                    public void onCancelled(DatabaseError error) {
                                        listener.onError(error.getMessage());
                                    }
                                });
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                        listener.onError(error.getMessage());
                    }
                });
    }
    
    private long getTodayStartTimestamp() {
        java.util.Calendar cal = java.util.Calendar.getInstance();
        cal.set(java.util.Calendar.HOUR_OF_DAY, 0);
        cal.set(java.util.Calendar.MINUTE, 0);
        cal.set(java.util.Calendar.SECOND, 0);
        cal.set(java.util.Calendar.MILLISECOND, 0);
        return cal.getTimeInMillis();
    }
    
    private long getTodayEndTimestamp() {
        java.util.Calendar cal = java.util.Calendar.getInstance();
        cal.set(java.util.Calendar.HOUR_OF_DAY, 23);
        cal.set(java.util.Calendar.MINUTE, 59);
        cal.set(java.util.Calendar.SECOND, 59);
        cal.set(java.util.Calendar.MILLISECOND, 999);
        return cal.getTimeInMillis();
    }



    public void getAllClients(String instructorId, OnClientsLoadedListener listener) {
        android.util.Log.d("InstructorService", "📖 Loading ALL clients (users) for instructor: " + instructorId);

        // Load ALL users with role='USER' from users table
        usersRef.orderByChild("role").equalTo("USER")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        android.util.Log.d("InstructorService", "📥 Received " + dataSnapshot.getChildrenCount() + " users from Firebase");

                        List<ClientOverview> clients = new ArrayList<>();
                        java.util.Set<String> addedClientIds = new java.util.HashSet<>(); // Prevent duplicates

                        for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                            try {
                                String clientId = userSnapshot.getKey();
                                String username = userSnapshot.child("username").getValue(String.class);
                                String role = userSnapshot.child("role").getValue(String.class);

                                // Only add users (not instructors) and avoid duplicates
                                if (clientId != null && username != null && "USER".equals(role) && !addedClientIds.contains(clientId)) {
                                    ClientOverview client = new ClientOverview();
                                    client.setClientId(clientId);
                                    client.setClientName(username);

                                    // Default values (can be enhanced later with actual data)
                                    client.setAverageMood(0.0);
                                    client.setStressLevel("N/A");
                                    client.setTotalSessions(0);
                                    // Note: setLastSessionDate expects String, not long
                                    // client.setLastSessionDate("No sessions yet");

                                    clients.add(client);
                                    addedClientIds.add(clientId);

                                    android.util.Log.d("InstructorService", "   ✅ Added client: " + username + " (ID: " + clientId + ")");
                                }
                            } catch (Exception e) {
                                android.util.Log.e("InstructorService", "⚠️ Error parsing user: " + e.getMessage());
                            }
                        }

                        android.util.Log.d("InstructorService", "✅ Loaded " + clients.size() + " clients (REAL-TIME UPDATE)");
                        listener.onClientsLoaded(clients);
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                        android.util.Log.e("InstructorService", "❌ Error loading clients: " + error.getMessage());
                        listener.onError(error.getMessage());
                    }
                });
    }


    public void getAllInstructors(OnInstructorsLoadedListener listener) {
        instructorsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                List<Instructor> instructors = new ArrayList<>();

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Instructor instructor = snapshot.getValue(Instructor.class);
                    if (instructor != null) {
                        instructor.setInstructorId(snapshot.getKey());
                        instructors.add(instructor);
                    }
                }

                listener.onInstructorsLoaded(instructors);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                listener.onError(error.getMessage());
            }
        });
    }

    // Interfaces
    public interface OnStatsLoadedListener {
        void onStatsLoaded(Map<String, Integer> stats);
        void onError(String error);
    }

    public interface OnClientsLoadedListener {
        void onClientsLoaded(List<ClientOverview> clients);
        void onError(String error);
    }

    public interface OnInstructorLoadedListener {
        void onInstructorLoaded(Instructor instructor);
        void onError(String error);
    }

    public interface OnInstructorsLoadedListener {
        void onInstructorsLoaded(List<Instructor> instructors);
        void onError(String error);
    }

    public interface OnUpdateCompleteListener {
        void onSuccess(String message);
        void onError(String error);
    }

    public interface OnClientStatsLoadedListener {
        void onStatsLoaded(int sessionCount, long lastSessionDate);
        void onError(String error);
    }


    public void getClientSessionStats(String instructorId, String clientId, OnClientStatsLoadedListener listener) {
        DatabaseReference sessionsRef = FirebaseDatabase.getInstance().getReference("therapy_sessions");
        
        sessionsRef.orderByChild("clientId").equalTo(clientId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        int sessionCount = 0;
                        long lastSessionDate = 0;
                        
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            Object instructorIdObj = snapshot.child("instructorId").getValue();
                            if (instructorIdObj != null && instructorIdObj.toString().equals(instructorId)) {
                                sessionCount++;
                                Object sessionDateObj = snapshot.child("sessionDate").getValue();
                                if (sessionDateObj != null) {
                                    long date = Long.parseLong(sessionDateObj.toString());
                                    if (date > lastSessionDate) {
                                        lastSessionDate = date;
                                    }
                                }
                            }
                        }
                        
                        listener.onStatsLoaded(sessionCount, lastSessionDate);
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                        listener.onError(error.getMessage());
                    }
                });
    }


    public void getSessionAnalytics(String instructorId, String timeRange, OnSessionAnalyticsLoadedListener listener) {
        DatabaseReference sessionsRef = FirebaseDatabase.getInstance().getReference("therapy_sessions");
        
        long currentTime = System.currentTimeMillis();
        long startTime;
        
        // Calculate start time based on range
        switch (timeRange) {
            case "week":
                startTime = currentTime - (7L * 24 * 60 * 60 * 1000);
                break;
            case "month":
                startTime = currentTime - (30L * 24 * 60 * 60 * 1000);
                break;
            case "year":
                startTime = currentTime - (365L * 24 * 60 * 60 * 1000);
                break;
            default:
                startTime = currentTime - (7L * 24 * 60 * 60 * 1000);
        }
        
        sessionsRef.orderByChild("instructorId").equalTo(instructorId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        List<Long> sessionDates = new ArrayList<>();
                        
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            Object sessionDateObj = snapshot.child("sessionDate").getValue();
                            if (sessionDateObj != null) {
                                long date = Long.parseLong(sessionDateObj.toString());
                                if (date >= startTime && date <= currentTime) {
                                    sessionDates.add(date);
                                }
                            }
                        }
                        
                        listener.onAnalyticsLoaded(sessionDates);
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                        listener.onError(error.getMessage());
                    }
                });
    }

    public interface OnSessionAnalyticsLoadedListener {
        void onAnalyticsLoaded(List<Long> sessionDates);
        void onError(String error);
    }
}
