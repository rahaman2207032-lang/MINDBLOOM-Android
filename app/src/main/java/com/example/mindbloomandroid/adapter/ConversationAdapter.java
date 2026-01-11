package com.example.mindbloomandroid.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.mindbloomandroid.R;
import com.example.mindbloomandroid.model.Conversation;
import java.util.List;

public class ConversationAdapter extends RecyclerView.Adapter<ConversationAdapter.ConversationViewHolder> {

    private Context context;
    private List<Conversation> conversations;
    private OnConversationClickListener listener;
    private int selectedPosition = -1;

    public interface OnConversationClickListener {
        void onConversationClick(Conversation conversation, int position);
    }

    public ConversationAdapter(Context context, List<Conversation> conversations) {
        this.context = context;
        this.conversations = conversations;
    }

    public void setOnConversationClickListener(OnConversationClickListener listener) {
        this.listener = listener;
    }

    public void setSelectedPosition(int position) {
        int oldPosition = selectedPosition;
        selectedPosition = position;
        if (oldPosition != -1) notifyItemChanged(oldPosition);
        if (selectedPosition != -1) notifyItemChanged(selectedPosition);
    }

    @NonNull
    @Override
    public ConversationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_conversation, parent, false);
        return new ConversationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ConversationViewHolder holder, int position) {
        Conversation conversation = conversations.get(position);

        holder.clientInitial.setText(conversation.getClientInitial());
        holder.clientName.setText(conversation.getClientName());
        holder.lastMessage.setText(conversation.getLastMessage().isEmpty() 
            ? "No messages yet" 
            : conversation.getLastMessage());

        if (conversation.getUnreadCount() > 0) {
            holder.unreadBadge.setVisibility(View.VISIBLE);
            holder.unreadBadge.setText(String.valueOf(conversation.getUnreadCount()));
        } else {
            holder.unreadBadge.setVisibility(View.GONE);
        }

        // Highlight selected conversation
        if (position == selectedPosition) {
            holder.itemView.setBackgroundColor(0xFFE8F5E9);
        } else {
            holder.itemView.setBackgroundColor(0xFFFFFFFF);
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onConversationClick(conversation, position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return conversations.size();
    }

    static class ConversationViewHolder extends RecyclerView.ViewHolder {
        TextView clientInitial, clientName, lastMessage, unreadBadge;

        public ConversationViewHolder(@NonNull View itemView) {
            super(itemView);
            clientInitial = itemView.findViewById(R.id.conversationInitial);
            clientName = itemView.findViewById(R.id.conversationClientName);
            lastMessage = itemView.findViewById(R.id.conversationLastMessage);
            unreadBadge = itemView.findViewById(R.id.conversationUnreadBadge);
        }
    }
}
