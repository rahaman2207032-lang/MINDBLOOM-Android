package com.example.mindbloomandroid.service;



import com.google.firebase. database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google. firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.example.mindbloomandroid.model.StressAssessment;

import java.util.ArrayList;
import java.util.List;

public class StressService {
    private DatabaseReference stressRef;

    public StressService() {
        stressRef = FirebaseDatabase.getInstance().getReference("stress_assessments");
    }


    public void saveStressAssessment(StressAssessment assessment, OnCompleteListener listener) {
        String assessmentId = stressRef.push().getKey();
        if (assessmentId != null) {
            assessment.setAssessmentId(assessmentId);
            assessment.setAssessmentDate(System.currentTimeMillis());
            assessment.calculateStressScore();

            android.util.Log.d("StressService", "📝 Saving stress assessment:");
            android.util.Log.d("StressService", "   ID: " + assessmentId);
            android.util.Log.d("StressService", "   UserID: " + assessment.getUserId());
            android.util.Log.d("StressService", "   Score: " + assessment.getStressScore());
            android.util.Log.d("StressService", "   Level: " + assessment.getStressLevel());
            android.util.Log.d("StressService", "   Date: " + assessment.getAssessmentDate());

            stressRef.child(assessmentId).setValue(assessment)
                    .addOnSuccessListener(aVoid -> {
                        android.util.Log.d("StressService", "✅ Stress assessment saved successfully");
                        android.util.Log.d("StressService", "📍 Saved at path: stress_assessments/" + assessmentId);
                        android.util.Log.d("StressService", "💡 To load: query stress_assessments where userId='" + assessment.getUserId() + "'");
                        listener.onSuccess();
                    })
                    .addOnFailureListener(e -> {
                        android.util.Log.e("StressService", "❌ Failed to save stress assessment: " + e.getMessage());
                        listener.onError(e.getMessage());
                    });
        } else {
            android.util.Log.e("StressService", "❌ Failed to generate assessment ID");
            listener.onError("Failed to generate assessment ID");
        }
    }


    public void getUserStressAssessments(String userId, OnAssessmentsLoadedListener listener) {
        android.util.Log.d("StressService", "📖 Loading stress assessments for user: " + userId);
        android.util.Log.d("StressService", "📍 Querying: stress_assessments.orderByChild('userId').equalTo('" + userId + "')");

        // Use addValueEventListener for REAL-TIME updates (not addListenerForSingleValueEvent)
        stressRef.orderByChild("userId").equalTo(userId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        List<StressAssessment> assessments = new ArrayList<>();
                        android.util.Log.d("StressService", "📥 Received " + dataSnapshot.getChildrenCount() + " assessments from Firebase (REAL-TIME)");

                        int successCount = 0;
                        int nullCount = 0;

                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            android.util.Log.d("StressService", "   🔍 Processing assessment ID: " + snapshot.getKey());

                            StressAssessment assessment = snapshot.getValue(StressAssessment.class);
                            if (assessment != null) {
                                assessment.setAssessmentId(snapshot.getKey());
                                assessments.add(assessment);
                                successCount++;
                                android.util.Log.d("StressService", "   ✅ Loaded: Score=" + assessment.getStressScore() + ", Level=" + assessment.getStressLevel());
                            } else {
                                nullCount++;
                                android.util.Log.w("StressService", "   ⚠️ Null assessment at: " + snapshot.getKey());
                            }
                        }

                        android.util.Log.d("StressService", "✅ Successfully loaded " + successCount + " stress assessments (REAL-TIME UPDATE)");
                        if (nullCount > 0) {
                            android.util.Log.w("StressService", "⚠️ " + nullCount + " entries were null (data structure issue?)");
                        }

                        if (assessments.isEmpty()) {
                            android.util.Log.i("StressService", "ℹ️ No stress assessments found for userId: " + userId);
                            android.util.Log.i("StressService", "💡 Check Firebase Console: Does stress_assessments have entries with userId='" + userId + "'?");
                        }

                        listener.onAssessmentsLoaded(assessments);
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                        android.util.Log.e("StressService", "❌ Error loading stress assessments: " + error.getMessage());
                        android.util.Log.e("StressService", "❌ Error code: " + error.getCode());
                        android.util.Log.e("StressService", "❌ Error details: " + error.getDetails());
                        listener.onError(error.getMessage());
                    }
                });
    }


    public void getLatestAssessment(String userId, OnAssessmentLoadedListener listener) {
        stressRef.orderByChild("userId").equalTo(userId)
                .limitToLast(1)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            StressAssessment assessment = snapshot.getValue(StressAssessment.class);
                            if (assessment != null) {
                                assessment.setAssessmentId(snapshot.getKey());
                                listener.onAssessmentLoaded(assessment);
                                return;
                            }
                        }
                        listener.onError("No assessment found");
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                        listener.onError(error.getMessage());
                    }
                });
    }


    public String getCopingSuggestion(String stressLevel) {
        switch (stressLevel) {
            case "LOW":
                return "Great job! Your stress levels are low. Keep up the good habits:\n\n" +
                        "• Continue your current routines\n" +
                        "• Practice gratitude daily\n" +
                        "• Maintain work-life balance\n" +
                        "• Stay connected with friends";

            case "MODERATE":
                return "Your stress is at a moderate level. Try these strategies:\n\n" +
                        "• Take short breaks every hour\n" +
                        "• Practice deep breathing exercises\n" +
                        "• Get at least 7-8 hours of sleep\n" +
                        "• Exercise for 30 minutes daily\n" +
                        "• Talk to someone you trust";

            case "HIGH":
                return "Your stress levels are high. Consider these urgent actions:\n\n" +
                        "• Schedule a session with your therapist\n" +
                        "• Practice mindfulness meditation\n" +
                        "• Avoid caffeine and alcohol\n" +
                        "• Reach out to support groups\n" +
                        "• Consider professional help immediately";

            default:
                return "Complete the assessment to get personalized suggestions. ";
        }
    }
    
    public void deleteStressAssessment(String assessmentId, OnCompleteListener listener) {
        if (assessmentId == null || assessmentId.isEmpty()) {
            listener.onError("Invalid assessment ID");
            return;
        }
        
        stressRef.child(assessmentId).removeValue()
            .addOnSuccessListener(aVoid -> listener.onSuccess())
            .addOnFailureListener(e -> listener.onError(e.getMessage()));
    }

    // Interfaces
    public interface OnCompleteListener {
        void onSuccess();
        void onError(String error);
    }

    public interface OnAssessmentLoadedListener {
        void onAssessmentLoaded(StressAssessment assessment);
        void onError(String error);
    }

    public interface OnAssessmentsLoadedListener {
        void onAssessmentsLoaded(List<StressAssessment> assessments);
        void onError(String error);
    }

    // Alias for compatibility
    public interface OnStressAssessmentsLoadedListener extends OnAssessmentsLoadedListener {}
}