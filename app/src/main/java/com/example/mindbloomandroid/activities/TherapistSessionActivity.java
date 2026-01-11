package com.example.mindbloomandroid.activities;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mindbloomandroid.R;
import com.example.mindbloomandroid.adapter.SessionRequestAdapter;
import com.example.mindbloomandroid.adapter.TherapySessionAdapter;
import com.example.mindbloomandroid.model.Instructor;
import com.example.mindbloomandroid.model.SessionRequest;
import com.example.mindbloomandroid.model.TherapySession;
import com.example.mindbloomandroid.service.InstructorService;
import com.example.mindbloomandroid.service.SessionRequestService;
import com.example.mindbloomandroid.service.TherapySessionService;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;


public class TherapistSessionActivity extends AppCompatActivity {
    

    private Spinner instructorSpinner;
    private Button selectDateButton;
    private Spinner sessionTimeSpinner;
    private Spinner sessionTypeSpinner;
    private EditText reasonEditText;
    private Button submitRequestBtn;


    private RecyclerView pendingRequestsRecyclerView;
    private TextView emptyPendingText;
    private SessionRequestAdapter pendingRequestsAdapter;
    private List<SessionRequest> pendingRequests;
    

    private RecyclerView confirmedSessionsRecyclerView;
    private TextView emptyConfirmedText;
    private TherapySessionAdapter confirmedSessionsAdapter;
    private List<TherapySession> confirmedSessions;
    
    private ProgressBar progressBar;
    

    private InstructorService instructorService;
    private SessionRequestService sessionRequestService;
    private TherapySessionService therapySessionService;
    

