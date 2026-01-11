package com.example.mindbloomandroid.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.mindbloomandroid.R;
import com.example.mindbloomandroid.adapter.StressAssessmentAdapter;
import com.example.mindbloomandroid.model.StressAssessment;
import com.example.mindbloomandroid.service.StressService;
import com.example.mindbloomandroid.utility.SharedPreferencesManager;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.util.ArrayList;
import java.util.List;


public class StressMonitorActivity extends AppCompatActivity {

    private RecyclerView stressRecyclerView;
    private FloatingActionButton fabAddStress;
    private ProgressBar progressBar;
    private Toolbar toolbar;

    private StressService stressService;
    private SharedPreferencesManager prefsManager;
    private StressAssessmentAdapter stressAdapter;
    private List<StressAssessment> stressAssessments;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stress_monitor);

        initializeViews();
        setupToolbar();

        stressService = new StressService();
        prefsManager = SharedPreferencesManager.getInstance(this);
        stressAssessments = new ArrayList<>();

        setupRecyclerView();
        loadStressAssessments();

        fabAddStress.setOnClickListener(v -> showAddStressDialog());
    }

    private void initializeViews() {
        toolbar = findViewById(R.id.toolbar);
        stressRecyclerView = findViewById(R.id.stressRecyclerView);
        fabAddStress = findViewById(R.id.fabAddStress);
        progressBar = findViewById(R.id.progressBar);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Stress Monitor");
        }
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void setupRecyclerView() {
        stressAdapter = new StressAssessmentAdapter(this, stressAssessments, this::deleteAssessment);
        stressRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        stressRecyclerView.setAdapter(stressAdapter);
    }

    private void loadStressAssessments() {
        String userId = prefsManager.getUserId();
        if (userId == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        stressService.getUserStressAssessments(userId, new StressService.OnStressAssessmentsLoadedListener() {
            @Override
            public void onAssessmentsLoaded(List<StressAssessment> assessments) {
                progressBar.setVisibility(View.GONE);
                stressAssessments.clear();
                stressAssessments.addAll(assessments);
                stressAdapter.notifyDataSetChanged();

                if (stressAssessments.isEmpty()) {
                    Toast.makeText(StressMonitorActivity.this, 
                        "No stress logs yet. Tap + to track your stress!", 
                        Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onError(String error) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(StressMonitorActivity.this, 
                    "Error loading stress assessments: " + error, 
                    Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showAddStressDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_stress, null);
        

        SeekBar workloadSeekBar = dialogView.findViewById(R.id.workloadSeekBar);
        SeekBar sleepQualitySeekBar = dialogView.findViewById(R.id.sleepQualitySeekBar);
        SeekBar anxietySeekBar = dialogView.findViewById(R.id.anxietySeekBar);
        SeekBar moodSeekBar = dialogView.findViewById(R.id.moodSeekBar);
        SeekBar physicalSymptomsSeekBar = dialogView.findViewById(R.id.physicalSymptomsSeekBar);
        SeekBar concentrationSeekBar = dialogView.findViewById(R.id.concentrationSeekBar);
        SeekBar socialConnectionSeekBar = dialogView.findViewById(R.id.socialConnectionSeekBar);
        
        TextView workloadValue = dialogView.findViewById(R.id.workloadValue);
        TextView sleepQualityValue = dialogView.findViewById(R.id.sleepQualityValue);
        TextView anxietyValue = dialogView.findViewById(R.id.anxietyValue);
        TextView moodValue = dialogView.findViewById(R.id.moodValue);
        TextView physicalSymptomsValue = dialogView.findViewById(R.id.physicalSymptomsValue);
        TextView concentrationValue = dialogView.findViewById(R.id.concentrationValue);
        TextView socialConnectionValue = dialogView.findViewById(R.id.socialConnectionValue);
        
        TextView stressScoreLabel = dialogView.findViewById(R.id.stressScoreLabel);
        TextView stressLevelLabel = dialogView.findViewById(R.id.stressLevelLabel);
        TextView copingSuggestionsText = dialogView.findViewById(R.id.copingSuggestionsText);
        EditText notesEditText = dialogView.findViewById(R.id.notesEditText);
        

        workloadValue.setText("3");
        sleepQualityValue.setText("3");
        anxietyValue.setText("3");
        moodValue.setText("3");
        physicalSymptomsValue.setText("3");
        concentrationValue.setText("3");
        socialConnectionValue.setText("3");
        

        Runnable updateScore = () -> {
            int score = workloadSeekBar.getProgress() + sleepQualitySeekBar.getProgress() +
                       anxietySeekBar.getProgress() + moodSeekBar.getProgress() +
                       physicalSymptomsSeekBar.getProgress() + concentrationSeekBar.getProgress() +
                       socialConnectionSeekBar.getProgress();
            
            stressScoreLabel.setText("Stress Score: " + score + "/35");
            
            String level;
            if (score <= 14) {
                level = "LOW";
                stressLevelLabel.setText("Stress Level: Low");
                stressLevelLabel.setTextColor(0xFF4CAF50); // Green
            } else if (score <= 24) {
                level = "MODERATE";
                stressLevelLabel.setText("Stress Level: Moderate");
                stressLevelLabel.setTextColor(0xFFFFC107); // Orange
            } else {
                level = "HIGH";
                stressLevelLabel.setText("Stress Level: High");
                stressLevelLabel.setTextColor(0xFFF44336); // Red
            }
            
            copingSuggestionsText.setText(stressService.getCopingSuggestion(level));
        };
        

        workloadSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                workloadValue.setText(String.valueOf(progress));
                updateScore.run();
            }
            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });
        
        sleepQualitySeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                sleepQualityValue.setText(String.valueOf(progress));
                updateScore.run();
            }
            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });
        
        anxietySeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                anxietyValue.setText(String.valueOf(progress));
                updateScore.run();
            }
            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });
        
        moodSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                moodValue.setText(String.valueOf(progress));
                updateScore.run();
            }
            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });
        
        physicalSymptomsSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                physicalSymptomsValue.setText(String.valueOf(progress));
                updateScore.run();
            }
            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });
        
        concentrationSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                concentrationValue.setText(String.valueOf(progress));
                updateScore.run();
            }
            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });
        
        socialConnectionSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                socialConnectionValue.setText(String.valueOf(progress));
                updateScore.run();
            }
            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });
        

        updateScore.run();
        
        AlertDialog dialog = new AlertDialog.Builder(this)
            .setTitle("Stress Assessment")
            .setView(dialogView)
            .setPositiveButton("Submit", (d, which) -> {
                String notes = notesEditText.getText().toString().trim();
                
                saveStressAssessment(
                    workloadSeekBar.getProgress(),
                    sleepQualitySeekBar.getProgress(),
                    anxietySeekBar.getProgress(),
                    moodSeekBar.getProgress(),
                    physicalSymptomsSeekBar.getProgress(),
                    concentrationSeekBar.getProgress(),
                    socialConnectionSeekBar.getProgress(),
                    notes
                );
            })
            .setNegativeButton("Cancel", null)
            .create();
        
        dialog.show();
    }

    private void saveStressAssessment(int workloadLevel, int sleepQualityLevel, 
                                       int anxietyLevel, int moodLevel,
                                       int physicalSymptomsLevel, int concentrationLevel,
                                       int socialConnectionLevel, String notes) {
        String userId = prefsManager.getUserId();
        
        StressAssessment assessment = new StressAssessment(
            userId, workloadLevel, sleepQualityLevel, anxietyLevel, 
            moodLevel, physicalSymptomsLevel, concentrationLevel, socialConnectionLevel
        );
        assessment.setNotes(notes);
        
        progressBar.setVisibility(View.VISIBLE);
        stressService.saveStressAssessment(assessment, new StressService.OnCompleteListener() {
            @Override
            public void onSuccess() {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(StressMonitorActivity.this, 
                    "Stress assessment saved!", 
                    Toast.LENGTH_SHORT).show();
                loadStressAssessments();
            }

            @Override
            public void onError(String error) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(StressMonitorActivity.this, 
                    "Error saving assessment: " + error, 
                    Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    private void deleteAssessment(StressAssessment assessment) {
        new AlertDialog.Builder(this)
            .setTitle("Delete Assessment")
            .setMessage("Are you sure you want to delete this stress assessment?")
            .setPositiveButton("Delete", (dialog, which) -> {
                progressBar.setVisibility(View.VISIBLE);
                stressService.deleteStressAssessment(assessment.getAssessmentId(), 
                    new StressService.OnCompleteListener() {
                        @Override
                        public void onSuccess() {
                            progressBar.setVisibility(View.GONE);
                            Toast.makeText(StressMonitorActivity.this, 
                                "Assessment deleted", 
                                Toast.LENGTH_SHORT).show();
                            loadStressAssessments();
                        }

                        @Override
                        public void onError(String error) {
                            progressBar.setVisibility(View.GONE);
                            Toast.makeText(StressMonitorActivity.this, 
                                "Error deleting: " + error, 
                                Toast.LENGTH_SHORT).show();
                        }
                    });
            })
            .setNegativeButton("Cancel", null)
            .show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadStressAssessments();
    }
}
