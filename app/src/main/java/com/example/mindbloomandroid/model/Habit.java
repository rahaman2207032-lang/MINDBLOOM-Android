package com.example.mindbloomandroid.model;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Habit {
    private String habitId;
    private String userId;
    private String name;
    private String description;
    private String frequency; // "DAILY" or "WEEKLY"
    private String targetDays; // For weekly:  "MON,WED,FRI" or for daily: "ALL"
    private int currentStreak;
    private int longestStreak;
    private long createdAt;
    private long lastCompletedAt;
    private boolean isActive;

    // Required empty constructor for Firebase
    public Habit() {
        this.currentStreak = 0;
        this.longestStreak = 0;
        this.isActive = true;
    }

    public Habit(String userId, String name, String description, String frequency, String targetDays) {
        this.userId = userId;
        this.name = name;
        this.description = description;
        this.frequency = frequency;
        this.targetDays = targetDays;
        this.currentStreak = 0;
        this.longestStreak = 0;
        this.isActive = true;
        this.createdAt = System.currentTimeMillis();
    }

    // Getters and Setters
    public String getHabitId() { return habitId; }
    public void setHabitId(String habitId) { this.habitId = habitId; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getFrequency() { return frequency; }
    public void setFrequency(String frequency) { this.frequency = frequency; }

    public String getTargetDays() { return targetDays; }
    public void setTargetDays(String targetDays) { this.targetDays = targetDays; }

    public int getCurrentStreak() { return currentStreak; }
    public void setCurrentStreak(int currentStreak) { this.currentStreak = currentStreak; }

    public int getLongestStreak() { return longestStreak; }
    public void setLongestStreak(int longestStreak) { this.longestStreak = longestStreak; }

    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }

    public long getLastCompletedAt() { return lastCompletedAt; }
    public void setLastCompletedAt(long lastCompletedAt) { this.lastCompletedAt = lastCompletedAt; }

    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }


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


    public String getFormattedLastCompleted() {
        if (lastCompletedAt == 0) return "Never";
        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy h:mm a", Locale.getDefault());
        return sdf.format(new Date(lastCompletedAt));
    }


    public String getLastCompletedShort() {
        if (lastCompletedAt == 0) return "Never";
        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
        return sdf.format(new Date(lastCompletedAt));
    }

    @Override
    public String toString() {
        return "Habit{" +
                "habitId='" + habitId + '\'' +
                ", name='" + name + '\'' +
                ", frequency='" + frequency + '\'' +
                ", currentStreak=" + currentStreak +
                '}';
    }
}