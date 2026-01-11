package com.example.mindbloomandroid.utility;



import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;


public class DateTimeUtils {


    public static String formatDate(long timestamp) {
        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy", Locale. getDefault());
        return sdf.format(new Date(timestamp));
    }


    public static String formatDateTime(long timestamp) {
        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy hh:mm a", Locale.getDefault());
        return sdf.format(new Date(timestamp));
    }


    public static String formatTime(long timestamp) {
        SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a", Locale.getDefault());
        return sdf.format(new Date(timestamp));
    }


    public static String getRelativeTime(long timestamp) {
        long now = System. currentTimeMillis();
        long diff = now - timestamp;

        long seconds = TimeUnit.MILLISECONDS. toSeconds(diff);
        long minutes = TimeUnit.MILLISECONDS.toMinutes(diff);
        long hours = TimeUnit. MILLISECONDS.toHours(diff);
        long days = TimeUnit.MILLISECONDS.toDays(diff);

        if (seconds < 60) {
            return "Just now";
        } else if (minutes < 60) {
            return minutes + " min ago";
        } else if (hours < 24) {
            return hours + " hour" + (hours > 1 ?  "s" : "") + " ago";
        } else if (days < 7) {
            return days + " day" + (days > 1 ? "s" : "") + " ago";
        } else {
            return formatDate(timestamp);
        }
    }


    public static long getStartOfDay(long timestamp) {
        Date date = new Date(timestamp);
        date.setHours(0);
        date.setMinutes(0);
        date.setSeconds(0);
        return date. getTime();
    }


    public static long getEndOfDay(long timestamp) {
        Date date = new Date(timestamp);
        date.setHours(23);
        date.setMinutes(59);
        date.setSeconds(59);
        return date.getTime();
    }
}