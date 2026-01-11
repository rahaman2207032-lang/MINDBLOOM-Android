package com.example.mindbloomandroid.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mindbloomandroid.R;
import com.example.mindbloomandroid.model.NotificationData;

import java.util.List;


public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.NotificationViewHolder> {

    public interface OnNotificationActionListener {
        void onMarkAsRead(NotificationData notification);
        void onJoinMeeting(String zoomLink);
        void onReply(String senderId, String senderName);
    }

    private Context context;
    private List<NotificationData> notifications;
    private OnNotificationActionListener listener;

    public NotificationAdapter(Context context, List<NotificationData> notifications, OnNotificationActionListener listener) {
        this.context = context;
        this.notifications = notifications;
        this.listener = listener;
    }

    public void updateNotifications(List<NotificationData> newNotifications) {
        this.notifications = newNotifications;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public NotificationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_notification, parent, false);
        return new NotificationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NotificationViewHolder holder, int position) {
        NotificationData notification = notifications.get(position);
        

        holder.iconText.setText(notification.getTypeIcon());
        holder.titleText.setText(notification.getTitle());
        holder.messageText.setText(notification.getMessage());
        holder.timeText.setText(notification.getTimeAgo());
        

        int bgColor = notification.isRead() ? Color.parseColor("#FFFFFF") : Color.parseColor("#E3F2FD");
        holder.itemView.setBackgroundColor(bgColor);
        

        holder.joinMeetingBtn.setVisibility(View.GONE);
        holder.replyBtn.setVisibility(View.GONE);
        

        if ("SESSION_ACCEPTED".equals(notification.getNotificationType())) {
            if (notification.getCanJoin() != null && notification.getCanJoin() && 
                notification.getZoomLink() != null && !notification.getZoomLink().isEmpty()) {
                

                if (notification.getSessionDate() != null || notification.getInstructorName() != null) {
                    String details = "";
                    if (notification.getSessionDate() != null) {
                        details += "📅 " + notification.getSessionDate();
                    }
                    if (notification.getInstructorName() != null) {
                        if (!details.isEmpty()) details += "\n";
                        details += "👨‍⚕️ " + notification.getInstructorName();
                    }
                    holder.sessionDetailsText.setText(details);
                    holder.sessionDetailsText.setVisibility(View.VISIBLE);
                } else {
                    holder.sessionDetailsText.setVisibility(View.GONE);
                }
                
                holder.joinMeetingBtn.setVisibility(View.VISIBLE);
                holder.joinMeetingBtn.setOnClickListener(v -> {
                    if (listener != null) {
                        listener.onJoinMeeting(notification.getZoomLink());
                    }
                });
            }
        } else if ("MESSAGE".equals(notification.getNotificationType())) {
            android.util.Log.d("NotificationAdapter", "📨 MESSAGE notification detected");
            android.util.Log.d("NotificationAdapter", "   - canReply: " + notification.getCanReply());
            android.util.Log.d("NotificationAdapter", "   - senderId: " + notification.getSenderId());
            android.util.Log.d("NotificationAdapter", "   - senderName: " + notification.getSenderName());

            if (notification.getCanReply() != null && notification.getCanReply() &&
                notification.getSenderId() != null) {
                
                android.util.Log.d("NotificationAdapter", "✅ SHOWING REPLY BUTTON");
                holder.sessionDetailsText.setVisibility(View.GONE);
                holder.replyBtn.setVisibility(View.VISIBLE);
                holder.replyBtn.setText("💬 Reply to " + 
                    (notification.getSenderName() != null ? notification.getSenderName() : "Sender"));
                holder.replyBtn.setOnClickListener(v -> {
                    if (listener != null) {
                        listener.onReply(notification.getSenderId(), notification.getSenderName());
                    }
                });
            } else {
                android.util.Log.e("NotificationAdapter", "❌ REPLY BUTTON HIDDEN - Conditions not met:");
                android.util.Log.e("NotificationAdapter", "   - canReply is null or false: " + (notification.getCanReply() == null || !notification.getCanReply()));
                android.util.Log.e("NotificationAdapter", "   - senderId is null: " + (notification.getSenderId() == null));
                holder.replyBtn.setVisibility(View.GONE);
            }
        } else {
            holder.sessionDetailsText.setVisibility(View.GONE);
        }
        

        if (notification.isRead()) {
            holder.markReadBtn.setVisibility(View.GONE);
        } else {
            holder.markReadBtn.setVisibility(View.VISIBLE);
            holder.markReadBtn.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onMarkAsRead(notification);
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return notifications.size();
    }

    static class NotificationViewHolder extends RecyclerView.ViewHolder {
        TextView iconText, titleText, messageText, timeText, sessionDetailsText;
        Button joinMeetingBtn, replyBtn, markReadBtn;

        public NotificationViewHolder(@NonNull View itemView) {
            super(itemView);
            iconText = itemView.findViewById(R.id.notificationIconText);
            titleText = itemView.findViewById(R.id.notificationTitleText);
            messageText = itemView.findViewById(R.id.notificationMessageText);
            timeText = itemView.findViewById(R.id.notificationTimeText);
            sessionDetailsText = itemView.findViewById(R.id.sessionDetailsText);
            joinMeetingBtn = itemView.findViewById(R.id.joinMeetingBtn);
            replyBtn = itemView.findViewById(R.id.replyBtn);
            markReadBtn = itemView.findViewById(R.id.markReadBtn);
        }
    }
}
