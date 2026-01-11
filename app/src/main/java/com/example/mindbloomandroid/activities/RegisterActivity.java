package com.example.mindbloomandroid.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.mindbloomandroid.R;
import com.example.mindbloomandroid.service.FirebaseAuthService;
import com.example.mindbloomandroid.utility.SharedPreferencesManager;
import com.google.android.material.textfield.TextInputEditText;

public class RegisterActivity extends AppCompatActivity {
    private EditText usernameEditText;
    private TextInputEditText passwordEditText, confirmPasswordEditText;
    private Button registerButton;
    private TextView roleTextView, backToLogin;
    private ProgressBar progressBar;
    private FirebaseAuthService authService;
    private SharedPreferencesManager prefsManager;
    private String selectedRole;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        selectedRole = getIntent().getStringExtra("SELECTED_ROLE");
        if (selectedRole == null) {
            selectedRole = "User";
        }

        authService = new FirebaseAuthService();
        prefsManager = SharedPreferencesManager.getInstance(this);

        usernameEditText = findViewById(R.id.usernameEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        confirmPasswordEditText = findViewById(R.id.confirmPasswordEditText);
        registerButton = findViewById(R.id.registerButton);
        roleTextView = findViewById(R.id.roleTextView);
        progressBar = findViewById(R.id.progressBar);
        backToLogin = findViewById(R.id.backToLogin);

        roleTextView.setText("Register as " + selectedRole);

        registerButton.setOnClickListener(v -> handleRegister());
        backToLogin.setOnClickListener(v -> {
            Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
            intent.putExtra("SELECTED_ROLE", selectedRole);
            startActivity(intent);
            finish();
        });
    }

    private void handleRegister() {
        String username = usernameEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();
        String confirmPassword = confirmPasswordEditText.getText().toString().trim();


        if (username.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        if (password.length() < 6) {
            Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!password.equals(confirmPassword)) {
            Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show();
            return;
        }

        showLoading(true);

        authService.registerUser(password, username, selectedRole,
                new FirebaseAuthService.OnAuthCompleteListener() {
                    @Override
                    public void onSuccess(String message) {

                        authService.fetchUserData(new FirebaseAuthService.OnUserDataFetchedListener() {
                            @Override
                            public void onSuccess(String userId, String fetchedUsername, String role) {

                                prefsManager.saveUserSession(userId, fetchedUsername, role);

                                showLoading(false);
                                Toast.makeText(RegisterActivity.this,
                                        "Welcome, " + fetchedUsername + "!",
                                        Toast.LENGTH_SHORT).show();


                                navigateToDashboardByRole(role);
                            }

                            @Override
                            public void onError(String error) {
                                showLoading(false);
                                Toast.makeText(RegisterActivity.this,
                                        "Registered but error: " + error,
                                        Toast.LENGTH_LONG).show();


                                Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                                intent.putExtra("SELECTED_ROLE", selectedRole);
                                startActivity(intent);
                                finish();
                            }
                        });
                    }

                    @Override
                    public void onError(String error) {
                        showLoading(false);
                        Toast.makeText(RegisterActivity.this, "Error: " + error, Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void navigateToDashboardByRole(String role) {
        Intent intent;
        if ("Instructor".equalsIgnoreCase(role)) {
            intent = new Intent(this, InstructorDashboardActivity.class);
        } else {
            intent = new Intent(this, UserDashboardActivity.class);
        }
        startActivity(intent);
        finish();
    }

    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        registerButton.setEnabled(!show);
    }
}