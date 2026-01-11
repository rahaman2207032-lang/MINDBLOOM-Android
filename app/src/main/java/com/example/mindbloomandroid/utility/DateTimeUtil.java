package com.example.mindbloomandroid.utility;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

/**
 * DateTimeUtil - Centralized date/time formatting utilities
 * Ensures consistent date formatting across the entire app
 */
public class DateTimeUtil {

    // Standard date formats
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
    private static final SimpleDateFormat DATE_TIME_FORMAT = new SimpleDateFormat("MMM dd, yyyy 'at' h:mm a", Locale.getDefault());
    private static final SimpleDateFormat TIME_FORMAT = new SimpleDateFormat("h:mm a", Locale.getDefault());
    private static final SimpleDateFormat FULL_DATE_FORMAT = new SimpleDateFormat("EEEE, MMMM dd, yyyy", Locale.getDefault());
    private static final SimpleDateFormat SHORT_DATE_FORMAT = new SimpleDateFormat("MM/dd/yy", Locale.getDefault());

    /**
     * Format timestamp to date (e.g., "Jan 11, 2026")
     */
    public static String formatDate(long timestamp) {
        if (timestamp <= 0) return "N/A";
        return DATE_FORMAT.format(new Date(timestamp));
    }

    /**
     * Format timestamp to date and time (e.g., "Jan 11, 2026 at 2:30 PM")
     */
    public static String formatDateTime(long timestamp) {
        if (timestamp <= 0) return "N/A";
        return DATE_TIME_FORMAT.format(new Date(timestamp));
    }

    /**
     * Format timestamp to time only (e.g., "2:30 PM")
     */
    public static String formatTime(long timestamp) {
        if (timestamp <= 0) return "N/A";
        return TIME_FORMAT.format(new Date(timestamp));
    }

    /**
     * Format timestamp to full date (e.g., "Saturday, January 11, 2026")
     */
    public static String formatFullDate(long timestamp) {
        if (timestamp <= 0) return "N/A";
        return FULL_DATE_FORMAT.format(new Date(timestamp));
    }

    /**
     * Format timestamp to short date (e.g., "01/11/26")
     */
    public static String formatShortDate(long timestamp) {
        if (timestamp <= 0) return "N/A";
        return SHORT_DATE_FORMAT.format(new Date(timestamp));
    }

    /**
     * Format relative time (e.g., "5 minutes ago", "2 hours ago", "Yesterday")
     */
    public static String formatRelativeTime(long timestamp) {
        if (timestamp <= 0) return "N/A";

        long now = System.currentTimeMillis();
        long diff = now - timestamp;

        if (diff < 0) return "Just now";

        long seconds = TimeUnit.MILLISECONDS.toSeconds(diff);
        long minutes = TimeUnit.MILLISECONDS.toMinutes(diff);
        long hours = TimeUnit.MILLISECONDS.toHours(diff);
        long days = TimeUnit.MILLISECONDS.toDays(diff);

        if (seconds < 60) {
            return "Just now";
        } else if (minutes < 60) {
            return minutes + (minutes == 1 ? " minute ago" : " minutes ago");
        } else if (hours < 24) {
            return hours + (hours == 1 ? " hour ago" : " hours ago");
        } else if (days == 1) {
            return "Yesterday";
        } else if (days < 7) {
            return days + " days ago";
        } else if (days < 30) {
            long weeks = days / 7;
            return weeks + (weeks == 1 ? " week ago" : " weeks ago");
        } else if (days < 365) {
            long months = days / 30;
            return months + (months == 1 ? " month ago" : " months ago");
        } else {
            long years = days / 365;
            return years + (years == 1 ? " year ago" : " years ago");
        }
    }

    /**
     * Get current timestamp
     */
    public static long getCurrentTimestamp() {
        return System.currentTimeMillis();
    }

    /**
     * Check if timestamp is today
     */
    public static boolean isToday(long timestamp) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd", Locale.getDefault());
        String today = dateFormat.format(new Date());
        String dateToCheck = dateFormat.format(new Date(timestamp));
        return today.equals(dateToCheck);
    }

    /**
     * Check if timestamp is this week
     */
    public static boolean isThisWeek(long timestamp) {
        long now = System.currentTimeMillis();
        long diff = now - timestamp;
        long days = TimeUnit.MILLISECONDS.toDays(diff);
        return days < 7;
    }

    /**
     * Format duration in hours (e.g., "8.5 hours")
     */
    public static String formatDuration(double hours) {
        if (hours < 1) {
            int minutes = (int) (hours * 60);
            return minutes + " min";
        } else if (hours == (int) hours) {
            return (int) hours + " hrs";
        } else {
            return String.format(Locale.getDefault(), "%.1f hrs", hours);
        }
    }

    /**
     * Calculate sleep duration between two timestamps
     */
    public static double calculateSleepDuration(long bedtime, long waketime) {
        long duration = waketime - bedtime;
        return duration / (1000.0 * 60 * 60); // Convert to hours
    }

    /**
     * Format for database storage (ISO 8601 compatible)
     */
    public static String formatForDatabase(long timestamp) {
        SimpleDateFormat isoFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault());
        return isoFormat.format(new Date(timestamp));
    }
}

