package com.example.mindbloomandroid.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.mindbloomandroid.R;
import com.example.mindbloomandroid.model.SessionRequest;
import com.example.mindbloomandroid.utility.DateTimeUtil;
import java.util.List;

public class SessionRequestAdapter extends RecyclerView.Adapter<SessionRequestAdapter.SessionRequestViewHolder> {

    private Context context;
    private List<SessionRequest> sessionRequests;

    public SessionRequestAdapter(Context context, List<SessionRequest> sessionRequests) {
        this.context = context;
        this.sessionRequests = sessionRequests;
    }

    @NonNull
    @Override
    public SessionRequestViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_session_request, parent, false);
        return new SessionRequestViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SessionRequestViewHolder holder, int position) {
        SessionRequest request = sessionRequests.get(position);
        
        holder.clientNameText.setText(request.getClientName());
        holder.statusText.setText(request.getStatus());
        holder.reasonText.setText(request.getReason());
        holder.dateText.setText("Requested: " + DateTimeUtil.formatDate(request.getRequestedDate().getTime()));

        if (request.getPreferredTime() != null && !request.getPreferredTime().isEmpty()) {
            holder.preferredTimeText.setVisibility(View.VISIBLE);
            holder.preferredTimeText.setText("Preferred: " + request.getPreferredTime());
        } else {
            holder.preferredTimeText.setVisibility(View.GONE);
        }

        // Color code by status
        int statusColor;
        switch (request.getStatus().toUpperCase()) {
            case "PENDING":
                statusColor = 0xFFFF9800; // Orange
                break;
            case "APPROVED":
            case "ACCEPTED":
                statusColor = 0xFF4CAF50; // Green
                break;
            case "COMPLETED":
                statusColor = 0xFF2196F3; // Blue
                break;
            case "REJECTED":
            case "CANCELLED":
                statusColor = 0xFFF44336; // Red
                break;
            default:
                statusColor = 0xFF9E9E9E; // Gray
        }
        holder.statusText.setTextColor(statusColor);
    }

    @Override
    public int getItemCount() {
        return sessionRequests.size();
    }

    static class SessionRequestViewHolder extends RecyclerView.ViewHolder {
        TextView clientNameText, statusText, reasonText, dateText, preferredTimeText;

        public SessionRequestViewHolder(@NonNull View itemView) {
            super(itemView);
            clientNameText = itemView.findViewById(R.id.clientNameText);
            statusText = itemView.findViewById(R.id.statusText);
            reasonText = itemView.findViewById(R.id.reasonText);
            dateText = itemView.findViewById(R.id.dateText);
            preferredTimeText = itemView.findViewById(R.id.preferredTimeText);
        }
    }
}
