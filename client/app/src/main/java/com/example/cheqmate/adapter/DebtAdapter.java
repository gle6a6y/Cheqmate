package com.example.cheqmate.adapter;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cheqmate.AnalyticsActivity;
import com.example.cheqmate.R;

import java.util.List;

public class DebtAdapter extends RecyclerView.Adapter<DebtAdapter.DebtViewHolder> {

    private List<AnalyticsActivity.Debtor> debtList;
    private boolean isNegative;

    public DebtAdapter(List<AnalyticsActivity.Debtor> debtList, boolean isNegative) {
        this.debtList = debtList;
        this.isNegative = isNegative;
    }

    public void setData(List<AnalyticsActivity.Debtor> debtList) {
        this.debtList = debtList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public DebtViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_debtor, parent, false);
        return new DebtViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DebtViewHolder holder, int position) {
        AnalyticsActivity.Debtor debtor = debtList.get(position);
        holder.tvName.setText(debtor.getName());
        holder.tvAmount.setText(debtor.getAmount());

        if (isNegative) {
            holder.tvAmount.setTextColor(Color.parseColor("#F44336"));
        } else {
            holder.tvAmount.setTextColor(Color.parseColor("#4CAF50"));
        }
    }

    @Override
    public int getItemCount() {
        return debtList != null ? debtList.size() : 0;
    }

    static class DebtViewHolder extends RecyclerView.ViewHolder {
        TextView tvName;
        TextView tvAmount;

        DebtViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvDebtorName);
            tvAmount = itemView.findViewById(R.id.tvDebtorAmount);
        }
    }
}