    private List<Instructor> instructors;
    private Calendar selectedDate;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_therapist_session);
        

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("💻 Therapist Sessions");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        
        initializeViews();
        setupSpinners();
        

        instructorService = new InstructorService();
        sessionRequestService = new SessionRequestService();
        therapySessionService = new TherapySessionService();
        

        loadInstructors();
        loadPendingRequests();
        loadConfirmedSessions();
    }
    
    private void initializeViews() {

        instructorSpinner = findViewById(R.id.instructorSpinner);
        selectDateButton = findViewById(R.id.selectDateButton);
        sessionTimeSpinner = findViewById(R.id.sessionTimeSpinner);
        sessionTypeSpinner = findViewById(R.id.sessionTypeSpinner);
        reasonEditText = findViewById(R.id.reasonEditText);
        submitRequestBtn = findViewById(R.id.submitRequestBtn);
        

        pendingRequestsRecyclerView = findViewById(R.id.pendingRequestsRecyclerView);
        emptyPendingText = findViewById(R.id.emptyPendingText);
        

        confirmedSessionsRecyclerView = findViewById(R.id.confirmedSessionsRecyclerView);
        emptyConfirmedText = findViewById(R.id.emptyConfirmedText);
        
        progressBar = findViewById(R.id.progressBar);
        

        pendingRequests = new ArrayList<>();
        pendingRequestsAdapter = new SessionRequestAdapter(this, pendingRequests);
        pendingRequestsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        pendingRequestsRecyclerView.setAdapter(pendingRequestsAdapter);
        
        confirmedSessions = new ArrayList<>();
        confirmedSessionsAdapter = new TherapySessionAdapter(this, confirmedSessions,
            this::openZoomLink);
        confirmedSessionsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        confirmedSessionsRecyclerView.setAdapter(confirmedSessionsAdapter);
        

        selectDateButton.setOnClickListener(v -> showDatePicker());
        submitRequestBtn.setOnClickListener(v -> handleSubmitRequest());
        findViewById(R.id.refreshBtn).setOnClickListener(v -> handleRefresh());
        

        selectedDate = Calendar.getInstance();
        updateDateButton();
    }
    
    private void setupSpinners() {
        // Session time slots
        String[] timeSlots = {
            "09:00 AM", "10:00 AM", "11:00 AM", "12:00 PM",
            "01:00 PM", "02:00 PM", "03:00 PM", "04:00 PM",
            "05:00 PM", "06:00 PM", "07:00 PM", "08:00 PM"
        };
        ArrayAdapter<String> timeAdapter = new ArrayAdapter<>(this, 
            android.R.layout.simple_spinner_item, timeSlots);
        timeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sessionTimeSpinner.setAdapter(timeAdapter);
        
        // Session types
        String[] sessionTypes = {
            "Individual Therapy",
            "Stress Management",
            "Anxiety Support",
            "Depression Support",
            "General Consultation",
            "Follow-up Session"
        };
        ArrayAdapter<String> typeAdapter = new ArrayAdapter<>(this,
            android.R.layout.simple_spinner_item, sessionTypes);
        typeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sessionTypeSpinner.setAdapter(typeAdapter);
    }
    
    private void showDatePicker() {
        DatePickerDialog picker = new DatePickerDialog(this,
            (view, year, month, dayOfMonth) -> {
                selectedDate.set(year, month, dayOfMonth);
                updateDateButton();
            },
            selectedDate.get(Calendar.YEAR),
            selectedDate.get(Calendar.MONTH),
            selectedDate.get(Calendar.DAY_OF_MONTH)
        );
        picker.getDatePicker().setMinDate(System.currentTimeMillis());
        picker.show();
    }
    
    private void updateDateButton() {
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("MMM dd, yyyy", java.util.Locale.getDefault());
        selectDateButton.setText(sdf.format(selectedDate.getTime()));
    }
    
    private void loadInstructors() {
        instructorService.getAllInstructors(new InstructorService.OnInstructorsLoadedListener() {
            @Override
            public void onInstructorsLoaded(List<Instructor> loadedInstructors) {
                instructors = loadedInstructors;
                
                // Create adapter with instructor names
                ArrayAdapter<Instructor> adapter = new ArrayAdapter<>(
                    TherapistSessionActivity.this,
                    android.R.layout.simple_spinner_item,
                    instructors
                );
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                instructorSpinner.setAdapter(adapter);
            }
            
            @Override
            public void onError(String error) {
                Toast.makeText(TherapistSessionActivity.this,
                    "Error loading instructors: " + error,
                    Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    private void handleSubmitRequest() {
        android.util.Log.d("TherapistSession", "🔵 handleSubmitRequest called");

        try {
            FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
            if (currentUser == null) {
                android.util.Log.e("TherapistSession", " User not logged in");
                Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
                return;
            }

            android.util.Log.d("TherapistSession", " User ID: " + currentUser.getUid());

            // Validate instructor selection
            if (instructorSpinner.getSelectedItem() == null) {
                android.util.Log.e("TherapistSession", " No instructor selected");
                Toast.makeText(this, "Please select an instructor", Toast.LENGTH_SHORT).show();
                return;
            }

            // Validate session type
            if (sessionTypeSpinner.getSelectedItem() == null) {
                android.util.Log.e("TherapistSession", " No session type selected");
                Toast.makeText(this, "Please select a session type", Toast.LENGTH_SHORT).show();
                return;
            }

            // Validate time selection
            if (sessionTimeSpinner.getSelectedItem() == null) {
                android.util.Log.e("TherapistSession", " No time selected");
                Toast.makeText(this, "Please select a time", Toast.LENGTH_SHORT).show();
                return;
            }

            Instructor selectedInstructor = (Instructor) instructorSpinner.getSelectedItem();
            String sessionType = sessionTypeSpinner.getSelectedItem().toString();
            String timeSlot = sessionTimeSpinner.getSelectedItem().toString();
            String reason = reasonEditText.getText().toString().trim();

            // Get current user's username
            String currentUsername = com.example.mindbloomandroid.utility.SharedPreferencesManager
                .getInstance(TherapistSessionActivity.this).getUsername();
            if (currentUsername == null) {
                currentUsername = "User"; // Fallback
            }

            android.util.Log.d("TherapistSession", "📋 Request details:");
            android.util.Log.d("TherapistSession", "   Instructor: " + selectedInstructor.getUsername());
            android.util.Log.d("TherapistSession", "   Client Name: " + currentUsername);
            android.util.Log.d("TherapistSession", "   Session Type: " + sessionType);
            android.util.Log.d("TherapistSession", "   Time Slot: " + timeSlot);
            android.util.Log.d("TherapistSession", "   Reason: " + reason);

            // Parse time
            Calendar sessionDateTime = (Calendar) selectedDate.clone();
            int hour = parseTimeSlot(timeSlot);
            sessionDateTime.set(Calendar.HOUR_OF_DAY, hour);
            sessionDateTime.set(Calendar.MINUTE, 0);
            sessionDateTime.set(Calendar.SECOND, 0);

            android.util.Log.d("TherapistSession", "📅 Session date/time: " + sessionDateTime.getTime());


            if (sessionDateTime.getTimeInMillis() < System.currentTimeMillis()) {
                android.util.Log.e("TherapistSession", " Date is in the past");
                Toast.makeText(this, "Please select a future date and time", Toast.LENGTH_SHORT).show();
                return;
            }


            SessionRequest request = new SessionRequest(
                currentUser.getUid(),
                selectedInstructor.getInstructorId(),
                sessionDateTime.getTimeInMillis(),
                sessionType,
                reason
            );


            request.setClientName(currentUsername);
            request.setInstructorName(selectedInstructor.getUsername());

            android.util.Log.d("TherapistSession", "📤 Submitting request to service...");

            submitRequestBtn.setEnabled(false);
            progressBar.setVisibility(View.VISIBLE);

            sessionRequestService.createSessionRequest(request,
                new SessionRequestService.OnSessionRequestCreatedListener() {
                    @Override
                    public void onSuccess(SessionRequest createdRequest) {
                        android.util.Log.d("TherapistSession", "✅ Request submitted successfully!");
                        progressBar.setVisibility(View.GONE);
                        submitRequestBtn.setEnabled(true);
                        Toast.makeText(TherapistSessionActivity.this,
                            "Session request submitted successfully!",
                            Toast.LENGTH_SHORT).show();


                        instructorSpinner.setSelection(0);
                        sessionTypeSpinner.setSelection(0);
                        sessionTimeSpinner.setSelection(0);
                        reasonEditText.setText("");
                        selectedDate = Calendar.getInstance();
                        updateDateButton();


                        loadPendingRequests();
                    }

                    @Override
                    public void onError(String error) {
                        android.util.Log.e("TherapistSession", "❌ Error submitting request: " + error);
                        progressBar.setVisibility(View.GONE);
                        submitRequestBtn.setEnabled(true);
                        Toast.makeText(TherapistSessionActivity.this,
                            "Error submitting request: " + error,
                            Toast.LENGTH_SHORT).show();
                    }
                });
        } catch (Exception e) {
            android.util.Log.e("TherapistSession", "❌ EXCEPTION in handleSubmitRequest: " + e.getMessage(), e);
            progressBar.setVisibility(View.GONE);
            submitRequestBtn.setEnabled(true);
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
    
    private int parseTimeSlot(String timeSlot) {
        // Parse "09:00 AM" format
        String[] parts = timeSlot.split(" ");
        String[] timeParts = parts[0].split(":");
        int hour = Integer.parseInt(timeParts[0]);
        
        if (parts[1].equals("PM") && hour != 12) {
            hour += 12;
        } else if (parts[1].equals("AM") && hour == 12) {
            hour = 0;
        }
        
        return hour;
    }
    
    private void loadPendingRequests() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) return;
        
        String userId = currentUser.getUid();
        
        android.util.Log.d("TherapistSession", "📖 Loading pending requests for user: " + userId);

        sessionRequestService.getUserSessionRequests(userId,
            new SessionRequestService.OnRequestsLoadedListener() {
                @Override
                public void onRequestsLoaded(List<SessionRequest> requests) {
                    android.util.Log.d("TherapistSession", "📥 Received " + requests.size() + " total requests");

                    pendingRequests.clear();


                    for (SessionRequest request : requests) {
                        if ("PENDING".equalsIgnoreCase(request.getStatus())) {
                            pendingRequests.add(request);
                        }
                    }

                    android.util.Log.d("TherapistSession", "✅ Filtered to " + pendingRequests.size() + " PENDING requests");

                    pendingRequestsAdapter.notifyDataSetChanged();
                    

                    if (pendingRequests.isEmpty()) {
                        emptyPendingText.setVisibility(View.VISIBLE);
                        pendingRequestsRecyclerView.setVisibility(View.GONE);
                    } else {
                        emptyPendingText.setVisibility(View.GONE);
                        pendingRequestsRecyclerView.setVisibility(View.VISIBLE);
                    }
                }
                
                @Override
                public void onError(String error) {
                    android.util.Log.e("TherapistSession", "❌ Error loading requests: " + error);
                    Toast.makeText(TherapistSessionActivity.this,
                        "Error loading requests: " + error,
                        Toast.LENGTH_SHORT).show();
                }
            });
    }
    
    private void loadConfirmedSessions() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) return;
        
        String userId = currentUser.getUid();
        
        android.util.Log.d("TherapistSession", "📖 Loading confirmed sessions for user: " + userId);

        therapySessionService.getScheduledSessionsForUser(userId,
            new TherapySessionService.OnSessionsLoadedListener() {
                @Override
                public void onSessionsLoaded(List<TherapySession> sessions) {
                    android.util.Log.d("TherapistSession", "✅ Received " + sessions.size() + " confirmed sessions");

                    confirmedSessions.clear();
                    confirmedSessions.addAll(sessions);
                    confirmedSessionsAdapter.notifyDataSetChanged();
                    

                    if (confirmedSessions.isEmpty()) {
                        android.util.Log.d("TherapistSession", "ℹ️ No upcoming sessions");
                        emptyConfirmedText.setVisibility(View.VISIBLE);
                        confirmedSessionsRecyclerView.setVisibility(View.GONE);
                    } else {
                        android.util.Log.d("TherapistSession", "✅ Displaying " + confirmedSessions.size() + " upcoming sessions");
                        emptyConfirmedText.setVisibility(View.GONE);
                        confirmedSessionsRecyclerView.setVisibility(View.VISIBLE);
                    }
                }
                
                @Override
                public void onError(String error) {
                    android.util.Log.e("TherapistSession", "❌ Error loading sessions: " + error);
                    Toast.makeText(TherapistSessionActivity.this,
                        "Error loading sessions: " + error,
                        Toast.LENGTH_SHORT).show();
                }
            });
    }
    
    private void handleRefresh() {
        loadPendingRequests();
        loadConfirmedSessions();
    }
    
    private void openZoomLink(String zoomLink) {
        if (zoomLink != null && !zoomLink.isEmpty()) {
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(zoomLink));
            startActivity(browserIntent);
            Toast.makeText(this, "Opening Zoom meeting...", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "No meeting link available", Toast.LENGTH_SHORT).show();
        }
    }
    
    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
