package com.example.mindbloomandroid.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.mindbloomandroid.R;
import com.example.mindbloomandroid.model.MoodLog;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MoodLogAdapter extends RecyclerView.Adapter<MoodLogAdapter.MoodLogViewHolder> {

    private Context context;
    private List<MoodLog> moodLogs;
    private OnDeleteClickListener deleteListener;

    public interface OnDeleteClickListener {
        void onDeleteClick(MoodLog moodLog);
    }

    public MoodLogAdapter(Context context, List<MoodLog> moodLogs, OnDeleteClickListener deleteListener) {
        this.context = context;
        this.moodLogs = moodLogs;
        this.deleteListener = deleteListener;
    }

    @NonNull
    @Override
    public MoodLogViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_mood_log, parent, false);
        return new MoodLogViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MoodLogViewHolder holder, int position) {
        MoodLog moodLog = moodLogs.get(position);
        
        holder.moodEmojiText.setText(moodLog.getMoodEmoji());
        holder.moodRatingText.setText("Mood: " + moodLog.getMoodRating() + "/5");
        
        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy - hh:mm a", Locale.getDefault());
        holder.dateText.setText(sdf.format(new Date(moodLog.getLogDate())));
        
        if (moodLog.getNotes() != null && !moodLog.getNotes().isEmpty()) {
            holder.notesText.setVisibility(View.VISIBLE);
            holder.notesText.setText(moodLog.getNotes());
        } else {
            holder.notesText.setVisibility(View.GONE);
        }
        
        if (moodLog.getActivities() != null && !moodLog.getActivities().isEmpty()) {
            holder.activitiesText.setVisibility(View.VISIBLE);
            holder.activitiesText.setText("Activities: " + moodLog.getActivities());
        } else {
            holder.activitiesText.setVisibility(View.GONE);
        }
        
        holder.itemView.setOnLongClickListener(v -> {
            if (deleteListener != null) {
                deleteListener.onDeleteClick(moodLog);
            }
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return moodLogs.size();
    }

    static class MoodLogViewHolder extends RecyclerView.ViewHolder {
        TextView moodEmojiText, moodRatingText, dateText, notesText, activitiesText;

        public MoodLogViewHolder(@NonNull View itemView) {
            super(itemView);
            moodEmojiText = itemView.findViewById(R.id.moodEmojiText);
            moodRatingText = itemView.findViewById(R.id.moodRatingText);
            dateText = itemView.findViewById(R.id.dateText);
            notesText = itemView.findViewById(R.id.notesText);
            activitiesText = itemView.findViewById(R.id.activitiesText);
        }
    }
}
