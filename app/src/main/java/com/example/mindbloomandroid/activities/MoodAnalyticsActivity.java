package com.example.mindbloomandroid.activities;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.mindbloomandroid.R;
import com.example.mindbloomandroid.adapter.MoodLogAdapter;
import com.example.mindbloomandroid.model.MoodLog;
import com.example.mindbloomandroid.service.MoodLogService;
import com.example.mindbloomandroid.utility.SharedPreferencesManager;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;


public class MoodAnalyticsActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private BarChart moodChart;
    private TextView averageMoodText;
    private TextView mostCommonMoodText;
    private TextView totalEntriesText;
    private RecyclerView moodHistoryRecyclerView;
    private FloatingActionButton fabAddMood;
    private ProgressBar progressBar;

    private MoodLogService moodLogService;
    private SharedPreferencesManager prefsManager;
    private MoodLogAdapter moodLogAdapter;
    private List<MoodLog> moodLogs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mood_analytics);

        initializeViews();
        setupToolbar();

        moodLogService = new MoodLogService();
        prefsManager = SharedPreferencesManager.getInstance(this);
        moodLogs = new ArrayList<>();

        setupChart();
        setupRecyclerView();
        loadMoodAnalytics();
        
        fabAddMood.setOnClickListener(v -> showAddMoodDialog());
    }
    
    private void setupRecyclerView() {
        moodLogAdapter = new MoodLogAdapter(this, moodLogs, this::deleteMoodLog);
        moodHistoryRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        moodHistoryRecyclerView.setAdapter(moodLogAdapter);
    }

    private void initializeViews() {
        toolbar = findViewById(R.id.toolbar);
        moodChart = findViewById(R.id.moodChart);
        averageMoodText = findViewById(R.id.averageMoodText);
        mostCommonMoodText = findViewById(R.id.mostCommonMoodText);
        totalEntriesText = findViewById(R.id.totalEntriesText);
        moodHistoryRecyclerView = findViewById(R.id.moodHistoryRecyclerView);
        fabAddMood = findViewById(R.id.fabAddMood);
        progressBar = findViewById(R.id.progressBar);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Mood Analytics");
        }
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void setupChart() {
        moodChart.getDescription().setEnabled(false);
        moodChart.setTouchEnabled(true);
        moodChart.setDragEnabled(true);
        moodChart.setScaleEnabled(true);
        moodChart.setPinchZoom(true);
        
        XAxis xAxis = moodChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
    }

    private void loadMoodAnalytics() {
        String userId = prefsManager.getUserId();
        if (userId == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        moodLogService.getUserMoodLogs(userId, new MoodLogService.OnMoodLogsLoadedListener() {
            @Override
            public void onMoodLogsLoaded(List<MoodLog> moodLogs) {
                progressBar.setVisibility(View.GONE);
                
                if (moodLogs.isEmpty()) {
                    Toast.makeText(MoodAnalyticsActivity.this, 
                        "No mood data to analyze yet", 
                        Toast.LENGTH_SHORT).show();
                    return;
                }

                MoodAnalyticsActivity.this.moodLogs.clear();
                MoodAnalyticsActivity.this.moodLogs.addAll(moodLogs);
                moodLogAdapter.notifyDataSetChanged();
                
                displayAnalytics(moodLogs);
                displayChart(moodLogs);
            }

            @Override
            public void onError(String error) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(MoodAnalyticsActivity.this, 
                    "Error loading mood data: " + error, 
                    Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void displayAnalytics(List<MoodLog> moodLogs) {
        int total = moodLogs.size();
        totalEntriesText.setText(String.valueOf(total));

        // Calculate average
        double sum = 0;
        int[] moodCounts = new int[6]; // 0-5
        for (MoodLog log : moodLogs) {
            sum += log.getMoodRating();
            moodCounts[log.getMoodRating()]++;
        }
        
        double average = sum / total;
        averageMoodText.setText(String.format(Locale.getDefault(), "%.1f/5", average));

        // Find most common mood
        int maxCount = 0;
        int mostCommonMood = 0;
        for (int i = 1; i <= 5; i++) {
            if (moodCounts[i] > maxCount) {
                maxCount = moodCounts[i];
                mostCommonMood = i;
            }
        }
        
        String[] moodEmojis = {"", "😢", "😟", "😐", "🙂", "😊"};
        mostCommonMoodText.setText(moodEmojis[mostCommonMood] + " " + mostCommonMood + "/5");
    }

    private void displayChart(List<MoodLog> moodLogs) {
        ArrayList<BarEntry> entries = new ArrayList<>();
        
        // Take last 7 entries for chart (matching desktop pattern)
        int startIndex = Math.max(0, moodLogs.size() - 7);
        for (int i = startIndex; i < moodLogs.size(); i++) {
            entries.add(new BarEntry(i - startIndex, moodLogs.get(i).getMoodRating()));
        }

        BarDataSet dataSet = new BarDataSet(entries, "Mood Rating (1-5)");
        dataSet.setColor(Color.parseColor("#7F9C96"));
        dataSet.setValueTextColor(Color.BLACK);
        dataSet.setValueTextSize(10f);

        BarData barData = new BarData(dataSet);
        barData.setBarWidth(0.5f);
        moodChart.setData(barData);
        moodChart.getAxisLeft().setAxisMinimum(0f);
        moodChart.getAxisLeft().setAxisMaximum(5f);
        moodChart.getAxisRight().setEnabled(false);
        moodChart.invalidate();
    }
    
    private void showAddMoodDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_mood, null);
        
        RadioGroup moodRadioGroup = dialogView.findViewById(R.id.moodRadioGroup);
        RadioButton mood1Radio = dialogView.findViewById(R.id.mood1Radio);
        RadioButton mood2Radio = dialogView.findViewById(R.id.mood2Radio);
        RadioButton mood3Radio = dialogView.findViewById(R.id.mood3Radio);
        RadioButton mood4Radio = dialogView.findViewById(R.id.mood4Radio);
        RadioButton mood5Radio = dialogView.findViewById(R.id.mood5Radio);
        EditText activitiesEditText = dialogView.findViewById(R.id.activitiesEditText);
        EditText notesEditText = dialogView.findViewById(R.id.notesEditText);
        
        // Set default selection to neutral (3)
        mood3Radio.setChecked(true);
        
        AlertDialog dialog = new AlertDialog.Builder(this)
            .setTitle("Log Your Mood")
            .setView(dialogView)
            .setPositiveButton("Save", (d, which) -> {
                int selectedId = moodRadioGroup.getCheckedRadioButtonId();
                int moodRating = 3; // default
                
                if (selectedId == R.id.mood1Radio) moodRating = 1;
                else if (selectedId == R.id.mood2Radio) moodRating = 2;
                else if (selectedId == R.id.mood3Radio) moodRating = 3;
                else if (selectedId == R.id.mood4Radio) moodRating = 4;
                else if (selectedId == R.id.mood5Radio) moodRating = 5;
                
                String activities = activitiesEditText.getText().toString().trim();
                String notes = notesEditText.getText().toString().trim();
                
                saveMoodLog(moodRating, activities, notes);
            })
            .setNegativeButton("Cancel", null)
            .create();
        
        dialog.show();
    }
    
    private void saveMoodLog(int moodRating, String activities, String notes) {
        String userId = prefsManager.getUserId();
        
        MoodLog moodLog = new MoodLog(userId, moodRating, notes, activities);
        
        progressBar.setVisibility(View.VISIBLE);
        moodLogService.saveMoodLog(moodLog, new MoodLogService.OnCompleteListener() {
            @Override
            public void onSuccess() {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(MoodAnalyticsActivity.this, 
                    "Mood logged successfully!", 
                    Toast.LENGTH_SHORT).show();
                loadMoodAnalytics();
            }

            @Override
            public void onError(String error) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(MoodAnalyticsActivity.this, 
                    "Error saving mood: " + error, 
                    Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    private void deleteMoodLog(MoodLog moodLog) {
        new AlertDialog.Builder(this)
            .setTitle("Delete Mood Log")
            .setMessage("Are you sure you want to delete this mood log?")
            .setPositiveButton("Delete", (dialog, which) -> {
                progressBar.setVisibility(View.VISIBLE);
                moodLogService.deleteMoodLog(moodLog.getMoodLogId(), 
                    new MoodLogService.OnCompleteListener() {
                        @Override
                        public void onSuccess() {
                            progressBar.setVisibility(View.GONE);
                            Toast.makeText(MoodAnalyticsActivity.this, 
                                "Mood log deleted", 
                                Toast.LENGTH_SHORT).show();
                            loadMoodAnalytics();
                        }

                        @Override
                        public void onError(String error) {
                            progressBar.setVisibility(View.GONE);
                            Toast.makeText(MoodAnalyticsActivity.this, 
                                "Error deleting: " + error, 
                                Toast.LENGTH_SHORT).show();
                        }
                    });
            })
            .setNegativeButton("Cancel", null)
            .show();
    }
}
