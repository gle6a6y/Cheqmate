package com.example.cheqmate;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
import java.util.function.Consumer;

public class YourDebtsAdapter extends RecyclerView.Adapter<YourDebtsAdapter.DebtViewHolder> {

    private final List<GroupDetailsActivity.DebtPerson> debtPeople;
    private final Consumer<GroupDetailsActivity.DebtPerson> onDebtSelected;
    private GroupDetailsActivity.DebtPerson selectedPerson;

    public YourDebtsAdapter(List<GroupDetailsActivity.DebtPerson> debtPeople, Consumer<GroupDetailsActivity.DebtPerson> onDebtSelected) {
        this.debtPeople = debtPeople;
        this.onDebtSelected = onDebtSelected;
    }

    @NonNull
    @Override
    public DebtViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_your_debt, parent, false);
        return new DebtViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DebtViewHolder holder, int position) {
        GroupDetailsActivity.DebtPerson person = debtPeople.get(position);
        holder.icon.setImageResource(person.getIconResId());
        holder.name.setText(person.getName());
        holder.amount.setText(person.getAmount());

        boolean isSelected = person.equals(selectedPerson);
        holder.itemView.setSelected(isSelected);

        holder.itemView.setOnClickListener(v -> {
            if (person.equals(selectedPerson)) {
                selectedPerson = null;
            } else {
                selectedPerson = person;
            }
            notifyDataSetChanged();
            onDebtSelected.accept(selectedPerson);
        });
    }

    @Override
    public int getItemCount() {
        return debtPeople.size();
    }

    static class DebtViewHolder extends RecyclerView.ViewHolder {
        ImageView icon;
        TextView name;
        TextView amount;

        public DebtViewHolder(@NonNull View itemView) {
            super(itemView);
            icon = itemView.findViewById(R.id.ivIcon);
            name = itemView.findViewById(R.id.tvName);
            amount = itemView.findViewById(R.id.tvAmount);
        }
    }
}