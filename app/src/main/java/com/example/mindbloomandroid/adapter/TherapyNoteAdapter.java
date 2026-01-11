package com.example.mindbloomandroid.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.mindbloomandroid.R;
import com.example.mindbloomandroid.model.TherapyNote;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class TherapyNoteAdapter extends RecyclerView.Adapter<TherapyNoteAdapter.NoteViewHolder> {

    private Context context;
    private List<TherapyNote> notes;
    private SimpleDateFormat dateFormat;

    public TherapyNoteAdapter(Context context, List<TherapyNote> notes) {
        this.context = context;
        this.notes = notes;
        this.dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
    }

    @NonNull
    @Override
    public NoteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_therapy_note, parent, false);
        return new NoteViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NoteViewHolder holder, int position) {
        TherapyNote note = notes.get(position);

        holder.noteDate.setText(dateFormat.format(new Date(note.getSessionDate())));
        holder.noteType.setText(note.getSessionType());
        holder.noteContent.setText(note.getNotes());
    }

    @Override
    public int getItemCount() {
        return notes.size();
    }

    static class NoteViewHolder extends RecyclerView.ViewHolder {
        TextView noteDate, noteType, noteContent;

        public NoteViewHolder(@NonNull View itemView) {
            super(itemView);
            noteDate = itemView.findViewById(R.id.noteDate);
            noteType = itemView.findViewById(R.id.noteType);
            noteContent = itemView.findViewById(R.id.noteContent);
        }
    }
}
