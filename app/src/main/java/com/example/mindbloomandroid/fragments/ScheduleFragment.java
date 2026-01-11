package com.example.mindbloomandroid.fragments;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.mindbloomandroid.R;
import com.example.mindbloomandroid.model.TherapySession;
import com.example.mindbloomandroid.service.TherapySessionService;
import com.example.mindbloomandroid.utility.SharedPreferencesManager;
import com.google.android.material.button.MaterialButton;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;


public class ScheduleFragment extends Fragment {

    private RecyclerView scheduleRecyclerView;
    private ProgressBar progressBar;
    private TextView weekLabel;
    private Button prevWeekBtn, nextWeekBtn;

    private TherapySessionService sessionService;
    private SharedPreferencesManager prefsManager;
    private List<TherapySession> weeklySessions;
    private Calendar currentWeekStart;
    private ScheduleAdapter scheduleAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_schedule, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        try {
            android.util.Log.d("ScheduleFragment", "🎯 onViewCreated started");

            initializeViews(view);
            sessionService = new TherapySessionService();
            prefsManager = SharedPreferencesManager.getInstance(requireContext());
            weeklySessions = new ArrayList<>();

            // Set current week start (Monday)
            currentWeekStart = Calendar.getInstance();
            currentWeekStart.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);

            setupRecyclerView();
            updateWeekLabel();
            loadWeeklySessions();

