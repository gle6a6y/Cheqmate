package com.example.cheqmate.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cheqmate.R;
import com.example.cheqmate.GroupDetailsActivity;

import java.util.List;

public class YourDebtsAdapter extends RecyclerView.Adapter<YourDebtsAdapter.DebtViewHolder> {

    public interface OnDebtClickListener {
        void onDebtSelected(GroupDetailsActivity.DebtPerson person);
    }

    private final List<GroupDetailsActivity.DebtPerson> debts;
    private final OnDebtClickListener onDebtClickListener;
    private int selectedPosition = RecyclerView.NO_POSITION;

    public YourDebtsAdapter(List<GroupDetailsActivity.DebtPerson> debts, OnDebtClickListener onDebtClickListener) {
        this.debts = debts;
        this.onDebtClickListener = onDebtClickListener;
    }

    @NonNull
    @Override
    public DebtViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_your_debt, parent, false);
        return new DebtViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DebtViewHolder holder, int position) {
        GroupDetailsActivity.DebtPerson debtPerson = debts.get(position);
        holder.ivAvatar.setImageResource(debtPerson.getIconResId());
        holder.tvName.setText(debtPerson.getName());
        holder.tvAmount.setText(debtPerson.getAmount());

        boolean isSelected = selectedPosition == position;
        holder.itemView.setAlpha(isSelected ? 1.0f : 0.7f);
        holder.ivAvatar.setBackgroundResource(isSelected ? R.drawable.circle_gray : R.drawable.circle_white);

        holder.itemView.setOnClickListener(v -> {
            int previousPosition = selectedPosition;
            if (selectedPosition == holder.getBindingAdapterPosition()) {
                selectedPosition = RecyclerView.NO_POSITION;
                onDebtClickListener.onDebtSelected(null);
            } else {
                selectedPosition = holder.getBindingAdapterPosition();
                onDebtClickListener.onDebtSelected(debtPerson);
            }

            if (previousPosition != RecyclerView.NO_POSITION) {
                notifyItemChanged(previousPosition);
            }
            if (selectedPosition != RecyclerView.NO_POSITION) {
                notifyItemChanged(selectedPosition);
            }
        });
    }

    @Override
    public int getItemCount() {
        return debts.size();
    }

    static class DebtViewHolder extends RecyclerView.ViewHolder {
        ImageView ivAvatar;
        TextView tvName;
        TextView tvAmount;

        public DebtViewHolder(@NonNull View itemView) {
            super(itemView);
            ivAvatar = itemView.findViewById(R.id.ivAvatar);
            tvName = itemView.findViewById(R.id.tvName);
            tvAmount = itemView.findViewById(R.id.tvAmount);
        }
    }
}
