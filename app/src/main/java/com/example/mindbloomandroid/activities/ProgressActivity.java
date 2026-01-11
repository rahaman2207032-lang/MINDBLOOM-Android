package com.example.mindbloomandroid.activities;

import android.app.DatePickerDialog;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.example.mindbloomandroid.R;
import com.example.mindbloomandroid.model.ProgressData;
import com.example.mindbloomandroid.service.ProgressService;
import com.example.mindbloomandroid.utility.SharedPreferencesManager;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;


public class ProgressActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private Button startDateButton;
    private Button endDateButton;
    private Button loadButton;
    private TextView averageMoodLabel;
    private TextView moodTrendLabel;
    private TextView averageStressLabel;
    private TextView stressTrendLabel;
    private TextView habitCompletionLabel;
    private TextView averageSleepLabel;
    private LineChart progressChart;
    private TextView milestonesText;
    private TextView correlationsText;
    private ProgressBar progressBar;

    private ProgressService progressService;
    private SharedPreferencesManager prefsManager;
    private Calendar startDate;
    private Calendar endDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_progress);

        initializeViews();
        setupToolbar();
        
        progressService = new ProgressService();
        prefsManager = SharedPreferencesManager.getInstance(this);
        

        endDate = Calendar.getInstance();
        startDate = Calendar.getInstance();
        startDate.add(Calendar.DAY_OF_MONTH, -30);
        
        updateDateButtons();
        setupChart();
        loadProgressData();
        
        startDateButton.setOnClickListener(v -> showStartDatePicker());
        endDateButton.setOnClickListener(v -> showEndDatePicker());
        loadButton.setOnClickListener(v -> loadProgressData());
    }

    private void initializeViews() {
        toolbar = findViewById(R.id.toolbar);
        startDateButton = findViewById(R.id.startDateButton);
        endDateButton = findViewById(R.id.endDateButton);
        loadButton = findViewById(R.id.loadButton);
        averageMoodLabel = findViewById(R.id.averageMoodLabel);
        moodTrendLabel = findViewById(R.id.moodTrendLabel);
        averageStressLabel = findViewById(R.id.averageStressLabel);
        stressTrendLabel = findViewById(R.id.stressTrendLabel);
        habitCompletionLabel = findViewById(R.id.habitCompletionLabel);
        averageSleepLabel = findViewById(R.id.averageSleepLabel);
        progressChart = findViewById(R.id.progressChart);
        milestonesText = findViewById(R.id.milestonesText);
        correlationsText = findViewById(R.id.correlationsText);
        progressBar = findViewById(R.id.progressBar);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Progress Dashboard");
        }
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void setupChart() {
        progressChart.getDescription().setEnabled(false);
        progressChart.setTouchEnabled(true);
        progressChart.setDragEnabled(true);
        progressChart.setScaleEnabled(true);
        progressChart.setPinchZoom(true);
        progressChart.setDrawGridBackground(false);
        
        XAxis xAxis = progressChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        
        progressChart.getAxisRight().setEnabled(false);
        progressChart.getAxisLeft().setAxisMinimum(0f);
        progressChart.getAxisLeft().setAxisMaximum(5f);
    }
    
    private void showStartDatePicker() {
        DatePickerDialog picker = new DatePickerDialog(this,
            (view, year, month, dayOfMonth) -> {
                startDate.set(year, month, dayOfMonth);
                updateDateButtons();
            },
            startDate.get(Calendar.YEAR),
            startDate.get(Calendar.MONTH),
            startDate.get(Calendar.DAY_OF_MONTH)
        );
        picker.show();
    }
    
    private void showEndDatePicker() {
        DatePickerDialog picker = new DatePickerDialog(this,
            (view, year, month, dayOfMonth) -> {
                endDate.set(year, month, dayOfMonth);
                updateDateButtons();
            },
            endDate.get(Calendar.YEAR),
            endDate.get(Calendar.MONTH),
            endDate.get(Calendar.DAY_OF_MONTH)
        );
        picker.show();
    }
    
    private void updateDateButtons() {
        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
        startDateButton.setText(sdf.format(startDate.getTime()));
        endDateButton.setText(sdf.format(endDate.getTime()));
    }

    private void loadProgressData() {
        String userId = prefsManager.getUserId();
        if (userId == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        progressService.calculateProgressDataForDateRange(userId, startDate.getTimeInMillis(), 
                endDate.getTimeInMillis(), new ProgressService.OnProgressCalculatedListener() {
            @Override
            public void onProgressCalculated(ProgressData data) {
                progressBar.setVisibility(View.GONE);
                displayProgressData(data);
            }

            @Override
            public void onError(String error) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(ProgressActivity.this, 
                    "Error loading progress: " + error, 
                    Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void displayProgressData(ProgressData data) {

        averageMoodLabel.setText(String.format(Locale.getDefault(), "%.1f", data.getAverageMoodRating()));
        moodTrendLabel.setText(data.getMoodTrend());
        setTrendColor(moodTrendLabel, data.getMoodTrend());
        

        averageStressLabel.setText(String.format(Locale.getDefault(), "%.1f", data.getAverageStressScore()));
        stressTrendLabel.setText(data.getStressTrend());
        setTrendColor(stressTrendLabel, data.getStressTrend());
        

        habitCompletionLabel.setText(String.format(Locale.getDefault(), "%.0f%%", data.getHabitCompletionRate()));
        

        averageSleepLabel.setText(String.format(Locale.getDefault(), "%.1f hrs", data.getAverageSleepHours()));
        

        if (data.getAchievedMilestones() != null && !data.getAchievedMilestones().isEmpty()) {
            StringBuilder milestones = new StringBuilder();
            for (String milestone : data.getAchievedMilestones()) {
                milestones.append("✓ ").append(milestone).append("\n");
            }
            milestonesText.setText(milestones.toString());
        } else {
            milestonesText.setText("Keep tracking your wellness to unlock milestones!");
        }
        

        String correlations = "";
        if (data.getSleepMoodCorrelation() != null) {
            correlations += "Sleep & Mood: " + data.getSleepMoodCorrelation() + "\n\n";
        }
        correlations += "Continue tracking to discover more insights about your wellness patterns.";
        correlationsText.setText(correlations);
        

        ArrayList<Entry> moodEntries = new ArrayList<>();
        ArrayList<Entry> stressEntries = new ArrayList<>();
        

        for (int i = 0; i < 7; i++) {
            moodEntries.add(new Entry(i, (float)(data.getAverageMoodRating() + (Math.random() - 0.5))));
            stressEntries.add(new Entry(i, (float)(data.getAverageStressScore() + (Math.random() - 0.5))));
        }
        
        LineDataSet moodDataSet = new LineDataSet(moodEntries, "Mood");
        moodDataSet.setColor(Color.parseColor("#87b5d8"));
        moodDataSet.setCircleColor(Color.parseColor("#87b5d8"));
        moodDataSet.setLineWidth(2f);
        moodDataSet.setCircleRadius(4f);
        moodDataSet.setDrawFilled(true);
        moodDataSet.setFillColor(Color.parseColor("#87b5d8"));
        moodDataSet.setFillAlpha(50);
        
        LineDataSet stressDataSet = new LineDataSet(stressEntries, "Stress");
        stressDataSet.setColor(Color.parseColor("#FF9800"));
        stressDataSet.setCircleColor(Color.parseColor("#FF9800"));
        stressDataSet.setLineWidth(2f);
        stressDataSet.setCircleRadius(4f);
        stressDataSet.setDrawFilled(true);
        stressDataSet.setFillColor(Color.parseColor("#FF9800"));
        stressDataSet.setFillAlpha(50);
        
        LineData lineData = new LineData(moodDataSet, stressDataSet);
        progressChart.setData(lineData);
        progressChart.invalidate();
    }
    
    private void setTrendColor(TextView textView, String trend) {
        if (trend == null) {
            textView.setTextColor(Color.parseColor("#999999"));
            return;
        }
        
        if (trend.contains("Improving") || trend.contains("Stable")) {
            textView.setTextColor(Color.parseColor("#4CAF50")); // Green
        } else if (trend.contains("Declining")) {
            textView.setTextColor(Color.parseColor("#F44336")); // Red
        } else {
            textView.setTextColor(Color.parseColor("#999999")); // Gray
        }
    }
}
