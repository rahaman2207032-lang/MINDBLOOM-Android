package com.example.mindbloomandroid.model;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Message {
    private String messageId;
    private String senderId;
    private String senderName;
    private String receiverId;
    private String receiverName;
    private String messageText;
    private long sentAt;
    private long readAt;

    public Message() {}

    public Message(String senderId, String senderName, String receiverId,
                   String receiverName, String messageText) {
        this.senderId = senderId;
        this.senderName = senderName;
        this.receiverId = receiverId;
        this.receiverName = receiverName;
        this.messageText = messageText;
        this.sentAt = System.currentTimeMillis();
    }

    // Getters and Setters
    public String getMessageId() { return messageId; }
    public void setMessageId(String messageId) { this.messageId = messageId; }

    public String getSenderId() { return senderId; }
    public void setSenderId(String senderId) { this.senderId = senderId; }

    public String getSenderName() { return senderName; }
    public void setSenderName(String senderName) { this.senderName = senderName; }

    public String getReceiverId() { return receiverId; }
    public void setReceiverId(String receiverId) { this.receiverId = receiverId; }

    public String getReceiverName() { return receiverName; }
    public void setReceiverName(String receiverName) { this.receiverName = receiverName; }

    public String getMessageText() { return messageText; }
    public void setMessageText(String messageText) { this.messageText = messageText; }

    // Alias methods for compatibility
    public String getContent() { return messageText; }
    public void setContent(String content) { this.messageText = content; }

    public long getSentAt() { return sentAt; }
    public void setSentAt(long sentAt) { this.sentAt = sentAt; }

    // Alias methods for compatibility
    public long getTimestamp() { return sentAt; }
    public void setTimestamp(long timestamp) { this.sentAt = timestamp; }

    // Status field for compatibility (not stored in Firebase, just for UI)
    private transient String status;
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public long getReadAt() { return readAt; }
    public void setReadAt(long readAt) { this.readAt = readAt; }


    public String getFormattedSentAt() {
        if (sentAt == 0) return "N/A";
        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy h:mm a", Locale.getDefault());
        return sdf.format(new Date(sentAt));
    }

    public String getSentAtShort() {
        if (sentAt == 0) return "N/A";
        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
        return sdf.format(new Date(sentAt));
    }


    public String getSentTimeOnly() {
        if (sentAt == 0) return "N/A";
        SimpleDateFormat sdf = new SimpleDateFormat("h:mm a", Locale.getDefault());
        return sdf.format(new Date(sentAt));
    }


    public String getFormattedReadAt() {
        if (readAt == 0) return "Unread";
        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy h:mm a", Locale.getDefault());
        return sdf.format(new Date(readAt));
    }


    public String getReadTimeOnly() {
        if (readAt == 0) return "Unread";
        SimpleDateFormat sdf = new SimpleDateFormat("h:mm a", Locale.getDefault());
        return sdf.format(new Date(readAt));
    }


    public boolean isRead() {
        return readAt > 0;
    }

    @Override
    public String toString() {
        return "Message{" +
                "messageId='" + messageId + '\'' +
                ", from='" + senderName + '\'' +
                ", sentAt=" + getFormattedSentAt() +
                ", read=" + isRead() +
                '}';
    }
}
