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
import com.example.mindbloomandroid.model.TherapySession;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;


public class TherapySessionAdapter extends RecyclerView.Adapter<TherapySessionAdapter.SessionViewHolder> {

    private Context context;
    private List<TherapySession> sessions;
    private OnJoinMeetingClickListener joinMeetingListener;

    public interface OnJoinMeetingClickListener {
        void onJoinMeetingClick(String zoomLink);
    }

    public TherapySessionAdapter(Context context, List<TherapySession> sessions,
                                 OnJoinMeetingClickListener listener) {
        this.context = context;
        this.sessions = sessions;
        this.joinMeetingListener = listener;
    }

    @NonNull
    @Override
    public SessionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_confirmed_session, parent, false);
        return new SessionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SessionViewHolder holder, int position) {
        TherapySession session = sessions.get(position);


        holder.sessionTypeText.setText(session.getSessionType() != null ?
                "✅ " + session.getSessionType() : "✅ Therapy Session");


        SimpleDateFormat dateFormat = new SimpleDateFormat("EEEE, MMM dd, yyyy", Locale.getDefault());
        SimpleDateFormat timeFormat = new SimpleDateFormat("h:mm a", Locale.getDefault());
        Date sessionDate = new Date(session.getSessionDate());
        holder.dateTimeText.setText(dateFormat.format(sessionDate) + " at " + timeFormat.format(sessionDate));


        holder.statusBadge.setText(session.getStatus() != null ? session.getStatus() : "CONFIRMED");
        holder.statusBadge.setBackgroundColor(0xFF27AE60); // Green for confirmed


        if (session.getInstructorName() != null && !session.getInstructorName().isEmpty()) {
            holder.instructorText.setVisibility(View.VISIBLE);
            holder.instructorText.setText("with " + session.getInstructorName());
        } else {
            holder.instructorText.setVisibility(View.GONE);
        }


        if (session.getNotes() != null && !session.getNotes().isEmpty()) {
            holder.notesText.setVisibility(View.VISIBLE);
            holder.notesText.setText(session.getNotes());
        } else {
            holder.notesText.setVisibility(View.GONE);
        }


        holder.joinMeetingBtn.setOnClickListener(v -> {
            if (joinMeetingListener != null) {
                joinMeetingListener.onJoinMeetingClick(session.getZoomLink());
            }
        });


        if (session.getZoomLink() == null || session.getZoomLink().isEmpty()) {
            holder.joinMeetingBtn.setVisibility(View.GONE);
        } else {
            holder.joinMeetingBtn.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public int getItemCount() {
        return sessions.size();
    }

    static class SessionViewHolder extends RecyclerView.ViewHolder {
        TextView sessionTypeText, dateTimeText, statusBadge, instructorText, notesText;
        Button joinMeetingBtn;

        public SessionViewHolder(@NonNull View itemView) {
            super(itemView);
            sessionTypeText = itemView.findViewById(R.id.sessionTypeText);
            dateTimeText = itemView.findViewById(R.id.dateTimeText);
            statusBadge = itemView.findViewById(R.id.statusBadge);
            instructorText = itemView.findViewById(R.id.instructorText);
            notesText = itemView.findViewById(R.id.notesText);
            joinMeetingBtn = itemView.findViewById(R.id.joinMeetingBtn);
        }
    }
}
