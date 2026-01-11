package com.example.mindbloomandroid.service;



import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.example.mindbloomandroid.model.Habit;
import com.example.mindbloomandroid.model.HabitCompletion;

import java.util.ArrayList;
import java.util.List;

public class HabitService {
    private DatabaseReference habitsRef;
    private DatabaseReference completionsRef;

    public HabitService() {
        habitsRef = FirebaseDatabase.getInstance().getReference("habits");
        completionsRef = FirebaseDatabase.getInstance().getReference("habit_completions");
    }


    public void addHabit(Habit habit, OnCompleteListener listener) {
        String habitId = habitsRef.push().getKey();
        if (habitId != null) {
            habit.setHabitId(habitId);
            habit.setCreatedAt(System.currentTimeMillis());

            android.util.Log.d("HabitService", "📝 Creating habit - ID: " + habitId + ", Name: " + habit.getName() + ", UserID: " + habit.getUserId());

            habitsRef.child(habitId).setValue(habit)
                    .addOnSuccessListener(aVoid -> {
                        android.util.Log.d("HabitService", "✅ Habit saved successfully - ID: " + habitId);
                        listener.onSuccess();
                    })
                    .addOnFailureListener(e -> {
                        android.util.Log.e("HabitService", "❌ Failed to save habit: " + e.getMessage());
                        listener.onError(e.getMessage());
                    });
        } else {
            android.util.Log.e("HabitService", "❌ Failed to generate habit ID");
            listener.onError("Failed to generate habit ID");
        }
    }


    public void getUserHabits(String userId, OnHabitsLoadedListener listener) {
        android.util.Log.d("HabitService", "📖 Loading habits for user: " + userId);

        // Use addValueEventListener for REAL-TIME updates (not addListenerForSingleValueEvent)
        habitsRef.orderByChild("userId").equalTo(userId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        List<Habit> habits = new ArrayList<>();
                        android.util.Log.d("HabitService", "📥 Received " + dataSnapshot.getChildrenCount() + " habits");

                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            Habit habit = snapshot.getValue(Habit.class);
                            if (habit != null) {
                                habit.setHabitId(snapshot.getKey());
                                habits.add(habit);
                            }
                        }
                        android.util.Log.d("HabitService", "✅ Loaded " + habits.size() + " habits (REAL-TIME UPDATE)");
                        listener.onHabitsLoaded(habits);
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                        android.util.Log.e("HabitService", "❌ Error loading habits: " + error.getMessage());
                        listener.onError(error.getMessage());
                    }
                });
    }


    public void getActiveHabitsRealtime(String userId, OnHabitsLoadedListener listener) {
        habitsRef.orderByChild("userId").equalTo(userId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        List<Habit> habits = new ArrayList<>();
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            Habit habit = snapshot.getValue(Habit.class);
                            if (habit != null && habit.isActive()) {
                                habit.setHabitId(snapshot.getKey());
                                habits.add(habit);
                            }
                        }
                        listener.onHabitsLoaded(habits);
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                        listener.onError(error.getMessage());
                    }
                });
    }


    public void updateHabit(Habit habit, OnCompleteListener listener) {
        if (habit.getHabitId() != null) {
            habitsRef.child(habit.getHabitId()).setValue(habit)
                    .addOnSuccessListener(aVoid -> listener. onSuccess())
                    .addOnFailureListener(e -> listener. onError(e.getMessage()));
        } else {
            listener. onError("Habit ID is null");
        }
    }

    public void completeHabit(String habitId, OnCompleteListener listener) {
        habitsRef.child(habitId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Habit habit = dataSnapshot.getValue(Habit.class);
                if (habit != null) {
                    habit.setHabitId(habitId);
                    habit.setCurrentStreak(habit.getCurrentStreak() + 1);

                    if (habit.getCurrentStreak() > habit.getLongestStreak()) {
                        habit.setLongestStreak(habit.getCurrentStreak());
                    }

                    habit.setLastCompletedAt(System.currentTimeMillis());

                    updateHabit(habit, listener);
                } else {
                    listener.onError("Habit not found");
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                listener.onError(error.getMessage());
            }
        });
    }


    public void completeHabit(HabitCompletion completion, OnCompleteListener listener) {
        String completionId = completionsRef.push().getKey();
        if (completionId != null) {
            completion.setCompletionId(completionId);
            completion.setCompletedAt(System.currentTimeMillis());

            // Save completion record
            completionsRef.child(completionId).setValue(completion)
                .addOnSuccessListener(aVoid -> {
                    // Update habit streak
                    completeHabit(completion.getHabitId(), listener);
                })
                .addOnFailureListener(e -> listener.onError(e.getMessage()));
        } else {
            listener.onError("Failed to generate completion ID");
        }
    }


    public void getCompletions(String habitId, OnCompletionsLoadedListener listener) {
        completionsRef.orderByChild("habitId").equalTo(habitId)
            .addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    List<HabitCompletion> completions = new ArrayList<>();
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        HabitCompletion completion = snapshot.getValue(HabitCompletion.class);
                        if (completion != null) {
                            completion.setCompletionId(snapshot.getKey());
                            completions.add(completion);
                        }
                    }
                    listener.onCompletionsLoaded(completions);
                }

                @Override
                public void onCancelled(DatabaseError error) {
                    listener.onError(error.getMessage());
                }
            });
    }

    public void deleteHabit(String habitId, OnCompleteListener listener) {
        habitsRef.child(habitId).removeValue()
                .addOnSuccessListener(aVoid -> listener.onSuccess())
                .addOnFailureListener(e -> listener.onError(e.getMessage()));
    }

    public void deactivateHabit(String habitId, OnCompleteListener listener) {
        habitsRef.child(habitId).child("active").setValue(false)
                .addOnSuccessListener(aVoid -> listener.onSuccess())
                .addOnFailureListener(e -> listener.onError(e. getMessage()));
    }


    public interface OnCompleteListener {
        void onSuccess();
        void onError(String error);
    }

    public interface OnHabitsLoadedListener {
        void onHabitsLoaded(List<Habit> habits);
        void onError(String error);
    }

    public interface OnCompletionsLoadedListener {
        void onCompletionsLoaded(List<HabitCompletion> completions);
        void onError(String error);
    }
}
