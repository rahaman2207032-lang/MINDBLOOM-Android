package com.example.mindbloomandroid.fragments;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.mindbloomandroid.R;
import com.example.mindbloomandroid.adapter.TherapyNoteAdapter;
import com.example.mindbloomandroid.model.ClientOverview;
import com.example.mindbloomandroid.model.TherapyNote;
import com.example.mindbloomandroid.service.InstructorService;
import com.example.mindbloomandroid.service.TherapyNoteService;
import com.example.mindbloomandroid.utility.SharedPreferencesManager;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;


public class TherapyNotesFragment extends Fragment {

    private Spinner clientSpinner;
    private TextInputEditText sessionDate;
    private AutoCompleteTextView sessionType;
    private TextInputEditText therapyNotes;
    private MaterialButton btnSaveNotes;
    private RecyclerView previousNotesRecyclerView;
    private ProgressBar progressBar;

    private InstructorService instructorService;
    private TherapyNoteService therapyNoteService;
    private SharedPreferencesManager prefsManager;
    private TherapyNoteAdapter noteAdapter;
    
    private List<ClientOverview> clients;
    private List<TherapyNote> previousNotes;
    private String selectedClientId;
    private Calendar selectedCalendar;
    private SimpleDateFormat dateFormat;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_therapy_notes, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        try {
            android.util.Log.d("TherapyNotesFragment", "🎯 onViewCreated started");

            initializeViews(view);

            instructorService = new InstructorService();
            therapyNoteService = new TherapyNoteService();
            prefsManager = SharedPreferencesManager.getInstance(requireContext());
            clients = new ArrayList<>();
            previousNotes = new ArrayList<>();
            selectedCalendar = Calendar.getInstance();
            dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());

            setupRecyclerView();
            setupDatePicker();
            setupSessionTypeDropdown();
            setupSaveButton();
            loadClients();

