package com.example.mindbloomandroid.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mindbloomandroid.R;
import com.example.mindbloomandroid.model.GroupChatMessage;
import com.google.firebase.auth.FirebaseAuth;

import java.util.List;


public class GroupChatMessageAdapter extends RecyclerView.Adapter<GroupChatMessageAdapter.MessageViewHolder> {

    private Context context;
    private List<GroupChatMessage> messages;

    public GroupChatMessageAdapter(Context context, List<GroupChatMessage> messages) {
        this.context = context;
        this.messages = messages;
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_group_chat_message, parent, false);
        return new MessageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {
        GroupChatMessage message = messages.get(position);


        String currentUserId = FirebaseAuth.getInstance().getCurrentUser() != null
                ? FirebaseAuth.getInstance().getCurrentUser().getUid()
                : "";
        boolean isOwnMessage = message.isOwnMessage(currentUserId);


        String usernameDisplay = message.getDisplayUserName();
        if (isOwnMessage && !message.isAnonymous()) {
            usernameDisplay += " (You)";
        }
        holder.usernameText.setText("👤 " + usernameDisplay);


        holder.messageText.setText(message.getMessage());


        holder.timestampText.setText("🕐 " + message.getFormattedTime());


        if (isOwnMessage) {
            holder.usernameText.setTextColor(0xFF3498DB); // Blue for own messages
            holder.itemView.setBackgroundResource(R.drawable.own_message_background);
        } else {
            holder.usernameText.setTextColor(0xFF34495E); // Dark gray for others
            holder.itemView.setBackgroundResource(R.drawable.other_message_background);
        }
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    static class MessageViewHolder extends RecyclerView.ViewHolder {
        TextView usernameText, messageText, timestampText;

        public MessageViewHolder(@NonNull View itemView) {
            super(itemView);
            usernameText = itemView.findViewById(R.id.usernameText);
            messageText = itemView.findViewById(R.id.messageText);
            timestampText = itemView.findViewById(R.id.timestampText);
        }
    }
}
