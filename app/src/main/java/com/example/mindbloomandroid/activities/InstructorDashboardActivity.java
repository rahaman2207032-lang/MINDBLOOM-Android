package com.example.mindbloomandroid.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;
import com.example.mindbloomandroid.R;
import com.example.mindbloomandroid.fragments.SessionRequestsFragment;
import com.example.mindbloomandroid.service.InstructorService;
import com.example.mindbloomandroid.utility.SharedPreferencesManager;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import java.util.Map;


public class InstructorDashboardActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {


    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private Toolbar toolbar;
    private TabLayout tabLayout;
    private ViewPager2 viewPager;


    private TextView pendingRequestsLabel;
    private TextView todaySessionsLabel;
    private TextView totalClientsLabel;
    private TextView availableSlotsLabel;


    private InstructorService instructorService;
    private SharedPreferencesManager prefsManager;


    private Handler refreshHandler;
    private Runnable refreshRunnable;
    private static final int REFRESH_INTERVAL_MS = 5000; // 5 seconds

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        android.util.Log.d("InstructorDash", "🎯 ============================================");
        android.util.Log.d("InstructorDash", "🎯 InstructorDashboardActivity.onCreate() called");
        android.util.Log.d("InstructorDash", "🎯 ============================================");

        try {
            setContentView(R.layout.activity_instructor_dashboard);
            android.util.Log.d("InstructorDash", " Layout inflated successfully");

            prefsManager = SharedPreferencesManager.getInstance(this);
            android.util.Log.d("InstructorDash", "SharedPreferencesManager initialized");

            String userId = prefsManager.getUserId();
            String username = prefsManager.getUsername();
            String role = prefsManager.getRole();

            android.util.Log.d("InstructorDash", "📋 User Info:");
            android.util.Log.d("InstructorDash", "   UserID: " + userId);
            android.util.Log.d("InstructorDash", "   Username: " + username);
            android.util.Log.d("InstructorDash", "   Role: " + role);

            instructorService = new InstructorService();
            android.util.Log.d("InstructorDash", " InstructorService initialized");

            initializeViews();
            android.util.Log.d("InstructorDash", " Views initialized");

            setupToolbar();
            android.util.Log.d("InstructorDash", "Toolbar setup complete");

            setupNavigationDrawer();
            android.util.Log.d("InstructorDash", " Navigation drawer setup complete");

            setupTabs();
            android.util.Log.d("InstructorDash", " Tabs setup complete");

            loadDashboardData();
            android.util.Log.d("InstructorDash", " Loading dashboard data");

            startAutoRefresh();
            android.util.Log.d("InstructorDash", " Auto-refresh started");

            android.util.Log.d("InstructorDash", "🎉 Instructor Dashboard loaded successfully!");

        } catch (Exception e) {
            android.util.Log.e("InstructorDash", " EXCEPTION in onCreate: " + e.getMessage(), e);
            Toast.makeText(this, "Error loading dashboard: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void initializeViews() {
        toolbar = findViewById(R.id.toolbar);
        drawerLayout = findViewById(R.id.drawerLayout);
        navigationView = findViewById(R.id.navigationView);
        tabLayout = findViewById(R.id.tabLayout);
        viewPager = findViewById(R.id.viewPager);

        pendingRequestsLabel = findViewById(R.id.pendingRequestsLabel);
        todaySessionsLabel = findViewById(R.id.todaySessionsLabel);
        totalClientsLabel = findViewById(R.id.totalClientsLabel);
        availableSlotsLabel = findViewById(R.id.availableSlotsLabel);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
        }
    }

    private void setupNavigationDrawer() {
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar,
                R.string.navigation_drawer_open,
                R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
        navigationView.setNavigationItemSelectedListener(this);
    }

    private void setupTabs() {
        ViewPagerAdapter adapter = new ViewPagerAdapter(this);
        viewPager.setAdapter(adapter);

        new TabLayoutMediator(tabLayout, viewPager,
                (tab, position) -> {
                    switch (position) {
                        case 0:
                            tab.setText("📬 Requests");
                            break;
                        case 1:
                            tab.setText("📅 Schedule");
                            break;
                        case 2:
                            tab.setText("👥 Clients");
                            break;
                        case 3:
                            tab.setText("📝 Notes");
                            break;
                        case 4:
                            tab.setText("💬 Messages");
                            break;
                        case 5:
                            tab.setText("📊 Analytics");
                            break;
                    }
                }
        ).attach();
    }


