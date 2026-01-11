package com.example.mindbloomandroid.model;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;


public class NotificationData {
    private String notificationId;
    private String userId;
    private String notificationType; // MESSAGE, SESSION, SESSION_ACCEPTED, SYSTEM
    private String title;
    private String message;
    private boolean isRead;
    private long createdAt;
    private String relatedEntityId;
    
    // Action details for SESSION_ACCEPTED
    private Boolean canJoin;
    private String zoomLink;
    private String sessionDate;
    private String instructorName;
    
    // Action details for MESSAGE
    private Boolean canReply;
    private String senderId;
    private String senderName;
    
    public NotificationData() {}
    
    // Getters and Setters
    public String getNotificationId() { return notificationId; }
    public void setNotificationId(String notificationId) { this.notificationId = notificationId; }
    
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    
    public String getNotificationType() { return notificationType; }
    public void setNotificationType(String notificationType) { this.notificationType = notificationType; }
    
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    
    public boolean isRead() { return isRead; }
    public void setRead(boolean read) { isRead = read; }
    
    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }
    
    public String getRelatedEntityId() { return relatedEntityId; }
    public void setRelatedEntityId(String relatedEntityId) { this.relatedEntityId = relatedEntityId; }
    
    // Session action fields
    public Boolean getCanJoin() { return canJoin; }
    public void setCanJoin(Boolean canJoin) { this.canJoin = canJoin; }
    
    public String getZoomLink() { return zoomLink; }
    public void setZoomLink(String zoomLink) { this.zoomLink = zoomLink; }
    
    public String getSessionDate() { return sessionDate; }
    public void setSessionDate(String sessionDate) { this.sessionDate = sessionDate; }
    
    public String getInstructorName() { return instructorName; }
    public void setInstructorName(String instructorName) { this.instructorName = instructorName; }
    
    // Message action fields
    public Boolean getCanReply() { return canReply; }
    public void setCanReply(Boolean canReply) { this.canReply = canReply; }
    
    public String getSenderId() { return senderId; }
    public void setSenderId(String senderId) { this.senderId = senderId; }
    
    public String getSenderName() { return senderName; }
    public void setSenderName(String senderName) { this.senderName = senderName; }
    
    // Helper methods
    public String getTypeIcon() {
        if (notificationType == null) return "❓";
        
        switch (notificationType.toUpperCase()) {
            case "MESSAGE":
                return "💬";
            case "SESSION":
            case "SESSION_ACCEPTED":
                return "📅";
            case "SYSTEM":
                return "🔔";
            default:
                return "📌";
        }
    }
    
    public String getFormattedCreatedAt() {
        if (createdAt == 0) return "N/A";
        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy h:mm a", Locale.getDefault());
        return sdf.format(new Date(createdAt));
    }
    
    public String getTimeAgo() {
        if (createdAt == 0) return "N/A";

        long now = System.currentTimeMillis();
        long diff = now - createdAt;

        long seconds = diff / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        long days = hours / 24;

        if (seconds < 60) {
            return "Just now";
        } else if (minutes < 60) {
            return minutes + " minute" + (minutes == 1 ? "" : "s") + " ago";
        } else if (hours < 24) {
            return hours + " hour" + (hours == 1 ? "" : "s") + " ago";
        } else if (days == 1) {
            return "Yesterday";
        } else if (days < 7) {
            return days + " days ago";
        } else {
            SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
            return sdf.format(new Date(createdAt));
        }
    }
}
