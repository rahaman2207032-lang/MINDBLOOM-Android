package com.example.mindbloomandroid.activities;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.example.mindbloomandroid.R;
import com.example.mindbloomandroid.model.JournalEntry;
import com.example.mindbloomandroid.service.JournalService;
import com.example.mindbloomandroid.utility.SharedPreferencesManager;
import com.example.mindbloomandroid.utility.DateTimeUtil;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;


public class JournalActivity extends AppCompatActivity {

    private EditText journalTextArea;
    private TextView lastUpdatedLabel;
    private TextView statusLabel;
    private Button saveButton;
    private ProgressBar progressBar;
    private Toolbar toolbar;

    private JournalService journalService;
    private SharedPreferencesManager prefsManager;
    private JournalEntry currentJournal;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_journal);

        initializeViews();
        setupToolbar();

        journalService = new JournalService();
        prefsManager = SharedPreferencesManager.getInstance(this);


        loadJournalFromDatabase();


        journalTextArea.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                statusLabel.setText("Unsaved changes");
                statusLabel.setTextColor(getResources().getColor(android.R.color.holo_orange_light));
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });


        saveButton.setOnClickListener(v -> handleSave());
    }

    private void initializeViews() {
        toolbar = findViewById(R.id.toolbar);
        journalTextArea = findViewById(R.id.journalTextArea);
        lastUpdatedLabel = findViewById(R.id.lastUpdatedLabel);
        statusLabel = findViewById(R.id.statusLabel);
        saveButton = findViewById(R.id.saveButton);
        progressBar = findViewById(R.id.progressBar);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("My Journal");
        }
        toolbar.setNavigationOnClickListener(v -> finish());
    }


    private void loadJournalFromDatabase() {
        showLoading(true);

        String userId = prefsManager.getUserId();
        if (userId == null) {
            android.util.Log.e("JournalActivity", "❌ User ID is null - cannot load journal");
            Toast.makeText(this, "Please login first", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        android.util.Log.d("JournalActivity", "📖 Loading journal for user: " + userId);


        journalService.getJournalByUserId(userId, new JournalService.OnJournalLoadedListener() {
            @Override
            public void onSuccess(JournalEntry journal) {
                currentJournal = journal;

                if (journal != null && journal.getContent() != null && !journal.getContent().isEmpty()) {
                    android.util.Log.d("JournalActivity", " Journal loaded - Content length: " + journal.getContent().length());
                    journalTextArea.setText(journal.getContent());
                    updateLastUpdatedLabel();
                    statusLabel.setText("Loaded");
                    statusLabel.setTextColor(getResources().getColor(android.R.color.holo_green_light));
                    Toast.makeText(JournalActivity.this, "Journal loaded", Toast.LENGTH_SHORT).show();
                } else {
                    // No journal yet - create new one
                    android.util.Log.d("JournalActivity", "ℹ️ No existing journal - ready for new entry");
                    statusLabel.setText("Ready to write");
                    statusLabel.setTextColor(getResources().getColor(android.R.color.holo_blue_light));
                }

                showLoading(false);
            }

            @Override
            public void onFailure(String error) {
                android.util.Log.e("JournalActivity", " Failed to load journal: " + error);
                showLoading(false);
                statusLabel.setText("Error: " + error);
                statusLabel.setTextColor(getResources().getColor(android.R.color.holo_red_light));
                Toast.makeText(JournalActivity.this, "Error loading journal: " + error, Toast.LENGTH_LONG).show();
            }
        });
    }


    private void handleSave() {
        String content = journalTextArea.getText().toString().trim();

        if (content.isEmpty()) {
            new AlertDialog.Builder(this)
                .setTitle("Empty Journal")
                .setMessage("Please write something before saving.")
                .setPositiveButton("OK", null)
                .show();
            return;
        }

        saveButton.setEnabled(false);
        statusLabel.setText("Saving...");
        statusLabel.setTextColor(getResources().getColor(android.R.color.holo_blue_light));

        String userId = prefsManager.getUserId();

        android.util.Log.d("JournalActivity", "💾 Attempting to save journal for user: " + userId);

        new Thread(() -> {
            try {
                if (currentJournal == null) {
                    // Create new journal
                    android.util.Log.d("JournalActivity", "📝 Creating NEW journal entry");

                    JournalEntry newJournal = new JournalEntry();
                    newJournal.setUserId(userId);
                    newJournal.setContent(content);
                    newJournal.setCreatedAt(System.currentTimeMillis());
                    newJournal.setUpdatedAt(System.currentTimeMillis());

                    journalService.createJournal(newJournal, new JournalService.OnCompleteListener() {
                        @Override
                        public void onSuccess(String journalId) {
                            android.util.Log.d("JournalActivity", "✅ Journal saved successfully!");
                            runOnUiThread(() -> {
                                currentJournal = newJournal;
                                currentJournal.setJournalId(journalId);
                                statusLabel.setText("Saved successfully");
                                statusLabel.setTextColor(getResources().getColor(android.R.color.holo_green_light));
                                saveButton.setEnabled(true);
                                updateLastUpdatedLabel();
                                Toast.makeText(JournalActivity.this, "✅ Journal saved!", Toast.LENGTH_SHORT).show();
                            });
                        }

                        @Override
                        public void onFailure(String error) {
                            android.util.Log.e("JournalActivity", "❌ Failed to save journal: " + error);
                            runOnUiThread(() -> {
                                statusLabel.setText("Error: " + error);
                                statusLabel.setTextColor(getResources().getColor(android.R.color.holo_red_light));
                                saveButton.setEnabled(true);


                                new AlertDialog.Builder(JournalActivity.this)
                                    .setTitle("Save Failed")
                                    .setMessage("Could not save journal.\n\nError: " + error +
                                               "\n\nPlease check:\n1. Internet connection\n2. Firebase rules are updated\n3. User is logged in")
                                    .setPositiveButton("OK", null)
                                    .show();
                            });
                        }
                    });
                } else {
                    // Update existing journal
                    android.util.Log.d("JournalActivity", "✏️ Updating EXISTING journal entry");

                    currentJournal.setContent(content);
                    currentJournal.setUpdatedAt(System.currentTimeMillis());

                    journalService.updateJournal(currentJournal.getJournalId(), currentJournal, 
                        new JournalService.OnCompleteListener() {
                        @Override
                        public void onSuccess(String message) {
                            android.util.Log.d("JournalActivity", "✅ Journal updated successfully!");
                            runOnUiThread(() -> {
                                statusLabel.setText("Saved successfully");
                                statusLabel.setTextColor(getResources().getColor(android.R.color.holo_green_light));
                                saveButton.setEnabled(true);
                                updateLastUpdatedLabel();
                                Toast.makeText(JournalActivity.this, "✅ Journal updated!", Toast.LENGTH_SHORT).show();
                            });
                        }

                        @Override
                        public void onFailure(String error) {
                            android.util.Log.e("JournalActivity", " Failed to update journal: " + error);
                            runOnUiThread(() -> {
                                statusLabel.setText("Error: " + error);
                                statusLabel.setTextColor(getResources().getColor(android.R.color.holo_red_light));
                                saveButton.setEnabled(true);
                            });
                        }
                    });
                }
            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> {
                    statusLabel.setText("Error saving");
                    statusLabel.setTextColor(getResources().getColor(android.R.color.holo_red_light));
                    saveButton.setEnabled(true);
                    Toast.makeText(this, "Failed to save: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
            }
        }).start();
    }


    private void updateLastUpdatedLabel() {
        if (currentJournal != null && currentJournal.getUpdatedAt() > 0) {
            String formattedDate = DateTimeUtil.formatDateTime(currentJournal.getUpdatedAt());
            lastUpdatedLabel.setText("Last updated: " + formattedDate);
        } else {
            lastUpdatedLabel.setText("Not saved yet");
        }
    }

    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        journalTextArea.setEnabled(!show);
        saveButton.setEnabled(!show);
    }
}

