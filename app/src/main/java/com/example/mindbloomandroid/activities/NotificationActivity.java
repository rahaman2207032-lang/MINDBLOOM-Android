package com.example.mindbloomandroid.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mindbloomandroid.R;
import com.example.mindbloomandroid.adapter.NotificationAdapter;
import com.example.mindbloomandroid.model.NotificationData;
import com.example.mindbloomandroid.service.NotificationService;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


public class NotificationActivity extends AppCompatActivity {
    
    private RecyclerView notificationsRecyclerView;
    private NotificationAdapter notificationAdapter;
    private ProgressBar progressBar;
    private TextView emptyStateText;
    

    private Button allNotificationsBtn;
    private Button messagesBtn;
    private Button sessionsBtn;
    private Button systemBtn;
    
    private NotificationService notificationService;
    private List<NotificationData> allNotifications;
    private String currentFilter = "ALL";
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification);
        

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("🔔 Notifications");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        
        initializeViews();
        setupRecyclerView();
        
        notificationService = new NotificationService();
        loadNotifications();
    }
    
    private void initializeViews() {
        notificationsRecyclerView = findViewById(R.id.notificationsRecyclerView);
        progressBar = findViewById(R.id.progressBar);
        emptyStateText = findViewById(R.id.emptyStateText);
        
        allNotificationsBtn = findViewById(R.id.allNotificationsBtn);
        messagesBtn = findViewById(R.id.messagesBtn);
        sessionsBtn = findViewById(R.id.sessionsBtn);
        systemBtn = findViewById(R.id.systemBtn);
        

        allNotificationsBtn.setOnClickListener(v -> showAllNotifications());
        messagesBtn.setOnClickListener(v -> showMessages());
        sessionsBtn.setOnClickListener(v -> showSessions());
        systemBtn.setOnClickListener(v -> showSystem());
        

        findViewById(R.id.markAllReadBtn).setOnClickListener(v -> handleMarkAllRead());
        

        findViewById(R.id.refreshBtn).setOnClickListener(v -> handleRefresh());
        
        updateFilterButtons();
    }
    
    private void setupRecyclerView() {
        notificationsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        allNotifications = new ArrayList<>();
        
        notificationAdapter = new NotificationAdapter(this, allNotifications, 
            new NotificationAdapter.OnNotificationActionListener() {
                @Override
                public void onMarkAsRead(NotificationData notification) {
                    markAsRead(notification);
                }
                
                @Override
                public void onJoinMeeting(String zoomLink) {
                    openZoomLink(zoomLink);
                }
                
                @Override
                public void onReply(String senderId, String senderName) {
                    openReplyDialog(senderId, senderName);
                }
            });
        
        notificationsRecyclerView.setAdapter(notificationAdapter);
    }
    
    private void loadNotifications() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        
        String userId = currentUser.getUid();
        android.util.Log.d("NotificationActivity", "📖 Loading notifications for user: " + userId);
        progressBar.setVisibility(View.VISIBLE);
        

        notificationService.getUserNotifications(userId,
            new NotificationService.OnNotificationsLoadedListener() {
                @Override
                public void onNotificationsLoaded(List<com.example.mindbloomandroid.model.Notification> notifications) {
                    android.util.Log.d("NotificationActivity", "✅ Loaded " + notifications.size() + " notifications");
                    progressBar.setVisibility(View.GONE);


                    allNotifications.clear();
                    for (com.example.mindbloomandroid.model.Notification notif : notifications) {
                        NotificationData data = new NotificationData();
                        data.setNotificationId(notif.getNotificationId());
                        data.setNotificationType(notif.getType());
                        data.setTitle(notif.getTitle());
                        data.setMessage(notif.getMessage());
                        data.setCreatedAt(notif.getCreatedAt());
                        data.setRead(notif.isRead());
                        data.setRelatedEntityId(notif.getRelatedEntityId());


                        if ("MESSAGE".equals(notif.getType())) {
                            android.util.Log.d("NotificationActivity", "📨 Processing MESSAGE notification:");
                            android.util.Log.d("NotificationActivity", "   Title: " + notif.getTitle());
                            android.util.Log.d("NotificationActivity", "   RelatedEntityId: " + notif.getRelatedEntityId());


                            String title = notif.getTitle();
                            if (title != null && title.contains("from ")) {
                                String senderName = title.substring(title.indexOf("from ") + 5);
                                data.setSenderName(senderName);
                                android.util.Log.d("NotificationActivity", "   ✅ Extracted sender name: " + senderName);
                            } else {
                                android.util.Log.e("NotificationActivity", "   ❌ Could not extract sender name from title");
                            }


                            String conversationId = notif.getRelatedEntityId();
                            if (conversationId != null && conversationId.contains("_")) {
                                String[] parts = conversationId.split("_");
                                // The sender is the one that's NOT the current user
                                String senderId = parts[0].equals(userId) ? parts[1] : parts[0];
                                data.setSenderId(senderId);
                                data.setCanReply(true);

                                android.util.Log.d("NotificationActivity", "    Extracted sender ID: " + senderId);
                                android.util.Log.d("NotificationActivity", "    canReply set to: TRUE");
                                android.util.Log.d("NotificationActivity", "    Reply button ENABLED for this notification");
                            } else {
                                android.util.Log.e("NotificationActivity", "    Could not extract sender ID - conversationId invalid: " + conversationId);
                                android.util.Log.e("NotificationActivity", "    Reply button will NOT show");
                            }


                            android.util.Log.d("NotificationActivity", "   Final state: canReply=" + data.getCanReply() + ", senderId=" + data.getSenderId());
                        }

                        allNotifications.add(data);
                    }

                    applyCurrentFilter();
                }
                
                @Override
                public void onError(String error) {
                    android.util.Log.e("NotificationActivity", " Error loading notifications: " + error);
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(NotificationActivity.this, 
                        "Error loading notifications: " + error, 
                        Toast.LENGTH_SHORT).show();
                }
            });
    }
    
    private void showAllNotifications() {
        currentFilter = "ALL";
        updateFilterButtons();
        applyCurrentFilter();
    }
    
    private void showMessages() {
        currentFilter = "MESSAGE";
        updateFilterButtons();
        applyCurrentFilter();
    }
    
    private void showSessions() {
        currentFilter = "SESSION";
        updateFilterButtons();
        applyCurrentFilter();
    }
    
    private void showSystem() {
        currentFilter = "SYSTEM";
        updateFilterButtons();
        applyCurrentFilter();
    }
    
    private void applyCurrentFilter() {
        List<NotificationData> filteredNotifications;
        
        if (currentFilter.equals("ALL")) {
            filteredNotifications = new ArrayList<>(allNotifications);
        } else if (currentFilter.equals("SESSION")) {
            filteredNotifications = allNotifications.stream()
                .filter(n -> n.getNotificationType().equals("SESSION") || 
                           n.getNotificationType().equals("SESSION_ACCEPTED"))
                .collect(Collectors.toList());
        } else {
            filteredNotifications = allNotifications.stream()
                .filter(n -> n.getNotificationType().equals(currentFilter))
                .collect(Collectors.toList());
        }
        
        notificationAdapter.updateNotifications(filteredNotifications);
        

        if (filteredNotifications.isEmpty()) {
            emptyStateText.setVisibility(View.VISIBLE);
            notificationsRecyclerView.setVisibility(View.GONE);
        } else {
            emptyStateText.setVisibility(View.GONE);
            notificationsRecyclerView.setVisibility(View.VISIBLE);
        }
    }
    
    private void updateFilterButtons() {

        int inactiveColor = getResources().getColor(R.color.filter_inactive, null);
        int activeColor = getResources().getColor(R.color.filter_active, null);
        
        allNotificationsBtn.setBackgroundColor(inactiveColor);
        messagesBtn.setBackgroundColor(inactiveColor);
        sessionsBtn.setBackgroundColor(inactiveColor);
        systemBtn.setBackgroundColor(inactiveColor);
        

        switch (currentFilter) {
            case "ALL":
                allNotificationsBtn.setBackgroundColor(activeColor);
                break;
            case "MESSAGE":
                messagesBtn.setBackgroundColor(activeColor);
                break;
            case "SESSION":
                sessionsBtn.setBackgroundColor(activeColor);
                break;
            case "SYSTEM":
                systemBtn.setBackgroundColor(activeColor);
                break;
        }
    }
    
    private void handleMarkAllRead() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) return;
        
        String userId = currentUser.getUid();
        notificationService.markAllAsRead(userId, new NotificationService.OnOperationCompleteListener() {
            @Override
            public void onSuccess() {
                Toast.makeText(NotificationActivity.this, 
                    "All notifications marked as read", 
                    Toast.LENGTH_SHORT).show();
                handleRefresh();
            }
            
            @Override
            public void onError(String error) {
                Toast.makeText(NotificationActivity.this, 
                    "Error: " + error, 
                    Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    private void handleRefresh() {
        loadNotifications();
    }
    
    private void markAsRead(NotificationData notification) {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) return;

        String userId = currentUser.getUid();
        notificationService.markAsRead(userId, notification.getNotificationId(),
            new NotificationService.OnCompleteListener() {
                @Override
                public void onSuccess() {

                    allNotifications.remove(notification);
                    applyCurrentFilter();
                    Toast.makeText(NotificationActivity.this, 
                        "Marked as read", 
                        Toast.LENGTH_SHORT).show();
                }
                
                @Override
                public void onError(String error) {
                    Toast.makeText(NotificationActivity.this, 
                        "Error: " + error, 
                        Toast.LENGTH_SHORT).show();
                }
            });
    }
    
    private void openZoomLink(String zoomLink) {
        if (zoomLink != null && !zoomLink.isEmpty()) {
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(zoomLink));
            startActivity(browserIntent);
            Toast.makeText(this, "Opening Zoom meeting...", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "No meeting link available", Toast.LENGTH_SHORT).show();
        }
    }
    
    private void openReplyDialog(String recipientId, String recipientName) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Reply to " + recipientName);
        

        final android.widget.EditText input = new android.widget.EditText(this);
        input.setHint("Your message");
        input.setMinLines(3);
        builder.setView(input);
        
        builder.setPositiveButton("Send", (dialog, which) -> {
            String messageText = input.getText().toString().trim();
            if (!messageText.isEmpty()) {
                sendReply(recipientId, recipientName, messageText);
            } else {
                Toast.makeText(this, "Message cannot be empty", Toast.LENGTH_SHORT).show();
            }
        });
        
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
        builder.show();
    }
    
    private void sendReply(String recipientId, String recipientName, String messageText) {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) return;
        
        String senderId = currentUser.getUid();
        
        android.util.Log.d("NotificationActivity", "📤 Sending reply to instructor...");
        android.util.Log.d("NotificationActivity", "   From: " + senderId);
        android.util.Log.d("NotificationActivity", "   To: " + recipientId + " (" + recipientName + ")");
        android.util.Log.d("NotificationActivity", "   Message: " + messageText);


        com.example.mindbloomandroid.utility.SharedPreferencesManager prefsManager =
            com.example.mindbloomandroid.utility.SharedPreferencesManager.getInstance(this);
        String senderName = prefsManager.getUsername();
        if (senderName == null) senderName = "User";


        com.example.mindbloomandroid.model.Message message = new com.example.mindbloomandroid.model.Message(
            senderId,
            senderName,
            recipientId,
            recipientName,
            messageText
        );


        com.example.mindbloomandroid.service.MessageService messageService =
            new com.example.mindbloomandroid.service.MessageService();

        messageService.sendMessage(message, new com.example.mindbloomandroid.service.MessageService.OnCompleteListener() {
            @Override
            public void onSuccess() {
                android.util.Log.d("NotificationActivity", "✅ Reply sent successfully!");
                Toast.makeText(NotificationActivity.this,
                    "Reply sent to " + recipientName,
                    Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(String error) {
                android.util.Log.e("NotificationActivity", "❌ Failed to send reply: " + error);
                Toast.makeText(NotificationActivity.this,
                    "Failed to send reply: " + error,
                    Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
