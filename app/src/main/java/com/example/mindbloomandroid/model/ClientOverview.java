package com.example.mindbloomandroid.model;


public class ClientOverview {
    private String clientId;
    private String clientName;
    private double averageMood; // 1-5 scale
    private String stressLevel; // "LOW", "MODERATE", "HIGH"
    private long lastSessionDate; // timestamp
    private int totalSessions;

    public ClientOverview() {}

    public ClientOverview(String clientId, String clientName, double averageMood,
                          String stressLevel, long lastSessionDate, int totalSessions) {
        this.clientId = clientId;
        this.clientName = clientName;
        this.averageMood = averageMood;
        this.stressLevel = stressLevel;
        this.lastSessionDate = lastSessionDate;
        this.totalSessions = totalSessions;
    }

    // Getters and Setters
    public String getClientId() { return clientId; }
    public void setClientId(String clientId) { this.clientId = clientId; }

    public String getClientName() { return clientName; }
    public void setClientName(String clientName) { this.clientName = clientName; }

    public double getAverageMood() { return averageMood; }
    public void setAverageMood(double averageMood) { this.averageMood = averageMood; }

    public String getStressLevel() { return stressLevel; }
    public void setStressLevel(String stressLevel) { this.stressLevel = stressLevel; }

    public long getLastSessionDate() { return lastSessionDate; }
    public void setLastSessionDate(long lastSessionDate) { this.lastSessionDate = lastSessionDate; }

    public int getTotalSessions() { return totalSessions; }
    public void setTotalSessions(int totalSessions) { this.totalSessions = totalSessions; }
}