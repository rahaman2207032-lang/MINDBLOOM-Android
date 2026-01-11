package com.example.mindbloomandroid.service;



import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.example.mindbloomandroid.model.Notification;
import com.example.mindbloomandroid.model.NotificationData;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class NotificationService {
    private DatabaseReference notificationsRef;

    public NotificationService() {
        notificationsRef = FirebaseDatabase.getInstance().getReference("notifications");
    }



    public void createNotification(Notification notification, OnCompleteListener listener) {
        String notificationId = notificationsRef.push().getKey();
        if (notificationId != null) {
            notification.setNotificationId(notificationId);
            notification.setCreatedAt(System.currentTimeMillis());
            notification.setRead(false);

            android.util.Log.d("NotificationService", "💾 Creating notification...");
            android.util.Log.d("NotificationService", "   Notification ID: " + notificationId);
            android.util.Log.d("NotificationService", "   User ID: " + notification.getUserId());
            android.util.Log.d("NotificationService", "   Type: " + notification.getType());
            android.util.Log.d("NotificationService", "   Title: " + notification.getTitle());
            android.util.Log.d("NotificationService", "   Path: notifications/" + notification.getUserId() + "/" + notificationId);

            notificationsRef.child(notification.getUserId()).child(notificationId)
                    .setValue(notification)
                    .addOnSuccessListener(aVoid -> {
                        android.util.Log.d("NotificationService", "✅ Notification saved to Firebase successfully!");
                        listener.onSuccess();
                    })
                    .addOnFailureListener(e -> {
                        android.util.Log.e("NotificationService", "❌ Failed to save notification: " + e.getMessage());
                        listener.onError(e.getMessage());
                    });
        } else {
            android.util.Log.e("NotificationService", "❌ Failed to generate notification ID");
            listener.onError("Failed to generate notification ID");
        }
    }


    public void getUserNotifications(String userId, OnNotificationsLoadedListener listener) {
        android.util.Log.d("NotificationService", "📖 Loading notifications for user: " + userId);
        android.util.Log.d("NotificationService", "   Path: notifications/" + userId);

        notificationsRef.child(userId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        android.util.Log.d("NotificationService", "📥 Received " + dataSnapshot.getChildrenCount() + " notifications (REAL-TIME)");

                        List<Notification> notifications = new ArrayList<>();
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            Notification notification = snapshot.getValue(Notification.class);
                            if (notification != null) {
                                notification.setNotificationId(snapshot.getKey());
                                notifications.add(notification);
                                android.util.Log.d("NotificationService", "   ✅ Notification: " + notification.getType() + " - " + notification.getTitle());
                            }
                        }

                        android.util.Log.d("NotificationService", "✅ Loaded " + notifications.size() + " notifications (REAL-TIME UPDATE)");
                        listener.onNotificationsLoaded(notifications);
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                        android.util.Log.e("NotificationService", "❌ Error loading notifications: " + error.getMessage());
                        listener.onError(error.getMessage());
                    }
                });
    }


    public void getUnreadCount(String userId, OnUnreadCountListener listener) {
        notificationsRef.child(userId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        int unreadCount = 0;
                        for (DataSnapshot snapshot :  dataSnapshot.getChildren()) {
                            Notification notification = snapshot. getValue(Notification.class);
                            if (notification != null && !notification.isRead()) {
                                unreadCount++;
                            }
                        }
                        listener.onUnreadCountLoaded(unreadCount);
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                        listener. onError(error.getMessage());
                    }
                });
    }


    public void markAsRead(String userId, String notificationId, OnCompleteListener listener) {
        notificationsRef.child(userId).child(notificationId).child("read").setValue(true)
                .addOnSuccessListener(aVoid -> listener.onSuccess())
                .addOnFailureListener(e -> listener.onError(e. getMessage()));
    }


    public void markAllAsRead(String userId, OnCompleteListener listener) {
        notificationsRef. child(userId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        for (DataSnapshot snapshot :  dataSnapshot.getChildren()) {
                            snapshot.getRef().child("read").setValue(true);
                        }
                        listener.onSuccess();
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                        listener.onError(error.getMessage());
                    }
                });
    }


    public void deleteNotification(String userId, String notificationId, OnCompleteListener listener) {
        notificationsRef.child(userId).child(notificationId).removeValue()
                .addOnSuccessListener(aVoid -> listener. onSuccess())
                .addOnFailureListener(e -> listener. onError(e.getMessage()));
    }


    public void getUnreadNotificationsWithDetails(String userId, OnNotificationsWithDetailsLoadedListener listener) {
        notificationsRef.child(userId)
                .orderByChild("read")
                .equalTo(false)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        List<Notification> notificationsList = new ArrayList<>();
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            Notification notification = snapshot.getValue(Notification.class);
                            if (notification != null) {
                                notification.setNotificationId(snapshot.getKey());
                                notificationsList.add(notification);
                            }
                        }

                        // Enrich notifications with session details (zoom links)
                        enrichNotificationsWithSessionDetails(notificationsList, listener);
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                        listener.onError(error.getMessage());
                    }
                });
    }


    private void enrichNotificationsWithSessionDetails(List<Notification> notifications, OnNotificationsWithDetailsLoadedListener listener) {
        List<NotificationData> enrichedNotifications = new ArrayList<>();
        int[] pendingCount = {0};

        for (Notification notification : notifications) {
            if ("SESSION_CONFIRMED".equals(notification.getType()) && notification.getRelatedEntityId() != null) {
                // Fetch TherapySession to get zoom link
                pendingCount[0]++;
                String sessionId = notification.getRelatedEntityId();

                DatabaseReference sessionsRef = FirebaseDatabase.getInstance().getReference("therapy_sessions").child(sessionId);
                sessionsRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot sessionSnapshot) {
                        NotificationData data = convertToNotificationData(notification, notification.getNotificationId());

                        if (sessionSnapshot.exists()) {
                            // Extract zoom link and session details
                            String zoomLink = sessionSnapshot.child("zoomLink").getValue(String.class);
                            Long sessionDate = sessionSnapshot.child("sessionDate").getValue(Long.class);
                            String instructorName = sessionSnapshot.child("instructorName").getValue(String.class);

                            data.setZoomLink(zoomLink);
                            data.setCanJoin(true);

                            if (sessionDate != null) {
                                SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy h:mm a", Locale.getDefault());
                                data.setSessionDate(sdf.format(new Date(sessionDate)));
                            }
                            if (instructorName != null) {
                                data.setInstructorName(instructorName);
                            }
                        }

                        enrichedNotifications.add(data);
                        pendingCount[0]--;

                        if (pendingCount[0] == 0) {
                            listener.onNotificationsLoaded(enrichedNotifications);
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                        // Still add the notification even if session fetch fails
                        NotificationData data = convertToNotificationData(notification, notification.getNotificationId());
                        enrichedNotifications.add(data);
                        pendingCount[0]--;

                        if (pendingCount[0] == 0) {
                            listener.onNotificationsLoaded(enrichedNotifications);
                        }
                    }
                });
            } else {
                // Non-session notifications don't need enrichment
                enrichedNotifications.add(convertToNotificationData(notification, notification.getNotificationId()));
            }
        }

        // If no session notifications to enrich, return immediately
        if (pendingCount[0] == 0) {
            listener.onNotificationsLoaded(enrichedNotifications);
        }
    }


    private NotificationData convertToNotificationData(Notification notification, String notificationId) {
        NotificationData data = new NotificationData();
        data.setNotificationId(notificationId);
        data.setUserId(notification.getUserId());
        data.setNotificationType(notification.getType());
        data.setTitle(notification.getTitle());
        data.setMessage(notification.getMessage());
        data.setRead(notification.isRead());
        data.setCreatedAt(notification.getCreatedAt());
        data.setRelatedEntityId(notification.getRelatedEntityId());

        // For SESSION_ACCEPTED type, fetch zoom link from therapy_sessions
        if ("SESSION_ACCEPTED".equals(notification.getType()) && notification.getRelatedEntityId() != null) {
            data.setCanJoin(true);
            // Zoom link will be fetched from Firebase therapy_sessions node using relatedEntityId
            // This is handled in NotificationActivity when displaying the notification
        } else if ("MESSAGE".equals(notification.getType())) {
            data.setCanReply(true);
        }

        return data;
    }


    public void markAsRead(String notificationId, OnOperationCompleteListener listener) {
        // Find the notification across all users (inefficient but works for demo)
        notificationsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                    if (userSnapshot.hasChild(notificationId)) {
                        userSnapshot.child(notificationId).child("read").getRef().setValue(true)
                                .addOnSuccessListener(aVoid -> listener.onSuccess())
                                .addOnFailureListener(e -> listener.onError(e.getMessage()));
                        return;
                    }
                }
                listener.onError("Notification not found");
            }

            @Override
            public void onCancelled(DatabaseError error) {
                listener.onError(error.getMessage());
            }
        });
    }


    public void markAllAsRead(String userId, OnOperationCompleteListener listener) {
        markAllAsRead(userId, new OnCompleteListener() {
            @Override
            public void onSuccess() {
                listener.onSuccess();
            }

            @Override
            public void onError(String error) {
                listener.onError(error);
            }
        });
    }

    // Interfaces
    public interface OnCompleteListener {
        void onSuccess();
        void onError(String error);
    }

    public interface OnNotificationsLoadedListener {
        void onNotificationsLoaded(List<Notification> notifications);
        void onError(String error);
    }

    public interface OnUnreadCountListener {
        void onUnreadCountLoaded(int count);
        void onError(String error);
    }

    public interface OnNotificationsWithDetailsLoadedListener {
        void onNotificationsLoaded(List<NotificationData> notifications);
        void onError(String error);
    }

    public interface OnOperationCompleteListener {
        void onSuccess();
        void onError(String error);
    }
    
    public interface OnSessionDetailsLoadedListener {
        void onSessionDetailsLoaded(String zoomLink, String sessionDate, String instructorName);
        void onError(String error);
    }
    

    public void getSessionDetails(String sessionId, OnSessionDetailsLoadedListener listener) {
        DatabaseReference sessionsRef = FirebaseDatabase.getInstance().getReference("therapy_sessions");
        sessionsRef.child(sessionId).get()
            .addOnSuccessListener(snapshot -> {
                if (snapshot.exists()) {
                    String zoomLink = snapshot.child("zoomLink").getValue(String.class);
                    Long sessionDateLong = snapshot.child("sessionDate").getValue(Long.class);
                    String instructorName = snapshot.child("instructorName").getValue(String.class);
                    
                    String sessionDate = "N/A";
                    if (sessionDateLong != null) {
                        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("MMM dd, yyyy h:mm a", java.util.Locale.getDefault());
                        sessionDate = sdf.format(new java.util.Date(sessionDateLong));
                    }
                    
                    listener.onSessionDetailsLoaded(
                        zoomLink != null ? zoomLink : "",
                        sessionDate,
                        instructorName != null ? instructorName : "Instructor"
                    );
                } else {
                    listener.onError("Session not found");
                }
            })
            .addOnFailureListener(e -> listener.onError(e.getMessage()));
    }
}