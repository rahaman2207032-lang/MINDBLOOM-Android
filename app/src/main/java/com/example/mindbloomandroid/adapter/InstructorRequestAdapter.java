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
import com.example.mindbloomandroid.model.SessionRequest;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;


public class InstructorRequestAdapter extends RecyclerView.Adapter<InstructorRequestAdapter.ViewHolder> {

    private Context context;
    private List<SessionRequest> requests;
    private OnRequestActionListener acceptListener;
    private OnRequestActionListener declineListener;

    public interface OnRequestActionListener {
        void onAction(SessionRequest request);
    }

    public InstructorRequestAdapter(Context context, List<SessionRequest> requests,
                                    OnRequestActionListener acceptListener,
                                    OnRequestActionListener declineListener) {
        this.context = context;
        this.requests = requests;
        this.acceptListener = acceptListener;
        this.declineListener = declineListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_instructor_request, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        SessionRequest request = requests.get(position);


        holder.clientNameText.setText("👤 " + (request.getClientName() != null ?
                request.getClientName() : "Unknown Client"));


        SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
        SimpleDateFormat timeFormat = new SimpleDateFormat("h:mm a", Locale.getDefault());
        String formattedDate = dateFormat.format(new Date(request.getRequestedDateTime()));
        String formattedTime = timeFormat.format(new Date(request.getRequestedDateTime()));
        holder.dateTimeText.setText("📅 " + formattedDate + " • 🕐 " + formattedTime);


        holder.sessionTypeText.setText("📝 " + request.getSessionType());


        if (request.getReason() != null && !request.getReason().isEmpty()) {
            holder.reasonText.setVisibility(View.VISIBLE);
            holder.reasonText.setText("\"" + request.getReason() + "\"");
        } else {
            holder.reasonText.setVisibility(View.GONE);
        }


        SimpleDateFormat requestDateFormat = new SimpleDateFormat("MMM dd", Locale.getDefault());
        String requestDate = requestDateFormat.format(new Date(request.getCreatedAt()));
        holder.requestDateText.setText("Requested: " + requestDate);


        holder.acceptButton.setOnClickListener(v -> {
            if (acceptListener != null) {
                acceptListener.onAction(request);
            }
        });

        holder.declineButton.setOnClickListener(v -> {
            if (declineListener != null) {
                declineListener.onAction(request);
            }
        });
    }

    @Override
    public int getItemCount() {
        return requests.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView clientNameText;
        TextView dateTimeText;
        TextView sessionTypeText;
        TextView reasonText;
        TextView requestDateText;
        Button acceptButton;
        Button declineButton;

        ViewHolder(View itemView) {
            super(itemView);
            clientNameText = itemView.findViewById(R.id.clientNameText);
            dateTimeText = itemView.findViewById(R.id.dateTimeText);
            sessionTypeText = itemView.findViewById(R.id.sessionTypeText);
            reasonText = itemView.findViewById(R.id.reasonText);
            requestDateText = itemView.findViewById(R.id.requestDateText);
            acceptButton = itemView.findViewById(R.id.acceptButton);
            declineButton = itemView.findViewById(R.id.declineButton);
        }
    }
}
