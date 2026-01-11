package com.example.mindbloomandroid.activities;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.ArrayAdapter;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.mindbloomandroid.R;
import com.example.mindbloomandroid.adapter.SleepEntryAdapter;
import com.example.mindbloomandroid.model.SleepEntry;
import com.example.mindbloomandroid.service.SleepTrackerService;
import com.example.mindbloomandroid.utility.SharedPreferencesManager;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;


public class SleepTrackerActivity extends AppCompatActivity {

    private RecyclerView sleepRecyclerView;
    private FloatingActionButton fabAddSleep;
    private ProgressBar progressBar;
    private Toolbar toolbar;
    private TextView avgSleepLabel;
    private TextView totalEntriesLabel;

    private SleepTrackerService sleepService;
    private SharedPreferencesManager prefsManager;
    private SleepEntryAdapter sleepAdapter;
    private List<SleepEntry> sleepEntries;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sleep_tracker);

        initializeViews();
        setupToolbar();

        sleepService = new SleepTrackerService();
        prefsManager = SharedPreferencesManager.getInstance(this);
        sleepEntries = new ArrayList<>();

        setupRecyclerView();
        loadSleepEntries();

        fabAddSleep.setOnClickListener(v -> showAddSleepDialog());
    }

    private void initializeViews() {
        toolbar = findViewById(R.id.toolbar);
        sleepRecyclerView = findViewById(R.id.sleepRecyclerView);
        fabAddSleep = findViewById(R.id.fabAddSleep);
        progressBar = findViewById(R.id.progressBar);
        avgSleepLabel = findViewById(R.id.avgSleepLabel);
        totalEntriesLabel = findViewById(R.id.totalEntriesLabel);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Sleep Tracker");
        }
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void setupRecyclerView() {
        sleepAdapter = new SleepEntryAdapter(this, sleepEntries);
        sleepRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        sleepRecyclerView.setAdapter(sleepAdapter);
    }

    private void loadSleepEntries() {
        String userId = prefsManager.getUserId();
        if (userId == null) {
            android.util.Log.e("SleepTracker", "❌ User ID is null - cannot load sleep entries");
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        android.util.Log.d("SleepTracker", "📖 Loading sleep entries for user: " + userId);
        progressBar.setVisibility(View.VISIBLE);

        sleepService.getUserSleepEntries(userId, new SleepTrackerService.OnSleepEntriesLoadedListener() {
            @Override
            public void onSleepEntriesLoaded(List<SleepEntry> entries) {
                android.util.Log.d("SleepTracker", "✅ Sleep entries loaded: " + entries.size() + " entries");

                progressBar.setVisibility(View.GONE);
                sleepEntries.clear();
                sleepEntries.addAll(entries);
                sleepAdapter.notifyDataSetChanged();
                updateStatistics();

                if (sleepEntries.isEmpty()) {
                    android.util.Log.d("SleepTracker", "ℹ️ No sleep entries found for user");
                    Toast.makeText(SleepTrackerActivity.this,
                        "No sleep entries yet. Tap + to log your sleep!", 
                        Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(SleepTrackerActivity.this,
                        "Loaded " + sleepEntries.size() + " sleep entries",
                        Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onError(String error) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(SleepTrackerActivity.this, 
                    "Error loading sleep entries: " + error, 
                    Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showAddSleepDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Log Sleep");
        
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_sleep, null);
        builder.setView(dialogView);

        Button dateButton = dialogView.findViewById(R.id.selectDateButton);
        EditText startHourField = dialogView.findViewById(R.id.sleepStartHour);
        EditText startMinuteField = dialogView.findViewById(R.id.sleepStartMinute);
        EditText endHourField = dialogView.findViewById(R.id.sleepEndHour);
        EditText endMinuteField = dialogView.findViewById(R.id.sleepEndMinute);
        Spinner qualitySpinner = dialogView.findViewById(R.id.qualitySpinner);
        EditText notesField = dialogView.findViewById(R.id.sleepNotes);


        ArrayAdapter<Integer> adapter = new ArrayAdapter<>(this,
            android.R.layout.simple_spinner_item,
            new Integer[]{1, 2, 3, 4, 5});
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        qualitySpinner.setAdapter(adapter);
        qualitySpinner.setSelection(2); // Default to 3


        Calendar today = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
        dateButton.setText(sdf.format(today.getTime()));

        final Calendar[] selectedDate = {today};

        startHourField.setHint("e.g., 22");
        startMinuteField.setHint("e.g., 00");
        endHourField.setHint("e.g., 07");
        endMinuteField.setHint("e.g., 00");


        dateButton.setOnClickListener(v -> {
            Calendar c = Calendar.getInstance();
            DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                (view, year, month, dayOfMonth) -> {
                    selectedDate[0] = Calendar.getInstance();
                    selectedDate[0].set(year, month, dayOfMonth);
                    dateButton.setText(sdf.format(selectedDate[0].getTime()));
                },
                c.get(Calendar.YEAR),
                c.get(Calendar.MONTH),
                c.get(Calendar.DAY_OF_MONTH));
            datePickerDialog.show();
        });

        builder.setPositiveButton("Save", (dialog, which) -> {
            try {

                String startHourStr = startHourField.getText().toString().trim();
                String startMinuteStr = startMinuteField.getText().toString().trim();
                String endHourStr = endHourField.getText().toString().trim();
                String endMinuteStr = endMinuteField.getText().toString().trim();

                if (startHourStr.isEmpty() || startMinuteStr.isEmpty() ||
                    endHourStr.isEmpty() || endMinuteStr.isEmpty()) {
                    Toast.makeText(this, "Please fill in all time fields", Toast.LENGTH_SHORT).show();
                    return;
                }

                int startHour = Integer.parseInt(startHourStr);
                int startMinute = Integer.parseInt(startMinuteStr);
                int endHour = Integer.parseInt(endHourStr);
                int endMinute = Integer.parseInt(endMinuteStr);
                int quality = (Integer) qualitySpinner.getSelectedItem();
                String notes = notesField.getText().toString().trim();


                if (startHour < 0 || startHour > 23 || endHour < 0 || endHour > 23) {
                    Toast.makeText(this, "Hour must be between 0-23", Toast.LENGTH_SHORT).show();
                    return;
                }


                if (startMinute < 0 || startMinute > 59 || endMinute < 0 || endMinute > 59) {
                    Toast.makeText(this, "Minute must be between 0-59", Toast.LENGTH_SHORT).show();
                    return;
                }


                Calendar bedtime = (Calendar) selectedDate[0].clone();
                bedtime.set(Calendar.HOUR_OF_DAY, startHour);
                bedtime.set(Calendar.MINUTE, startMinute);
                bedtime.set(Calendar.SECOND, 0);
                bedtime.set(Calendar.MILLISECOND, 0);


                Calendar wakeTime = (Calendar) selectedDate[0].clone();
                wakeTime.set(Calendar.HOUR_OF_DAY, endHour);
                wakeTime.set(Calendar.MINUTE, endMinute);
                wakeTime.set(Calendar.SECOND, 0);
                wakeTime.set(Calendar.MILLISECOND, 0);


                if (wakeTime.getTimeInMillis() <= bedtime.getTimeInMillis()) {
                    wakeTime.add(Calendar.DAY_OF_MONTH, 1);
                }


                long durationMs = wakeTime.getTimeInMillis() - bedtime.getTimeInMillis();
                double durationHours = durationMs / (1000.0 * 60 * 60);


                if (durationHours < 1 || durationHours > 24) {
                    Toast.makeText(this,
                        String.format(Locale.getDefault(), "Sleep duration (%.1f hours) seems unusual. Please check your times.", durationHours),
                        Toast.LENGTH_LONG).show();
                    return;
                }

                saveSleepEntry(bedtime.getTimeInMillis(), wakeTime.getTimeInMillis(), quality, notes);
                
            } catch (NumberFormatException e) {
                Toast.makeText(this, "Please enter valid numbers for time fields", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Cancel", null);
        builder.create().show();
    }


    private void saveSleepEntry(long bedtime, long wakeTime, int quality, String notes) {
        String userId = prefsManager.getUserId();
        
        SleepEntry entry = new SleepEntry(userId, bedtime, wakeTime, quality, notes);
        
        progressBar.setVisibility(View.VISIBLE);
        sleepService.saveSleepEntry(entry, new SleepTrackerService.OnCompleteListener() {
            @Override
            public void onSuccess() {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(SleepTrackerActivity.this, 
                    "Sleep logged successfully! 😴", 
                    Toast.LENGTH_SHORT).show();
                loadSleepEntries();
            }

            @Override
            public void onError(String error) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(SleepTrackerActivity.this, 
                    "Error saving sleep entry: " + error, 
                    Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void deleteSleepEntry(SleepEntry entry) {
        new AlertDialog.Builder(this)
            .setTitle("Delete Sleep Entry")
            .setMessage("Are you sure you want to delete this sleep entry?")
            .setPositiveButton("Delete", (dialog, which) -> {
                sleepService.deleteSleepEntry(entry.getSleepEntryId(), new SleepTrackerService.OnCompleteListener() {
                    @Override
                    public void onSuccess() {
                        Toast.makeText(SleepTrackerActivity.this,
                            "Sleep entry deleted",
                            Toast.LENGTH_SHORT).show();
                        loadSleepEntries();
                    }

                    @Override
                    public void onError(String error) {
                        Toast.makeText(SleepTrackerActivity.this,
                            "Error deleting entry: " + error,
                            Toast.LENGTH_SHORT).show();
                    }
                });
            })
            .setNegativeButton("Cancel", null)
            .show();
    }


    private void updateStatistics() {
        if (sleepEntries.isEmpty()) {
            if (avgSleepLabel != null) avgSleepLabel.setText("0.0 hours");
            if (totalEntriesLabel != null) totalEntriesLabel.setText("0");
            return;
        }

        double totalHours = 0;
        for (SleepEntry entry : sleepEntries) {
            long duration = entry.getSleepEndTime() - entry.getSleepStartTime();
            totalHours += duration / (1000.0 * 60 * 60); // Convert to hours
        }

        double avgHours = totalHours / sleepEntries.size();
        if (avgSleepLabel != null) {
            avgSleepLabel.setText(String.format(Locale.getDefault(), "%.1f hours", avgHours));
        }
        if (totalEntriesLabel != null) {
            totalEntriesLabel.setText(String.valueOf(sleepEntries.size()));
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadSleepEntries();
    }
}
