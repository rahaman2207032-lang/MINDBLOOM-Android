package com.example.mindbloomandroid.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mindbloomandroid.R;
import com.example.mindbloomandroid.adapter.ForumPostAdapter;
import com.example.mindbloomandroid.adapter.GroupChatMessageAdapter;
import com.example.mindbloomandroid.model.GroupChatMessage;
import com.example.mindbloomandroid.model.Post;
import com.example.mindbloomandroid.service.CommunityForumService;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.List;


public class CommunityForumActivity extends AppCompatActivity {

    private TabLayout tabLayout;
    private View forumPostsTab, groupChatTab;
    

    private EditText postTitleField, postContentArea, searchField;
    private CheckBox anonymousCheckBox;
    private Button createPostButton, searchButton, clearSearchButton;
    private Spinner sortSpinner;
    private RecyclerView postsRecyclerView;
    private TextView emptyPostsText;
    private ProgressBar postsProgressBar;
    private ForumPostAdapter forumPostAdapter;
    private List<Post> posts;

    private RecyclerView chatRecyclerView;
    private EditText chatMessageInput;
    private CheckBox chatAnonymousCheckBox;
    private Button sendMessageButton;
    private TextView onlineUsersLabel;
    private GroupChatMessageAdapter chatAdapter;
    private List<GroupChatMessage> chatMessages;

