package com.example.mindbloomandroid.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.mindbloomandroid.R;
import com.example.mindbloomandroid.model.SleepEntry;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class SleepEntryAdapter extends RecyclerView.Adapter<SleepEntryAdapter.SleepEntryViewHolder> {

    private Context context;
    private List<SleepEntry> sleepEntries;

    public SleepEntryAdapter(Context context, List<SleepEntry> sleepEntries) {
        this.context = context;
        this.sleepEntries = sleepEntries;
    }

    @NonNull
    @Override
    public SleepEntryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_sleep_entry, parent, false);
        return new SleepEntryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SleepEntryViewHolder holder, int position) {
        SleepEntry entry = sleepEntries.get(position);
        
        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
        SimpleDateFormat timeSdf = new SimpleDateFormat("hh:mm a", Locale.getDefault());
        
        holder.dateText.setText(sdf.format(new Date(entry.getSleepStartTime())));
        holder.bedtimeText.setText("Bedtime: " + timeSdf.format(new Date(entry.getSleepStartTime())));
        holder.wakeTimeText.setText("Wake: " + timeSdf.format(new Date(entry.getSleepEndTime())));
        holder.durationText.setText(String.format("%.1f hours", entry.getSleepDurationHours()));
        holder.qualityText.setText("Quality: " + entry.getSleepQuality() + "/5");

        if (entry.getNotes() != null && !entry.getNotes().isEmpty()) {
            holder.notesText.setVisibility(View.VISIBLE);
            holder.notesText.setText(entry.getNotes());
        } else {
            holder.notesText.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return sleepEntries.size();
    }

    static class SleepEntryViewHolder extends RecyclerView.ViewHolder {
        TextView dateText, bedtimeText, wakeTimeText, durationText, qualityText, notesText;

        public SleepEntryViewHolder(@NonNull View itemView) {
            super(itemView);
            dateText = itemView.findViewById(R.id.dateText);
            bedtimeText = itemView.findViewById(R.id.bedtimeText);
            wakeTimeText = itemView.findViewById(R.id.wakeTimeText);
            durationText = itemView.findViewById(R.id.durationText);
            qualityText = itemView.findViewById(R.id.qualityText);
            notesText = itemView.findViewById(R.id.notesText);
        }
    }
}
