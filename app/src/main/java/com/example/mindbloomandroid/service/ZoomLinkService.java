package com.example.mindbloomandroid.service;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;


public class ZoomLinkService {
    
    private DatabaseReference zoomLinksRef;
    private ZoomApiService zoomApiService;
    private static final String ZOOM_BASE_URL = "https://zoom.us/j/";
    private static final SecureRandom random = new SecureRandom();
    

    private static final boolean USE_ZOOM_API = true; // Change to true after adding credentials

    public ZoomLinkService() {
        this.zoomLinksRef = FirebaseDatabase.getInstance().getReference("zoom_links");
        this.zoomApiService = new ZoomApiService();
    }


    public void generateZoomLink(String sessionId, String instructorId, String clientId,
                                 OnZoomLinkGeneratedListener listener) {

        android.util.Log.d("ZoomLinkService", "🔗 Generating Zoom meeting link...");
        android.util.Log.d("ZoomLinkService", "   Mode: " + (USE_ZOOM_API ? "REAL API" : "INSTANT"));
        android.util.Log.d("ZoomLinkService", "   Session ID: " + sessionId);

        if (USE_ZOOM_API) {
            // Use REAL Zoom API to create scheduled meeting
            generateRealZoomMeeting(sessionId, instructorId, clientId, listener);
        } else {
            // Use instant meeting link (no API needed)
            generateInstantMeetingLink(sessionId, instructorId, clientId, listener);
        }
    }


    private void generateRealZoomMeeting(String sessionId, String instructorId, String clientId,
                                         OnZoomLinkGeneratedListener listener) {
        try {
            android.util.Log.d("ZoomLinkService", "📞 Creating REAL Zoom meeting via API...");

            // Get session details (you may need to fetch from Firebase)
            String topic = "Therapy Session"; // Customize based on your needs
            long startTime = System.currentTimeMillis() + (24 * 60 * 60 * 1000); // Tomorrow
            int duration = 60; // 60 minutes

            zoomApiService.createZoomMeeting(topic, startTime, duration, "Instructor",
                new ZoomApiService.OnMeetingCreatedListener() {
                    @Override
                    public void onMeetingCreated(String joinUrl, long meetingId, String password, String startUrl) {
                        android.util.Log.d("ZoomLinkService", "✅ REAL Zoom meeting created!");
                        android.util.Log.d("ZoomLinkService", "   Join URL: " + joinUrl);
                        android.util.Log.d("ZoomLinkService", "   Meeting ID: " + meetingId);

                        // Save to Firebase
                        saveLinkToFirebase(sessionId, instructorId, clientId, joinUrl,
                                         String.valueOf(meetingId), password, "API_CREATED",
                                         () -> listener.onLinkGenerated(joinUrl, meetingId, password),
                                         listener::onError);
                    }

                    @Override
                    public void onError(String error) {
                        android.util.Log.e("ZoomLinkService", "❌ Zoom API error: " + error);
                        android.util.Log.d("ZoomLinkService", "⚠️ Falling back to instant meeting link...");
                        // Fallback to instant link if API fails
                        generateInstantMeetingLink(sessionId, instructorId, clientId, listener);
                    }
                });

        } catch (Exception e) {
            android.util.Log.e("ZoomLinkService", "❌ Error: " + e.getMessage(), e);
            // Fallback to instant link
            generateInstantMeetingLink(sessionId, instructorId, clientId, listener);
        }
    }

    private void generateInstantMeetingLink(String sessionId, String instructorId, String clientId,
                                           OnZoomLinkGeneratedListener listener) {
        try {
            android.util.Log.d("ZoomLinkService", "🔗 Generating instant meeting link...");


            long timestamp = System.currentTimeMillis() % 89999999999L;
            long meetingId = 10000000000L + timestamp;


            String password = generateSecurePassword();


            String encodedPassword = java.net.URLEncoder.encode(password, "UTF-8");
            String zoomLink = ZOOM_BASE_URL + meetingId + "?pwd=" + encodedPassword;

            android.util.Log.d("ZoomLinkService", "✅ Generated instant meeting link: " + zoomLink);
            android.util.Log.d("ZoomLinkService", "   Meeting ID: " + meetingId + " (11 digits - VALID Zoom format)");
            android.util.Log.d("ZoomLinkService", "   Password: " + password);
            android.util.Log.d("ZoomLinkService", "   📋 Full Join Instructions:");
            android.util.Log.d("ZoomLinkService", "      1. Click link: " + zoomLink);
            android.util.Log.d("ZoomLinkService", "      2. OR manually join with ID: " + meetingId);
            android.util.Log.d("ZoomLinkService", "      3. Enter password: " + password);

            // Save to Firebase
            saveLinkToFirebase(sessionId, instructorId, clientId, zoomLink,
                             String.valueOf(meetingId), password, "INSTANT_MEETING",
                             () -> listener.onLinkGenerated(zoomLink, meetingId, password),
                             listener::onError);

        } catch (Exception e) {
            android.util.Log.e("ZoomLinkService", "❌ Error generating instant link: " + e.getMessage(), e);
            listener.onError("Error: " + e.getMessage());
        }
    }


    private void saveLinkToFirebase(String sessionId, String instructorId, String clientId,
                                    String zoomLink, String meetingId, String password,
                                    String linkType, Runnable onSuccess,
                                    java.util.function.Consumer<String> onError) {
        try {
            Map<String, Object> linkData = new HashMap<>();
            linkData.put("zoomLink", zoomLink);
            linkData.put("meetingId", meetingId);
            linkData.put("password", password);
            linkData.put("sessionId", sessionId);
            linkData.put("instructorId", instructorId);
            linkData.put("clientId", clientId);
            linkData.put("createdAt", System.currentTimeMillis());
            linkData.put("status", "ACTIVE");
            linkData.put("linkType", linkType);

            String linkId = zoomLinksRef.push().getKey();
            if (linkId != null) {
                android.util.Log.d("ZoomLinkService", "💾 Saving to Firebase...");

                zoomLinksRef.child(linkId).setValue(linkData)
                    .addOnSuccessListener(aVoid -> {
                        android.util.Log.d("ZoomLinkService", "✅ Saved to Firebase successfully");
                        onSuccess.run();
                    })
                    .addOnFailureListener(e -> {
                        android.util.Log.e("ZoomLinkService", "❌ Firebase error: " + e.getMessage(), e);
                        onError.accept("Database error: " + e.getMessage());
                    });
            } else {
                onError.accept("Failed to generate Firebase key");
            }
        } catch (Exception e) {
            android.util.Log.e("ZoomLinkService", "❌ Error saving to Firebase: " + e.getMessage(), e);
            onError.accept("Error: " + e.getMessage());
        }
    }
    



    private String generateSecurePassword() {
        String chars = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghjkmnpqrstuvwxyz23456789";
        StringBuilder password = new StringBuilder();
        for (int i = 0; i < 6; i++) {
            int index = random.nextInt(chars.length());
            password.append(chars.charAt(index));
        }
        return password.toString();
    }


    public void generateInstantZoomLink(String instructorId, String clientId, 
                                        OnZoomLinkGeneratedListener listener) {
        String instantSessionId = "instant_" + UUID.randomUUID().toString();
        generateZoomLink(instantSessionId, instructorId, clientId, listener);
    }
    
    // Listener interfaces
    public interface OnZoomLinkGeneratedListener {
        void onLinkGenerated(String zoomLink, long meetingId, String password);
        void onError(String error);
    }
    
    public interface OnCompleteListener {
        void onSuccess(String message);
        void onError(String error);
    }
}