    private CommunityForumService forumService;
    private Handler chatRefreshHandler;
    private Runnable chatRefreshRunnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_community_forum);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("💬 Community Forum");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        initializeViews();
        setupTabs();
        
        forumService = new CommunityForumService();
        loadPosts();
    }

    private void initializeViews() {
        tabLayout = findViewById(R.id.tabLayout);
        forumPostsTab = findViewById(R.id.forumPostsTab);
        groupChatTab = findViewById(R.id.groupChatTab);


        postTitleField = findViewById(R.id.postTitleField);
        postContentArea = findViewById(R.id.postContentArea);
        anonymousCheckBox = findViewById(R.id.anonymousCheckBox);
        createPostButton = findViewById(R.id.createPostButton);
        sortSpinner = findViewById(R.id.sortSpinner);
        searchField = findViewById(R.id.searchField);
        searchButton = findViewById(R.id.searchButton);
        clearSearchButton = findViewById(R.id.clearSearchButton);
        postsRecyclerView = findViewById(R.id.postsRecyclerView);
        emptyPostsText = findViewById(R.id.emptyPostsText);
        postsProgressBar = findViewById(R.id.postsProgressBar);


        chatRecyclerView = findViewById(R.id.chatRecyclerView);
        chatMessageInput = findViewById(R.id.chatMessageInput);
        chatAnonymousCheckBox = findViewById(R.id.chatAnonymousCheckBox);
        sendMessageButton = findViewById(R.id.sendMessageButton);
        onlineUsersLabel = findViewById(R.id.onlineUsersLabel);


        posts = new ArrayList<>();
        forumPostAdapter = new ForumPostAdapter(this, posts, 
            new ForumPostAdapter.OnPostActionListener() {
                @Override
                public void onLikeClick(Post post) {
                    CommunityForumActivity.this.onPostLikeClick(post);
                }

                @Override
                public void onCommentClick(Post post) {
                    CommunityForumActivity.this.onPostCommentClick(post);
                }

                @Override
                public void onDeleteClick(Post post) {
                    CommunityForumActivity.this.onPostDeleteClick(post);
                }
            });
        postsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        postsRecyclerView.setAdapter(forumPostAdapter);

        chatMessages = new ArrayList<>();
        chatAdapter = new GroupChatMessageAdapter(this, chatMessages);
        chatRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        chatRecyclerView.setAdapter(chatAdapter);

        // Sort spinner
        ArrayAdapter<CharSequence> sortAdapter = ArrayAdapter.createFromResource(this,
                R.array.forum_sort_options, android.R.layout.simple_spinner_item);
        sortAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sortSpinner.setAdapter(sortAdapter);

        // Listeners
        createPostButton.setOnClickListener(v -> handleCreatePost());
        searchButton.setOnClickListener(v -> handleSearch());
        clearSearchButton.setOnClickListener(v -> handleClearSearch());
        sendMessageButton.setOnClickListener(v -> handleSendChatMessage());
    }

    private void setupTabs() {
        tabLayout.addTab(tabLayout.newTab().setText("📝 Forum Posts"));
        tabLayout.addTab(tabLayout.newTab().setText("💬 Group Chat"));

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if (tab.getPosition() == 0) {
                    forumPostsTab.setVisibility(View.VISIBLE);
                    groupChatTab.setVisibility(View.GONE);
                    stopChatRefresh();
                } else {
                    forumPostsTab.setVisibility(View.GONE);
                    groupChatTab.setVisibility(View.VISIBLE);
                    loadChatMessages();
                    startChatRefresh();
                    updateOnlineUsers();
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });
    }

    private void handleCreatePost() {
        String title = postTitleField.getText().toString().trim();
        String content = postContentArea.getText().toString().trim();

        if (title.isEmpty() || content.isEmpty()) {
            Toast.makeText(this, "Please fill in both title and content", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        createPostButton.setEnabled(false);
        postsProgressBar.setVisibility(View.VISIBLE);

        String username = anonymousCheckBox.isChecked() ? "Anonymous" : 
            (currentUser.getDisplayName() != null ? currentUser.getDisplayName() : "User");

        Post post = new Post(currentUser.getUid(), username, title, content);

        forumService.createPost(post, new CommunityForumService.OnPostActionListener() {
            @Override
            public void onSuccess(String message) {
                runOnUiThread(() -> {
                    postsProgressBar.setVisibility(View.GONE);
                    createPostButton.setEnabled(true);
                    Toast.makeText(CommunityForumActivity.this, 
                        "Post created successfully!", Toast.LENGTH_SHORT).show();
                    
                    postTitleField.setText("");
                    postContentArea.setText("");
                    anonymousCheckBox.setChecked(false);
                    
                    loadPosts();
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    postsProgressBar.setVisibility(View.GONE);
                    createPostButton.setEnabled(true);
                    Toast.makeText(CommunityForumActivity.this, 
                        "Error: " + error, Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private void loadPosts() {
        postsProgressBar.setVisibility(View.VISIBLE);
        emptyPostsText.setVisibility(View.GONE);

        forumService.getAllPosts(new CommunityForumService.OnPostsLoadedListener() {
            @Override
            public void onPostsLoaded(List<Post> loadedPosts) {
                runOnUiThread(() -> {
                    postsProgressBar.setVisibility(View.GONE);
                    forumPostAdapter.notifyDataSetChanged();
                    posts.clear();
                    posts.addAll(loadedPosts);

                    if (posts.isEmpty()) {
                        emptyPostsText.setVisibility(View.VISIBLE);
                        postsRecyclerView.setVisibility(View.GONE);
                    } else {
                        emptyPostsText.setVisibility(View.GONE);
                        postsRecyclerView.setVisibility(View.VISIBLE);
                    }
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    postsProgressBar.setVisibility(View.GONE);
                    Toast.makeText(CommunityForumActivity.this, 
                        "Error loading posts: " + error, Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private void handleSearch() {
        String query = searchField.getText().toString().trim();
        if (query.isEmpty()) {
            loadPosts();
            return;
        }

        postsProgressBar.setVisibility(View.VISIBLE);
        List<Post> filtered = new ArrayList<>();
        for (Post post : posts) {
            if (post.getTitle().toLowerCase().contains(query.toLowerCase()) ||
                post.getContent().toLowerCase().contains(query.toLowerCase())) {
                filtered.add(post);
            }
        }
forumPostAdapter.notifyDataSetChanged();
        
        posts.clear();
        posts.addAll(filtered);
        postsProgressBar.setVisibility(View.GONE);

        if (posts.isEmpty()) {
            emptyPostsText.setText("No posts found");
            emptyPostsText.setVisibility(View.VISIBLE);
            postsRecyclerView.setVisibility(View.GONE);
        }
    }

    private void handleClearSearch() {
        searchField.setText("");
        loadPosts();
    }

    private void handleSendChatMessage() {
        String message = chatMessageInput.getText().toString().trim();

        if (message.isEmpty()) {
            return;
        }

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        sendMessageButton.setEnabled(false);

        String username = chatAnonymousCheckBox.isChecked() ? "Anonymous" :
            (currentUser.getDisplayName() != null ? currentUser.getDisplayName() : "User");

        GroupChatMessage chatMessage = new GroupChatMessage(message, currentUser.getUid(), 
            username, chatAnonymousCheckBox.isChecked());

        forumService.sendChatMessage(chatMessage, new CommunityForumService.OnMessageSentListener() {
            @Override
            public void onSuccess(GroupChatMessage message) {
                runOnUiThread(() -> {
                    sendMessageButton.setEnabled(true);
                    chatMessageInput.setText("");
                    loadChatMessages();
                    scrollChatToBottom();
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    sendMessageButton.setEnabled(true);
                    Toast.makeText(CommunityForumActivity.this, 
                        "Error: " + error, Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private void loadChatMessages() {
        forumService.getChatMessages(new CommunityForumService.OnChatMessagesLoadedListener() {
            @Override
            public void onMessagesLoaded(List<GroupChatMessage> messages) {
                runOnUiThread(() -> {
                    chatAdapter.notifyDataSetChanged();
                    chatMessages.clear();
                    chatMessages.addAll(messages);
                    scrollChatToBottom();
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    Toast.makeText(CommunityForumActivity.this, 
                        "Error loading messages: " + error, Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private void startChatRefresh() {
        chatRefreshHandler = new Handler(Looper.getMainLooper());
        chatRefreshRunnable = new Runnable() {
            @Override
            public void run() {
                loadChatMessages();
                chatRefreshHandler.postDelayed(this, 3000);
            }
        };
        chatRefreshHandler.postDelayed(chatRefreshRunnable, 3000);
    }

    private void stopChatRefresh() {
        if (chatRefreshHandler != null && chatRefreshRunnable != null) {
            chatRefreshHandler.removeCallbacks(chatRefreshRunnable);
        }
    }

    private void onPostLikeClick(Post post) {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "Please login to like posts", Toast.LENGTH_SHORT).show();
            return;
        }

        forumService.toggleLike(post.getPostId(), currentUser.getUid(),
            new CommunityForumService.OnOperationCompleteListener() {
                @Override
                public void onSuccess() {
                    loadPosts();
                }

                @Override
                public void onError(String error) {
                    Toast.makeText(CommunityForumActivity.this, "Error: " + error, Toast.LENGTH_SHORT).show();
                }
            });
    }

    private void onPostCommentClick(Post post) {
        Intent intent = new Intent(this, PostDetailActivity.class);
        intent.putExtra("POST_ID", post.getPostId());
        startActivity(intent);
    }

    private void onPostDeleteClick(Post post) {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null || !currentUser.getUid().equals(post.getAuthorId())) {
            Toast.makeText(this, "You can only delete your own posts", Toast.LENGTH_SHORT).show();
            return;
        }

        new android.app.AlertDialog.Builder(this)
            .setTitle("Delete Post")
            .setMessage("Are you sure you want to delete this post?")
            .setPositiveButton("Delete", (dialog, which) -> {
                forumService.deletePost(post.getPostId(), new CommunityForumService.OnOperationCompleteListener() {
                    @Override
                    public void onSuccess() {
                        Toast.makeText(CommunityForumActivity.this, "Post deleted", Toast.LENGTH_SHORT).show();
                        loadPosts();
                    }

                    @Override
                    public void onError(String error) {
                        Toast.makeText(CommunityForumActivity.this, "Error: " + error, Toast.LENGTH_SHORT).show();
                    }
                });
            })
            .setNegativeButton("Cancel", null)
            .show();
    }

    private void scrollChatToBottom() {
        if (chatMessages.size() > 0) {
            chatRecyclerView.smoothScrollToPosition(chatMessages.size() - 1);
        }
    }

    private void updateOnlineUsers() {
        int onlineCount = forumService.getOnlineUsersCount();
        onlineUsersLabel.setText("👥 " + onlineCount + " users online");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopChatRefresh();
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
