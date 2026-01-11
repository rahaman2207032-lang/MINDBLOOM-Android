package com.example.mindbloomandroid.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.mindbloomandroid.R;
import com.example.mindbloomandroid.adapter.MoodLogAdapter;
import com.example.mindbloomandroid.model.MoodLog;
import com.example.mindbloomandroid.service.MoodLogService;
import com.example.mindbloomandroid.utility.SharedPreferencesManager;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import java.util.ArrayList;
import java.util.List;


public class MoodTrackerActivity extends AppCompatActivity {

    private RecyclerView moodRecyclerView;
    private FloatingActionButton fabAddMood;
    private ProgressBar progressBar;
    private Toolbar toolbar;

    private MoodLogService moodLogService;
    private SharedPreferencesManager prefsManager;
    private MoodLogAdapter moodAdapter;
    private List<MoodLog> moodLogs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mood_tracker);

        initializeViews();
        setupToolbar();

        moodLogService = new MoodLogService();
        prefsManager = SharedPreferencesManager.getInstance(this);
        moodLogs = new ArrayList<>();

        setupRecyclerView();
        loadMoodLogs();

        fabAddMood.setOnClickListener(v -> showAddMoodDialog());
    }

    private void initializeViews() {
        toolbar = findViewById(R.id.toolbar);
        moodRecyclerView = findViewById(R.id.moodRecyclerView);
        fabAddMood = findViewById(R.id.fabAddMood);
        progressBar = findViewById(R.id.progressBar);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Mood Tracker");
        }
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void setupRecyclerView() {
        moodAdapter = new MoodLogAdapter(this, moodLogs, moodLog -> deleteMoodLog(moodLog));
        moodRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        moodRecyclerView.setAdapter(moodAdapter);
    }

    private void loadMoodLogs() {
        String userId = prefsManager.getUserId();
        if (userId == null) {
            android.util.Log.e("MoodTracker", "❌ User ID is null - cannot load mood logs");
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        android.util.Log.d("MoodTracker", "📖 Loading mood logs for user: " + userId);
        progressBar.setVisibility(View.VISIBLE);

        moodLogService.getUserMoodLogs(userId, new MoodLogService.OnMoodLogsLoadedListener() {
            @Override
            public void onMoodLogsLoaded(List<MoodLog> logs) {
                android.util.Log.d("MoodTracker", "✅ Mood logs loaded: " + logs.size() + " entries");

                progressBar.setVisibility(View.GONE);
                moodLogs.clear();
                moodLogs.addAll(logs);
                moodAdapter.notifyDataSetChanged();

                if (moodLogs.isEmpty()) {
                    android.util.Log.d("MoodTracker", "ℹ️ No mood logs found for user");
                    Toast.makeText(MoodTrackerActivity.this,
                        "No mood logs yet. Tap + to add your first mood!", 
                        Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(MoodTrackerActivity.this,
                        "Loaded " + moodLogs.size() + " mood entries",
                        Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onError(String error) {
                android.util.Log.e("MoodTracker", "❌ Error loading mood logs: " + error);
                progressBar.setVisibility(View.GONE);
                Toast.makeText(MoodTrackerActivity.this, 
                    "Error loading mood logs: " + error, 
                    Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showAddMoodDialog() {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this);
        View sheetView = getLayoutInflater().inflate(R.layout.bottom_sheet_add_mood, null);
        bottomSheetDialog.setContentView(sheetView);

        RadioGroup moodRadioGroup = sheetView.findViewById(R.id.moodRadioGroup);
        EditText notesEditText = sheetView.findViewById(R.id.notesEditText);
        EditText activitiesEditText = sheetView.findViewById(R.id.activitiesEditText);
        Button saveButton = sheetView.findViewById(R.id.saveButton);
        Button cancelButton = sheetView.findViewById(R.id.cancelButton);

        saveButton.setOnClickListener(v -> {
            int selectedId = moodRadioGroup.getCheckedRadioButtonId();
            if (selectedId == -1) {
                Toast.makeText(this, "Please select a mood", Toast.LENGTH_SHORT).show();
                return;
            }

            int moodRating = 0;
            if (selectedId == R.id.mood1) moodRating = 1;
            else if (selectedId == R.id.mood2) moodRating = 2;
            else if (selectedId == R.id.mood3) moodRating = 3;
            else if (selectedId == R.id.mood4) moodRating = 4;
            else if (selectedId == R.id.mood5) moodRating = 5;

            String notes = notesEditText.getText().toString().trim();
            String activities = activitiesEditText.getText().toString().trim();

            saveMoodLog(moodRating, notes, activities);
            bottomSheetDialog.dismiss();
        });

        cancelButton.setOnClickListener(v -> bottomSheetDialog.dismiss());

        bottomSheetDialog.show();
    }

    private void saveMoodLog(int moodRating, String notes, String activities) {
        String userId = prefsManager.getUserId();
        
        MoodLog moodLog = new MoodLog(userId, moodRating, notes, activities);
        
        progressBar.setVisibility(View.VISIBLE);
        moodLogService.saveMoodLog(moodLog, new MoodLogService.OnCompleteListener() {
            @Override
            public void onSuccess() {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(MoodTrackerActivity.this, 
                    "Mood logged successfully!", 
                    Toast.LENGTH_SHORT).show();
                loadMoodLogs();
            }

            @Override
            public void onError(String error) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(MoodTrackerActivity.this, 
                    "Error saving mood: " + error, 
                    Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void deleteMoodLog(MoodLog moodLog) {
        new android.app.AlertDialog.Builder(this)
            .setTitle("Delete Mood Log")
            .setMessage("Are you sure you want to delete this mood entry?")
            .setPositiveButton("Delete", (dialog, which) -> {
                moodLogService.deleteMoodLog(moodLog.getMoodLogId(), new MoodLogService.OnCompleteListener() {
                    @Override
                    public void onSuccess() {
                        Toast.makeText(MoodTrackerActivity.this, "Mood log deleted", Toast.LENGTH_SHORT).show();
                        loadMoodLogs();
                    }

                    @Override
                    public void onError(String error) {
                        Toast.makeText(MoodTrackerActivity.this, "Error deleting: " + error, Toast.LENGTH_SHORT).show();
                    }
                });
            })
            .setNegativeButton("Cancel", null)
            .show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadMoodLogs();
    }
}
