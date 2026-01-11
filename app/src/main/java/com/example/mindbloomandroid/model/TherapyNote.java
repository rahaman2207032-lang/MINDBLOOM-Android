package com.example.mindbloomandroid.model;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class TherapyNote {
    private String noteId;
    private String instructorId;
    private String clientId;
    private String clientName;
    private String sessionId;
    private long sessionDate; // timestamp
    private String sessionType;
    private String notes;
    private long createdAt;
    private long updatedAt;

    public TherapyNote() {}

    public TherapyNote(String instructorId, String clientId, String clientName,
                       long sessionDate, String sessionType, String notes) {
        this.instructorId = instructorId;
        this.clientId = clientId;
        this.clientName = clientName;
        this.sessionDate = sessionDate;
        this.sessionType = sessionType;
        this.notes = notes;
        this.createdAt = System. currentTimeMillis();
        this.updatedAt = System.currentTimeMillis();
    }

    // Getters and Setters
    public String getNoteId() { return noteId; }
    public void setNoteId(String noteId) { this.noteId = noteId; }

    public String getInstructorId() { return instructorId; }
    public void setInstructorId(String instructorId) { this.instructorId = instructorId; }

    public String getClientId() { return clientId; }
    public void setClientId(String clientId) { this.clientId = clientId; }

    public String getClientName() { return clientName; }
    public void setClientName(String clientName) { this.clientName = clientName; }

    public String getSessionId() { return sessionId; }
    public void setSessionId(String sessionId) { this.sessionId = sessionId; }

    public long getSessionDate() { return sessionDate; }
    public void setSessionDate(long sessionDate) { this.sessionDate = sessionDate; }

    public String getSessionType() { return sessionType; }
    public void setSessionType(String sessionType) { this.sessionType = sessionType; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }

    public long getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(long updatedAt) { this.updatedAt = updatedAt; }


    public String getFormattedSessionDate() {
        if (sessionDate == 0) return "N/A";
        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
        return sdf.format(new Date(sessionDate));
    }


    public String getFormattedSessionDateTime() {
        if (sessionDate == 0) return "N/A";
        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy h:mm a", Locale.getDefault());
        return sdf.format(new Date(sessionDate));
    }


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

    @Override
    public String toString() {
        return "TherapyNote{" +
                "noteId='" + noteId + '\'' +
                ", client='" + clientName + '\'' +
                ", sessionType='" + sessionType + '\'' +
                ", sessionDate=" + getFormattedSessionDate() +
                '}';
    }
}