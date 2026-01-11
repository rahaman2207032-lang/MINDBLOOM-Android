package com.example.mindbloomandroid.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mindbloomandroid.R;
import com.example.mindbloomandroid.model.Post;
import com.google.firebase.auth.FirebaseAuth;

import java.util.List;


public class ForumPostAdapter extends RecyclerView.Adapter<ForumPostAdapter.PostViewHolder> {

    private Context context;
    private List<Post> posts;
    private OnPostActionListener listener;

    public interface OnPostActionListener {
        void onLikeClick(Post post);
        void onCommentClick(Post post);
        void onDeleteClick(Post post);
    }

    public ForumPostAdapter(Context context, List<Post> posts, OnPostActionListener listener) {
        this.context = context;
        this.posts = posts;
        this.listener = listener;
    }

    @NonNull
    @Override
    public PostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_forum_post, parent, false);
        return new PostViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PostViewHolder holder, int position) {
        Post post = posts.get(position);


        String authorDisplay = post.getUsername() != null && !post.getUsername().isEmpty()
                ? "👤 " + post.getUsername()
                : "👤 Anonymous User";
        holder.authorText.setText(authorDisplay);


        holder.timestampText.setText("🕐 " + post.getTimeAgo());


        holder.titleText.setText(post.getTitle());


        holder.contentText.setText(post.getContent());


        String likeText = (post.getLikeCount() > 0 ? "❤️ " : "🤍 ") + post.getLikeCount() + " Likes";
        holder.likeButton.setText(likeText);


        String commentText = "💬 " + post.getCommentCount() + " Comments";
        holder.commentButton.setText(commentText);


        String currentUserId = FirebaseAuth.getInstance().getCurrentUser() != null
                ? FirebaseAuth.getInstance().getCurrentUser().getUid()
                : "";
        
        if (post.getUserId() != null && post.getUserId().equals(currentUserId)) {
            holder.deleteButton.setVisibility(View.VISIBLE);
        } else {
            holder.deleteButton.setVisibility(View.GONE);
        }


        holder.likeButton.setOnClickListener(v -> {
            if (listener != null) {
                listener.onLikeClick(post);
            }
        });

        holder.commentButton.setOnClickListener(v -> {
            if (listener != null) {
                listener.onCommentClick(post);
            }
        });

        holder.deleteButton.setOnClickListener(v -> {
            if (listener != null) {
                listener.onDeleteClick(post);
            }
        });
    }

    @Override
    public int getItemCount() {
        return posts.size();
    }

    static class PostViewHolder extends RecyclerView.ViewHolder {
        TextView authorText, timestampText, titleText, contentText;
        Button likeButton, commentButton, deleteButton;

        public PostViewHolder(@NonNull View itemView) {
            super(itemView);
            authorText = itemView.findViewById(R.id.authorText);
            timestampText = itemView.findViewById(R.id.timestampText);
            titleText = itemView.findViewById(R.id.titleText);
            contentText = itemView.findViewById(R.id.contentText);
            likeButton = itemView.findViewById(R.id.likeButton);
            commentButton = itemView.findViewById(R.id.commentButton);
            deleteButton = itemView.findViewById(R.id.deleteButton);
        }
    }
}
