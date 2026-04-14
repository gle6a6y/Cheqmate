package com.example.cheqmate;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class DebtorsAdapter extends RecyclerView.Adapter<DebtorsAdapter.DebtorViewHolder> {

    private List<AnalyticsActivity.Debtor> debtorsList;

    public DebtorsAdapter(List<AnalyticsActivity.Debtor> debtorsList) {
        this.debtorsList = debtorsList;
    }

    public void setData(List<AnalyticsActivity.Debtor> debtorsList) {
        this.debtorsList = debtorsList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public DebtorViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_debtor, parent, false);
        return new DebtorViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DebtorViewHolder holder, int position) {
        AnalyticsActivity.Debtor debtor = debtorsList.get(position);
        holder.tvName.setText(debtor.getName());
        holder.tvAmount.setText(debtor.getAmount());
    }

    @Override
    public int getItemCount() {
        return debtorsList != null ? debtorsList.size() : 0;
    }

    static class DebtorViewHolder extends RecyclerView.ViewHolder {
        TextView tvName;
        TextView tvAmount;

        DebtorViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvDebtorName);
            tvAmount = itemView.findViewById(R.id.tvDebtorAmount);
        }
    }
}