            android.util.Log.d("TherapyNotesFragment", "✅ onViewCreated completed successfully");
        } catch (Exception e) {
            android.util.Log.e("TherapyNotesFragment", "❌ CRASH in onViewCreated: " + e.getMessage(), e);
            if (getContext() != null) {
                Toast.makeText(getContext(), "Error loading therapy notes: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        }
    }


    private void initializeViews(View view) {
        clientSpinner = view.findViewById(R.id.clientSpinner);
        sessionDate = view.findViewById(R.id.sessionDate);
        sessionType = view.findViewById(R.id.sessionType);
        therapyNotes = view.findViewById(R.id.therapyNotes);
        btnSaveNotes = view.findViewById(R.id.btnSaveNotes);
        previousNotesRecyclerView = view.findViewById(R.id.previousNotesRecyclerView);
        progressBar = view.findViewById(R.id.progressBar);
    }

    private void setupRecyclerView() {
        noteAdapter = new TherapyNoteAdapter(requireContext(), previousNotes);
        previousNotesRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        previousNotesRecyclerView.setAdapter(noteAdapter);
    }

    private void setupDatePicker() {
        sessionDate.setText(dateFormat.format(selectedCalendar.getTime()));
        sessionDate.setOnClickListener(v -> showDatePicker());
    }

    private void showDatePicker() {
        DatePickerDialog dialog = new DatePickerDialog(
            requireContext(),
            (view, year, month, dayOfMonth) -> {
                selectedCalendar.set(year, month, dayOfMonth);
                sessionDate.setText(dateFormat.format(selectedCalendar.getTime()));
            },
            selectedCalendar.get(Calendar.YEAR),
            selectedCalendar.get(Calendar.MONTH),
            selectedCalendar.get(Calendar.DAY_OF_MONTH)
        );
        dialog.show();
    }

    private void setupSessionTypeDropdown() {
        String[] sessionTypes = {
            "Initial Consultation",
            "Cognitive Behavioral Therapy (CBT)",
            "Mindfulness Meditation",
            "Stress Management",
            "Anxiety Treatment",
            "Depression Support",
            "Follow-up Session",
            "Crisis Intervention"
        };
        
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
            requireContext(),
            android.R.layout.simple_dropdown_item_1line,
            sessionTypes
        );
        sessionType.setAdapter(adapter);
    }

    private void setupSaveButton() {
        btnSaveNotes.setOnClickListener(v -> saveTherapyNote());
    }

    private void loadClients() {
        String instructorId = prefsManager.getUserId();
        if (instructorId == null) {
            Toast.makeText(requireContext(), "User not logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        instructorService.getAllClients(instructorId, new InstructorService.OnClientsLoadedListener() {
            @Override
            public void onClientsLoaded(List<ClientOverview> loadedClients) {
                if (getActivity() == null) return;
                
                progressBar.setVisibility(View.GONE);
                clients.clear();
                clients.addAll(loadedClients);
                setupClientSpinner();
            }

            @Override
            public void onError(String error) {
                if (getActivity() == null) return;
                
                progressBar.setVisibility(View.GONE);
                Toast.makeText(requireContext(), "Error loading clients: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupClientSpinner() {
        List<String> clientNames = new ArrayList<>();
        clientNames.add("Select a client...");
        for (ClientOverview client : clients) {
            clientNames.add(client.getClientName());
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
            requireContext(),
            android.R.layout.simple_spinner_item,
            clientNames
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        clientSpinner.setAdapter(adapter);

        clientSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position > 0) {
                    selectedClientId = clients.get(position - 1).getClientId();
                    loadPreviousNotes();
                } else {
                    selectedClientId = null;
                    previousNotes.clear();
                    noteAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                selectedClientId = null;
            }
        });
    }

    private void loadPreviousNotes() {
        if (selectedClientId == null) return;

        String instructorId = prefsManager.getUserId();
        progressBar.setVisibility(View.VISIBLE);
        
        therapyNoteService.getClientNotes(selectedClientId, instructorId, new TherapyNoteService.OnNotesLoadedListener() {
            @Override
            public void onNotesLoaded(List<TherapyNote> notes) {
                if (getActivity() == null) return;
                
                progressBar.setVisibility(View.GONE);
                previousNotes.clear();
                previousNotes.addAll(notes);
                noteAdapter.notifyDataSetChanged();
            }

            @Override
            public void onError(String error) {
                if (getActivity() == null) return;
                
                progressBar.setVisibility(View.GONE);
                Toast.makeText(requireContext(), "Error: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void saveTherapyNote() {
        if (selectedClientId == null) {
            Toast.makeText(requireContext(), "Please select a client", Toast.LENGTH_SHORT).show();
            return;
        }

        String type = sessionType.getText().toString().trim();
        String notes = therapyNotes.getText().toString().trim();

        if (type.isEmpty()) {
            sessionType.setError("Please select session type");
            return;
        }

        if (notes.isEmpty()) {
            therapyNotes.setError("Please enter therapy notes");
            return;
        }

        String instructorId = prefsManager.getUserId();
        progressBar.setVisibility(View.VISIBLE);

        TherapyNote note = new TherapyNote();
        note.setInstructorId(instructorId);
        note.setClientId(selectedClientId);
        note.setSessionDate(selectedCalendar.getTimeInMillis());
        note.setSessionType(type);
        note.setNotes(notes);
        note.setCreatedAt(System.currentTimeMillis());

        therapyNoteService.createTherapyNote(note, new TherapyNoteService.OnCompleteListener() {
            @Override
            public void onSuccess() {
                if (getActivity() == null) return;
                
                progressBar.setVisibility(View.GONE);
                android.util.Log.d("TherapyNotesFragment", "✅ Note saved successfully!");
                Toast.makeText(requireContext(), "Notes saved successfully", Toast.LENGTH_SHORT).show();
                therapyNotes.setText("");

                // Note: loadPreviousNotes() is optional here since we now use ValueEventListener
                // which automatically updates when data changes. But we keep it for immediate feedback.
                loadPreviousNotes();
            }

            @Override
            public void onError(String error) {
                if (getActivity() == null) return;
                
                progressBar.setVisibility(View.GONE);
                android.util.Log.e("TherapyNotesFragment", "❌ Error saving note: " + error);
                Toast.makeText(requireContext(), "Error: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
