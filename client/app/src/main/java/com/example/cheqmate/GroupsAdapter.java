package com.example.cheqmate;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class GroupsAdapter extends RecyclerView.Adapter<GroupsAdapter.GroupViewHolder> {

    private List<AnalyticsActivity.Group> groupsList;

    public GroupsAdapter(List<AnalyticsActivity.Group> groupsList) {
        this.groupsList = groupsList;
    }

    public void setData(List<AnalyticsActivity.Group> groupsList) {
        this.groupsList = groupsList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public GroupViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_group, parent, false);
        return new GroupViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull GroupViewHolder holder, int position) {
        AnalyticsActivity.Group group = groupsList.get(position);
        holder.ivIcon.setImageResource(group.getIconResId());
        holder.tvName.setText(group.getName());
        holder.tvAmount.setText(group.getAmount());
    }

    @Override
    public int getItemCount() {
        return groupsList != null ? groupsList.size() : 0;
    }

    static class GroupViewHolder extends RecyclerView.ViewHolder {
        ImageView ivIcon;
        TextView tvName;
        TextView tvAmount;

        GroupViewHolder(@NonNull View itemView) {
            super(itemView);
            ivIcon = itemView.findViewById(R.id.ivGroupIcon);
            tvName = itemView.findViewById(R.id.tvGroupName);
            tvAmount = itemView.findViewById(R.id.tvGroupAmount);
        }
    }
}