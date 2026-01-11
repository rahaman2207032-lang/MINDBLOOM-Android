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
import com.example.mindbloomandroid.model.Habit;
import java.util.List;

public class HabitAdapter extends RecyclerView.Adapter<HabitAdapter.HabitViewHolder> {

    public interface OnHabitCompleteListener {
        void onHabitComplete(Habit habit);
    }

    public interface OnHabitHistoryListener {
        void onViewHistory(Habit habit);
    }

    public interface OnHabitDeleteListener {
        void onDelete(Habit habit);
    }

    private Context context;
    private List<Habit> habits;
    private OnHabitCompleteListener completeListener;
    private OnHabitHistoryListener historyListener;
    private OnHabitDeleteListener deleteListener;

    public HabitAdapter(Context context, List<Habit> habits,
                       OnHabitCompleteListener completeListener,
                       OnHabitHistoryListener historyListener,
                       OnHabitDeleteListener deleteListener) {
        this.context = context;
        this.habits = habits;
        this.completeListener = completeListener;
        this.historyListener = historyListener;
        this.deleteListener = deleteListener;
    }

    @NonNull
    @Override
    public HabitViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_habit, parent, false);
        return new HabitViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HabitViewHolder holder, int position) {
        Habit habit = habits.get(position);
        
        holder.nameText.setText(habit.getName());
        holder.descriptionText.setText(habit.getDescription());
        holder.streakText.setText("🔥 " + habit.getCurrentStreak() + " day streak");
        holder.frequencyText.setText(habit.getFrequency());
        
        holder.completeButton.setOnClickListener(v -> {
            if (completeListener != null) {
                completeListener.onHabitComplete(habit);
            }
        });

        holder.itemView.setOnClickListener(v -> {
            if (historyListener != null) {
                historyListener.onViewHistory(habit);
            }
        });

        holder.itemView.setOnLongClickListener(v -> {
            if (deleteListener != null) {
                deleteListener.onDelete(habit);
            }
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return habits.size();
    }

    static class HabitViewHolder extends RecyclerView.ViewHolder {
        TextView nameText, descriptionText, streakText, frequencyText;
        Button completeButton;

        public HabitViewHolder(@NonNull View itemView) {
            super(itemView);
            nameText = itemView.findViewById(R.id.habitNameText);
            descriptionText = itemView.findViewById(R.id.habitDescriptionText);
            streakText = itemView.findViewById(R.id.streakText);
            frequencyText = itemView.findViewById(R.id.frequencyText);
            completeButton = itemView.findViewById(R.id.completeButton);
        }
    }
}
