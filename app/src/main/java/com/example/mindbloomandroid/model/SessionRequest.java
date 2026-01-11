package com.example.mindbloomandroid.model;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class SessionRequest {
    private String requestId;
    private String userId;
    private String clientName;
    private String instructorId;
    private String instructorName;
    private long requestedDateTime;
    private String sessionType;
    private String reason;
    private String status; // PENDING, CONFIRMED, REJECTED, COMPLETED
    private String zoomLink;
    private long createdAt;
    private long updatedAt;

    public SessionRequest() {}

    public SessionRequest(String userId, String instructorId, long requestedDateTime,
                          String sessionType, String reason) {
        this.userId = userId;
        this.instructorId = instructorId;
        this.requestedDateTime = requestedDateTime;
        this.sessionType = sessionType;
        this.reason = reason;
        this.status = "PENDING";
        this.createdAt = System.currentTimeMillis();
    }

    // Getters and Setters
    public String getRequestId() { return requestId; }
    public void setRequestId(String requestId) { this.requestId = requestId; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getClientName() { return clientName; }
    public void setClientName(String clientName) { this.clientName = clientName; }

    public String getInstructorId() { return instructorId; }
    public void setInstructorId(String instructorId) { this.instructorId = instructorId; }

    public String getInstructorName() { return instructorName; }
    public void setInstructorName(String instructorName) { this.instructorName = instructorName; }

    public long getRequestedDateTime() { return requestedDateTime; }
    public void setRequestedDateTime(long requestedDateTime) { this.requestedDateTime = requestedDateTime; }

    public String getSessionType() { return sessionType; }
    public void setSessionType(String sessionType) { this.sessionType = sessionType; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getZoomLink() { return zoomLink; }
    public void setZoomLink(String zoomLink) { this.zoomLink = zoomLink; }

    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }

    public void setUpdatedAt(long updatedAt) { this.updatedAt = updatedAt; }

    // Helper methods for Android - Fixed to return String IDs
    public String getId() {
        return requestId;
    }

    public String getClientId() {
        return userId;
    }

    public java.util.Date getRequestedDate() {
        return new java.util.Date(requestedDateTime);
    }

    // Alias method for compatibility
    public String getPreferredTime() {
        return getFormattedRequestedDateTime();
    }


    public String getFormattedRequestedDateTime() {
        if (requestedDateTime == 0) return "N/A";
        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy h:mm a", Locale.getDefault());
        return sdf.format(new Date(requestedDateTime));
    }

    public String getRequestedDateShort() {
        if (requestedDateTime == 0) return "N/A";
        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
        return sdf.format(new Date(requestedDateTime));
    }


    public String getRequestedTimeOnly() {
        if (requestedDateTime == 0) return "N/A";
        SimpleDateFormat sdf = new SimpleDateFormat("h:mm a", Locale.getDefault());
        return sdf.format(new Date(requestedDateTime));
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
        return "SessionRequest{" +
                "requestId='" + requestId + '\'' +
                ", client='" + clientName + '\'' +
                ", status='" + status + '\'' +
                ", requestedFor=" + getFormattedRequestedDateTime() +
                '}';
    }
}
