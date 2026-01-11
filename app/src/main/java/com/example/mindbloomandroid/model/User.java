package com.example.mindbloomandroid.model;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class User {
    private String userId;
    private int displayId;
    private String username;
    private String password;
    private String role;
    private long createdAt;

    // Required empty constructor for Firebase
    public User() {}

    public User(String userId, String username, String password, String role) {
        this.userId = userId;
        this.username = username;
        this.password = password;
        // Force role to be "USER" - Instructors use the Instructor model
        this.role = "USER";
        this.createdAt = System.currentTimeMillis();
        // displayId will be set by database counter
    }

    // Getters and Setters
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public int getDisplayId() { return displayId; }
    public void setDisplayId(int displayId) { this.displayId = displayId; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }


    public String getRole() { return role; }
    public void setRole(String role) {
        // SECURITY: Prevent role escalation - Users can only be "USER"
        // Instructors use the Instructor model in a separate table
        this.role = "USER";
    }


    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }


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


    public String getDisplayIdText() {
        return displayId > 0 ? "User #" + displayId : "New User";
    }

    @Override
    public String toString() {
        return "User: " + username + " (ID: " + displayId + ")";
    }
}