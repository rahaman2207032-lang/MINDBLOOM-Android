package com.example.mindbloomandroid.service;



import com.google.firebase.database. DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase. database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.example.mindbloomandroid.model.TherapyNote;

import java. util.ArrayList;
import java. util.List;

public class TherapyNoteService {
    private DatabaseReference therapyNotesRef;

    public TherapyNoteService() {
        therapyNotesRef = FirebaseDatabase. getInstance().getReference("therapy_notes");
    }


    public void createTherapyNote(TherapyNote note, OnCompleteListener listener) {
        String noteId = therapyNotesRef. push().getKey();
        if (noteId != null) {
            note.setNoteId(noteId);
            note.setCreatedAt(System.currentTimeMillis());
            note.setUpdatedAt(System. currentTimeMillis());

            therapyNotesRef.child(noteId).setValue(note)
                    .addOnSuccessListener(aVoid -> listener.onSuccess())
                    .addOnFailureListener(e -> listener.onError(e.getMessage()));
        } else {
            listener.onError("Failed to generate note ID");
        }
    }



    public void getClientNotes(String clientId, String instructorId, OnNotesLoadedListener listener) {
        android.util.Log.d("TherapyNoteService", "📖 Loading notes for client: " + clientId + ", instructor: " + instructorId);

        therapyNotesRef.orderByChild("clientId").equalTo(clientId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        android.util.Log.d("TherapyNoteService", "📥 Received " + dataSnapshot.getChildrenCount() + " notes from Firebase");

                        List<TherapyNote> notes = new ArrayList<>();
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            TherapyNote note = snapshot.getValue(TherapyNote.class);
                            if (note != null && instructorId.equals(note.getInstructorId())) {
                                note.setNoteId(snapshot.getKey());
                                notes.add(note);
                                android.util.Log.d("TherapyNoteService", "   ✅ Loaded note: " + note.getNoteId() + " - " + note.getSessionType());
                            }
                        }

                        android.util.Log.d("TherapyNoteService", "✅ Total notes loaded: " + notes.size() + " (REAL-TIME)");
                        listener.onNotesLoaded(notes);
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                        android.util.Log.e("TherapyNoteService", "❌ Error loading notes: " + error.getMessage());
                        listener.onError(error.getMessage());
                    }
                });
    }


    public void getInstructorNotes(String instructorId, OnNotesLoadedListener listener) {
        therapyNotesRef.orderByChild("instructorId").equalTo(instructorId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        List<TherapyNote> notes = new ArrayList<>();
                        for (DataSnapshot snapshot :  dataSnapshot.getChildren()) {
                            TherapyNote note = snapshot.getValue(TherapyNote.class);
                            if (note != null) {
                                note.setNoteId(snapshot.getKey());
                                notes.add(note);
                            }
                        }
                        listener.onNotesLoaded(notes);
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                        listener.onError(error.getMessage());
                    }
                });
    }


    public void updateTherapyNote(TherapyNote note, OnCompleteListener listener) {
        if (note.getNoteId() != null) {
            note.setUpdatedAt(System. currentTimeMillis());

            therapyNotesRef.child(note.getNoteId()).setValue(note)
                    .addOnSuccessListener(aVoid -> listener.onSuccess())
                    .addOnFailureListener(e -> listener.onError(e.getMessage()));
        } else {
            listener.onError("Note ID is null");
        }
    }


    public void deleteTherapyNote(String noteId, OnCompleteListener listener) {
        therapyNotesRef. child(noteId).removeValue()
                .addOnSuccessListener(aVoid -> listener.onSuccess())
                .addOnFailureListener(e -> listener.onError(e.getMessage()));
    }

    // Interfaces
    public interface OnCompleteListener {
        void onSuccess();
        void onError(String error);
    }

    public interface OnNotesLoadedListener {
        void onNotesLoaded(List<TherapyNote> notes);
        void onError(String error);
    }
}