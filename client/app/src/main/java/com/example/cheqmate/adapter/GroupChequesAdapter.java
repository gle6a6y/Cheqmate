package com.example.cheqmate.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cheqmate.R;
import com.example.cheqmate.dto.ChequeResponse;

import java.util.List;
import java.util.Locale;

public class GroupChequesAdapter extends RecyclerView.Adapter<GroupChequesAdapter.ViewHolder> {

    private final List<ChequeResponse> cheques;

    public GroupChequesAdapter(List<ChequeResponse> cheques) {
        this.cheques = cheques;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_group_cheque, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ChequeResponse cheque = cheques.get(position);

        String title = cheque.getChequeName();
        title = "#" + cheque.getId() + " " + title;
        holder.tvChequeTitle.setText(title);

        String whoPaid = cheque.getWhoPaidName() != null ? cheque.getWhoPaidName() : "—";
        holder.tvChequeSubtitle.setText(String.format(
                Locale.getDefault(),
                "%.2f ₽ • платил %s",
                cheque.getTotal(),
                whoPaid
        ));

        holder.divider.setVisibility(position == cheques.size() - 1 ? View.GONE : View.VISIBLE);
    }

    @Override
    public int getItemCount() {
        return cheques.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView tvChequeTitle;
        final TextView tvChequeSubtitle;
        final View divider;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvChequeTitle = itemView.findViewById(R.id.tvChequeTitle);
            tvChequeSubtitle = itemView.findViewById(R.id.tvChequeSubtitle);
            divider = itemView.findViewById(R.id.chequeDivider);
        }
    }
}
