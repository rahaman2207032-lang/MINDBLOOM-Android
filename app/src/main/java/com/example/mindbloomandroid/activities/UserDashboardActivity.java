package com.example.mindbloomandroid.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import com.example.mindbloomandroid.R;
import com.example.mindbloomandroid.utility.SharedPreferencesManager;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.navigation.NavigationView;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;


public class UserDashboardActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private Toolbar toolbar;
    private TextView welcomeText;
    private TextView dateText;

    // Feature Cards
    private MaterialCardView journalCard;
    private MaterialCardView meditationCard;
    private MaterialCardView sleepTrackerCard;
    private MaterialCardView habitsCard;
    private MaterialCardView stressMonitorCard;
    private MaterialCardView notificationCard;
    private MaterialCardView moodAnalyticsCard;
    private MaterialCardView progressCard;
    private MaterialCardView therapistZoomCard;
    private MaterialCardView communityForumCard;

    private SharedPreferencesManager prefsManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_dashboard);

        prefsManager = SharedPreferencesManager.getInstance(this);

        initializeViews();
        setupToolbar();
        setupNavigationDrawer();
        setupFeatureCards();
        updateWelcomeMessage();

        // Handle back press with new API
        getOnBackPressedDispatcher().addCallback(this, new androidx.activity.OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (drawerLayout.isDrawerOpen(androidx.core.view.GravityCompat.START)) {
                    drawerLayout.closeDrawer(androidx.core.view.GravityCompat.START);
                } else {
                    finishAffinity();
                }
            }
        });
    }

    private void initializeViews() {

        toolbar = findViewById(R.id.toolbar);
        drawerLayout = findViewById(R.id.drawerLayout);
        navigationView = findViewById(R.id.navigationView);


        welcomeText = findViewById(R.id.welcomeText);
        dateText = findViewById(R.id.dateText);


        journalCard = findViewById(R.id.journalCard);
        meditationCard = findViewById(R.id.meditationCard);
        sleepTrackerCard = findViewById(R.id.sleepTrackerCard);
        habitsCard = findViewById(R.id.habitsCard);
        stressMonitorCard = findViewById(R.id.stressMonitorCard);
        notificationCard = findViewById(R.id.notificationCard);
        moodAnalyticsCard = findViewById(R.id.moodAnalyticsCard);
        progressCard = findViewById(R.id.progressCard);
        therapistZoomCard = findViewById(R.id.therapistCard);
        communityForumCard = findViewById(R.id.communityForumCard);
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


        updateNavHeader();
    }

    private void updateNavHeader() {

    }

    private void updateWelcomeMessage() {
        String username = prefsManager.getUsername();
        if (username != null) {
            welcomeText.setText("Welcome back, " + username + "!");
        }


        SimpleDateFormat sdf = new SimpleDateFormat("EEEE, MMMM d, yyyy", Locale.getDefault());
        dateText.setText(sdf.format(new Date()));
    }

    private void setupFeatureCards() {

        journalCard.setOnClickListener(v -> {
            Intent intent = new Intent(this, JournalActivity.class);
            startActivity(intent);
        });


        meditationCard.setOnClickListener(v -> {
            Intent intent = new Intent(this, MeditationActivity.class);
            startActivity(intent);
        });


        sleepTrackerCard.setOnClickListener(v -> {
            Intent intent = new Intent(this, SleepTrackerActivity.class);
            startActivity(intent);
        });


        habitsCard.setOnClickListener(v -> {
            Intent intent = new Intent(this, HabitTrackerActivity.class);
            startActivity(intent);
        });


        stressMonitorCard.setOnClickListener(v -> {
            Intent intent = new Intent(this, StressMonitorActivity.class);
            startActivity(intent);
        });


        notificationCard.setOnClickListener(v -> {
            Intent intent = new Intent(this, NotificationActivity.class);
            startActivity(intent);
        });


        moodAnalyticsCard.setOnClickListener(v -> {
            Intent intent = new Intent(this, MoodTrackerActivity.class);
            startActivity(intent);
        });


        progressCard.setOnClickListener(v -> {
            Intent intent = new Intent(this, ProgressActivity.class);
            startActivity(intent);
        });

        // Therapist Sessions
        therapistZoomCard.setOnClickListener(v -> {
            Intent intent = new Intent(this, TherapistSessionActivity.class);
            startActivity(intent);
        });

        // Community Forum
        communityForumCard.setOnClickListener(v -> {
            Intent intent = new Intent(this, CommunityForumActivity.class);
            startActivity(intent);
        });
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.nav_home) {
            // Already on home
            Toast.makeText(this, "Home", Toast.LENGTH_SHORT).show();
        } else if (id == R.id.nav_profile) {
            Toast.makeText(this, "Profile - Coming Soon", Toast.LENGTH_SHORT).show();
        } else if (id == R.id.nav_settings) {
            Toast.makeText(this, "Settings - Coming Soon", Toast.LENGTH_SHORT).show();
        } else if (id == R.id.nav_logout) {
            handleLogout();
        }

        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    private void handleLogout() {
        prefsManager.clearSession();
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
        Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show();
    }
}
