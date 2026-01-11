package com.example.mindbloomandroid.model;

public class Conversation {
    private String conversationId;
    private String clientId;
    private String clientName;
    private String lastMessage;
    private long lastMessageTime;
    private int unreadCount;

    public Conversation() {}

    public Conversation(String clientId, String clientName) {
        this.clientId = clientId;
        this.clientName = clientName;
        this.lastMessage = "";
        this.lastMessageTime = 0;
        this.unreadCount = 0;
    }

    public String getConversationId() { return conversationId; }
    public void setConversationId(String conversationId) { this.conversationId = conversationId; }

    public String getClientId() { return clientId; }
    public void setClientId(String clientId) { this.clientId = clientId; }

    public String getClientName() { return clientName; }
    public void setClientName(String clientName) { this.clientName = clientName; }

    public String getLastMessage() { return lastMessage; }
    public void setLastMessage(String lastMessage) { this.lastMessage = lastMessage; }

    public long getLastMessageTime() { return lastMessageTime; }
    public void setLastMessageTime(long lastMessageTime) { this.lastMessageTime = lastMessageTime; }

    public int getUnreadCount() { return unreadCount; }
    public void setUnreadCount(int unreadCount) { this.unreadCount = unreadCount; }

    public String getClientInitial() {
        return clientName != null && !clientName.isEmpty() 
            ? clientName.substring(0, 1).toUpperCase() 
            : "?";
    }
}
