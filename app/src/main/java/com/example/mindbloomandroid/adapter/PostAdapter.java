package com.example.mindbloomandroid.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.mindbloomandroid.R;
import com.example.mindbloomandroid.model.Post;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.PostViewHolder> {

    public interface OnPostClickListener {
        void onPostClick(Post post);
    }

    public interface OnLikeClickListener {
        void onLikeClick(Post post);
    }

    private Context context;
    private List<Post> posts;
    private OnPostClickListener postClickListener;
    private OnLikeClickListener likeClickListener;

    public PostAdapter(Context context, List<Post> posts, OnPostClickListener postClickListener, OnLikeClickListener likeClickListener) {
        this.context = context;
        this.posts = posts;
        this.postClickListener = postClickListener;
        this.likeClickListener = likeClickListener;
    }

    @NonNull
    @Override
    public PostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_post, parent, false);
        return new PostViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PostViewHolder holder, int position) {
        Post post = posts.get(position);
        
        holder.usernameText.setText(post.getUsername());
        holder.contentText.setText(post.getContent());
        holder.likeCountText.setText(String.valueOf(post.getLikeCount()));
        holder.commentCountText.setText(String.valueOf(post.getCommentCount()));
        
        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, hh:mm a", Locale.getDefault());
        holder.timestampText.setText(sdf.format(new Date(post.getTimestamp())));
        
        holder.itemView.setOnClickListener(v -> {
            if (postClickListener != null) {
                postClickListener.onPostClick(post);
            }
        });
        
        holder.likeButton.setOnClickListener(v -> {
            if (likeClickListener != null) {
                likeClickListener.onLikeClick(post);
            }
        });
    }

    @Override
    public int getItemCount() {
        return posts.size();
    }

    static class PostViewHolder extends RecyclerView.ViewHolder {
        TextView usernameText, contentText, timestampText, likeCountText, commentCountText;
        ImageButton likeButton;

        public PostViewHolder(@NonNull View itemView) {
            super(itemView);
            usernameText = itemView.findViewById(R.id.usernameText);
            contentText = itemView.findViewById(R.id.contentText);
            timestampText = itemView.findViewById(R.id.timestampText);
            likeCountText = itemView.findViewById(R.id.likeCountText);
            commentCountText = itemView.findViewById(R.id.commentCountText);
            likeButton = itemView.findViewById(R.id.likeButton);
        }
    }
}
