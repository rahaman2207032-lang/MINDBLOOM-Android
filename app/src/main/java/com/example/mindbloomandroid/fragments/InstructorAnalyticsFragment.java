package com.example.mindbloomandroid.fragments;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.example.mindbloomandroid.R;
import com.example.mindbloomandroid.model.ClientOverview;
import com.example.mindbloomandroid.service.InstructorService;
import com.example.mindbloomandroid.utility.SharedPreferencesManager;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


public class InstructorAnalyticsFragment extends Fragment {

    private RadioGroup timeRangeGroup;
    private TextView totalSessions;
    private TextView activeClients;
    private LineChart sessionsChart;
    private BarChart clientProgressChart;
    private ProgressBar progressBar;

    private InstructorService instructorService;
    private SharedPreferencesManager prefsManager;
    private String selectedTimeRange = "week";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_instructor_analytics, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        try {
            android.util.Log.d("InstructorAnalyticsFragment", "🎯 onViewCreated started");

            initializeViews(view);

            instructorService = new InstructorService();
            prefsManager = SharedPreferencesManager.getInstance(requireContext());

            setupTimeRangeSelector();
            loadAnalytics();

            android.util.Log.d("InstructorAnalyticsFragment", "✅ onViewCreated completed successfully");
        } catch (Exception e) {
            android.util.Log.e("InstructorAnalyticsFragment", "❌ CRASH in onViewCreated: " + e.getMessage(), e);
            if (getContext() != null) {
                Toast.makeText(getContext(), "Error loading analytics: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        }
    }

    private void initializeViews(View view) {
        timeRangeGroup = view.findViewById(R.id.timeRangeGroup);
        totalSessions = view.findViewById(R.id.totalSessions);
        activeClients = view.findViewById(R.id.activeClients);
        sessionsChart = view.findViewById(R.id.sessionsChart);
        clientProgressChart = view.findViewById(R.id.clientProgressChart);
        progressBar = view.findViewById(R.id.progressBar);
    }

    private void setupTimeRangeSelector() {
        timeRangeGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.radioWeek) {
                selectedTimeRange = "week";
            } else if (checkedId == R.id.radioMonth) {
                selectedTimeRange = "month";
            } else if (checkedId == R.id.radioYear) {
                selectedTimeRange = "year";
            }
            loadAnalytics();
        });
    }

    private void loadAnalytics() {
        String instructorId = prefsManager.getUserId();
        if (instructorId == null) {
            Toast.makeText(requireContext(), "User not logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        instructorService.getDashboardStats(instructorId, new InstructorService.OnStatsLoadedListener() {
            @Override
            public void onStatsLoaded(Map<String, Integer> stats) {
                if (getActivity() == null) return;

                progressBar.setVisibility(View.GONE);
                updateStats(stats);
                setupSessionsChart();
                setupClientProgressChart();
            }

            @Override
            public void onError(String error) {
                if (getActivity() == null) return;

                progressBar.setVisibility(View.GONE);
                Toast.makeText(requireContext(), "Error: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateStats(Map<String, Integer> stats) {
        // Get real session count from Firebase - totalSessions is already calculated in InstructorService
        int totalSessionsCount = stats.getOrDefault("totalSessions", 0);
        int activeClientsCount = stats.getOrDefault("totalClients", 0);

        totalSessions.setText(String.valueOf(totalSessionsCount));
        activeClients.setText(String.valueOf(activeClientsCount));
    }

    private void setupSessionsChart() {
        String instructorId = prefsManager.getUserId();
        if (instructorId == null) return;
        
        progressBar.setVisibility(View.VISIBLE);
        instructorService.getSessionAnalytics(instructorId, selectedTimeRange, 
            new InstructorService.OnSessionAnalyticsLoadedListener() {
                @Override
                public void onAnalyticsLoaded(List<Long> sessionDates) {
                    if (getActivity() == null) return;
                    progressBar.setVisibility(View.GONE);
                    
                    List<Entry> entries = new ArrayList<>();
                    
                    if (selectedTimeRange.equals("week")) {
                        // Group by day of week
                        int[] dayCounts = new int[7];
                        for (Long date : sessionDates) {
                            java.util.Calendar cal = java.util.Calendar.getInstance();
                            cal.setTimeInMillis(date);
                            int dayOfWeek = cal.get(java.util.Calendar.DAY_OF_WEEK) - 1;
                            if (dayOfWeek >= 0 && dayOfWeek < 7) {
                                dayCounts[dayOfWeek]++;
                            }
                        }
                        for (int i = 0; i < 7; i++) {
                            entries.add(new Entry(i, dayCounts[i]));
                        }
                    } else if (selectedTimeRange.equals("month")) {
                        // Group by week
                        int[] weekCounts = new int[4];
                        long currentTime = System.currentTimeMillis();
                        long weekMs = 7L * 24 * 60 * 60 * 1000;
                        for (Long date : sessionDates) {
                            long diff = currentTime - date;
                            int weekIndex = (int) (diff / weekMs);
                            if (weekIndex >= 0 && weekIndex < 4) {
                                weekCounts[3 - weekIndex]++;
                            }
                        }
                        for (int i = 0; i < 4; i++) {
                            entries.add(new Entry(i, weekCounts[i]));
                        }
                    } else {
                        // Group by month
                        int[] monthCounts = new int[12];
                        for (Long date : sessionDates) {
                            java.util.Calendar cal = java.util.Calendar.getInstance();
                            cal.setTimeInMillis(date);
                            int month = cal.get(java.util.Calendar.MONTH);
                            if (month >= 0 && month < 12) {
                                monthCounts[month]++;
                            }
                        }
                        for (int i = 0; i < 12; i++) {
                            entries.add(new Entry(i, monthCounts[i]));
                        }
                    }
                    
                    displayChart(entries);
                }

                @Override
                public void onError(String error) {
                    if (getActivity() == null) return;
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(requireContext(), "Error loading analytics", Toast.LENGTH_SHORT).show();
                }
            });
    }
    
    private void displayChart(List<Entry> entries) {

        if (entries.isEmpty()) {
            entries.add(new Entry(0, 0));
        }
        
        LineDataSet dataSet = new LineDataSet(entries, "Sessions");
        dataSet.setColor(Color.parseColor("#7F9C96"));
        dataSet.setCircleColor(Color.parseColor("#7F9C96"));
        dataSet.setLineWidth(2f);
        dataSet.setCircleRadius(4f);
        dataSet.setDrawValues(true);
        dataSet.setValueTextSize(10f);

        LineData lineData = new LineData(dataSet);
        sessionsChart.setData(lineData);
        sessionsChart.getDescription().setEnabled(false);
        sessionsChart.getLegend().setEnabled(false);
        sessionsChart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
        sessionsChart.getXAxis().setGranularity(1f);
        sessionsChart.getAxisRight().setEnabled(false);
        sessionsChart.animateY(1000);
        sessionsChart.invalidate();
    }

    private void setupClientProgressChart() {
        String instructorId = prefsManager.getUserId();
        if (instructorId == null) return;
        

        instructorService.getAllClients(instructorId, new InstructorService.OnClientsLoadedListener() {
            @Override
            public void onClientsLoaded(List<ClientOverview> clients) {
                if (getActivity() == null) return;
                
                List<BarEntry> entries = new ArrayList<>();
                
                // Calculate engagement for each client (up to 5)
                int maxClients = Math.min(clients.size(), 5);
                for (int i = 0; i < maxClients; i++) {
                    // For now, show 80% engagement as placeholder
                    // In a real app, calculate based on session attendance, activity completion, etc.
                    float engagement = 80f;
                    entries.add(new BarEntry(i, engagement));
                }
                
                if (entries.isEmpty()) {
                    entries.add(new BarEntry(0, 0));
                }
                
                displayProgressChart(entries);
            }

            @Override
            public void onError(String error) {
                if (getActivity() == null) return;
                List<BarEntry> entries = new ArrayList<>();
                entries.add(new BarEntry(0, 0));
                displayProgressChart(entries);
            }
        });
    }
    
    private void displayProgressChart(List<BarEntry> entries) {

        BarDataSet dataSet = new BarDataSet(entries, "Client Engagement (%)");
        dataSet.setColor(Color.parseColor("#81C784"));
        dataSet.setValueTextSize(11f);
        dataSet.setValueTextColor(Color.BLACK);

        BarData barData = new BarData(dataSet);
        barData.setBarWidth(0.5f);
        
        clientProgressChart.setData(barData);
        clientProgressChart.getDescription().setEnabled(false);
        clientProgressChart.getLegend().setEnabled(false);
        clientProgressChart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
        clientProgressChart.getXAxis().setGranularity(1f);
        clientProgressChart.getAxisRight().setEnabled(false);
        clientProgressChart.getAxisLeft().setAxisMinimum(0f);
        clientProgressChart.getAxisLeft().setAxisMaximum(100f);
        clientProgressChart.animateY(1000);
        clientProgressChart.invalidate();
    }
}
