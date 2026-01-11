package com.example.mindbloomandroid.service;



import com.google.firebase.database.DataSnapshot;
import com.google. firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com. google.firebase.database.FirebaseDatabase;
import com.google. firebase.database.ValueEventListener;
import com.example.mindbloomandroid.model.SessionRequest;

import java.util.ArrayList;
import java.util.List;

public class SessionRequestService {
    private DatabaseReference sessionRequestsRef;

    public SessionRequestService() {
        sessionRequestsRef = FirebaseDatabase.getInstance().getReference("session_requests");
    }


    public void createSessionRequest(SessionRequest request, OnCompleteListener listener) {
        String requestId = sessionRequestsRef.push().getKey();
        if (requestId != null) {
            request.setRequestId(requestId);
            request.setCreatedAt(System.currentTimeMillis());
            request.setStatus("PENDING");

            sessionRequestsRef.child(requestId).setValue(request)
                    .addOnSuccessListener(aVoid -> listener.onSuccess())
                    .addOnFailureListener(e -> listener.onError(e. getMessage()));
        } else {
            listener.onError("Failed to generate request ID");
        }
    }


    public void createSessionRequest(SessionRequest request, OnSessionRequestCreatedListener listener) {
        String requestId = sessionRequestsRef.push().getKey();
        if (requestId != null) {
            request.setRequestId(requestId);
            request.setCreatedAt(System.currentTimeMillis());
            request.setStatus("PENDING");

            sessionRequestsRef.child(requestId).setValue(request)
                    .addOnSuccessListener(aVoid -> listener.onSuccess(request))
                    .addOnFailureListener(e -> listener.onError(e.getMessage()));
        } else {
            listener.onError("Failed to generate request ID");
        }
    }


    public void getUserSessionRequests(String userId, OnRequestsLoadedListener listener) {
        android.util.Log.d("SessionRequestService", "📖 Loading session requests for user: " + userId);

        // Use addValueEventListener for REAL-TIME updates (not single event)
        sessionRequestsRef.orderByChild("userId").equalTo(userId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        List<SessionRequest> requests = new ArrayList<>();

                        android.util.Log.d("SessionRequestService", "📥 Received " + dataSnapshot.getChildrenCount() + " requests from Firebase (REAL-TIME)");

                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            SessionRequest request = snapshot.getValue(SessionRequest.class);
                            if (request != null) {
                                request.setRequestId(snapshot.getKey());
                                requests.add(request);
                                android.util.Log.d("SessionRequestService", "   ✅ Request ID: " + snapshot.getKey() + ", Status: " + request.getStatus());
                            }
                        }

                        android.util.Log.d("SessionRequestService", "✅ Loaded " + requests.size() + " session requests (REAL-TIME UPDATE)");
                        listener.onRequestsLoaded(requests);
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                        android.util.Log.e("SessionRequestService", "❌ Error loading requests: " + error.getMessage());
                        listener.onError(error.getMessage());
                    }
                });
    }


    public void getInstructorPendingRequests(String instructorId, OnRequestsLoadedListener listener) {
        sessionRequestsRef.orderByChild("instructorId").equalTo(instructorId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        List<SessionRequest> requests = new ArrayList<>();
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            SessionRequest request = snapshot.getValue(SessionRequest.class);
                            if (request != null && "PENDING".equals(request.getStatus())) {
                                request.setRequestId(snapshot.getKey());
                                requests.add(request);
                            }
                        }
                        listener. onRequestsLoaded(requests);
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                        listener.onError(error.getMessage());
                    }
                });
    }


    public void getConfirmedSessions(String userId, OnRequestsLoadedListener listener) {
        sessionRequestsRef.orderByChild("userId").equalTo(userId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        List<SessionRequest> sessions = new ArrayList<>();
                        for (DataSnapshot snapshot : dataSnapshot. getChildren()) {
                            SessionRequest request = snapshot.getValue(SessionRequest.class);
                            if (request != null && "CONFIRMED".equals(request.getStatus())) {
                                request.setRequestId(snapshot.getKey());
                                sessions.add(request);
                            }
                        }
                        listener.onRequestsLoaded(sessions);
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                        listener. onError(error.getMessage());
                    }
                });
    }


    public void updateRequestStatus(String requestId, String status, String zoomLink,
                                    OnCompleteListener listener) {
        sessionRequestsRef.child(requestId).child("status").setValue(status)
                .addOnSuccessListener(aVoid -> {
                    if (zoomLink != null && !zoomLink.isEmpty()) {
                        sessionRequestsRef. child(requestId).child("zoomLink").setValue(zoomLink)
                                .addOnSuccessListener(aVoid2 -> {
                                    sessionRequestsRef.child(requestId).child("updatedAt")
                                            .setValue(System. currentTimeMillis());
                                    listener.onSuccess();
                                })
                                . addOnFailureListener(e -> listener.onError(e.getMessage()));
                    } else {
                        sessionRequestsRef.child(requestId).child("updatedAt")
                                .setValue(System.currentTimeMillis());
                        listener.onSuccess();
                    }
                })
                .addOnFailureListener(e -> listener.onError(e.getMessage()));
    }


    public void cancelRequest(String requestId, OnCompleteListener listener) {
        updateRequestStatus(requestId, "CANCELLED", null, listener);
    }

    // Interfaces
    public interface OnCompleteListener {
        void onSuccess();
        void onError(String error);
    }

    public interface OnRequestsLoadedListener {
        void onRequestsLoaded(List<SessionRequest> requests);
        void onError(String error);
    }

    public interface OnSessionRequestCreatedListener {
        void onSuccess(SessionRequest request);
        void onError(String error);
    }

    public interface OnSessionRequestsLoadedListener {
        void onRequestsLoaded(List<SessionRequest> requests);
        void onError(String error);
    }
}