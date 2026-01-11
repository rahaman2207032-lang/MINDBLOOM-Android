package com.example.mindbloomandroid.model;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class HabitCompletion {
    private String completionId;
    private String habitId;
    private String userId;
    private long completionDate; // timestamp (date only)
    private long completedAt; // timestamp (exact time)
    private String notes;

    public HabitCompletion() {}

    public HabitCompletion(String habitId, String userId, long completionDate, String notes) {
        this.habitId = habitId;
        this.userId = userId;
        this.completionDate = completionDate;
        this.completedAt = System.currentTimeMillis();
        this.notes = notes;
    }

    // Getters and Setters
    public String getCompletionId() { return completionId; }
    public void setCompletionId(String completionId) { this.completionId = completionId; }

    public String getHabitId() { return habitId; }
    public void setHabitId(String habitId) { this.habitId = habitId; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public long getCompletionDate() { return completionDate; }
    public void setCompletionDate(long completionDate) { this.completionDate = completionDate; }

    public long getCompletedAt() { return completedAt; }
    public void setCompletedAt(long completedAt) { this.completedAt = completedAt; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }


    public String getFormattedCompletedAt() {
        if (completedAt == 0) return "N/A";
        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy h:mm a", Locale.getDefault());
        return sdf.format(new Date(completedAt));
    }


    public String getCompletedAtShort() {
        if (completedAt == 0) return "N/A";
        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
        return sdf.format(new Date(completedAt));
    }


    public String getCompletedTimeOnly() {
        if (completedAt == 0) return "N/A";
        SimpleDateFormat sdf = new SimpleDateFormat("h:mm a", Locale.getDefault());
        return sdf.format(new Date(completedAt));
    }


    public String getFormattedCompletionDate() {
        if (completionDate == 0) return "N/A";
        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
        return sdf.format(new Date(completionDate));
    }

    @Override
    public String toString() {
        return "HabitCompletion{" +
                "completionId='" + completionId + '\'' +
                ", habitId='" + habitId + '\'' +
                ", completedAt=" + getFormattedCompletedAt() +
                '}';
    }
}