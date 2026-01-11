package com.example.mindbloomandroid.model;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Notification {
    private String notificationId;
    private String userId;
    private String type; // MESSAGE, SESSION, SYSTEM
    private String title;
    private String message;
    private boolean read;
    private long createdAt;
    private String relatedEntityId; // For linking to session requests, messages, etc.

    public Notification() {}

    public Notification(String userId, String type, String title, String message) {
        this.userId = userId;
        this.type = type;
        this.title = title;
        this.message = message;
        this.read = false;
        this.createdAt = System.currentTimeMillis();
    }

    // Getters and Setters
    public String getNotificationId() { return notificationId; }
    public void setNotificationId(String notificationId) { this.notificationId = notificationId; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public boolean isRead() { return read; }
    public void setRead(boolean read) { this.read = read; }

    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }

    public String getRelatedEntityId() { return relatedEntityId; }
    public void setRelatedEntityId(String relatedEntityId) { this.relatedEntityId = relatedEntityId; }


    public String getFormattedCreatedAt() {
        if (createdAt == 0) return "N/A";
        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy h:mm a", Locale.getDefault());
        return sdf.format(new Date(createdAt));
    }


    public String getCreatedAtShort() {
        if (createdAt == 0) return "N/A";
        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
        return sdf.format(new Date(createdAt));
    }

    public String getCreatedTimeOnly() {
        if (createdAt == 0) return "N/A";
        SimpleDateFormat sdf = new SimpleDateFormat("h:mm a", Locale.getDefault());
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
            return getCreatedAtShort();
        }
    }

    @Override
    public String toString() {
        return "Notification{" +
                "notificationId='" + notificationId + '\'' +
                ", type='" + type + '\'' +
                ", title='" + title + '\'' +
                ", read=" + read +
                ", createdAt=" + getTimeAgo() +
                '}';
    }
}

