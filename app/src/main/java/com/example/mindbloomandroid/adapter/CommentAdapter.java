package com.example.mindbloomandroid.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.mindbloomandroid.R;
import com.example.mindbloomandroid.model.Comment;
import com.example.mindbloomandroid.utility.DateTimeUtil;
import java.util.List;

public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.CommentViewHolder> {

    private Context context;
    private List<Comment> comments;

    public CommentAdapter(Context context, List<Comment> comments) {
        this.context = context;
        this.comments = comments;
    }

    @NonNull
    @Override
    public CommentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_comment, parent, false);
        return new CommentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CommentViewHolder holder, int position) {
        Comment comment = comments.get(position);
        
        holder.usernameText.setText(comment.getUsername());
        holder.contentText.setText(comment.getCommentText());
        holder.timestampText.setText(DateTimeUtil.formatRelativeTime(comment.getCreatedAt()));
    }

    @Override
    public int getItemCount() {
        return comments.size();
    }

    static class CommentViewHolder extends RecyclerView.ViewHolder {
        TextView usernameText, contentText, timestampText;

        public CommentViewHolder(@NonNull View itemView) {
            super(itemView);
            usernameText = itemView.findViewById(R.id.commentUsernameText);
            contentText = itemView.findViewById(R.id.commentContentText);
            timestampText = itemView.findViewById(R.id.commentTimestampText);
        }
    }
}
