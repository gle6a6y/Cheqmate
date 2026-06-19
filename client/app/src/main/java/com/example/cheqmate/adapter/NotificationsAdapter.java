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
        bindIcon(holder, n.getType());

        boolean unread = !n.isRead();
        holder.viewUnreadDot.setVisibility(unread ? View.VISIBLE : View.GONE);
        holder.tvTitle.setTextColor(holder.itemView.getContext().getColor(
                unread ? R.color.black : R.color.text_secondary));
        holder.tvBody.setTextColor(holder.itemView.getContext().getColor(
                unread ? R.color.text_secondary : R.color.money_gray));

        holder.itemView.setOnClickListener(v ->
                listener.onNotificationClicked(n, holder.getBindingAdapterPosition()));
    }

    private void bindIcon(NotificationViewHolder holder, String type) {
        if ("CHEQUE_ADDED".equals(type)) {
            holder.tvIcon.setText("🧾");
            holder.tvIcon.setBackgroundResource(R.drawable.bg_notification_icon_cheque);
        } else if ("GROUP_INVITE".equals(type)) {
            holder.tvIcon.setText("👥");
            holder.tvIcon.setBackgroundResource(R.drawable.bg_notification_icon_group);
        } else if ("DEBT_ADDED".equals(type)) {
            holder.tvIcon.setText("💸");
            holder.tvIcon.setBackgroundResource(R.drawable.bg_notification_icon_debt);
        } else if ("ACHIEVEMENT_UNLOCKED".equals(type)) {
            holder.tvIcon.setText("🏆");
            holder.tvIcon.setBackgroundResource(R.drawable.bg_notification_icon_achievement);
        } else {
            holder.tvIcon.setText("🔔");
            holder.tvIcon.setBackgroundResource(R.drawable.bg_notification_icon);
        }
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
        if (s.length() >= 16) {
            return s.substring(0, 16);
        }
        return s;
    }

    static class NotificationViewHolder extends RecyclerView.ViewHolder {
        final TextView tvIcon;
        final TextView tvTitle;
        final TextView tvBody;
        final TextView tvTime;
        final View viewUnreadDot;

        NotificationViewHolder(@NonNull View itemView) {
            super(itemView);
            tvIcon = itemView.findViewById(R.id.tvIcon);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvBody = itemView.findViewById(R.id.tvBody);
            tvTime = itemView.findViewById(R.id.tvTime);
            viewUnreadDot = itemView.findViewById(R.id.viewUnreadDot);
        }
    }
}
