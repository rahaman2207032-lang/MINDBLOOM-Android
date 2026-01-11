package com.example.mindbloomandroid.fragments;

import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.mindbloomandroid.R;
import com.example.mindbloomandroid.adapter.ChatMessageAdapter;
import com.example.mindbloomandroid.adapter.ConversationAdapter;
import com.example.mindbloomandroid.model.ClientOverview;
import com.example.mindbloomandroid.model.Conversation;
import com.example.mindbloomandroid.model.Message;
import com.example.mindbloomandroid.model.User;
import com.example.mindbloomandroid.service.InstructorService;
import com.example.mindbloomandroid.service.MessageService;
import com.example.mindbloomandroid.service.ZoomLinkService;
import com.example.mindbloomandroid.utility.SharedPreferencesManager;
import android.content.Intent;
import android.net.Uri;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.ImageView;
import android.widget.LinearLayout;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import java.util.ArrayList;
import java.util.List;


public class InstructorMessagesFragment extends Fragment {

    private RecyclerView conversationsRecyclerView;
    private RecyclerView messagesRecyclerView;
    private TextInputEditText messageInput;
    private TextInputEditText searchConversations;
    private FloatingActionButton btnSendMessage;
    private Button btnVideoCall;
    private ImageView btnAttachment;
    private TextView chatClientName;
    private TextView chatClientInitial;
    private TextView chatClientStatus;
    private TextView totalConversations;
    private LinearLayout emptyMessagesState;
    private ProgressBar progressBar;

    private InstructorService instructorService;
    private MessageService messageService;
    private SharedPreferencesManager prefsManager;
    private ConversationAdapter conversationAdapter;
    private ChatMessageAdapter chatMessageAdapter;

