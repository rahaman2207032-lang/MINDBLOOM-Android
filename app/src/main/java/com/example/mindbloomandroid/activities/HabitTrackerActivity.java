package com.example.mindbloomandroid.activities;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.mindbloomandroid.R;
import com.example.mindbloomandroid.adapter.HabitAdapter;
import com.example.mindbloomandroid.model.Habit;
import com.example.mindbloomandroid.model.HabitCompletion;
import com.example.mindbloomandroid.service.HabitService;
import com.example.mindbloomandroid.utility.SharedPreferencesManager;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;


public class HabitTrackerActivity extends AppCompatActivity {

    private RecyclerView habitRecyclerView;
    private FloatingActionButton fabAddHabit;
    private ProgressBar progressBar;
    private Toolbar toolbar;
    private TextView totalHabitsText;
    private TextView longestStreakText;

    private HabitService habitService;
    private SharedPreferencesManager prefsManager;
    private HabitAdapter habitAdapter;
    private List<Habit> habits;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_habit_tracker);

        initializeViews();
        setupToolbar();

        habitService = new HabitService();
        prefsManager = SharedPreferencesManager.getInstance(this);
        habits = new ArrayList<>();

        setupRecyclerView();
        loadHabits();

        fabAddHabit.setOnClickListener(v -> showAddHabitDialog());
    }

    private void initializeViews() {
        toolbar = findViewById(R.id.toolbar);
        habitRecyclerView = findViewById(R.id.habitRecyclerView);
        fabAddHabit = findViewById(R.id.fabAddHabit);
        progressBar = findViewById(R.id.progressBar);
        totalHabitsText = findViewById(R.id.totalHabitsText);
        longestStreakText = findViewById(R.id.longestStreakText);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Habit Tracker");
        }
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void setupRecyclerView() {
        habitAdapter = new HabitAdapter(this, habits, 
            habit -> markHabitComplete(habit),
            habit -> viewHabitHistory(habit),
            habit -> deleteHabit(habit));
        habitRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        habitRecyclerView.setAdapter(habitAdapter);
    }

    private void loadHabits() {
        String userId = prefsManager.getUserId();
        if (userId == null) {
            android.util.Log.e("HabitTracker", "❌ User ID is null - cannot load habits");
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        android.util.Log.d("HabitTracker", "📖 Loading habits for user: " + userId);
        progressBar.setVisibility(View.VISIBLE);

        habitService.getUserHabits(userId, new HabitService.OnHabitsLoadedListener() {
            @Override
            public void onHabitsLoaded(List<Habit> loadedHabits) {
                android.util.Log.d("HabitTracker", "✅ Habits loaded: " + loadedHabits.size() + " habits");

                progressBar.setVisibility(View.GONE);
                habits.clear();
                habits.addAll(loadedHabits);
                habitAdapter.notifyDataSetChanged();
                
                updateStatistics();

                if (habits.isEmpty()) {
                    android.util.Log.d("HabitTracker", "ℹ️ No habits found for user");
                    Toast.makeText(HabitTrackerActivity.this,
                        "No habits yet. Tap + to add your first habit!", 
                        Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(HabitTrackerActivity.this,
                        "Loaded " + habits.size() + " habits",
                        Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onError(String error) {
                android.util.Log.e("HabitTracker", "❌ Error loading habits: " + error);
                progressBar.setVisibility(View.GONE);
                Toast.makeText(HabitTrackerActivity.this, 
                    "Error loading habits: " + error, 
                    Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void showAddHabitDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Create New Habit");
        
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_habit, null);
        builder.setView(dialogView);

        EditText nameField = dialogView.findViewById(R.id.habitNameEditText);
        EditText descField = dialogView.findViewById(R.id.habitDescriptionEditText);
        Spinner frequencySpinner = dialogView.findViewById(R.id.frequencySpinner);
        EditText targetDaysField = dialogView.findViewById(R.id.targetDaysEditText);

        // Setup frequency spinner
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
            android.R.layout.simple_spinner_item,
            new String[]{"DAILY", "WEEKLY"});
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        frequencySpinner.setAdapter(adapter);
        targetDaysField.setText("ALL");

        builder.setPositiveButton("Create", (dialog, which) -> {
            String name = nameField.getText().toString().trim();
            String description = descField.getText().toString().trim();
            String frequency = frequencySpinner.getSelectedItem().toString();
            String targetDays = targetDaysField.getText().toString().trim();

            if (name.isEmpty()) {
                Toast.makeText(this, "Please enter habit name", Toast.LENGTH_SHORT).show();
                return;
            }

            if (targetDays.isEmpty()) {
                targetDays = "ALL";
            }

            saveHabit(name, description, frequency, targetDays);
        });

        builder.setNegativeButton("Cancel", null);
        builder.create().show();
    }

    private void saveHabit(String name, String description, String frequency, String targetDays) {
        String userId = prefsManager.getUserId();
        
        Habit habit = new Habit(userId, name, description, frequency, targetDays);
        
        progressBar.setVisibility(View.VISIBLE);
        habitService.addHabit(habit, new HabitService.OnCompleteListener() {
            @Override
            public void onSuccess() {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(HabitTrackerActivity.this, 
                    "Habit added successfully!", 
                    Toast.LENGTH_SHORT).show();
                loadHabits();
            }

            @Override
            public void onError(String error) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(HabitTrackerActivity.this, 
                    "Error saving habit: " + error, 
                    Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void markHabitComplete(Habit habit) {
        String userId = prefsManager.getUserId();

        // Get today's date as timestamp (midnight)
        java.util.Calendar cal = java.util.Calendar.getInstance();
        cal.set(java.util.Calendar.HOUR_OF_DAY, 0);
        cal.set(java.util.Calendar.MINUTE, 0);
        cal.set(java.util.Calendar.SECOND, 0);
        cal.set(java.util.Calendar.MILLISECOND, 0);
        long todayTimestamp = cal.getTimeInMillis();

        HabitCompletion completion = new HabitCompletion(habit.getHabitId(), userId, todayTimestamp, "");

        habitService.completeHabit(completion, new HabitService.OnCompleteListener() {
            @Override
            public void onSuccess() {
                Toast.makeText(HabitTrackerActivity.this, 
                    "Great job! ✅ Keep up the streak!", 
                    Toast.LENGTH_SHORT).show();
                loadHabits();
            }

            @Override
            public void onError(String error) {
                Toast.makeText(HabitTrackerActivity.this, 
                    "Error: " + error, 
                    Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void viewHabitHistory(Habit habit) {
        habitService.getCompletions(habit.getHabitId(), new HabitService.OnCompletionsLoadedListener() {
            @Override
            public void onCompletionsLoaded(List<HabitCompletion> completions) {
                showHistoryDialog(habit, completions);
            }

            @Override
            public void onError(String error) {
                Toast.makeText(HabitTrackerActivity.this,
                    "Error loading history: " + error,
                    Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showHistoryDialog(Habit habit, List<HabitCompletion> completions) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(habit.getName() + " - History");

        StringBuilder message = new StringBuilder();
        message.append("Total completions: ").append(completions.size()).append("\n\n");
        message.append("Recent completions:\n");

        SimpleDateFormat displayFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
        int count = 0;
        for (HabitCompletion completion : completions) {
            if (count++ >= 10) break; // Show last 10
            long timestamp = completion.getCompletionDate();
            message.append("✓ ").append(displayFormat.format(new Date(timestamp))).append("\n");
        }

        if (completions.isEmpty()) {
            message.append("No completions yet. Start your streak today!");
        }

        builder.setMessage(message.toString());
        builder.setPositiveButton("OK", null);
        builder.show();
    }


    private void deleteHabit(Habit habit) {
        new AlertDialog.Builder(this)
            .setTitle("Delete Habit")
            .setMessage("Are you sure you want to delete \"" + habit.getName() + "\"? This cannot be undone.")
            .setPositiveButton("Delete", (dialog, which) -> {
                habitService.deleteHabit(habit.getHabitId(), new HabitService.OnCompleteListener() {
                    @Override
                    public void onSuccess() {
                        Toast.makeText(HabitTrackerActivity.this,
                            "Habit deleted",
                            Toast.LENGTH_SHORT).show();
                        loadHabits();
                    }

                    @Override
                    public void onError(String error) {
                        Toast.makeText(HabitTrackerActivity.this,
                            "Error deleting habit: " + error,
                            Toast.LENGTH_SHORT).show();
                    }
                });
            })
            .setNegativeButton("Cancel", null)
            .show();
    }
    
    private void updateStatistics() {
        totalHabitsText.setText(String.valueOf(habits.size()));
        
        int maxStreak = 0;
        for (Habit habit : habits) {
            if (habit.getLongestStreak() > maxStreak) {
                maxStreak = habit.getLongestStreak();
            }
        }
        
        longestStreakText.setText(maxStreak + " days");
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadHabits();
    }
}
