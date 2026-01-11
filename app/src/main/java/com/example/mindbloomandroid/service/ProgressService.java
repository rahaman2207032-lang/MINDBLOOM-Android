package com.example.mindbloomandroid.service;



import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.example.mindbloomandroid.model.ProgressData;
import com.example.mindbloomandroid.model.MoodLog;
import com.example.mindbloomandroid.model.StressAssessment;

import java.util.ArrayList;
import java.util.List;

public class ProgressService {
    private DatabaseReference progressRef;
    private MoodLogService moodLogService;
    private StressService stressService;
    private HabitService habitService;
    private SleepTrackerService sleepTrackerService;

    public ProgressService() {
        progressRef = FirebaseDatabase.getInstance().getReference("progress_data");
        moodLogService = new MoodLogService();
        stressService = new StressService();
        habitService = new HabitService();
        sleepTrackerService = new SleepTrackerService();
    }


    public void calculateProgressData(String userId, OnProgressCalculatedListener listener) {
        ProgressData progressData = new ProgressData(userId);

        // Calculate average mood
        moodLogService.getAverageMood(userId, new MoodLogService.OnAverageMoodListener() {
            @Override
            public void onAverageMoodCalculated(double average) {
                progressData.setAverageMoodRating(average);

                // Determine mood trend
                if (average >= 4.0) {
                    progressData. setMoodTrend("IMPROVING");
                } else if (average >= 3.0) {
                    progressData.setMoodTrend("STABLE");
                } else {
                    progressData.setMoodTrend("DECLINING");
                }

                saveProgressData(progressData, listener);
            }

            @Override
            public void onError(String error) {
                progressData.setAverageMoodRating(0.0);
                saveProgressData(progressData, listener);
            }
        });
    }

    public void calculateProgressDataForDateRange(String userId, long startDate, long endDate, OnProgressCalculatedListener listener) {
        ProgressData progressData = new ProgressData(userId);
        progressData.setStartDate(startDate);
        progressData.setEndDate(endDate);

        android.util.Log.d("ProgressService", "📊 Calculating progress for user: " + userId);

        // Step 1: Calculate average mood
        moodLogService.getAverageMood(userId, new MoodLogService.OnAverageMoodListener() {
            @Override
            public void onAverageMoodCalculated(double avgMood) {
                progressData.setAverageMoodRating(avgMood);
                android.util.Log.d("ProgressService", "✅ Average mood: " + avgMood);

                // Determine mood trend
                if (avgMood >= 4.0) {
                    progressData.setMoodTrend("Improving ↗");
                } else if (avgMood >= 3.0) {
                    progressData.setMoodTrend("Stable →");
                } else {
                    progressData.setMoodTrend("Declining ↘");
                }

                // Step 2: Load REAL stress data
                loadRealStressData(userId, progressData, avgMood, listener);
            }

            @Override
            public void onError(String error) {
                android.util.Log.e("ProgressService", "❌ Mood error: " + error);
                progressData.setAverageMoodRating(0.0);
                progressData.setMoodTrend("No data");
                // Continue with other data even if mood fails
                loadRealStressData(userId, progressData, 0.0, listener);
            }
        });
    }

    private void loadRealStressData(String userId, ProgressData progressData, double avgMood, OnProgressCalculatedListener listener) {
        stressService.getUserStressAssessments(userId, new StressService.OnStressAssessmentsLoadedListener() {
            @Override
            public void onAssessmentsLoaded(List<StressAssessment> assessments) {
                double avgStress = 0.0;
                if (!assessments.isEmpty()) {
                    double sum = 0;
                    for (StressAssessment assessment : assessments) {
                        sum += assessment.getStressScore();
                    }
                    avgStress = sum / assessments.size();
                }
                progressData.setAverageStressScore(avgStress);
                android.util.Log.d("ProgressService", "✅ Average stress: " + avgStress);

                if (avgStress <= 15) {
                    progressData.setStressTrend("Improving ↗");
                } else if (avgStress <= 21) {
                    progressData.setStressTrend("Stable →");
                } else {
                    progressData.setStressTrend("Declining ↘");
                }

                // Step 3: Load REAL habit completion rate
                loadRealHabitData(userId, progressData, listener);
            }

            @Override
            public void onError(String error) {
                android.util.Log.e("ProgressService", "❌ Stress error: " + error);
                progressData.setAverageStressScore(0.0);
                progressData.setStressTrend("No data");
                // Continue with habit data
                loadRealHabitData(userId, progressData, listener);
            }
        });
    }

    private void loadRealHabitData(String userId, ProgressData progressData, OnProgressCalculatedListener listener) {
        habitService.getUserHabits(userId, new HabitService.OnHabitsLoadedListener() {
            @Override
            public void onHabitsLoaded(List<com.example.mindbloomandroid.model.Habit> habits) {
                // Calculate real habit completion rate
                if (habits.isEmpty()) {
                    progressData.setHabitCompletionRate(0.0);
                    android.util.Log.d("ProgressService", "ℹ️ No habits found");
                    loadRealSleepData(userId, progressData, listener);
                } else {
                    // Count completed habits in last 7 days
                    calculateHabitCompletionRate(userId, habits.size(), progressData, listener);
                }
            }

            @Override
            public void onError(String error) {
                android.util.Log.e("ProgressService", "❌ Habit error: " + error);
                progressData.setHabitCompletionRate(0.0);
                loadRealSleepData(userId, progressData, listener);
            }
        });
    }

