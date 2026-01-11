package com.example.mindbloomandroid.model;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class JournalEntry {
    private String journalId;
    private String userId;
    private String content;
    private long createdAt;
    private long updatedAt;

    public JournalEntry() {}

    public JournalEntry(String userId, String content) {
        this.userId = userId;
        this.content = content;
        this.createdAt = System.currentTimeMillis();
        this.updatedAt = System.currentTimeMillis();
    }

    // Getters and Setters
    public String getJournalId() { return journalId; }
    public void setJournalId(String journalId) { this.journalId = journalId; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }

    public long getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(long updatedAt) { this.updatedAt = updatedAt; }


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


    public String getFormattedUpdatedAt() {
        if (updatedAt == 0) return "Never";
        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy h:mm a", Locale.getDefault());
        return sdf.format(new Date(updatedAt));
    }


    public String getUpdatedAtShort() {
        if (updatedAt == 0) return "Never";
        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
        return sdf.format(new Date(updatedAt));
    }

    @Override
    public String toString() {
        return "JournalEntry{" +
                "journalId='" + journalId + '\'' +
                ", createdAt=" + getFormattedCreatedAt() +
                '}';
    }
}
