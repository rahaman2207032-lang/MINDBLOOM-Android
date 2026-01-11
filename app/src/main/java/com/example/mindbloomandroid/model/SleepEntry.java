package com.example.mindbloomandroid.model;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class SleepEntry {
    private String sleepEntryId;
    private String userId;
    private long sleepStartTime; // timestamp
    private long sleepEndTime; // timestamp
    private int sleepQuality; // 1-5 rating
    private String notes;
    private long createdAt;

    public SleepEntry() {}

    public SleepEntry(String userId, long sleepStartTime, long sleepEndTime,
                      int sleepQuality, String notes) {
        this.userId = userId;
        this.sleepStartTime = sleepStartTime;
        this.sleepEndTime = sleepEndTime;
        this.sleepQuality = sleepQuality;
        this.notes = notes;
        this.createdAt = System.currentTimeMillis();
    }

    // Calculate sleep duration in hours
    public double getSleepDurationHours() {
        if (sleepStartTime > 0 && sleepEndTime > 0) {
            long durationMillis = sleepEndTime - sleepStartTime;
            return durationMillis / (1000.0 * 60.0 * 60.0); // Convert to hours
        }
        return 0.0;
    }

    // Getters and Setters
    public String getSleepEntryId() { return sleepEntryId; }
    public void setSleepEntryId(String sleepEntryId) { this.sleepEntryId = sleepEntryId; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public long getSleepStartTime() { return sleepStartTime; }
    public void setSleepStartTime(long sleepStartTime) { this.sleepStartTime = sleepStartTime; }

    public long getSleepEndTime() { return sleepEndTime; }
    public void setSleepEndTime(long sleepEndTime) { this.sleepEndTime = sleepEndTime; }

    public int getSleepQuality() { return sleepQuality; }
    public void setSleepQuality(int sleepQuality) { this.sleepQuality = sleepQuality; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }


    public String getFormattedStartTime() {
        if (sleepStartTime == 0) return "N/A";
        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy h:mm a", Locale.getDefault());
        return sdf.format(new Date(sleepStartTime));
    }


    public String getFormattedEndTime() {
        if (sleepEndTime == 0) return "N/A";
        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy h:mm a", Locale.getDefault());
        return sdf.format(new Date(sleepEndTime));
    }


    public String getStartTimeOnly() {
        if (sleepStartTime == 0) return "N/A";
        SimpleDateFormat sdf = new SimpleDateFormat("h:mm a", Locale.getDefault());
        return sdf.format(new Date(sleepStartTime));
    }


    public String getEndTimeOnly() {
        if (sleepEndTime == 0) return "N/A";
        SimpleDateFormat sdf = new SimpleDateFormat("h:mm a", Locale.getDefault());
        return sdf.format(new Date(sleepEndTime));
    }


    public String getSleepDateShort() {
        if (sleepStartTime == 0) return "N/A";
        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
        return sdf.format(new Date(sleepStartTime));
    }


    public String getFormattedDuration() {
        double hours = getSleepDurationHours();
        if (hours == 0) return "N/A";
        if (hours == (int) hours) {
            return String.format(Locale.getDefault(), "%d hours", (int) hours);
        }
        return String.format(Locale.getDefault(), "%.1f hours", hours);
    }

    @Override
    public String toString() {
        return "SleepEntry{" +
                "sleepEntryId='" + sleepEntryId + '\'' +
                ", duration=" + getFormattedDuration() +
                ", quality=" + sleepQuality +
                '}';
    }
}