    private void loadDashboardData() {
        String instructorId = prefsManager.getUserId();
        if (instructorId == null) {
            android.util.Log.e("InstructorDash", " User ID is null");
            Toast.makeText(this, "User ID not found", Toast.LENGTH_SHORT).show();
            return;
        }

        android.util.Log.d("InstructorDash", "📊 Loading dashboard stats for instructor: " + instructorId);

        instructorService.getDashboardStats(instructorId, new InstructorService.OnStatsLoadedListener() {
            @Override
            public void onStatsLoaded(Map<String, Integer> stats) {
                android.util.Log.d("InstructorDash", " Stats loaded: " + stats.toString());

                runOnUiThread(() -> {
                    try {
                        if (pendingRequestsLabel != null) {
                            pendingRequestsLabel.setText(String.valueOf(stats.getOrDefault("pendingRequests", 0)));
                        }
                        if (todaySessionsLabel != null) {
                            todaySessionsLabel.setText(String.valueOf(stats.getOrDefault("todaySessions", 0)));
                        }
                        if (totalClientsLabel != null) {
                            totalClientsLabel.setText(String.valueOf(stats.getOrDefault("totalClients", 0)));
                        }
                        if (availableSlotsLabel != null) {
                            availableSlotsLabel.setText(String.valueOf(stats.getOrDefault("availableSlots", 0)));
                        }
                        android.util.Log.d("InstructorDash", " Stats displayed successfully");
                    } catch (Exception e) {
                        android.util.Log.e("InstructorDash", " Error displaying stats: " + e.getMessage(), e);
                    }
                });
            }

            @Override
            public void onError(String error) {
                android.util.Log.e("InstructorDash", " Error loading stats: " + error);

                runOnUiThread(() -> {
                    // Set default values instead of showing error
                    try {
                        if (pendingRequestsLabel != null) pendingRequestsLabel.setText("0");
                        if (todaySessionsLabel != null) todaySessionsLabel.setText("0");
                        if (totalClientsLabel != null) totalClientsLabel.setText("0");
                        if (availableSlotsLabel != null) availableSlotsLabel.setText("0");
                    } catch (Exception e) {
                        android.util.Log.e("InstructorDash", " Error setting default stats: " + e.getMessage());
                    }
                });
            }
        });
    }


    private void startAutoRefresh() {
        refreshHandler = new Handler(Looper.getMainLooper());
        refreshRunnable = new Runnable() {
            @Override
            public void run() {
                loadDashboardData();
                refreshHandler.postDelayed(this, REFRESH_INTERVAL_MS);
            }
        };
        refreshHandler.post(refreshRunnable);
    }

