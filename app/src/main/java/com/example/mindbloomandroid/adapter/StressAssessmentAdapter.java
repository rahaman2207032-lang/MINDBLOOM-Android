package com.example.mindbloomandroid.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.mindbloomandroid.R;
import com.example.mindbloomandroid.model.StressAssessment;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class StressAssessmentAdapter extends RecyclerView.Adapter<StressAssessmentAdapter.StressAssessmentViewHolder> {

    private Context context;
    private List<StressAssessment> assessments;
    private OnDeleteClickListener deleteListener;

    public interface OnDeleteClickListener {
        void onDeleteClick(StressAssessment assessment);
    }

    public StressAssessmentAdapter(Context context, List<StressAssessment> assessments, OnDeleteClickListener deleteListener) {
        this.context = context;
        this.assessments = assessments;
        this.deleteListener = deleteListener;
    }

    @NonNull
    @Override
    public StressAssessmentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_stress_assessment, parent, false);
        return new StressAssessmentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull StressAssessmentViewHolder holder, int position) {
        StressAssessment assessment = assessments.get(position);
        
        holder.dateText.setText(assessment.getFormattedAssessmentDate());
        holder.levelText.setText(assessment.getStressLevel() + " - Score: " + 
                                assessment.getStressScore() + "/35");
        

        if (assessment.getStressLevel().equals("LOW")) {
            holder.levelText.setTextColor(0xFF4CAF50); // Green
        } else if (assessment.getStressLevel().equals("MODERATE")) {
            holder.levelText.setTextColor(0xFFFFC107); // Orange
        } else {
            holder.levelText.setTextColor(0xFFF44336); // Red
        }
        

        String breakdown = "Workload: " + assessment.getWorkloadLevel() +
                         ", Sleep: " + assessment.getSleepQualityLevel() +
                         ", Anxiety: " + assessment.getAnxietyLevel() +
                         "\nMood: " + assessment.getMoodLevel() +
                         ", Physical: " + assessment.getPhysicalSymptomsLevel() +
                         ", Concentration: " + assessment.getConcentrationLevel() +
                         ", Social: " + assessment.getSocialConnectionLevel();
        holder.triggersText.setVisibility(View.VISIBLE);
        holder.triggersText.setText(breakdown);
        
        holder.symptomsText.setVisibility(View.GONE);
        
        if (assessment.getNotes() != null && !assessment.getNotes().isEmpty()) {
            holder.notesText.setVisibility(View.VISIBLE);
            holder.notesText.setText("Notes: " + assessment.getNotes());
        } else {
            holder.notesText.setVisibility(View.GONE);
        }
        
        holder.itemView.setOnLongClickListener(v -> {
            if (deleteListener != null) {
                deleteListener.onDeleteClick(assessment);
            }
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return assessments.size();
    }

    static class StressAssessmentViewHolder extends RecyclerView.ViewHolder {
        TextView dateText, levelText, triggersText, symptomsText, notesText;

        public StressAssessmentViewHolder(@NonNull View itemView) {
            super(itemView);
            dateText = itemView.findViewById(R.id.dateText);
            levelText = itemView.findViewById(R.id.levelText);
            triggersText = itemView.findViewById(R.id.triggersText);
            symptomsText = itemView.findViewById(R.id.symptomsText);
            notesText = itemView.findViewById(R.id.notesText);
        }
    }
}
