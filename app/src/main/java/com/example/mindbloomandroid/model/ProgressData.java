package com.example.mindbloomandroid.model;



import java.util.ArrayList;
import java.util.List;

public class ProgressData {
    private String progressId;
    private String userId;
    private long startDate; // timestamp
    private long endDate; // timestamp

    // Aggregated metrics
    private double averageMoodRating;
    private double averageStressScore;
    private int totalHabitsCompleted;
    private double habitCompletionRate; // percentage
    private double averageSleepHours;

    // Trend indicators
    private String moodTrend; // "IMPROVING", "STABLE", "DECLINING"
    private String stressTrend; // "IMPROVING", "STABLE", "WORSENING"

    // Milestone tracking
    private int consecutiveDaysOfMoodLogging;
    private int consecutiveDaysOfStressManagement;
    private List<String> achievedMilestones;

    // Correlations
    private String sleepMoodCorrelation;

    public ProgressData() {
        this.achievedMilestones = new ArrayList<>();
    }

    public ProgressData(String userId) {
        this.userId = userId;
        this.endDate = System.currentTimeMillis();
        this.startDate = endDate - (30L * 24 * 60 * 60 * 1000); // 30 days ago
        this.achievedMilestones = new ArrayList<>();
    }

    // Getters and Setters
    public String getProgressId() { return progressId; }
    public void setProgressId(String progressId) { this.progressId = progressId; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public long getStartDate() { return startDate; }
    public void setStartDate(long startDate) { this.startDate = startDate; }

    public long getEndDate() { return endDate; }
    public void setEndDate(long endDate) { this.endDate = endDate; }

    public double getAverageMoodRating() { return averageMoodRating; }
    public void setAverageMoodRating(double averageMoodRating) { this.averageMoodRating = averageMoodRating; }

    public double getAverageStressScore() { return averageStressScore; }
    public void setAverageStressScore(double averageStressScore) { this.averageStressScore = averageStressScore; }

    public int getTotalHabitsCompleted() { return totalHabitsCompleted; }
    public void setTotalHabitsCompleted(int totalHabitsCompleted) { this.totalHabitsCompleted = totalHabitsCompleted; }

    public double getHabitCompletionRate() { return habitCompletionRate; }
    public void setHabitCompletionRate(double habitCompletionRate) { this.habitCompletionRate = habitCompletionRate; }

    public double getAverageSleepHours() { return averageSleepHours; }
    public void setAverageSleepHours(double averageSleepHours) { this.averageSleepHours = averageSleepHours; }

    public String getMoodTrend() { return moodTrend; }
    public void setMoodTrend(String moodTrend) { this.moodTrend = moodTrend; }

    public String getStressTrend() { return stressTrend; }
    public void setStressTrend(String stressTrend) { this.stressTrend = stressTrend; }

    public int getConsecutiveDaysOfMoodLogging() { return consecutiveDaysOfMoodLogging; }
    public void setConsecutiveDaysOfMoodLogging(int consecutiveDaysOfMoodLogging) {
        this.consecutiveDaysOfMoodLogging = consecutiveDaysOfMoodLogging;
    }

    public int getConsecutiveDaysOfStressManagement() { return consecutiveDaysOfStressManagement; }
    public void setConsecutiveDaysOfStressManagement(int consecutiveDaysOfStressManagement) {
        this.consecutiveDaysOfStressManagement = consecutiveDaysOfStressManagement;
    }

    public List<String> getAchievedMilestones() { return achievedMilestones; }
    public void setAchievedMilestones(List<String> achievedMilestones) { this.achievedMilestones = achievedMilestones; }

    public String getSleepMoodCorrelation() { return sleepMoodCorrelation; }
    public void setSleepMoodCorrelation(String sleepMoodCorrelation) { this.sleepMoodCorrelation = sleepMoodCorrelation; }
}