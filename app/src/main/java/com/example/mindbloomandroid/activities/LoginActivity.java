package com.example.mindbloomandroid.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.mindbloomandroid.R;
import com.example.mindbloomandroid.service.FirebaseAuthService;
import com.example.mindbloomandroid.utility.SharedPreferencesManager;
import com.google.android.material.textfield.TextInputEditText;

public class LoginActivity extends AppCompatActivity {
    private TextInputEditText usernameEditText, passwordEditText;
    private Button loginButton, backButton;
    private TextView signupTextView;
    private ProgressBar progressBar;
    private FirebaseAuthService authService;
    private SharedPreferencesManager prefsManager;
    private String selectedRole;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        selectedRole = getIntent().getStringExtra("SELECTED_ROLE");
        if (selectedRole == null) {
            selectedRole = "User"; // Default
        }

        authService = new FirebaseAuthService();
        prefsManager = SharedPreferencesManager.getInstance(this);

        usernameEditText = findViewById(R.id.username);
        passwordEditText = findViewById(R.id.password);
        loginButton = findViewById(R.id.loginButton);
        signupTextView = findViewById(R.id.Signup);
        backButton = findViewById(R.id.back);
        progressBar = findViewById(R.id.progressBar);



        loginButton.setOnClickListener(v -> handleLogin());
        signupTextView.setOnClickListener(v -> navigateToRegister());
        backButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            finish();
        });
    }

    private void handleLogin() {
        String username = usernameEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();

        android.util.Log.d("Login", "🔐 Login attempt - Username: " + username);
        android.util.Log.d("Login", "📋 Selected role from intent: " + selectedRole);

        if (TextUtils.isEmpty(username) || TextUtils.isEmpty(password)) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }


        showLoading(true);


        authService.loginUser(username, password, new FirebaseAuthService.OnAuthCompleteListener() {
            @Override
            public void onSuccess(String message) {
                android.util.Log.d("Login", "✅ Authentication successful");


                authService.fetchUserData(new FirebaseAuthService.OnUserDataFetchedListener() {
                    @Override
                    public void onSuccess(String userId, String fetchedUsername, String role) {
                        android.util.Log.d("Login", "✅ User data fetched:");
                        android.util.Log.d("Login", "   UserID: " + userId);
                        android.util.Log.d("Login", "   Username: " + fetchedUsername);
                        android.util.Log.d("Login", "   Role from database: " + role);


                        prefsManager.saveUserSession(userId, fetchedUsername, role);


                        String savedRole = prefsManager.getRole();
                        android.util.Log.d("Login", "✅ Saved to SharedPreferences - Role: " + savedRole);

                        showLoading(false);
                        Toast.makeText(LoginActivity.this, "Welcome back, " + fetchedUsername + "!", Toast.LENGTH_SHORT).show();


                        android.util.Log.d("Login", "🚀 Navigating to dashboard based on role: " + role);
                        navigateToDashboardByRole(role);
                    }

                    @Override
                    public void onError(String error) {
                        android.util.Log.e("Login", "❌ Error fetching user data: " + error);
                        showLoading(false);
                        Toast.makeText(LoginActivity.this, "Error fetching user data: " + error, Toast.LENGTH_LONG).show();

                        authService.logout();
                    }
                });
            }

            @Override
            public void onError(String error) {
                android.util.Log.e("Login", "❌ Authentication failed: " + error);
                showLoading(false);
                Toast.makeText(LoginActivity.this, "Login Error: " + error, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void navigateToDashboard() {
        // Get role from SharedPreferences
        String role = prefsManager.getRole();
        navigateToDashboardByRole(role);
    }

    private void navigateToDashboardByRole(String role) {
        android.util.Log.d("Login", "📍 navigateToDashboardByRole called");
        android.util.Log.d("Login", "   Input role: '" + role + "'");
        android.util.Log.d("Login", "   Role equalsIgnoreCase 'Instructor': " + "Instructor".equalsIgnoreCase(role));

        Intent intent;
        if ("Instructor".equalsIgnoreCase(role)) {
            android.util.Log.d("Login", "🎯 Opening InstructorDashboardActivity");
            intent = new Intent(this, InstructorDashboardActivity.class);
        } else {
            android.util.Log.d("Login", "🎯 Opening UserDashboardActivity (role: " + role + ")");
            intent = new Intent(this, UserDashboardActivity.class);
        }

        android.util.Log.d("Login", "✅ Starting activity: " + intent.getComponent().getClassName());
        startActivity(intent);
        finish();
    }

    private void navigateToRegister() {
        Intent intent = new Intent(this, RegisterActivity.class);
        intent.putExtra("SELECTED_ROLE", selectedRole);
        startActivity(intent);
    }

    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        loginButton.setEnabled(!show);
        signupTextView.setEnabled(!show);
        backButton.setEnabled(!show);
    }
}
