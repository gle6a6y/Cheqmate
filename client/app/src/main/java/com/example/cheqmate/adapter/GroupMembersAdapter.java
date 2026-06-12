package com.example.cheqmate.adapter;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import com.example.cheqmate.R;
import com.example.cheqmate.model.GroupMember;

import java.util.List;

public class GroupMembersAdapter {

    private final List<GroupMember> members;

    public GroupMembersAdapter(List<GroupMember> members) {
        this.members = members;
    }

    public void renderInto(LinearLayout container) {
        container.removeAllViews();
        LayoutInflater inflater = LayoutInflater.from(container.getContext());
        for (GroupMember member : members) {
            View row = inflater.inflate(R.layout.item_group_member, container, false);
            bindRow(row, member);
            container.addView(row);
        }
    }

    private void bindRow(View row, GroupMember member) {
        TextView tvName = row.findViewById(R.id.tvName);
        TextView tvYouBadge = row.findViewById(R.id.tvYouBadge);
        TextView tvReliability = row.findViewById(R.id.tvReliability);

        tvName.setText(member.getName());
        tvYouBadge.setVisibility(member.isCurrentUser() ? View.VISIBLE : View.GONE);

        Double rating = member.getReliabilityRating();
        if (rating == null) {
            tvReliability.setText(R.string.reliability_unknown);
            tvReliability.setTextColor(
                    ContextCompat.getColor(row.getContext(), R.color.text_secondary));
            tvReliability.setBackgroundResource(android.R.color.transparent);
        } else {
            int rounded = (int) Math.round(rating);
            tvReliability.setText(row.getContext().getString(R.string.reliability_percent, rounded));
            applyRatingStyle(row, tvReliability, rounded);
        }
    }

    private void applyRatingStyle(View row, TextView tvReliability, int percent) {
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
        tvReliability.setTextColor(ContextCompat.getColor(row.getContext(), textColor));
        GradientDrawable badge = new GradientDrawable();
        badge.setColor(bgColor);
        badge.setCornerRadius(10f * row.getResources().getDisplayMetrics().density);
        tvReliability.setBackground(badge);
    }
}
