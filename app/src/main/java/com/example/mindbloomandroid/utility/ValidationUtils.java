package com.example.mindbloomandroid.utility;



import android.util.Patterns;


public class ValidationUtils {


    public static boolean isValidEmail(String email) {
        return email != null && Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    public static boolean isValidPassword(String password) {
        return password != null && password.length() >= 6;
    }


    public static boolean isValidUsername(String username) {
        return username != null && username. length() >= 3 && username.length() <= 20;
    }


    public static boolean isEmpty(String text) {
        return text == null || text.trim().isEmpty();
    }
}