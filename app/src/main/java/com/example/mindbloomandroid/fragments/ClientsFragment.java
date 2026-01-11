package com.example.mindbloomandroid.fragments;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.mindbloomandroid.R;
import com.example.mindbloomandroid.adapter.ClientAdapter;
import com.example.mindbloomandroid.model.ClientOverview;
import com.example.mindbloomandroid.service.InstructorService;
import com.example.mindbloomandroid.utility.SharedPreferencesManager;
import java.util.ArrayList;
import java.util.List;


public class ClientsFragment extends Fragment {

    private RecyclerView clientsRecyclerView;
    private EditText searchClients;
    private ProgressBar progressBar;

    private InstructorService instructorService;
    private SharedPreferencesManager prefsManager;
    private ClientAdapter clientAdapter;
    private List<ClientOverview> allClients;
    private List<ClientOverview> filteredClients;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_clients, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        try {
            android.util.Log.d("ClientsFragment", "🎯 onViewCreated started");

            initializeViews(view);

            instructorService = new InstructorService();
            prefsManager = SharedPreferencesManager.getInstance(requireContext());
            allClients = new ArrayList<>();
            filteredClients = new ArrayList<>();

            setupRecyclerView();
            setupSearchFilter();
            loadClients();

            android.util.Log.d("ClientsFragment", "✅ onViewCreated completed successfully");
        } catch (Exception e) {
            android.util.Log.e("ClientsFragment", "❌ CRASH in onViewCreated: " + e.getMessage(), e);
            if (getContext() != null) {
                Toast.makeText(getContext(), "Error loading clients: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        }
    }

    private void initializeViews(View view) {
        clientsRecyclerView = view.findViewById(R.id.clientsRecyclerView);
        searchClients = view.findViewById(R.id.searchClients);
        progressBar = view.findViewById(R.id.progressBar);
    }

    private void setupRecyclerView() {
        clientAdapter = new ClientAdapter(requireContext(), filteredClients);
        clientsRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        clientsRecyclerView.setAdapter(clientAdapter);
    }

    private void setupSearchFilter() {
        searchClients.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterClients(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void filterClients(String query) {
        filteredClients.clear();
        
        if (query == null || query.isEmpty()) {
            filteredClients.addAll(allClients);
        } else {
            String lowerQuery = query.toLowerCase();
            for (ClientOverview client : allClients) {

                if (client != null && client.getClientName() != null) {
                    if (client.getClientName().toLowerCase().contains(lowerQuery)) {
                        filteredClients.add(client);
                    }
                }
            }
        }
        
        if (clientAdapter != null) {
            clientAdapter.notifyDataSetChanged();
        }
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
            public void onClientsLoaded(List<ClientOverview> clients) {
                if (getActivity() == null) return;
                
                progressBar.setVisibility(View.GONE);
                allClients.clear();
                allClients.addAll(clients);
                filteredClients.clear();
                filteredClients.addAll(clients);
                clientAdapter.notifyDataSetChanged();

                if (clients.isEmpty()) {
                    Toast.makeText(requireContext(), "No clients yet", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onError(String error) {
                if (getActivity() == null) return;
                
                progressBar.setVisibility(View.GONE);
                Toast.makeText(requireContext(), "Error: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
