package com.example.mindbloomandroid.service;

import com.example.mindbloomandroid.model.Post;
import com.example.mindbloomandroid.model.Comment;
import com.example.mindbloomandroid.model.GroupChatMessage;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CommunityForumService {
    private DatabaseReference forumPostsRef;
    private DatabaseReference forumCommentsRef;
    private DatabaseReference groupChatRef;

    public CommunityForumService() {
        forumPostsRef = FirebaseDatabase.getInstance().getReference("forum_posts");
        forumCommentsRef = FirebaseDatabase.getInstance().getReference("forum_comments");
        groupChatRef = FirebaseDatabase.getInstance().getReference("group_chat_messages");
    }


    public void createPost(Post post, OnPostActionListener listener) {
        String postId = forumPostsRef.push().getKey();
        if (postId != null) {
            post.setPostId(postId);

            forumPostsRef.child(postId).setValue(post)
                    .addOnSuccessListener(aVoid -> listener.onSuccess("Post created"))
                    .addOnFailureListener(e -> listener.onError(e.getMessage()));
        } else {
            listener.onError("Failed to generate post ID");
        }
    }


    public void getAllPosts(OnPostsLoadedListener listener) {
        forumPostsRef.orderByChild("createdAt")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        List<Post> posts = new ArrayList<>();
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            Post post = snapshot.getValue(Post.class);
                            if (post != null) {
                                posts.add(0, post); // Add to beginning (newest first)
                            }
                        }
                        listener.onPostsLoaded(posts);
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                        listener.onError(error.getMessage());
                    }
                });
    }


    public void getPostById(String postId, OnPostLoadedListener listener) {
        forumPostsRef.child(postId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        Post post = dataSnapshot.getValue(Post.class);
                        if (post != null) {
                            listener.onPostLoaded(post);
                        } else {
                            listener.onError("Post not found");
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                        listener.onError(error.getMessage());
                    }
                });
    }


    public void addComment(Comment comment, OnPostActionListener listener) {
        String postId = comment.getPostId();
        String commentId = forumCommentsRef.child(postId).push().getKey();

        if (commentId != null) {
            comment.setCommentId(commentId);

            forumCommentsRef.child(postId).child(commentId).setValue(comment)
                    .addOnSuccessListener(aVoid -> {
                        // Increment comment count
                        forumPostsRef.child(postId).child("commentCount")
                                .setValue(com.google.firebase.database.ServerValue.increment(1));
                        listener.onSuccess("Comment added");
                    })
                    .addOnFailureListener(e -> listener.onError(e.getMessage()));
        } else {
            listener.onError("Failed to generate comment ID");
        }
    }


    public void getCommentsByPostId(String postId, OnCommentsLoadedListener listener) {
        android.util.Log.d("ForumService", "📖 Loading comments for post: " + postId);

        forumCommentsRef.child(postId).orderByChild("createdAt")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        List<Comment> comments = new ArrayList<>();
                        android.util.Log.d("ForumService", "📥 Received " + dataSnapshot.getChildrenCount() + " comments");

                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            Comment comment = snapshot.getValue(Comment.class);
                            if (comment != null) {
                                comments.add(comment);
                                android.util.Log.d("ForumService", "   💬 Comment by " + comment.getUsername());
                            }
                        }
                        android.util.Log.d("ForumService", "✅ Loaded " + comments.size() + " comments");
                        listener.onCommentsLoaded(comments);
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                        android.util.Log.e("ForumService", "❌ Error loading comments: " + error.getMessage());
                        listener.onError(error.getMessage());
                    }
                });
    }


    public void deleteComment(String commentId, String postId, OnPostActionListener listener) {
        forumCommentsRef.child(postId).child(commentId).removeValue()
                .addOnSuccessListener(aVoid -> {
                    // Decrement comment count
                    forumPostsRef.child(postId).child("commentCount")
                            .setValue(com.google.firebase.database.ServerValue.increment(-1));
                    listener.onSuccess("Comment deleted");
                })
                .addOnFailureListener(e -> listener.onError(e.getMessage()));
    }


    public void likePost(String postId, OnPostActionListener listener) {
        forumPostsRef.child(postId).child("likeCount")
                .setValue(com.google.firebase.database.ServerValue.increment(1))
                .addOnSuccessListener(aVoid -> listener.onSuccess("Post liked"))
                .addOnFailureListener(e -> listener.onError(e.getMessage()));
    }

    public void deletePost(String postId, OnPostActionListener listener) {
        // Delete post
        forumPostsRef.child(postId).removeValue()
                .addOnSuccessListener(aVoid -> {
                    // Delete all comments for this post
                    forumCommentsRef.child(postId).removeValue();
                    listener.onSuccess("Post deleted");
                })
                .addOnFailureListener(e -> listener.onError(e.getMessage()));
    }

    // Interfaces
    public interface OnPostActionListener {
        void onSuccess(String message);
        void onError(String error);
    }

    public interface OnPostsLoadedListener {
        void onPostsLoaded(List<Post> posts);
        void onError(String error);
    }

    public interface OnPostLoadedListener {
        void onPostLoaded(Post post);
        void onError(String error);
    }

    public interface OnCommentsLoadedListener {
        void onCommentsLoaded(List<Comment> comments);
        void onError(String error);
    }

    // ==================== GROUP CHAT METHODS ====================


    public void sendChatMessage(GroupChatMessage message, OnMessageSentListener listener) {
        String messageId = groupChatRef.push().getKey();
        if (messageId != null) {
            message.setMessageId(messageId);
            message.setCreatedAt(System.currentTimeMillis());

            groupChatRef.child(messageId).setValue(message)
                    .addOnSuccessListener(aVoid -> listener.onSuccess(message))
                    .addOnFailureListener(e -> listener.onError(e.getMessage()));
        } else {
            listener.onError("Failed to generate message ID");
        }
    }


    public void getChatMessages(OnChatMessagesLoadedListener listener) {
        groupChatRef.limitToLast(100).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                List<GroupChatMessage> messages = new ArrayList<>();
                
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    GroupChatMessage message = snapshot.getValue(GroupChatMessage.class);
                    if (message != null) {
                        message.setMessageId(snapshot.getKey());
                        messages.add(message);
                    }
                }

                // Sort by time (oldest first for display)
                Collections.sort(messages, (m1, m2) -> 
                    Long.compare(m1.getCreatedAt(), m2.getCreatedAt()));
                
                listener.onMessagesLoaded(messages);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                listener.onError(error.getMessage());
            }
        });
    }


    public int getOnlineUsersCount() {
        // In a real app, you'd track active sessions
        // For now, return a random number between 1-10
        return (int) (Math.random() * 10) + 1;
    }


    public void toggleLike(String postId, String userId, OnOperationCompleteListener listener) {
        DatabaseReference postLikesRef = FirebaseDatabase.getInstance()
                .getReference("post_likes")
                .child(postId)
                .child(userId);

        android.util.Log.d("ForumService", "🔄 Toggling like - PostID: " + postId + ", UserID: " + userId);

        // Check if user already liked this post
        postLikesRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    // User already liked - UNLIKE (remove)
                    android.util.Log.d("ForumService", "👎 Removing like");
                    postLikesRef.removeValue()
                        .addOnSuccessListener(aVoid -> {
                            // Decrement like count
                            forumPostsRef.child(postId).child("likeCount")
                                .setValue(com.google.firebase.database.ServerValue.increment(-1))
                                .addOnSuccessListener(aVoid2 -> {
                                    android.util.Log.d("ForumService", "✅ Like removed successfully");
                                    listener.onSuccess();
                                })
                                .addOnFailureListener(e -> listener.onError(e.getMessage()));
                        })
                        .addOnFailureListener(e -> listener.onError(e.getMessage()));
                } else {
                    // User hasn't liked yet - LIKE (add)
                    android.util.Log.d("ForumService", "👍 Adding like");
                    Map<String, Object> likeData = new HashMap<>();
                    likeData.put("userId", userId);
                    likeData.put("postId", postId);
                    likeData.put("likedAt", System.currentTimeMillis());

                    postLikesRef.setValue(likeData)
                        .addOnSuccessListener(aVoid -> {
                            // Increment like count
                            forumPostsRef.child(postId).child("likeCount")
                                .setValue(com.google.firebase.database.ServerValue.increment(1))
                                .addOnSuccessListener(aVoid2 -> {
                                    android.util.Log.d("ForumService", "✅ Like added successfully");
                                    listener.onSuccess();
                                })
                                .addOnFailureListener(e -> listener.onError(e.getMessage()));
                        })
                        .addOnFailureListener(e -> listener.onError(e.getMessage()));
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                android.util.Log.e("ForumService", "❌ Error toggling like: " + error.getMessage());
                listener.onError(error.getMessage());
            }
        });
    }


    public void deletePost(String postId, OnOperationCompleteListener listener) {
        forumPostsRef.child(postId).removeValue()
            .addOnSuccessListener(aVoid -> listener.onSuccess())
            .addOnFailureListener(e -> listener.onError(e.getMessage()));
    }


    public void getComments(String postId, OnCommentsLoadedListener listener) {
        android.util.Log.d("ForumService", "📖 Loading comments for post: " + postId);

        // Use the existing getCommentsByPostId method
        getCommentsByPostId(postId, listener);
    }


    public void addComment(Comment comment, OnOperationCompleteListener listener) {
        String postId = comment.getPostId();
        String commentId = forumCommentsRef.child(postId).push().getKey();

        if (commentId != null) {
            comment.setCommentId(commentId);
            comment.setCreatedAt(System.currentTimeMillis());

            android.util.Log.d("ForumService", "💬 Saving comment - PostID: " + postId + ", CommentID: " + commentId);

            // Save comment under postId path: forum_comments/{postId}/{commentId}
            forumCommentsRef.child(postId).child(commentId).setValue(comment)
                .addOnSuccessListener(aVoid -> {
                    android.util.Log.d("ForumService", "✅ Comment saved successfully");

                    // Increment comment count on post
                    forumPostsRef.child(postId).child("commentCount")
                        .setValue(com.google.firebase.database.ServerValue.increment(1));

                    listener.onSuccess();
                })
                .addOnFailureListener(e -> {
                    android.util.Log.e("ForumService", "❌ Failed to save comment: " + e.getMessage());
                    listener.onError(e.getMessage());
                });
        } else {
            listener.onError("Failed to generate comment ID");
        }
    }


    public void likePost(String postId, String userId, OnOperationCompleteListener listener) {
        toggleLike(postId, userId, listener);
    }

    public interface OnMessageSentListener {
        void onSuccess(GroupChatMessage message);
        void onError(String error);
    }

    public interface OnChatMessagesLoadedListener {
        void onMessagesLoaded(List<GroupChatMessage> messages);
        void onError(String error);
    }

    public interface OnOperationCompleteListener {
        void onSuccess();
        void onError(String error);
    }
}
