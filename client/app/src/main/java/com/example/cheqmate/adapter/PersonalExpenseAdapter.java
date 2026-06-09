package com.example.cheqmate.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cheqmate.R;
import com.example.cheqmate.dto.PersonalExpenseResponse;

import java.util.List;

public class PersonalExpenseAdapter extends RecyclerView.Adapter<PersonalExpenseAdapter.ViewHolder> {

    private final List<PersonalExpenseResponse> items;
    private final OnDeleteListener onDelete;

    public interface OnDeleteListener {
        void onDelete(int id);
    }

    public PersonalExpenseAdapter(List<PersonalExpenseResponse> items, OnDeleteListener onDelete) {
        this.items = items;
        this.onDelete = onDelete;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_personal_expense, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        PersonalExpenseResponse item = items.get(position);
        holder.tvCategory.setText(item.getCategory());
        holder.tvAmount.setText(String.format("%.0f ₽", item.getAmount()));
        holder.tvDate.setText(item.getDate() != null ? item.getDate() : "");
        String desc = item.getDescription();
        if (desc != null && !desc.isEmpty()) {
            holder.tvDescription.setVisibility(View.VISIBLE);
            holder.tvDescription.setText(desc);
        } else {
            holder.tvDescription.setVisibility(View.GONE);
        }
        holder.btnDelete.setOnClickListener(v -> onDelete.onDelete(item.getId()));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public void setData(List<PersonalExpenseResponse> newItems) {
        items.clear();
        items.addAll(newItems);
        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvCategory, tvAmount, tvDescription, tvDate;
        ImageButton btnDelete;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvCategory = itemView.findViewById(R.id.tvCategory);
            tvAmount = itemView.findViewById(R.id.tvAmount);
            tvDescription = itemView.findViewById(R.id.tvDescription);
            tvDate = itemView.findViewById(R.id.tvDate);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }
}
