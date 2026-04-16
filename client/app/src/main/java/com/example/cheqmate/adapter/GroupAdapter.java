package com.example.cheqmate.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cheqmate.R;
import com.example.cheqmate.model.Group;

import java.util.List;
import java.util.Locale;

public class GroupAdapter extends RecyclerView.Adapter<GroupAdapter.GroupViewHolder> {

    private List<Group> groups;
    private OnGroupClickListener listener;

    public interface OnGroupClickListener {
        void onGroupClick(int groupId);
    }

    public GroupAdapter(List<Group> groups, OnGroupClickListener listener) {
        this.groups = groups;
        this.listener = listener;
    }

    @NonNull
    @Override
    public GroupViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_main, parent, false);
        return new GroupViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull GroupViewHolder holder, int position) {
        Group group = groups.get(position);

        holder.tvIcon.setText(group.getIcon());
        holder.tvTitle.setText(group.getName());

        String participantsText = group.getParticipantsCount() + " " +
                getCorrectWord(group.getParticipantsCount());
        holder.tvParticipants.setText(participantsText);

        holder.tvIncome.setText(String.format(Locale.getDefault(),
                "+%d ₽", (int) group.getIncome()));
        holder.tvExpense.setText(String.format(Locale.getDefault(),
                "-%d ₽", (int) group.getExpense()));

        if (group.getIncome() > 0) {
            holder.tvIncome.setTextColor(holder.itemView.getContext().getColor(R.color.money_black));
        } else {
            holder.tvIncome.setTextColor(holder.itemView.getContext().getColor(R.color.money_gray));
        }

        if (group.getExpense() > 0) {
            holder.tvExpense.setTextColor(holder.itemView.getContext().getColor(R.color.money_black));
        } else {
            holder.tvExpense.setTextColor(holder.itemView.getContext().getColor(R.color.money_gray));
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onGroupClick(group.getId());
            }
        });
    }

    private String getCorrectWord(int count) {
        if (count % 10 == 1 && count % 100 != 11) {
            return "участник";
        } else if (count % 10 >= 2 && count % 10 <= 4 &&
                (count % 100 < 10 || count % 100 >= 20)) {
            return "участника";
        } else {
            return "участников";
        }
    }

    @Override
    public int getItemCount() {
        return groups.size();
    }

    static class GroupViewHolder extends RecyclerView.ViewHolder {
        TextView tvIcon, tvTitle, tvParticipants, tvIncome, tvExpense;

        GroupViewHolder(@NonNull View itemView) {
            super(itemView);
            tvIcon = itemView.findViewById(R.id.tvIcon);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvParticipants = itemView.findViewById(R.id.tvParticipants);
            tvIncome = itemView.findViewById(R.id.tvIncome);
            tvExpense = itemView.findViewById(R.id.tvExpense);
        }
    }
}