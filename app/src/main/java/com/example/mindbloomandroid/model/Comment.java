package com.example.mindbloomandroid.model;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;


public class Comment {
    private String commentId;
    private String postId;
    private String userId;
    private String username;
    private String commentText;
    private long createdAt;

    // Required empty constructor for Firebase
    public Comment() {}

    public Comment(String postId, String userId, String username, String commentText) {
        this.postId = postId;
        this.userId = userId;
        this.username = username;
        this.commentText = commentText;
        this.createdAt = System.currentTimeMillis();
    }

    // Getters and Setters
    public String getCommentId() { return commentId; }
    public void setCommentId(String commentId) { this.commentId = commentId; }

    public String getPostId() { return postId; }
    public void setPostId(String postId) { this.postId = postId; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getCommentText() { return commentText; }
    public void setCommentText(String commentText) { this.commentText = commentText; }

    // Alias methods for compatibility
    public String getContent() { return commentText; }
    public void setContent(String content) { this.commentText = content; }

    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }

    // Alias methods for compatibility
    public long getTimestamp() { return createdAt; }
    public void setTimestamp(long timestamp) { this.createdAt = timestamp; }


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

    public String getTimeAgo() {
        if (createdAt == 0) return "N/A";

        long now = System.currentTimeMillis();
        long diff = now - createdAt;

        long seconds = diff / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        long days = hours / 24;

        if (seconds < 60) {
            return "Just now";
        } else if (minutes < 60) {
            return minutes + " minute" + (minutes == 1 ? "" : "s") + " ago";
        } else if (hours < 24) {
            return hours + " hour" + (hours == 1 ? "" : "s") + " ago";
        } else if (days == 1) {
            return "Yesterday";
        } else if (days < 7) {
            return days + " days ago";
        } else {
            return getCreatedAtShort();
        }
    }

    @Override
    public String toString() {
        return "Comment{" +
                "commentId='" + commentId + '\'' +
                ", username='" + username + '\'' +
                ", text='" + commentText + '\'' +
                ", createdAt=" + getTimeAgo() +
                '}';
    }
}

