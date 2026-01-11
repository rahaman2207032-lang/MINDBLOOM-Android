package com.example.mindbloomandroid.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.mindbloomandroid.R;
import com.example.mindbloomandroid.model.ClientOverview;
import com.google.android.material.button.MaterialButton;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ClientAdapter extends RecyclerView.Adapter<ClientAdapter.ClientViewHolder> {

    private Context context;
    private List<ClientOverview> clients;
    private OnClientClickListener listener;

    public interface OnClientClickListener {
        void onViewClient(ClientOverview client);
    }

    public ClientAdapter(Context context, List<ClientOverview> clients) {
        this.context = context;
        this.clients = clients;
    }

    public void setOnClientClickListener(OnClientClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public ClientViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_client, parent, false);
        return new ClientViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ClientViewHolder holder, int position) {
        ClientOverview client = clients.get(position);

        String initial = client.getClientName() != null && !client.getClientName().isEmpty()
                ? client.getClientName().substring(0, 1).toUpperCase()
                : "?";
        holder.clientInitial.setText(initial);
        holder.clientName.setText(client.getClientName());

        // Use data from ClientOverview
        holder.clientSessions.setText(String.valueOf(client.getTotalSessions()));

        if (client.getLastSessionDate() > 0) {
            SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
            holder.clientLastSession.setText(sdf.format(new Date(client.getLastSessionDate())));
        } else {
            holder.clientLastSession.setText("No sessions");
        }

        holder.btnViewClient.setOnClickListener(v -> {

            android.content.Intent intent = new android.content.Intent(context,
                com.example.mindbloomandroid.activities.ClientProgressActivity.class);
            intent.putExtra("CLIENT_ID", client.getClientId());
            intent.putExtra("CLIENT_NAME", client.getClientName());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return clients.size();
    }

    static class ClientViewHolder extends RecyclerView.ViewHolder {
        TextView clientInitial, clientName, clientSessions, clientLastSession;
        MaterialButton btnViewClient;

        public ClientViewHolder(@NonNull View itemView) {
            super(itemView);
            clientInitial = itemView.findViewById(R.id.clientInitial);
            clientName = itemView.findViewById(R.id.clientName);
            clientSessions = itemView.findViewById(R.id.clientSessions);
            clientLastSession = itemView.findViewById(R.id.clientLastSession);
            btnViewClient = itemView.findViewById(R.id.btnViewClient);
        }
    }
}