            android.util.Log.d("ScheduleFragment", "✅ onViewCreated completed successfully");
        } catch (Exception e) {
            android.util.Log.e("ScheduleFragment", "❌ CRASH in onViewCreated: " + e.getMessage(), e);
            if (getContext() != null) {
                Toast.makeText(getContext(), "Error loading schedule: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        }
    }

    private void initializeViews(View view) {
        try {
            android.util.Log.d("ScheduleFragment", "🔍 Initializing views...");

            scheduleRecyclerView = view.findViewById(R.id.scheduleRecyclerView);
            if (scheduleRecyclerView == null) {
                throw new RuntimeException("scheduleRecyclerView not found in layout!");
            }

            progressBar = view.findViewById(R.id.progressBar);
            if (progressBar == null) {
                throw new RuntimeException("progressBar not found in layout!");
            }

            weekLabel = view.findViewById(R.id.weekLabel);
            if (weekLabel == null) {
                throw new RuntimeException("weekLabel not found in layout!");
            }

            prevWeekBtn = view.findViewById(R.id.prevWeekBtn);
            if (prevWeekBtn == null) {
                android.util.Log.e("ScheduleFragment", "⚠️ prevWeekBtn not found!");
            }

            nextWeekBtn = view.findViewById(R.id.nextWeekBtn);
            if (nextWeekBtn == null) {
                android.util.Log.e("ScheduleFragment", "⚠️ nextWeekBtn not found!");
            }

            if (prevWeekBtn != null) {
                prevWeekBtn.setOnClickListener(v -> {
                    try {
                        navigateToPreviousWeek();
                    } catch (Exception e) {
                        android.util.Log.e("ScheduleFragment", "Error navigating to previous week", e);
                        Toast.makeText(getContext(), "Error loading previous week", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            if (nextWeekBtn != null) {
                nextWeekBtn.setOnClickListener(v -> {
                    try {
                        navigateToNextWeek();
                    } catch (Exception e) {
                        android.util.Log.e("ScheduleFragment", "Error navigating to next week", e);
                        Toast.makeText(getContext(), "Error loading next week", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            android.util.Log.d("ScheduleFragment", "✅ All views initialized successfully");
            android.util.Log.d("ScheduleFragment", "   RecyclerView: " + (scheduleRecyclerView != null ? "OK" : "NULL"));
            android.util.Log.d("ScheduleFragment", "   ProgressBar: " + (progressBar != null ? "OK" : "NULL"));
            android.util.Log.d("ScheduleFragment", "   WeekLabel: " + (weekLabel != null ? "OK" : "NULL"));
            android.util.Log.d("ScheduleFragment", "   PrevBtn: " + (prevWeekBtn != null ? "OK" : "NULL"));
            android.util.Log.d("ScheduleFragment", "   NextBtn: " + (nextWeekBtn != null ? "OK" : "NULL"));

        } catch (Exception e) {
            android.util.Log.e("ScheduleFragment", "❌ FATAL: Error initializing views: " + e.getMessage(), e);
            android.util.Log.e("ScheduleFragment", "   Layout file: fragment_schedule.xml");
            android.util.Log.e("ScheduleFragment", "   Check if all view IDs match the layout file!");
            throw e;
        }
    }

    private void setupRecyclerView() {
        try {
            android.util.Log.d("ScheduleFragment", "📋 Setting up RecyclerView...");

            if (scheduleRecyclerView == null) {
                throw new RuntimeException("Cannot setup RecyclerView - view is null!");
            }

            if (getContext() == null) {
                throw new RuntimeException("Cannot setup RecyclerView - context is null!");
            }

            scheduleAdapter = new ScheduleAdapter(new ArrayList<>(), this::openZoomLink);
            scheduleRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
            scheduleRecyclerView.setAdapter(scheduleAdapter);

            android.util.Log.d("ScheduleFragment", "✅ RecyclerView setup complete");
        } catch (Exception e) {
            android.util.Log.e("ScheduleFragment", "❌ FATAL: Error setting up RecyclerView: " + e.getMessage(), e);
            if (getContext() != null) {
                Toast.makeText(getContext(), "Error loading schedule view: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
            throw e;
        }
    }

    private void loadWeeklySessions() {
        try {
            String instructorId = prefsManager.getUserId();
            if (instructorId == null) {
                android.util.Log.e("ScheduleFragment", "❌ User ID is null");
                Toast.makeText(requireContext(), "User not logged in", Toast.LENGTH_SHORT).show();
                return;
            }

            android.util.Log.d("ScheduleFragment", "📖 Loading weekly sessions for instructor: " + instructorId);
            android.util.Log.d("ScheduleFragment", "📅 Week start: " + currentWeekStart.getTime());

            progressBar.setVisibility(View.VISIBLE);

            sessionService.getInstructorWeeklySessions(instructorId,
                currentWeekStart.getTimeInMillis(),
                new TherapySessionService.OnSessionsLoadedListener() {
                    @Override
                    public void onSessionsLoaded(List<TherapySession> sessions) {
                        if (getActivity() == null) return;

                        android.util.Log.d("ScheduleFragment", "✅ Loaded " + sessions.size() + " sessions");

                        progressBar.setVisibility(View.GONE);
                        weeklySessions = sessions;

                        try {
                            List<DaySchedule> daySchedules = groupSessionsByDay(sessions);
                            scheduleAdapter.updateSessions(daySchedules);
                            android.util.Log.d("ScheduleFragment", "✅ Schedule updated successfully");
                        } catch (Exception e) {
                            android.util.Log.e("ScheduleFragment", "❌ Error updating schedule: " + e.getMessage(), e);
                            Toast.makeText(requireContext(), "Error displaying schedule", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onError(String error) {
                        if (getActivity() == null) return;

                        android.util.Log.e("ScheduleFragment", "❌ Error loading sessions: " + error);
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(requireContext(), "Error loading schedule: " + error,
                            Toast.LENGTH_SHORT).show();
                    }
                });
        } catch (Exception e) {
            android.util.Log.e("ScheduleFragment", "❌ EXCEPTION in loadWeeklySessions: " + e.getMessage(), e);
            progressBar.setVisibility(View.GONE);
            Toast.makeText(requireContext(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private List<DaySchedule> groupSessionsByDay(List<TherapySession> sessions) {
        List<DaySchedule> daySchedules = new ArrayList<>();


        for (int i = 0; i < 7; i++) {
            Calendar day = (Calendar) currentWeekStart.clone();
            day.add(Calendar.DAY_OF_WEEK, i);

            DaySchedule daySchedule = new DaySchedule();
            daySchedule.date = day.getTime();
            daySchedule.sessions = new ArrayList<>();

            // Find sessions for this day
            for (TherapySession session : sessions) {
                Calendar sessionCal = Calendar.getInstance();
                sessionCal.setTimeInMillis(session.getSessionDate());

                if (isSameDay(day, sessionCal)) {
                    daySchedule.sessions.add(session);
                }
            }

            daySchedules.add(daySchedule);
        }

        return daySchedules;
    }

    private boolean isSameDay(Calendar cal1, Calendar cal2) {
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
               cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR);
    }

    private void updateWeekLabel() {
        try {
            if (weekLabel == null) {
                android.util.Log.e("ScheduleFragment", "❌ Cannot update week label - view is null!");
                return;
            }

            if (currentWeekStart == null) {
                android.util.Log.e("ScheduleFragment", "❌ Cannot update week label - currentWeekStart is null!");
                return;
            }

            Calendar weekEnd = (Calendar) currentWeekStart.clone();
            weekEnd.add(Calendar.DAY_OF_WEEK, 6);

            SimpleDateFormat sdf = new SimpleDateFormat("MMM d", Locale.getDefault());
            SimpleDateFormat yearSdf = new SimpleDateFormat("MMM d, yyyy", Locale.getDefault());

            String weekStr = sdf.format(currentWeekStart.getTime()) + " - " +
                             yearSdf.format(weekEnd.getTime());
            weekLabel.setText("Week of " + weekStr);

            android.util.Log.d("ScheduleFragment", "✅ Week label updated: " + weekStr);
        } catch (Exception e) {
            android.util.Log.e("ScheduleFragment", "❌ Error updating week label: " + e.getMessage(), e);
        }
    }

    private void navigateToPreviousWeek() {
        currentWeekStart.add(Calendar.WEEK_OF_YEAR, -1);
        updateWeekLabel();
        loadWeeklySessions();
    }

    private void navigateToNextWeek() {
        currentWeekStart.add(Calendar.WEEK_OF_YEAR, 1);
        updateWeekLabel();
        loadWeeklySessions();
    }


    private void openZoomLink(String zoomLink) {
        if (zoomLink != null && !zoomLink.isEmpty()) {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(zoomLink));
            startActivity(intent);
            Toast.makeText(requireContext(), "Opening Zoom meeting...", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(requireContext(), "No Zoom link available", Toast.LENGTH_SHORT).show();
        }
    }

    // Data class for day schedule
    static class DaySchedule {
        Date date;
        List<TherapySession> sessions;
    }

    // Interface for zoom link clicks
    interface OnZoomLinkClickListener {
        void onZoomLinkClick(String zoomLink);
    }

    // RecyclerView Adapter
    class ScheduleAdapter extends RecyclerView.Adapter<ScheduleAdapter.DayViewHolder> {
        private List<DaySchedule> daySchedules;
        private OnZoomLinkClickListener zoomLinkClickListener;


        ScheduleAdapter(List<DaySchedule> daySchedules, OnZoomLinkClickListener listener) {
            this.daySchedules = daySchedules;
            this.zoomLinkClickListener = listener;
        }

        void updateSessions(List<DaySchedule> newSchedules) {
            this.daySchedules = newSchedules;
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public DayViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_day_schedule, parent, false);
            return new DayViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull DayViewHolder holder, int position) {
            try {
                DaySchedule daySchedule = daySchedules.get(position);

                if (daySchedule == null) {
                    android.util.Log.e("ScheduleFragment", "❌ DaySchedule is null at position " + position);
                    return;
                }

                if (holder.dayLabel == null || holder.sessionsContainer == null || holder.cardView == null) {
                    android.util.Log.e("ScheduleFragment", "❌ Holder views are null!");
                    return;
                }

                // Format day label
                SimpleDateFormat dayFormat = new SimpleDateFormat("EEE, MMM d", Locale.getDefault());
                holder.dayLabel.setText(dayFormat.format(daySchedule.date));

                // Clear previous sessions
                holder.sessionsContainer.removeAllViews();

                if (daySchedule.sessions == null || daySchedule.sessions.isEmpty()) {
                    TextView noSessions = new TextView(holder.itemView.getContext());
                    noSessions.setText("No sessions");
                    noSessions.setTextColor(0xFF999999);
                    noSessions.setTextSize(12);
                    holder.sessionsContainer.addView(noSessions);
                } else {
                    // Add each session
                    for (TherapySession session : daySchedule.sessions) {
                        if (session != null) {
                            View sessionView = createSessionView(session, holder.itemView.getContext());
                            if (sessionView != null) {
                                holder.sessionsContainer.addView(sessionView);
                            }
                        }
                    }
                }

                // Highlight today
                Calendar today = Calendar.getInstance();
                Calendar dayDate = Calendar.getInstance();
                dayDate.setTime(daySchedule.date);

                if (isSameDay(today, dayDate)) {
                    holder.cardView.setCardBackgroundColor(0xFFDFF9FB); // Light cyan
                } else {
                    holder.cardView.setCardBackgroundColor(0xFFF8F9FA); // Light gray
                }
            } catch (Exception e) {
                android.util.Log.e("ScheduleFragment", "❌ CRASH in onBindViewHolder at position " + position + ": " + e.getMessage(), e);
            }
        }

        private View createSessionView(TherapySession session, android.content.Context context) {
            android.widget.LinearLayout sessionLayout = new android.widget.LinearLayout(context);
            sessionLayout.setOrientation(android.widget.LinearLayout.VERTICAL);
            sessionLayout.setPadding(16, 12, 16, 12);
            sessionLayout.setBackgroundColor(0xFFFFFFFF);

            android.view.ViewGroup.MarginLayoutParams params = new android.view.ViewGroup.MarginLayoutParams(
                android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                android.view.ViewGroup.LayoutParams.WRAP_CONTENT
            );
            params.setMargins(0, 0, 0, 12);
            sessionLayout.setLayoutParams(params);

            // Time and client info
            TextView sessionInfo = new TextView(context);
            SimpleDateFormat timeFormat = new SimpleDateFormat("h:mm a", Locale.getDefault());
            sessionInfo.setText("📅 " + timeFormat.format(new Date(session.getSessionDate())) +
                               "\n👤 Client: " + session.getClientName());
            sessionInfo.setTextSize(14);
            sessionInfo.setTextColor(0xFF2D3436);
            sessionLayout.addView(sessionInfo);

            // ✅ CLICKABLE ZOOM LINK BUTTON
            if (session.getZoomLink() != null && !session.getZoomLink().isEmpty()) {
                Button joinBtn = new Button(context);
                joinBtn.setText("🎥 Join Zoom Meeting");
                joinBtn.setBackgroundColor(0xFF27AE60);
                joinBtn.setTextColor(0xFFFFFFFF);
                joinBtn.setPadding(24, 16, 24, 16);
                android.view.ViewGroup.MarginLayoutParams btnParams = new android.view.ViewGroup.MarginLayoutParams(
                    android.view.ViewGroup.LayoutParams.WRAP_CONTENT,
                    android.view.ViewGroup.LayoutParams.WRAP_CONTENT
                );
                btnParams.setMargins(0, 12, 0, 0);
                joinBtn.setLayoutParams(btnParams);

                joinBtn.setOnClickListener(v -> {
                    if (zoomLinkClickListener != null) {
                        zoomLinkClickListener.onZoomLinkClick(session.getZoomLink());
                    }
                });

                sessionLayout.addView(joinBtn);
            }

            return sessionLayout;
        }

        @Override
        public int getItemCount() {
            return daySchedules.size();
        }

        class DayViewHolder extends RecyclerView.ViewHolder {
            CardView cardView;
            TextView dayLabel;
            android.widget.LinearLayout sessionsContainer;

            DayViewHolder(@NonNull View itemView) {
                super(itemView);
                cardView = (CardView) itemView;
                dayLabel = itemView.findViewById(R.id.dayLabel);
                sessionsContainer = itemView.findViewById(R.id.sessionsContainer);
            }
        }
    }
}
