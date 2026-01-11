package com.example.mindbloomandroid.fragments;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mindbloomandroid.R;
import com.example.mindbloomandroid.adapter.InstructorRequestAdapter;
import com.example.mindbloomandroid.model.SessionRequest;
import com.example.mindbloomandroid.model.TherapySession;
import com.example.mindbloomandroid.service.NotificationService;
import com.example.mindbloomandroid.service.SessionRequestService;
import com.example.mindbloomandroid.service.TherapySessionService;
import com.example.mindbloomandroid.service.ZoomLinkService;
import com.example.mindbloomandroid.utility.SharedPreferencesManager;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.List;


public class SessionRequestsFragment extends Fragment {

    private RecyclerView requestsRecyclerView;
    private TextView emptyRequestsText;
    private ProgressBar progressBar;
    
    private InstructorRequestAdapter requestsAdapter;
    private List<SessionRequest> sessionRequests;
    
    private SessionRequestService sessionRequestService;
    private TherapySessionService therapySessionService;
    private NotificationService notificationService;
    private ZoomLinkService zoomLinkService;
    private SharedPreferencesManager prefsManager;
    
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_session_requests, container, false);
        
        prefsManager = SharedPreferencesManager.getInstance(requireContext());
        sessionRequestService = new SessionRequestService();
        therapySessionService = new TherapySessionService();
        notificationService = new NotificationService();
        zoomLinkService = new ZoomLinkService();
        
        initializeViews(view);
        loadPendingRequests();
        
        return view;
    }
    
    private void initializeViews(View view) {
        requestsRecyclerView = view.findViewById(R.id.requestsRecyclerView);
        emptyRequestsText = view.findViewById(R.id.emptyRequestsText);
        progressBar = view.findViewById(R.id.progressBar);
        
        sessionRequests = new ArrayList<>();
        requestsAdapter = new InstructorRequestAdapter(requireContext(), sessionRequests,
                this::handleAcceptRequest,
                this::handleDeclineRequest);
        
        requestsRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        requestsRecyclerView.setAdapter(requestsAdapter);
    }
    
    private void loadPendingRequests() {
        String instructorId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        android.util.Log.d("SessionRequestsFrag", "📖 Loading pending requests for instructor: " + instructorId);

        progressBar.setVisibility(View.VISIBLE);
        
        sessionRequestService.getInstructorPendingRequests(instructorId,
                new SessionRequestService.OnRequestsLoadedListener() {
                    @Override
                    public void onRequestsLoaded(List<SessionRequest> requests) {
                        android.util.Log.d("SessionRequestsFrag", "✅ Received " + requests.size() + " pending requests");

                        progressBar.setVisibility(View.GONE);
                        sessionRequests.clear();
                        sessionRequests.addAll(requests);
                        requestsAdapter.notifyDataSetChanged();
                        
                        if (sessionRequests.isEmpty()) {
                            android.util.Log.d("SessionRequestsFrag", "ℹ️ No pending requests");
                            emptyRequestsText.setVisibility(View.VISIBLE);
                            requestsRecyclerView.setVisibility(View.GONE);
                        } else {
                            android.util.Log.d("SessionRequestsFrag", "✅ Displaying " + sessionRequests.size() + " requests");
                            emptyRequestsText.setVisibility(View.GONE);
                            requestsRecyclerView.setVisibility(View.VISIBLE);
                        }
                    }
                    
                    @Override
                    public void onError(String error) {
                        android.util.Log.e("SessionRequestsFrag", "❌ Error loading requests: " + error);
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(requireContext(), "Error loading requests: " + error,
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }
    
    private void handleAcceptRequest(SessionRequest request) {
        // Auto-generate Zoom link for the session
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Accept Session Request");
        builder.setMessage("Accept this session? A unique Zoom meeting link will be automatically generated and sent to the client.");
        
        builder.setPositiveButton("Accept & Generate Link", (dialog, which) -> {
            progressBar.setVisibility(View.VISIBLE);
            
            // Generate unique Zoom link
            String instructorId = prefsManager.getUserId();
            String tempSessionId = "temp_" + System.currentTimeMillis(); // Temporary ID until session is created
            
            zoomLinkService.generateZoomLink(tempSessionId, instructorId, request.getUserId(),
                    new ZoomLinkService.OnZoomLinkGeneratedListener() {
                        @Override
                        public void onLinkGenerated(String zoomLink, long meetingId, String password) {
                            // Zoom link generated successfully, now update request
                            updateRequestWithZoomLink(request, zoomLink);
                        }

                        @Override
                        public void onError(String error) {
                            progressBar.setVisibility(View.GONE);
                            Toast.makeText(requireContext(), 
                                "Failed to generate Zoom link: " + error, 
                                Toast.LENGTH_SHORT).show();
                        }
                    });
        });
        
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }
    
    private void updateRequestWithZoomLink(SessionRequest request, String zoomLink) {
        // Update request status to CONFIRMED with zoom link
        sessionRequestService.updateRequestStatus(request.getRequestId(), "CONFIRMED", zoomLink,
                    new SessionRequestService.OnCompleteListener() {
                        @Override
                        public void onSuccess() {
                            // Create TherapySession
                            TherapySession session = new TherapySession(
                                    request.getUserId(),
                                    request.getClientName(),
                                    request.getInstructorId(),
                                    request.getRequestedDateTime(),
                                    request.getSessionType(),
                                    zoomLink
                            );
                            session.setInstructorName(prefsManager.getUsername());
                            session.setStatus("SCHEDULED");
                            
                            therapySessionService.createTherapySession(session,
                                    new TherapySessionService.OnCompleteListener() {
                                        @Override
                                        public void onSuccess() {
                                            // Send notification to user with session details
                                            sendSessionConfirmationNotification(request, zoomLink, session.getSessionId());

                                            progressBar.setVisibility(View.GONE);
                                            Toast.makeText(requireContext(),
                                                    "Session confirmed! Zoom link sent to client.",
                                                    Toast.LENGTH_LONG).show();
                                            
                                            loadPendingRequests(); // Refresh list
                                        }
                                        
                                        @Override
                                        public void onError(String error) {
                                            progressBar.setVisibility(View.GONE);
                                            Toast.makeText(requireContext(),
                                                    "Error creating session: " + error,
                                                    Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        }
                        
                        @Override
                        public void onError(String error) {
                            progressBar.setVisibility(View.GONE);
                            Toast.makeText(requireContext(), "Error accepting request: " + error,
                                    Toast.LENGTH_SHORT).show();
                        }
                    });
    }
    
    private void handleDeclineRequest(SessionRequest request) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Decline Session Request");
        builder.setMessage("Are you sure you want to decline this session request from " +
                request.getClientName() + "?");
        
        builder.setPositiveButton("Yes, Decline", (dialog, which) -> {
            progressBar.setVisibility(View.VISIBLE);
            
            sessionRequestService.updateRequestStatus(request.getRequestId(), "REJECTED", null,
                    new SessionRequestService.OnCompleteListener() {
                        @Override
                        public void onSuccess() {
                            // Send rejection notification
                            sendSessionRejectionNotification(request);
                            
                            progressBar.setVisibility(View.GONE);
                            Toast.makeText(requireContext(), "Session request declined",
                                    Toast.LENGTH_SHORT).show();
                            
                            loadPendingRequests(); // Refresh list
                        }
                        
                        @Override
                        public void onError(String error) {
                            progressBar.setVisibility(View.GONE);
                            Toast.makeText(requireContext(), "Error declining request: " + error,
                                    Toast.LENGTH_SHORT).show();
                        }
                    });
        });
        
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }
    
    private void sendSessionConfirmationNotification(SessionRequest request, String zoomLink, String sessionId) {
        com.example.mindbloomandroid.model.Notification notification = new com.example.mindbloomandroid.model.Notification();
        notification.setUserId(request.getUserId());
        notification.setType("SESSION_CONFIRMED");
        notification.setTitle("✅ Session Confirmed!");
        notification.setMessage("Your therapy session with " + prefsManager.getUsername() +
                " has been confirmed.\n\n📅 Date: " + request.getFormattedRequestedDateTime() +
                "\n🎥 Zoom Link: " + zoomLink +
                "\n\nClick the notification to view session details and join.");
        notification.setRead(false);
        notification.setCreatedAt(System.currentTimeMillis());
        notification.setRelatedEntityId(sessionId); // Store session ID so user can retrieve full session details with zoom link

        notificationService.createNotification(notification, new NotificationService.OnCompleteListener() {
            @Override
            public void onSuccess() {
                // Notification sent successfully
            }
            
            @Override
            public void onError(String error) {
                // Silent fail - don't block the main flow
            }
        });
    }
    
    private void sendSessionRejectionNotification(SessionRequest request) {
        com.example.mindbloomandroid.model.Notification notification = new com.example.mindbloomandroid.model.Notification();
        notification.setUserId(request.getUserId());
        notification.setType("SESSION_REJECTED");
        notification.setTitle("Session Request Update");
        notification.setMessage("Your therapy session request has been declined by the instructor. Please try booking another time.");
        notification.setRead(false);
        notification.setCreatedAt(System.currentTimeMillis());
        
        notificationService.createNotification(notification, new NotificationService.OnCompleteListener() {
            @Override
            public void onSuccess() {
                // Notification sent successfully
            }
            
            @Override
            public void onError(String error) {
                // Silent fail
            }
        });
    }
    
    @Override
    public void onResume() {
        super.onResume();
        loadPendingRequests(); // Refresh when fragment becomes visible
    }
}
