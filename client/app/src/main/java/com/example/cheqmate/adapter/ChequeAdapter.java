package com.example.cheqmate.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cheqmate.R;

import java.util.List;

public class ChequeAdapter extends RecyclerView.Adapter<ChequeAdapter.ViewHolder> {

    public static class ChequeItem {
        public final String name;
        public final String info;

        public ChequeItem(String name, String info) {
            this.name = name;
            this.info = info;
        }
    }

    private final List<ChequeItem> items;

    public ChequeAdapter(List<ChequeItem> items) {
        this.items = items;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_cheque, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ChequeItem item = items.get(position);
        holder.tvName.setText(item.name);
        holder.tvInfo.setText(item.info);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public void setData(List<ChequeItem> newItems) {
        items.clear();
        items.addAll(newItems);
        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvInfo;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvChequeName);
            tvInfo = itemView.findViewById(R.id.tvChequeInfo);
        }
    }
}
