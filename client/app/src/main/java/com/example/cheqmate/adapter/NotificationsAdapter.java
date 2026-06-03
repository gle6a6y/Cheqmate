package com.example.cheqmate.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cheqmate.R;
import com.example.cheqmate.dto.NotificationResponse;

import java.util.List;

public class NotificationsAdapter extends RecyclerView.Adapter<NotificationsAdapter.NotificationViewHolder> {

    public interface OnNotificationClickListener {
        void onNotificationClicked(NotificationResponse notification, int position);
    }

    private final List<NotificationResponse> notifications;
    private final OnNotificationClickListener listener;

    public NotificationsAdapter(List<NotificationResponse> notifications, OnNotificationClickListener listener) {
        this.notifications = notifications;
        this.listener = listener;
    }

    @NonNull
    @Override
    public NotificationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_notification, parent, false);
        return new NotificationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NotificationViewHolder holder, int position) {
        NotificationResponse n = notifications.get(position);
        holder.tvTitle.setText(n.getTitle());
        holder.tvBody.setText(n.getBody());
        holder.tvTime.setText(formatTime(n.getCreatedAt()));

        holder.viewUnreadDot.setVisibility(n.isRead() ? View.INVISIBLE : View.VISIBLE);
        holder.itemView.setAlpha(n.isRead() ? 0.6f : 1.0f);

        holder.itemView.setOnClickListener(v ->
                listener.onNotificationClicked(n, holder.getBindingAdapterPosition()));
    }

    @Override
    public int getItemCount() {
        return notifications.size();
    }

    private String formatTime(String isoCreatedAt) {
        if (isoCreatedAt == null) {
            return "";
        }
        String s = isoCreatedAt.replace('T', ' ');
        return s.length() >= 16 ? s.substring(0, 16) : s;
    }

    static class NotificationViewHolder extends RecyclerView.ViewHolder {
        final TextView tvTitle;
        final TextView tvBody;
        final TextView tvTime;
        final View viewUnreadDot;

        NotificationViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvBody = itemView.findViewById(R.id.tvBody);
            tvTime = itemView.findViewById(R.id.tvTime);
            viewUnreadDot = itemView.findViewById(R.id.viewUnreadDot);
        }
    }
}
