package com.example.mindbloomandroid.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.mindbloomandroid.R;
import com.example.mindbloomandroid.adapter.CommentAdapter;
import com.example.mindbloomandroid.model.Comment;
import com.example.mindbloomandroid.model.Post;
import com.example.mindbloomandroid.service.CommunityForumService;
import com.example.mindbloomandroid.utility.SharedPreferencesManager;
import com.google.android.material.card.MaterialCardView;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;


public class PostDetailActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private MaterialCardView postCard;
    private TextView usernameText, contentText, timestampText, likeCountText, commentCountText;
    private ImageButton likeButton;
    private RecyclerView commentsRecyclerView;
    private EditText commentEditText;
    private Button postCommentButton;
    private ProgressBar progressBar;

    private CommunityForumService forumService;
    private SharedPreferencesManager prefsManager;
    private CommentAdapter commentAdapter;
    private List<Comment> comments;
    private String postId;
    private Post currentPost;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_detail);

        postId = getIntent().getStringExtra("POST_ID");
        if (postId == null) {
            Toast.makeText(this, "Invalid post", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initializeViews();
        setupToolbar();

        forumService = new CommunityForumService();
        prefsManager = SharedPreferencesManager.getInstance(this);
        comments = new ArrayList<>();

        setupRecyclerView();
        loadPostDetails();
        loadComments();

        postCommentButton.setOnClickListener(v -> postComment());
        likeButton.setOnClickListener(v -> likePost());
    }

    private void initializeViews() {
        toolbar = findViewById(R.id.toolbar);
        postCard = findViewById(R.id.postCard);
        usernameText = findViewById(R.id.usernameText);
        contentText = findViewById(R.id.contentText);
        timestampText = findViewById(R.id.timestampText);
        likeCountText = findViewById(R.id.likeCountText);
        commentCountText = findViewById(R.id.commentCountText);
        likeButton = findViewById(R.id.likeButton);
        commentsRecyclerView = findViewById(R.id.commentsRecyclerView);
        commentEditText = findViewById(R.id.commentEditText);
        postCommentButton = findViewById(R.id.postCommentButton);
        progressBar = findViewById(R.id.progressBar);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Post Details");
        }
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void setupRecyclerView() {
        commentAdapter = new CommentAdapter(this, comments);
        commentsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        commentsRecyclerView.setAdapter(commentAdapter);
    }

    private void loadPostDetails() {
        progressBar.setVisibility(View.VISIBLE);
        forumService.getPostById(postId, new CommunityForumService.OnPostLoadedListener() {
            @Override
            public void onPostLoaded(Post post) {
                progressBar.setVisibility(View.GONE);
                currentPost = post;
                displayPost(post);
            }

            @Override
            public void onError(String error) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(PostDetailActivity.this, 
                    "Error loading post: " + error, 
                    Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void displayPost(Post post) {
        usernameText.setText(post.getUsername());
        contentText.setText(post.getContent());
        likeCountText.setText(String.valueOf(post.getLikeCount()));
        commentCountText.setText(String.valueOf(post.getCommentCount()));
        
        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy - hh:mm a", Locale.getDefault());
        timestampText.setText(sdf.format(new Date(post.getTimestamp())));
    }

    private void loadComments() {
        android.util.Log.d("PostDetail", "📖 Loading comments for post: " + postId);

        forumService.getComments(postId, new CommunityForumService.OnCommentsLoadedListener() {
            @Override
            public void onCommentsLoaded(List<Comment> loadedComments) {
                android.util.Log.d("PostDetail", "✅ Received " + loadedComments.size() + " comments");

                comments.clear();
                comments.addAll(loadedComments);
                commentAdapter.notifyDataSetChanged();

                if (loadedComments.isEmpty()) {
                    android.util.Log.i("PostDetail", "ℹ️ No comments yet for this post");
                } else {
                    for (Comment c : loadedComments) {
                        android.util.Log.d("PostDetail", "   💬 Comment by " + c.getUsername() + ": " + c.getCommentText().substring(0, Math.min(20, c.getCommentText().length())));
                    }
                }
            }

            @Override
            public void onError(String error) {
                android.util.Log.e("PostDetail", "❌ Error loading comments: " + error);
                Toast.makeText(PostDetailActivity.this,
                    "Error loading comments: " + error, 
                    Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void postComment() {
        String commentText = commentEditText.getText().toString().trim();
        if (commentText.isEmpty()) {
            Toast.makeText(this, "Please enter a comment", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = prefsManager.getUserId();
        String username = prefsManager.getUsername();
        
        Comment comment = new Comment();
        comment.setPostId(postId);
        comment.setUserId(userId);
        comment.setUsername(username);
        comment.setContent(commentText);
        comment.setTimestamp(System.currentTimeMillis());
        
        progressBar.setVisibility(View.VISIBLE);
        forumService.addComment(comment, new CommunityForumService.OnOperationCompleteListener() {
            @Override
            public void onSuccess() {
                progressBar.setVisibility(View.GONE);
                commentEditText.setText("");
                Toast.makeText(PostDetailActivity.this, 
                    "Comment posted!", 
                    Toast.LENGTH_SHORT).show();
                loadComments();
                loadPostDetails();
            }

            @Override
            public void onError(String error) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(PostDetailActivity.this, 
                    "Error posting comment: " + error, 
                    Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void likePost() {
        String userId = prefsManager.getUserId();
        forumService.likePost(postId, userId, new CommunityForumService.OnOperationCompleteListener() {
            @Override
            public void onSuccess() {
                loadPostDetails(); // Refresh to show updated like count
            }

            @Override
            public void onError(String error) {
                Toast.makeText(PostDetailActivity.this, 
                    "Error: " + error, 
                    Toast.LENGTH_SHORT).show();
            }
        });
    }
}
