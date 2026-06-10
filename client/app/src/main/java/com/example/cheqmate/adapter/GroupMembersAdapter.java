package com.example.cheqmate.adapter;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cheqmate.R;
import com.example.cheqmate.model.GroupMember;

import java.util.ArrayList;
import java.util.List;

public class GroupMembersAdapter extends RecyclerView.Adapter<GroupMembersAdapter.ViewHolder> {

    private final List<GroupMember> members;

    public GroupMembersAdapter(List<GroupMember> members) {
        this.members = members;
    }

    public void setMembers(List<GroupMember> newMembers) {
        members.clear();
        members.addAll(new ArrayList<>(newMembers));
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_group_member, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        GroupMember member = members.get(position);
        holder.tvName.setText(member.getName());
        holder.tvYouBadge.setVisibility(member.isCurrentUser() ? View.VISIBLE : View.GONE);

        Double rating = member.getReliabilityRating();
        if (rating == null) {
            holder.tvReliability.setText(R.string.reliability_unknown);
            holder.tvReliability.setTextColor(
                    ContextCompat.getColor(holder.itemView.getContext(), R.color.text_secondary));
            holder.tvReliability.setBackgroundColor(Color.TRANSPARENT);
        } else {
            int rounded = (int) Math.round(rating);
            holder.tvReliability.setText(holder.itemView.getContext()
                    .getString(R.string.reliability_percent, rounded));
            applyRatingStyle(holder, rounded);
        }
    }

    private void applyRatingStyle(ViewHolder holder, int percent) {
        int textColor;
        int bgColor;
        if (percent >= 80) {
            textColor = R.color.money_positive;
            bgColor = Color.parseColor("#E8F5E9");
        } else if (percent >= 50) {
            textColor = R.color.black;
            bgColor = Color.parseColor("#F5F5F5");
        } else {
            textColor = R.color.logout_red;
            bgColor = Color.parseColor("#FFEBEE");
        }
        holder.tvReliability.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), textColor));
        holder.tvReliability.setBackgroundColor(bgColor);
    }

    @Override
    public int getItemCount() {
        return members.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivAvatar;
        TextView tvName;
        TextView tvYouBadge;
        TextView tvReliability;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivAvatar = itemView.findViewById(R.id.ivAvatar);
            tvName = itemView.findViewById(R.id.tvName);
            tvYouBadge = itemView.findViewById(R.id.tvYouBadge);
            tvReliability = itemView.findViewById(R.id.tvReliability);
        }
    }
}
