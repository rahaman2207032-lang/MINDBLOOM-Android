package com.example.mindbloomandroid.model;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class TherapySession {
    private String sessionId;
    private String clientId;
    private String clientName;
    private String instructorId;
    private String instructorName;
    private long sessionDate; // timestamp
    private String sessionType;
    private String zoomLink;
    private String status; // "SCHEDULED", "COMPLETED", "CANCELLED"
    private String notes;
    private long createdAt;

    public TherapySession() {}

    public TherapySession(String clientId, String clientName, String instructorId,
                          long sessionDate, String sessionType, String zoomLink) {
        this.clientId = clientId;
        this.clientName = clientName;
        this.instructorId = instructorId;
        this.sessionDate = sessionDate;
        this.sessionType = sessionType;
        this.zoomLink = zoomLink;
        this.status = "SCHEDULED";
        this.createdAt = System.currentTimeMillis();
    }

    // Getters and Setters
    public String getSessionId() { return sessionId; }
    public void setSessionId(String sessionId) { this.sessionId = sessionId; }

    public String getClientId() { return clientId; }
    public void setClientId(String clientId) { this.clientId = clientId; }

    public String getClientName() { return clientName; }
    public void setClientName(String clientName) { this.clientName = clientName; }

    public String getInstructorId() { return instructorId; }
    public void setInstructorId(String instructorId) { this.instructorId = instructorId; }

    public String getInstructorName() { return instructorName; }
    public void setInstructorName(String instructorName) { this.instructorName = instructorName; }

    public long getSessionDate() { return sessionDate; }
    public void setSessionDate(long sessionDate) { this.sessionDate = sessionDate; }

    public String getSessionType() { return sessionType; }
    public void setSessionType(String sessionType) { this.sessionType = sessionType; }

    public String getZoomLink() { return zoomLink; }
    public void setZoomLink(String zoomLink) { this.zoomLink = zoomLink; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }

    // Helper method for getting sessionDate as timestamp
    public long getSessionDateTimestamp() {
        return sessionDate;
    }


    public String getFormattedSessionDate() {
        if (sessionDate == 0) return "N/A";
        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy h:mm a", Locale.getDefault());
        return sdf.format(new Date(sessionDate));
    }


    public String getSessionDateShort() {
        if (sessionDate == 0) return "N/A";
        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
        return sdf.format(new Date(sessionDate));
    }


    public String getSessionTimeOnly() {
        if (sessionDate == 0) return "N/A";
        SimpleDateFormat sdf = new SimpleDateFormat("h:mm a", Locale.getDefault());
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

    @Override
    public String toString() {
        return "TherapySession{" +
                "sessionId='" + sessionId + '\'' +
                ", client='" + clientName + '\'' +
                ", instructor='" + instructorName + '\'' +
                ", status='" + status + '\'' +
                ", sessionDate=" + getFormattedSessionDate() +
                '}';
    }
}