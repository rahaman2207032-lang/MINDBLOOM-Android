package com.example.mindbloomandroid.activities;

import android.os.Bundle;
import android.view.MenuItem;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.mindbloomandroid.R;
import com.example.mindbloomandroid.adapter.MoodLogAdapter;
import com.example.mindbloomandroid.adapter.SleepEntryAdapter;
import com.example.mindbloomandroid.model.MoodLog;
import com.example.mindbloomandroid.model.SleepEntry;
import com.example.mindbloomandroid.model.StressAssessment;
import com.example.mindbloomandroid.service.MoodLogService;
import com.example.mindbloomandroid.service.SleepTrackerService;
import com.example.mindbloomandroid.service.StressService;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;


public class ClientProgressActivity extends AppCompatActivity {

    private TextView clientNameText;
    private TextView avgMoodValue;
    private TextView avgSleepValue;
    private TextView stressLevelValue;
    private TextView totalSessionsValue;

    private RecyclerView moodLogRecyclerView;
    private RecyclerView sleepLogRecyclerView;
    private ProgressBar progressBar;

    private MoodLogAdapter moodAdapter;
    private SleepEntryAdapter sleepAdapter;

    private String clientId;
    private String clientName;

    private MoodLogService moodLogService;
    private SleepTrackerService sleepService;
    private StressService stressService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_client_progress);

        // Get client info from intent
        clientId = getIntent().getStringExtra("CLIENT_ID");
        clientName = getIntent().getStringExtra("CLIENT_NAME");

        if (clientId == null || clientName == null) {
            Toast.makeText(this, "Invalid client data", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initializeViews();
        setupToolbar();
        setupRecyclerViews();

        moodLogService = new MoodLogService();
        sleepService = new SleepTrackerService();
        stressService = new StressService();

        loadClientProgress();
    }

    private void initializeViews() {
        clientNameText = findViewById(R.id.clientNameText);
        avgMoodValue = findViewById(R.id.avgMoodValue);
        avgSleepValue = findViewById(R.id.avgSleepValue);
        stressLevelValue = findViewById(R.id.stressLevelValue);
        totalSessionsValue = findViewById(R.id.totalSessionsValue);
        moodLogRecyclerView = findViewById(R.id.moodLogRecyclerView);
        sleepLogRecyclerView = findViewById(R.id.sleepLogRecyclerView);
        progressBar = findViewById(R.id.progressBar);

        clientNameText.setText(clientName + "'s Progress");
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Client Progress");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    private void setupRecyclerViews() {

        List<MoodLog> emptyMoodList = new ArrayList<>();
        moodAdapter = new MoodLogAdapter(this, emptyMoodList, null);
        moodLogRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        moodLogRecyclerView.setAdapter(moodAdapter);


        List<SleepEntry> emptySleepList = new ArrayList<>();
        sleepAdapter = new SleepEntryAdapter(this, emptySleepList);
        sleepLogRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        sleepLogRecyclerView.setAdapter(sleepAdapter);
    }

    private void loadClientProgress() {
        android.util.Log.d("ClientProgress", "📊 Loading progress for client: " + clientName + " (ID: " + clientId + ")");
        progressBar.setVisibility(android.view.View.VISIBLE);


        loadMoodLogs();


        loadSleepEntries();


        loadStressLevel();
    }

    private void loadMoodLogs() {
        moodLogService.getUserMoodLogs(clientId, new MoodLogService.OnMoodLogsLoadedListener() {
            @Override
            public void onMoodLogsLoaded(List<MoodLog> moodLogs) {
                android.util.Log.d("ClientProgress", "✅ Loaded " + moodLogs.size() + " mood logs");


                moodAdapter = new MoodLogAdapter(ClientProgressActivity.this, moodLogs, null);
                moodLogRecyclerView.setAdapter(moodAdapter);


                if (!moodLogs.isEmpty()) {
                    double sum = 0;
                    for (MoodLog log : moodLogs) {
                        sum += log.getMoodRating();
                    }
                    double avg = sum / moodLogs.size();
                    avgMoodValue.setText(String.format(Locale.getDefault(), "%.1f/5", avg));
                } else {
                    avgMoodValue.setText("N/A");
                }
            }

            @Override
            public void onError(String error) {
                android.util.Log.e("ClientProgress", "❌ Error loading mood logs: " + error);
                avgMoodValue.setText("N/A");
            }
        });
    }

    private void loadSleepEntries() {
        sleepService.getUserSleepEntries(clientId, new SleepTrackerService.OnSleepEntriesLoadedListener() {
            @Override
            public void onSleepEntriesLoaded(List<SleepEntry> sleepEntries) {
                android.util.Log.d("ClientProgress", "✅ Loaded " + sleepEntries.size() + " sleep entries");


                sleepAdapter = new SleepEntryAdapter(ClientProgressActivity.this, sleepEntries);
                sleepLogRecyclerView.setAdapter(sleepAdapter);


                if (!sleepEntries.isEmpty()) {
                    double sum = 0;
                    for (SleepEntry entry : sleepEntries) {
                        // Calculate hours from start/end time
                        long duration = entry.getSleepEndTime() - entry.getSleepStartTime();
                        double hours = duration / (1000.0 * 60 * 60);
                        sum += hours;
                    }
                    double avg = sum / sleepEntries.size();
                    avgSleepValue.setText(String.format(Locale.getDefault(), "%.1f hrs", avg));
                } else {
                    avgSleepValue.setText("N/A");
                }

                progressBar.setVisibility(android.view.View.GONE);
            }

            @Override
            public void onError(String error) {
                android.util.Log.e("ClientProgress", " Error loading sleep entries: " + error);
                avgSleepValue.setText("N/A");
                progressBar.setVisibility(android.view.View.GONE);
            }
        });
    }

    private void loadStressLevel() {
        stressService.getUserStressAssessments(clientId, new StressService.OnStressAssessmentsLoadedListener() {
            @Override
            public void onAssessmentsLoaded(List<StressAssessment> assessments) {
                android.util.Log.d("ClientProgress", " Loaded " + assessments.size() + " stress assessments");

                if (!assessments.isEmpty()) {
                    // Get most recent assessment
                    StressAssessment latest = assessments.get(0);
                    for (StressAssessment assessment : assessments) {
                        if (assessment.getAssessmentDate() > latest.getAssessmentDate()) {
                            latest = assessment;
                        }
                    }
                    stressLevelValue.setText(latest.getStressLevel());
                } else {
                    stressLevelValue.setText("N/A");
                }
            }

            @Override
            public void onError(String error) {
                android.util.Log.e("ClientProgress", " Error loading stress assessments: " + error);
                stressLevelValue.setText("N/A");
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}