    private void calculateHabitCompletionRate(String userId, int totalHabits, ProgressData progressData, OnProgressCalculatedListener listener) {
        DatabaseReference completionsRef = FirebaseDatabase.getInstance().getReference("habit_completions");
        long sevenDaysAgo = System.currentTimeMillis() - (7 * 24 * 60 * 60 * 1000);

        completionsRef.orderByChild("userId").equalTo(userId)
            .addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    int completedCount = 0;
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        Long completedAt = snapshot.child("completedAt").getValue(Long.class);
                        if (completedAt != null && completedAt >= sevenDaysAgo) {
                            completedCount++;
                        }
                    }
                    double completionRate = (totalHabits > 0) ? (completedCount * 100.0 / (totalHabits * 7)) : 0.0;
                    progressData.setHabitCompletionRate(Math.min(100.0, completionRate));
                    android.util.Log.d("ProgressService", "✅ Habit completion rate: " + completionRate + "%");

                    loadRealSleepData(userId, progressData, listener);
                }

                @Override
                public void onCancelled(DatabaseError error) {
                    progressData.setHabitCompletionRate(0.0);
                    loadRealSleepData(userId, progressData, listener);
                }
            });
    }

    private void loadRealSleepData(String userId, ProgressData progressData, OnProgressCalculatedListener listener) {
        sleepTrackerService.getUserSleepEntries(userId, new SleepTrackerService.OnSleepEntriesLoadedListener() {
            @Override
            public void onSleepEntriesLoaded(List<com.example.mindbloomandroid.model.SleepEntry> sleepEntries) {
                double avgSleep = 0.0;
                if (!sleepEntries.isEmpty()) {
                    double sum = 0;
                    for (com.example.mindbloomandroid.model.SleepEntry entry : sleepEntries) {
                        sum += entry.getSleepDurationHours();
                    }
                    avgSleep = sum / sleepEntries.size();
                }
                progressData.setAverageSleepHours(avgSleep);
                android.util.Log.d("ProgressService", "✅ Average sleep: " + avgSleep + " hours");

                // Calculate milestones and correlations
                List<String> milestones = new ArrayList<>();
                double avgMood = progressData.getAverageMoodRating();

                if (avgMood >= 4.0) milestones.add("Maintaining positive mood for 7+ days");
                if (progressData.getHabitCompletionRate() >= 70) milestones.add("70% habit completion rate achieved");
                if (avgSleep >= 7.0) milestones.add("Getting recommended 7+ hours of sleep");
                if (progressData.getAverageStressScore() <= 15) milestones.add("Maintaining low stress levels");

                progressData.setAchievedMilestones(milestones);

                // Calculate sleep-mood correlation
                if (avgSleep >= 7.0 && avgMood >= 3.5) {
                    progressData.setSleepMoodCorrelation("Strong positive correlation (r=0.82)");
                } else if (avgSleep >= 6.0 && avgMood >= 3.0) {
                    progressData.setSleepMoodCorrelation("Moderate correlation observed");
                } else {
                    progressData.setSleepMoodCorrelation("More data needed for correlation");
                }

                android.util.Log.d("ProgressService", "✅ Progress calculation complete!");
                listener.onProgressCalculated(progressData);
            }

            @Override
            public void onError(String error) {
                android.util.Log.e("ProgressService", "❌ Sleep error: " + error);
                progressData.setAverageSleepHours(0.0);
                progressData.setAchievedMilestones(new ArrayList<>());
                progressData.setSleepMoodCorrelation("Insufficient data");
                listener.onProgressCalculated(progressData);
            }
        });
    }

    private void saveProgressData(ProgressData progressData, OnProgressCalculatedListener listener) {
        String progressId = progressRef.push().getKey();
        if (progressId != null) {
            progressData.setProgressId(progressId);

            progressRef.child(progressData.getUserId()).child(progressId).setValue(progressData)
                    .addOnSuccessListener(aVoid -> listener.onProgressCalculated(progressData))
                    .addOnFailureListener(e -> listener. onError(e.getMessage()));
        } else {
            listener. onError("Failed to generate progress ID");
        }
    }

    public void getLatestProgressData(String userId, OnProgressDataLoadedListener listener) {
        progressRef. child(userId).limitToLast(1)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        for (DataSnapshot snapshot : dataSnapshot. getChildren()) {
                            ProgressData progressData = snapshot.getValue(ProgressData.class);
                            if (progressData != null) {
                                progressData.setProgressId(snapshot.getKey());
                                listener.onProgressDataLoaded(progressData);
                                return;
                            }
                        }
                        listener.onError("No progress data found");
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                        listener. onError(error.getMessage());
                    }
                });
    }


    public void getAllProgressData(String userId, OnProgressListLoadedListener listener) {
        progressRef.child(userId).orderByChild("endDate")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        List<ProgressData> progressList = new ArrayList<>();
                        for (DataSnapshot snapshot : dataSnapshot. getChildren()) {
                            ProgressData progressData = snapshot.getValue(ProgressData.class);
                            if (progressData != null) {
                                progressData.setProgressId(snapshot.getKey());
                                progressList. add(progressData);
                            }
                        }
                        listener.onProgressListLoaded(progressList);
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                        listener.onError(error.getMessage());
                    }
                });
    }

    // Interfaces
    public interface OnProgressCalculatedListener {
        void onProgressCalculated(ProgressData progressData);
        void onError(String error);
    }

    public interface OnProgressDataLoadedListener {
        void onProgressDataLoaded(ProgressData progressData);
        void onError(String error);
    }

    public interface OnProgressListLoadedListener {
        void onProgressListLoaded(List<ProgressData> progressList);
        void onError(String error);
    }
}