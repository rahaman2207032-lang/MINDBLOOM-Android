package com.example.mindbloomandroid.service;

import android.util.Base64;
import android.util.Log;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;


public class ZoomApiService {

    private static final String TAG = "ZoomApiService";


    private static final String ZOOM_ACCOUNT_ID = System.getenv("zoomid");
    private static final String ZOOM_CLIENT_ID = System.getenv("clientid");
    private static final String ZOOM_CLIENT_SECRET = System.getenv("clientsecret");


    private static final String ZOOM_OAUTH_TOKEN_URL = "https://zoom.us/oauth/token?grant_type=account_credentials&account_id=" + ZOOM_ACCOUNT_ID;
    private static final String ZOOM_CREATE_MEETING_URL = "https://api.zoom.us/v2/users/me/meetings";

    private final ExecutorService executorService;
    private String accessToken;
    private long tokenExpiryTime;

    public ZoomApiService() {
        this.executorService = Executors.newSingleThreadExecutor();


        if (ZOOM_ACCOUNT_ID == null || ZOOM_ACCOUNT_ID.isEmpty()) {
            Log.e(TAG, "❌ ZOOM_ACCOUNT_ID environment variable not set!");
            Log.e(TAG, "   Set it in system environment variables or Android Studio Run Configuration");
        }
        if (ZOOM_CLIENT_ID == null || ZOOM_CLIENT_ID.isEmpty()) {
            Log.e(TAG, "❌ ZOOM_CLIENT_ID environment variable not set!");
        }
        if (ZOOM_CLIENT_SECRET == null || ZOOM_CLIENT_SECRET.isEmpty()) {
            Log.e(TAG, "❌ ZOOM_CLIENT_SECRET environment variable not set!");
        }

        if (ZOOM_ACCOUNT_ID != null && ZOOM_CLIENT_ID != null && ZOOM_CLIENT_SECRET != null) {
            Log.d(TAG, "✅ Zoom credentials loaded from environment variables");
            Log.d(TAG, "   Account ID: " + ZOOM_ACCOUNT_ID.substring(0, Math.min(4, ZOOM_ACCOUNT_ID.length())) + "...");
        }
    }


    public void createZoomMeeting(String topic, long startTime, int durationMinutes,
                                   String instructorName, OnMeetingCreatedListener listener) {

        executorService.execute(() -> {
            try {
                Log.d(TAG, "🎯 Creating REAL Zoom meeting via API...");
                Log.d(TAG, "   Topic: " + topic);
                Log.d(TAG, "   Instructor: " + instructorName);
                Log.d(TAG, "   Duration: " + durationMinutes + " minutes");

                // Step 1: Get OAuth access token (if expired)
                if (accessToken == null || System.currentTimeMillis() >= tokenExpiryTime) {
                    if (!getAccessToken()) {
                        listener.onError("Failed to authenticate with Zoom API");
                        return;
                    }
                }

                // Step 2: Create meeting via Zoom API
                JSONObject meetingDetails = createMeetingViaApi(topic, startTime, durationMinutes);

                if (meetingDetails == null) {
                    listener.onError("Failed to create Zoom meeting");
                    return;
                }

                // Extract meeting details
                String joinUrl = meetingDetails.getString("join_url");
                long meetingId = meetingDetails.getLong("id");
                String password = meetingDetails.optString("password", "");
                String startUrl = meetingDetails.getString("start_url");

                Log.d(TAG, "✅ REAL Zoom meeting created successfully!");
                Log.d(TAG, "   Meeting ID: " + meetingId);
                Log.d(TAG, "   Join URL: " + joinUrl);
                Log.d(TAG, "   Password: " + password);

                listener.onMeetingCreated(joinUrl, meetingId, password, startUrl);

            } catch (Exception e) {
                Log.e(TAG, "❌ Error creating Zoom meeting: " + e.getMessage(), e);
                listener.onError("Error: " + e.getMessage());
            }
        });
    }