    private List<Conversation> conversations;
    private List<Message> currentMessages;
    private String currentUserId;
    private String selectedClientId;
    private String selectedClientName;
    private Handler autoRefreshHandler;
    private Runnable autoRefreshRunnable;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_instructor_messages, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        try {
            android.util.Log.d("InstructorMessagesFragment", "🎯 onViewCreated started");

            initializeViews(view);

            instructorService = new InstructorService();
            messageService = new MessageService();
            prefsManager = SharedPreferencesManager.getInstance(requireContext());
            currentUserId = prefsManager.getUserId();
            conversations = new ArrayList<>();
            currentMessages = new ArrayList<>();

            setupRecyclerViews();
            setupSearchFilter();
            setupSendButton();
            setupVideoCallButton();
            setupAttachmentButton();
            loadConversations();
            startAutoRefresh();

            android.util.Log.d("InstructorMessagesFragment", "✅ onViewCreated completed successfully");
        } catch (Exception e) {
            android.util.Log.e("InstructorMessagesFragment", "❌ CRASH in onViewCreated: " + e.getMessage(), e);
            if (getContext() != null) {
                Toast.makeText(getContext(), "Error loading messages: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        }
    }

    private void initializeViews(View view) {
        conversationsRecyclerView = view.findViewById(R.id.conversationsRecyclerView);
        messagesRecyclerView = view.findViewById(R.id.messagesRecyclerView);
        messageInput = view.findViewById(R.id.messageInput);
        searchConversations = view.findViewById(R.id.searchConversations);
        btnSendMessage = view.findViewById(R.id.btnSendMessage);
        btnVideoCall = view.findViewById(R.id.btnVideoCall);
        btnAttachment = view.findViewById(R.id.btnAttachment);
        chatClientName = view.findViewById(R.id.chatClientName);
        chatClientInitial = view.findViewById(R.id.chatClientInitial);
        chatClientStatus = view.findViewById(R.id.chatClientStatus);
        totalConversations = view.findViewById(R.id.totalConversations);
        emptyMessagesState = view.findViewById(R.id.emptyMessagesState);
        progressBar = view.findViewById(R.id.progressBar);
    }

    private void setupRecyclerViews() {
        // Conversations list
        conversationAdapter = new ConversationAdapter(requireContext(), conversations);
        conversationsRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        conversationsRecyclerView.setAdapter(conversationAdapter);

        conversationAdapter.setOnConversationClickListener((conversation, position) -> {
            selectedClientId = conversation.getClientId();
            selectedClientName = conversation.getClientName();
            conversationAdapter.setSelectedPosition(position);
            updateChatHeader();
            loadMessages();
        });

        // Messages list
        chatMessageAdapter = new ChatMessageAdapter(requireContext(), currentMessages, currentUserId);
        LinearLayoutManager messagesLayoutManager = new LinearLayoutManager(requireContext());
        messagesLayoutManager.setStackFromEnd(true);
        messagesRecyclerView.setLayoutManager(messagesLayoutManager);
        messagesRecyclerView.setAdapter(chatMessageAdapter);
    }

    private void updateChatHeader() {
        if (selectedClientName != null) {
            chatClientName.setText(selectedClientName);
            String initial = selectedClientName.substring(0, 1).toUpperCase();
            chatClientInitial.setText(initial);
            chatClientStatus.setText("Active");
        }
    }

    private void setupSearchFilter() {
        searchConversations.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterConversations(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void filterConversations(String query) {
        // Filter conversations based on search query
        List<Conversation> filtered = new ArrayList<>();
        if (query.isEmpty()) {
            filtered.addAll(conversations);
        } else {
            String lowerQuery = query.toLowerCase();
            for (Conversation conv : conversations) {
                if (conv.getClientName().toLowerCase().contains(lowerQuery)) {
                    filtered.add(conv);
                }
            }
        }
        conversationAdapter.notifyDataSetChanged();
    }

    private void setupSendButton() {
        btnSendMessage.setOnClickListener(v -> sendMessage());
    }

    private void setupVideoCallButton() {
        btnVideoCall.setOnClickListener(v -> {
            if (selectedClientId != null) {
                startInstantVideoCall();
            } else {
                Toast.makeText(requireContext(), "Please select a conversation first", Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    private void startInstantVideoCall() {
        progressBar.setVisibility(View.VISIBLE);
        ZoomLinkService zoomService = new ZoomLinkService();
        
        zoomService.generateInstantZoomLink(currentUserId, selectedClientId,
                new ZoomLinkService.OnZoomLinkGeneratedListener() {
                    @Override
                    public void onLinkGenerated(String zoomLink, long meetingId, String password) {
                        progressBar.setVisibility(View.GONE);
                        
                        // Send zoom link as message to client
                        Message videoCallMessage = new Message();
                        videoCallMessage.setSenderId(currentUserId);
                        videoCallMessage.setReceiverId(selectedClientId);
                        videoCallMessage.setContent("📹 Video Call Invitation\n\nClick to join: " + zoomLink);
                        videoCallMessage.setTimestamp(System.currentTimeMillis());
                        videoCallMessage.setStatus("sent");
                        
                        MessageService messageService = new MessageService();
                        messageService.sendMessage(videoCallMessage, new MessageService.OnCompleteListener() {
                            @Override
                            public void onSuccess() {
                                Toast.makeText(requireContext(), 
                                    "Video call link sent! Opening meeting...", 
                                    Toast.LENGTH_SHORT).show();
                                
                                // Open zoom link for instructor
                                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(zoomLink));
                                startActivity(browserIntent);
                                
                                loadMessages(); // Refresh messages
                            }

                            @Override
                            public void onError(String error) {
                                Toast.makeText(requireContext(), 
                                    "Failed to send link: " + error, 
                                    Toast.LENGTH_SHORT).show();
                            }
                        });
                    }

                    @Override
                    public void onError(String error) {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(requireContext(), 
                            "Failed to generate link: " + error, 
                            Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void setupAttachmentButton() {
        btnAttachment.setOnClickListener(v -> {
            Toast.makeText(requireContext(), "Attachment feature coming soon", Toast.LENGTH_SHORT).show();
        });
    }

    private void loadConversations() {
        if (currentUserId == null) {
            Toast.makeText(requireContext(), "User not logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        android.util.Log.d("InstructorMessagesFragment", "📖 Loading conversations for instructor: " + currentUserId);
        progressBar.setVisibility(View.VISIBLE);

        instructorService.getAllClients(currentUserId, new InstructorService.OnClientsLoadedListener() {
            @Override
            public void onClientsLoaded(List<ClientOverview> clients) {
                if (getActivity() == null) return;

                android.util.Log.d("InstructorMessagesFragment", "✅ Loaded " + clients.size() + " clients");
                progressBar.setVisibility(View.GONE);
                conversations.clear();

                // Create conversation for each client and check for unread messages
                for (ClientOverview client : clients) {
                    Conversation conv = new Conversation(
                        client.getClientId(),
                        client.getClientName()
                    );

                    // Load unread count for this conversation
                    messageService.getUnreadCount(currentUserId, client.getClientId(),
                        new MessageService.OnUnreadCountListener() {
                            @Override
                            public void onUnreadCountLoaded(int count) {
                                if (getActivity() == null) return;

                                conv.setUnreadCount(count);
                                conversationAdapter.notifyDataSetChanged();

                                if (count > 0) {
                                    android.util.Log.d("InstructorMessagesFragment",
                                        "   📬 " + client.getClientName() + " has " + count + " unread messages");
                                }
                            }

                            @Override
                            public void onError(String error) {
                                android.util.Log.e("InstructorMessagesFragment",
                                    "Error loading unread count for " + client.getClientName() + ": " + error);
                            }
                        });

                    conversations.add(conv);
                }

                conversationAdapter.notifyDataSetChanged();
                totalConversations.setText(String.valueOf(conversations.size()));

                if (conversations.isEmpty()) {
                    Toast.makeText(requireContext(), "No clients yet", Toast.LENGTH_SHORT).show();
                } else {
                    android.util.Log.d("InstructorMessagesFragment", "✅ Displaying " + conversations.size() + " conversations");
                }
            }

            @Override
            public void onError(String error) {
                if (getActivity() == null) return;

                android.util.Log.e("InstructorMessagesFragment", "❌ Error loading clients: " + error);
                progressBar.setVisibility(View.GONE);
                Toast.makeText(requireContext(), "Error: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadMessages() {
        if (selectedClientId == null) return;

        messageService.getConversationMessages(currentUserId, selectedClientId, 
            new MessageService.OnMessagesLoadedListener() {
                @Override
                public void onMessagesLoaded(List<Message> messages) {
                    if (getActivity() == null) return;

                    currentMessages.clear();
                    currentMessages.addAll(messages);
                    chatMessageAdapter.notifyDataSetChanged();
                    
                    if (currentMessages.isEmpty()) {
                        emptyMessagesState.setVisibility(View.VISIBLE);
                        messagesRecyclerView.setVisibility(View.GONE);
                    } else {
                        emptyMessagesState.setVisibility(View.GONE);
                        messagesRecyclerView.setVisibility(View.VISIBLE);
                        messagesRecyclerView.scrollToPosition(currentMessages.size() - 1);
                    }
                }

                @Override
                public void onError(String error) {
                    if (getActivity() == null) return;
                    Toast.makeText(requireContext(), "Error: " + error, Toast.LENGTH_SHORT).show();
                }
            });
    }

    private void sendMessage() {
        if (selectedClientId == null) {
            Toast.makeText(requireContext(), "Please select a conversation", Toast.LENGTH_SHORT).show();
            return;
        }

        String messageText = messageInput.getText().toString().trim();
        if (messageText.isEmpty()) {
            messageInput.setError("Message cannot be empty");
            return;
        }

        String instructorName = prefsManager.getUsername();
        Message message = new Message(currentUserId, instructorName, selectedClientId, selectedClientName, messageText);

        messageService.sendMessage(message, new MessageService.OnCompleteListener() {
            @Override
            public void onSuccess() {
                if (getActivity() == null) return;

                messageInput.setText("");
                loadMessages();
            }

            @Override
            public void onError(String error) {
                if (getActivity() == null) return;
                Toast.makeText(requireContext(), "Error: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void startAutoRefresh() {
        autoRefreshHandler = new Handler();
        autoRefreshRunnable = new Runnable() {
            @Override
            public void run() {
                if (selectedClientId != null) {
                    loadMessages();
                }
                autoRefreshHandler.postDelayed(this, 5000); // Refresh every 5 seconds
            }
        };
        autoRefreshHandler.post(autoRefreshRunnable);
    }

    private void stopAutoRefresh() {
        if (autoRefreshHandler != null && autoRefreshRunnable != null) {
            autoRefreshHandler.removeCallbacks(autoRefreshRunnable);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        stopAutoRefresh();
    }
}
