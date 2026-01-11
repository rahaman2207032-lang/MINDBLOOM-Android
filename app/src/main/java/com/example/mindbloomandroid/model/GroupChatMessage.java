package com.example.mindbloomandroid.model;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;


public class GroupChatMessage {
    private String messageId;
    private String message;
    private String userId;
    private String userName;
    private boolean anonymous;
    private long createdAt;

    public GroupChatMessage() {}

    public GroupChatMessage(String message, String userId, String userName, boolean anonymous) {
        this.message = message;
        this.userId = userId;
        this.userName = userName;
        this.anonymous = anonymous;
        this.createdAt = System.currentTimeMillis();
    }

    // Getters and Setters
    public String getMessageId() { return messageId; }
    public void setMessageId(String messageId) { this.messageId = messageId; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }

    public boolean isAnonymous() { return anonymous; }
    public void setAnonymous(boolean anonymous) { this.anonymous = anonymous; }

    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }

    // Helper methods
    public String getFormattedTime() {
        if (createdAt == 0) return "Just now";
        SimpleDateFormat sdf = new SimpleDateFormat("h:mm a", Locale.getDefault());
        return sdf.format(new Date(createdAt));
    }

    public String getDisplayUserName() {
        return anonymous ? "Anonymous" : userName;
    }

    public boolean isOwnMessage(String currentUserId) {
        return userId != null && userId.equals(currentUserId);
    }
}
