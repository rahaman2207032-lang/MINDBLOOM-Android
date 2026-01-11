package com.example.mindbloomandroid.service;



import com.google.firebase.database. DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase. database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.example.mindbloomandroid.model.Message;
import com.example.mindbloomandroid.model.Notification;

import java.util.ArrayList;
import java.util.List;

public class MessageService {
    private DatabaseReference messagesRef;

    public MessageService() {
        messagesRef = FirebaseDatabase.getInstance().getReference("messages");
    }


    public void sendMessage(Message message, OnCompleteListener listener) {
        // Create conversation ID (smaller_userId + larger_userId for consistency)
        String conversationId = getConversationId(message.getSenderId(), message.getReceiverId());

        String messageId = messagesRef.child(conversationId).push().getKey();
        if (messageId != null) {
            message.setMessageId(messageId);
            message.setSentAt(System.currentTimeMillis());

            messagesRef.child(conversationId).child(messageId).setValue(message)
                    .addOnSuccessListener(aVoid -> {
                        // Send real-time notification to receiver
                        sendMessageNotification(message, conversationId);
                        listener. onSuccess();
                    })
                    .addOnFailureListener(e -> listener. onError(e.getMessage()));
        } else {
            listener. onError("Failed to generate message ID");
        }
    }
    

    private void sendMessageNotification(Message message, String conversationId) {
        NotificationService notificationService = new NotificationService();
        
        // Try users table first, then instructors table
        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("users");
        DatabaseReference instructorsRef = FirebaseDatabase.getInstance().getReference("instructors");

        usersRef.child(message.getSenderId()).get()
            .addOnSuccessListener(userSnapshot -> {
                String senderName = "Someone";

                // Check if found in users table
                if (userSnapshot.exists()) {
                    String username = userSnapshot.child("username").getValue(String.class);
                    if (username != null) {
                        senderName = username;
                    }
                    android.util.Log.d("MessageService", "✅ Found sender in users table: " + senderName);
                    createAndSendNotification(message, conversationId, senderName, notificationService);
                } else {
                    // Not in users table, check instructors table
                    android.util.Log.d("MessageService", "⚠️ Sender not in users table, checking instructors...");
                    instructorsRef.child(message.getSenderId()).get()
                        .addOnSuccessListener(instructorSnapshot -> {
                            String finalSenderName = "Someone";
                            if (instructorSnapshot.exists()) {
                                String username = instructorSnapshot.child("username").getValue(String.class);
                                if (username != null) {
                                    finalSenderName = username;
                                }
                                android.util.Log.d("MessageService", "✅ Found sender in instructors table: " + finalSenderName);
                            } else {
                                android.util.Log.e("MessageService", "❌ Sender not found in users OR instructors table!");
                            }
                            createAndSendNotification(message, conversationId, finalSenderName, notificationService);
                        })
                        .addOnFailureListener(e -> {
                            android.util.Log.e("MessageService", "❌ Error checking instructors table: " + e.getMessage());
                            createAndSendNotification(message, conversationId, "Someone", notificationService);
                        });
                }
            })
            .addOnFailureListener(e -> {
                android.util.Log.e("MessageService", "❌ Error checking users table: " + e.getMessage());
                // Still try to send notification with generic name
                createAndSendNotification(message, conversationId, "Someone", notificationService);
            });
    }


    private void createAndSendNotification(Message message, String conversationId, String senderName, NotificationService notificationService) {
        // Create notification
        String messagePreview = message.getContent();
        if (messagePreview.length() > 50) {
            messagePreview = messagePreview.substring(0, 47) + "...";
        }

        Notification notification = new Notification(
            message.getReceiverId(),
            "MESSAGE",
            "💬 New Message from " + senderName,
            messagePreview
        );
        notification.setRelatedEntityId(conversationId);

        android.util.Log.d("MessageService", "📤 Sending notification to user: " + message.getReceiverId());
        android.util.Log.d("MessageService", "   Title: New Message from " + senderName);
        android.util.Log.d("MessageService", "   Preview: " + messagePreview);

        // Save notification in real-time
        notificationService.createNotification(notification, new NotificationService.OnCompleteListener() {
            @Override
            public void onSuccess() {
                android.util.Log.d("MessageService", "✅ Notification sent successfully!");
            }

            @Override
            public void onError(String error) {
                android.util.Log.e("MessageService", "❌ Notification failed: " + error);
            }
        });
    }


    public void getConversationMessages(String userId1, String userId2,
                                        OnMessagesLoadedListener listener) {
        String conversationId = getConversationId(userId1, userId2);

        messagesRef. child(conversationId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        List<Message> messages = new ArrayList<>();
                        for (DataSnapshot snapshot : dataSnapshot. getChildren()) {
                            Message message = snapshot.getValue(Message. class);
                            if (message != null) {
                                message.setMessageId(snapshot.getKey());
                                messages.add(message);
                            }
                        }
                        listener. onMessagesLoaded(messages);
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                        listener.onError(error.getMessage());
                    }
                });
    }


    public void markMessageAsRead(String senderId, String receiverId, String messageId,
                                  OnCompleteListener listener) {
        String conversationId = getConversationId(senderId, receiverId);

        messagesRef. child(conversationId).child(messageId).child("readAt")
                .setValue(System. currentTimeMillis())
                .addOnSuccessListener(aVoid -> listener.onSuccess())
                .addOnFailureListener(e -> listener.onError(e.getMessage()));
    }


    public void getUnreadCount(String userId, String otherUserId, OnUnreadCountListener listener) {
        String conversationId = getConversationId(userId, otherUserId);

        messagesRef.child(conversationId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        int unreadCount = 0;
                        for (DataSnapshot snapshot :  dataSnapshot.getChildren()) {
                            Message message = snapshot.getValue(Message. class);
                            if (message != null &&
                                    message.getReceiverId().equals(userId) &&
                                    message.getReadAt() == 0) {
                                unreadCount++;
                            }
                        }
                        listener.onUnreadCountLoaded(unreadCount);
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                        listener.onError(error.getMessage());
                    }
                });
    }


    private String getConversationId(String userId1, String userId2) {
        return userId1.compareTo(userId2) < 0 ?
                userId1 + "_" + userId2 : userId2 + "_" + userId1;
    }

    // Interfaces
    public interface OnCompleteListener {
        void onSuccess();
        void onError(String error);
    }

    public interface OnMessagesLoadedListener {
        void onMessagesLoaded(List<Message> messages);
        void onError(String error);
    }

    public interface OnUnreadCountListener {
        void onUnreadCountLoaded(int count);
        void onError(String error);
    }
}
