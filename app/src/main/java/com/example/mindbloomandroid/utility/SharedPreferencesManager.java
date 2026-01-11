package com.example.mindbloomandroid.utility;



import android.content.Context;
import android.content.SharedPreferences;


public class SharedPreferencesManager {
    private static final String PREF_NAME = "MindbloomPrefs";
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_USERNAME = "username";
    // KEY_EMAIL removed - email not stored anymore
    private static final String KEY_ROLE = "role";
    private static final String KEY_IS_LOGGED_IN = "is_logged_in";

    private static SharedPreferencesManager instance;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;

    private SharedPreferencesManager(Context context) {
        sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
    }

    public static synchronized SharedPreferencesManager getInstance(Context context) {
        if (instance == null) {
            instance = new SharedPreferencesManager(context.getApplicationContext());
        }
        return instance;
    }


    public void saveUserSession(String userId, String username, String role) {
        editor.putString(KEY_USER_ID, userId);
        editor.putString(KEY_USERNAME, username);
        editor.putString(KEY_ROLE, role);
        editor.putBoolean(KEY_IS_LOGGED_IN, true);
        editor.apply();
    }


    @Deprecated
    public void saveUserSession(String userId, String username, String email, String role) {
        // Ignore email parameter, just save other fields
        saveUserSession(userId, username, role);
    }

    public String getUserId() {
        return sharedPreferences.getString(KEY_USER_ID, null);
    }


    public String getUsername() {
        return sharedPreferences.getString(KEY_USERNAME, null);
    }


    public String getRole() {
        return sharedPreferences.getString(KEY_ROLE, "User");
    }


    public boolean isLoggedIn() {
        return sharedPreferences.getBoolean(KEY_IS_LOGGED_IN, false);
    }


    public boolean isInstructor() {
        return "Instructor".equalsIgnoreCase(getRole());
    }


    public void clearSession() {
        editor.clear();
        editor.apply();
    }
}