    private boolean getAccessToken() {
        try {
            Log.d(TAG, "🔐 Getting Zoom OAuth token...");

            URL url = new URL(ZOOM_OAUTH_TOKEN_URL);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");

            // Add Basic Auth header
            String auth = ZOOM_CLIENT_ID + ":" + ZOOM_CLIENT_SECRET;
            String encodedAuth = Base64.encodeToString(auth.getBytes(StandardCharsets.UTF_8), Base64.NO_WRAP);
            conn.setRequestProperty("Authorization", "Basic " + encodedAuth);
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

            int responseCode = conn.getResponseCode();

            if (responseCode == 200) {
                BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = in.readLine()) != null) {
                    response.append(line);
                }
                in.close();

                JSONObject jsonResponse = new JSONObject(response.toString());
                accessToken = jsonResponse.getString("access_token");
                int expiresIn = jsonResponse.getInt("expires_in");
                tokenExpiryTime = System.currentTimeMillis() + (expiresIn * 1000);

                Log.d(TAG, "✅ OAuth token obtained successfully");
                return true;
            } else {
                Log.e(TAG, "❌ Failed to get OAuth token. Response code: " + responseCode);
                return false;
            }

        } catch (Exception e) {
            Log.e(TAG, "❌ Error getting OAuth token: " + e.getMessage(), e);
            return false;
        }
    }


    private JSONObject createMeetingViaApi(String topic, long startTime, int durationMinutes) {
        try {
            Log.d(TAG, "📞 Calling Zoom API to create meeting...");

            URL url = new URL(ZOOM_CREATE_MEETING_URL);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Authorization", "Bearer " + accessToken);
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);

            // Create meeting request body
            JSONObject meetingSettings = new JSONObject();
            meetingSettings.put("host_video", true);
            meetingSettings.put("participant_video", true);
            meetingSettings.put("join_before_host", false);
            meetingSettings.put("mute_upon_entry", false);
            meetingSettings.put("waiting_room", false);
            meetingSettings.put("audio", "both");

            JSONObject meetingRequest = new JSONObject();
            meetingRequest.put("topic", topic);
            meetingRequest.put("type", 2); // Scheduled meeting
            meetingRequest.put("start_time", formatZoomDateTime(startTime));
            meetingRequest.put("duration", durationMinutes);
            meetingRequest.put("timezone", "America/Los_Angeles"); // Adjust as needed
            meetingRequest.put("settings", meetingSettings);

            // Send request
            OutputStream os = conn.getOutputStream();
            os.write(meetingRequest.toString().getBytes(StandardCharsets.UTF_8));
            os.flush();
            os.close();

            int responseCode = conn.getResponseCode();

            if (responseCode == 201) {
                BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = in.readLine()) != null) {
                    response.append(line);
                }
                in.close();

                JSONObject meetingDetails = new JSONObject(response.toString());
                Log.d(TAG, "✅ Zoom meeting created via API!");
                return meetingDetails;
            } else {
                BufferedReader in = new BufferedReader(new InputStreamReader(conn.getErrorStream()));
                StringBuilder errorResponse = new StringBuilder();
                String line;
                while ((line = in.readLine()) != null) {
                    errorResponse.append(line);
                }
                in.close();

                Log.e(TAG, "❌ Failed to create meeting. Response code: " + responseCode);
                Log.e(TAG, "   Error: " + errorResponse.toString());
                return null;
            }

        } catch (Exception e) {
            Log.e(TAG, "❌ Error calling Zoom API: " + e.getMessage(), e);
            return null;
        }
    }


    private String formatZoomDateTime(long timestamp) {
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", java.util.Locale.US);
        sdf.setTimeZone(java.util.TimeZone.getTimeZone("America/Los_Angeles"));
        return sdf.format(new java.util.Date(timestamp));
    }


    public interface OnMeetingCreatedListener {
        void onMeetingCreated(String joinUrl, long meetingId, String password, String startUrl);
        void onError(String error);
    }
}

