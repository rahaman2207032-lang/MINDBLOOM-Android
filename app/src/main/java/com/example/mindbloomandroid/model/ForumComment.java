package com.example.mindbloomandroid.model;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ForumComment {
    private String commentId;
    private String postId;
    private String content;
    private String userId;
    private String userName;
    private boolean anonymous;
    private long createdAt;

    public ForumComment() {}

    public ForumComment(String postId, String content, String userId, String userName, boolean anonymous) {
        this.postId = postId;
        this.content = content;
        this.userId = userId;
        this.userName = userName;
        this.anonymous = anonymous;
        this.createdAt = System.currentTimeMillis();
    }

    // Getters and Setters
    public String getCommentId() { return commentId; }
    public void setCommentId(String commentId) { this.commentId = commentId; }

    public String getPostId() { return postId; }
    public void setPostId(String postId) { this.postId = postId; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }

    public boolean isAnonymous() { return anonymous; }
    public void setAnonymous(boolean anonymous) { this.anonymous = anonymous; }

    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }

    // Helper methods
    public String getFormattedCreatedAt() {
        if (createdAt == 0) return "Just now";
        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, h:mm a", Locale.getDefault());
        return sdf.format(new Date(createdAt));
    }

    public String getDisplayUserName() {
        return anonymous ? "Anonymous" : userName;
    }

    public String getTimeAgo() {
        if (createdAt == 0) return "Just now";
        
        long diff = System.currentTimeMillis() - createdAt;
        long seconds = diff / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        long days = hours / 24;

        if (days > 0) return days + "d ago";
        if (hours > 0) return hours + "h ago";
        if (minutes > 0) return minutes + "m ago";
        return "Just now";
    }
}
