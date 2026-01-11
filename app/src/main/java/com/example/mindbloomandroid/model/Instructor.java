package com.example.mindbloomandroid.model;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;


public class Instructor {
    private Long id;                  // Sequential ID (1, 2, 3...) - matches backend
    private String instructorId;      // Firebase UID (for Firebase integration)
    private String username;          // Username for login
    private String password;          // ✅ Password for authentication (stored in database)
    private String role;              // Always "Instructor"
    private long createdAt;           // Creation timestamp (milliseconds)
    private long updatedAt;           // Last update timestamp (milliseconds)

    // Required empty constructor for Firebase
    public Instructor() {
        this.role = "Instructor"; // Default role
    }

    public Instructor(String instructorId, String username, String password) {
        this.instructorId = instructorId;
        this.username = username;
        this.password = password;  // ✅ Store password
        this.role = "Instructor";
        this.createdAt = System.currentTimeMillis();
        this.updatedAt = System.currentTimeMillis();
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getInstructorId() {
        return instructorId;
    }

    public void setInstructorId(String instructorId) {
        this.instructorId = instructorId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        // SECURITY: Instructors must always have "Instructor" role
        this.role = "Instructor";
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    public long getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(long updatedAt) {
        this.updatedAt = updatedAt;
    }

    // Utility methods for proper date formatting


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
        if (updatedAt == 0) return "N/A";
        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy h:mm a", Locale.getDefault());
        return sdf.format(new Date(updatedAt));
    }


    public String getDisplayIdText() {
        return id != null ? "Instructor #" + id : "New Instructor";
    }


    public void onCreate() {
        createdAt = System.currentTimeMillis();
        updatedAt = System.currentTimeMillis();
    }


    public void onUpdate() {
        updatedAt = System.currentTimeMillis();
    }

    @Override
    public String toString() {
        // For Spinner display - show username only
        return username != null ? username : "Unknown Instructor";
    }
}

