package com.example.mindbloomandroid.model;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MoodLog {
    private String moodLogId;
    private String userId;
    private long logDate;
    private int moodRating; // 1-5 scale
    private String moodEmoji; // 😢, 😟, 😐, 🙂, 😊
    private String notes;
    private String activities;

    public MoodLog() {}

    public MoodLog(String userId, int moodRating, String notes, String activities) {
        this.userId = userId;
        this.logDate = System.currentTimeMillis();
        this.moodRating = moodRating;
        this.notes = notes;
        this.activities = activities;
        setMoodEmojiFromRating();
    }

    // Set emoji based on rating
    public void setMoodEmojiFromRating() {
        switch (moodRating) {
            case 1: this.moodEmoji = "😢"; break;
            case 2: this.moodEmoji = "😟"; break;
            case 3: this.moodEmoji = "😐"; break;
            case 4: this.moodEmoji = "🙂"; break;
            case 5: this.moodEmoji = "😊"; break;
            default: this.moodEmoji = "😐";
        }
    }

    // Getters and Setters
    public String getMoodLogId() { return moodLogId; }
    public void setMoodLogId(String moodLogId) { this.moodLogId = moodLogId; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public long getLogDate() { return logDate; }
    public void setLogDate(long logDate) { this.logDate = logDate; }

    public int getMoodRating() { return moodRating; }
    public void setMoodRating(int moodRating) {
        this.moodRating = moodRating;
        setMoodEmojiFromRating();
    }

    public String getMoodEmoji() { return moodEmoji; }
    public void setMoodEmoji(String moodEmoji) { this.moodEmoji = moodEmoji; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public String getActivities() { return activities; }
    public void setActivities(String activities) { this.activities = activities; }


    public String getFormattedLogDate() {
        if (logDate == 0) return "N/A";
        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy h:mm a", Locale.getDefault());
        return sdf.format(new Date(logDate));
    }


    public String getLogDateShort() {
        if (logDate == 0) return "N/A";
        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
        return sdf.format(new Date(logDate));
    }


    public String getLogTimeOnly() {
        if (logDate == 0) return "N/A";
        SimpleDateFormat sdf = new SimpleDateFormat("h:mm a", Locale.getDefault());
        return sdf.format(new Date(logDate));
    }

    @Override
    public String toString() {
        return "MoodLog{" +
                "moodLogId='" + moodLogId + '\'' +
                ", moodRating=" + moodRating +
                ", moodEmoji='" + moodEmoji + '\'' +
                ", logDate=" + getFormattedLogDate() +
                '}';
    }
}
