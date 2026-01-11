package com.example.mindbloomandroid.model;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class StressAssessment {
    private String assessmentId;
    private String userId;
    private long assessmentDate;

    // Questionnaire answers (scale 1-5)
    private int workloadLevel;
    private int sleepQualityLevel;
    private int anxietyLevel;
    private int moodLevel;
    private int physicalSymptomsLevel;
    private int concentrationLevel;
    private int socialConnectionLevel;

    // Calculated stress score (7-35 range)
    private int stressScore;

    // Stress level category
    private String stressLevel; // LOW, MODERATE, HIGH

    // Optional notes
    private String notes;

    public StressAssessment() {}

    public StressAssessment(String userId, int workloadLevel, int sleepQualityLevel,
                            int anxietyLevel, int moodLevel, int physicalSymptomsLevel,
                            int concentrationLevel, int socialConnectionLevel) {
        this.userId = userId;
        this.assessmentDate = System.currentTimeMillis();
        this.workloadLevel = workloadLevel;
        this.sleepQualityLevel = sleepQualityLevel;
        this.anxietyLevel = anxietyLevel;
        this.moodLevel = moodLevel;
        this.physicalSymptomsLevel = physicalSymptomsLevel;
        this.concentrationLevel = concentrationLevel;
        this.socialConnectionLevel = socialConnectionLevel;
        calculateStressScore();
    }

    // Calculate total stress score and determine level
    public void calculateStressScore() {
        this.stressScore = workloadLevel + sleepQualityLevel + anxietyLevel +
                moodLevel + physicalSymptomsLevel + concentrationLevel +
                socialConnectionLevel;

        if (stressScore <= 14) {
            this.stressLevel = "LOW";
        } else if (stressScore <= 24) {
            this.stressLevel = "MODERATE";
        } else {
            this.stressLevel = "HIGH";
        }
    }

    // Getters and Setters
    public String getAssessmentId() { return assessmentId; }
    public void setAssessmentId(String assessmentId) { this.assessmentId = assessmentId; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public long getAssessmentDate() { return assessmentDate; }
    public void setAssessmentDate(long assessmentDate) { this.assessmentDate = assessmentDate; }

    public int getWorkloadLevel() { return workloadLevel; }
    public void setWorkloadLevel(int workloadLevel) { this.workloadLevel = workloadLevel; }

    public int getSleepQualityLevel() { return sleepQualityLevel; }
    public void setSleepQualityLevel(int sleepQualityLevel) { this.sleepQualityLevel = sleepQualityLevel; }

    public int getAnxietyLevel() { return anxietyLevel; }
    public void setAnxietyLevel(int anxietyLevel) { this.anxietyLevel = anxietyLevel; }

    public int getMoodLevel() { return moodLevel; }
    public void setMoodLevel(int moodLevel) { this.moodLevel = moodLevel; }

    public int getPhysicalSymptomsLevel() { return physicalSymptomsLevel; }
    public void setPhysicalSymptomsLevel(int physicalSymptomsLevel) { this.physicalSymptomsLevel = physicalSymptomsLevel; }

    public int getConcentrationLevel() { return concentrationLevel; }
    public void setConcentrationLevel(int concentrationLevel) { this.concentrationLevel = concentrationLevel; }

    public int getSocialConnectionLevel() { return socialConnectionLevel; }
    public void setSocialConnectionLevel(int socialConnectionLevel) { this.socialConnectionLevel = socialConnectionLevel; }

    public int getStressScore() { return stressScore; }
    public void setStressScore(int stressScore) { this.stressScore = stressScore; }

    public String getStressLevel() { return stressLevel; }
    public void setStressLevel(String stressLevel) { this.stressLevel = stressLevel; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }


    public String getFormattedAssessmentDate() {
        if (assessmentDate == 0) return "N/A";
        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy h:mm a", Locale.getDefault());
        return sdf.format(new Date(assessmentDate));
    }


    public String getAssessmentDateShort() {
        if (assessmentDate == 0) return "N/A";
        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
        return sdf.format(new Date(assessmentDate));
    }


    public String getAssessmentTimeOnly() {
        if (assessmentDate == 0) return "N/A";
        SimpleDateFormat sdf = new SimpleDateFormat("h:mm a", Locale.getDefault());
        return sdf.format(new Date(assessmentDate));
    }

    @Override
    public String toString() {
        return "StressAssessment{" +
                "assessmentId='" + assessmentId + '\'' +
                ", stressLevel='" + stressLevel + '\'' +
                ", stressScore=" + stressScore +
                ", date=" + getFormattedAssessmentDate() +
                '}';
    }
}