    private void stopAutoRefresh() {
        if (refreshHandler != null && refreshRunnable != null) {
            refreshHandler.removeCallbacks(refreshRunnable);
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.nav_home) {
            Toast.makeText(this, "Dashboard", Toast.LENGTH_SHORT).show();
        } else if (id == R.id.nav_schedule) {
            Toast.makeText(this, "Schedule - Coming Soon", Toast.LENGTH_SHORT).show();
        } else if (id == R.id.nav_profile) {
            Toast.makeText(this, "Profile - Coming Soon", Toast.LENGTH_SHORT).show();
        } else if (id == R.id.nav_logout) {
            handleLogout();
        }

        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    private void handleLogout() {
        new AlertDialog.Builder(this)
            .setTitle("Logout")
            .setMessage("Are you sure you want to logout?")
            .setPositiveButton("Yes", (dialog, which) -> {
                prefsManager.clearSession();
                Intent intent = new Intent(this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            })
            .setNegativeButton("No", null)
            .show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopAutoRefresh();
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }


    private class ViewPagerAdapter extends FragmentStateAdapter {
        public ViewPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
            super(fragmentActivity);
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            // Tab 0: Session Requests, Tab 1: Schedule, Tab 2: Clients, Tab 3: Notes, Tab 4: Messages, Tab 5: Analytics
            android.util.Log.d("ViewPagerAdapter", "📍 Creating fragment for tab position: " + position);

            try {
                switch (position) {
                    case 0:
                        android.util.Log.d("ViewPagerAdapter", "✅ Creating SessionRequestsFragment");
                        return new SessionRequestsFragment();
                    case 1:
                        android.util.Log.d("ViewPagerAdapter", "✅ Creating ScheduleFragment");
                        return new com.example.mindbloomandroid.fragments.ScheduleFragment();
                    case 2:
                        android.util.Log.d("ViewPagerAdapter", "✅ Creating ClientsFragment");
                        return new com.example.mindbloomandroid.fragments.ClientsFragment();
                    case 3:
                        android.util.Log.d("ViewPagerAdapter", "✅ Creating TherapyNotesFragment");
                        return new com.example.mindbloomandroid.fragments.TherapyNotesFragment();
                    case 4:
                        android.util.Log.d("ViewPagerAdapter", "✅ Creating InstructorMessagesFragment");
                        return new com.example.mindbloomandroid.fragments.InstructorMessagesFragment();
                    case 5:
                        android.util.Log.d("ViewPagerAdapter", "✅ Creating InstructorAnalyticsFragment");
                        return new com.example.mindbloomandroid.fragments.InstructorAnalyticsFragment();
                    default:
                        android.util.Log.d("ViewPagerAdapter", "⚠ Creating PlaceholderFragment for position: " + position);
                        return PlaceholderFragment.newInstance(position);
                }
            } catch (Exception e) {
                android.util.Log.e("ViewPagerAdapter", "❌ Error creating fragment at position " + position + ": " + e.getMessage(), e);

                return PlaceholderFragment.newInstance(position);
            }
        }

        @Override
        public int getItemCount() {
            return 6; // 6 tabs total
        }
    }


    public static class PlaceholderFragment extends Fragment {

        private static final String ARG_POSITION = "position";


        public static PlaceholderFragment newInstance(int position) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_POSITION, position);
            fragment.setArguments(args);
            return fragment;
        }


        public PlaceholderFragment() {
            // Empty constructor required by Android
        }

        @Nullable
        @Override
        public android.view.View onCreateView(@NonNull LayoutInflater inflater,
                                             @Nullable ViewGroup container,
                                             @Nullable Bundle savedInstanceState) {
            // Create a FrameLayout as root (ViewGroup required for dynamic content)
            android.widget.FrameLayout frameLayout = new android.widget.FrameLayout(requireContext());
            frameLayout.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            ));
            return frameLayout;
        }

        @Override
        public void onViewCreated(@NonNull android.view.View view, Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);

            try {

                int position = getArguments() != null
                    ? getArguments().getInt(ARG_POSITION, -1)
                    : -1;

                android.util.Log.d("PlaceholderFragment", "📍 Loading placeholder for position: " + position);

                String message;
                switch (position) {
                    case 3:
                        message = "💬 Messages\n\n" +
                                "Coming Soon:\n" +
                                "• Private messaging with clients\n" +
                                "• Conversation history\n" +
                                "• Real-time chat";
                        break;
                    case 4:
                        message = "📊 Analytics\n\n" +
                                "Coming Soon:\n" +
                                "• Performance metrics\n" +
                                "• Session statistics\n" +
                                "• Client progress charts";
                        break;
                    default:
                        message = "Feature Coming Soon";
                }

                TextView textView = new TextView(requireContext());
                textView.setText(message);
                textView.setTextSize(16);
                textView.setPadding(32, 32, 32, 32);
                textView.setTextAlignment(android.view.View.TEXT_ALIGNMENT_CENTER);
                textView.setGravity(android.view.Gravity.CENTER);


                if (view instanceof android.view.ViewGroup) {
                    ((android.view.ViewGroup) view).addView(textView);
                    android.util.Log.d("PlaceholderFragment", " Placeholder view added successfully");
                }

            } catch (Exception e) {
                android.util.Log.e("PlaceholderFragment", " Error in onViewCreated: " + e.getMessage(), e);
                if (getContext() != null) {
                    Toast.makeText(getContext(), "Error loading tab", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }
}

