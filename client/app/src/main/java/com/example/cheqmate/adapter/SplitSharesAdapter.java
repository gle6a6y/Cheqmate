package com.example.cheqmate.adapter;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cheqmate.R;

import java.util.List;

public class SplitSharesAdapter extends RecyclerView.Adapter<SplitSharesAdapter.ViewHolder> {

    public static class Entry {
        public final String name;
        public boolean selected;
        public int percentage;

        public Entry(String name, boolean selected, int percentage) {
            this.name = name;
            this.selected = selected;
            this.percentage = percentage;
        }
    }

    private final List<Entry> entries;
    private final Runnable onChanged;

    public SplitSharesAdapter(List<Entry> entries, Runnable onChanged) {
        this.entries = entries;
        this.onChanged = onChanged;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_split_participant_row, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Entry entry = entries.get(position);

        holder.cbSelected.setOnCheckedChangeListener(null);
        holder.etPercentage.removeTextChangedListener(holder.watcher);

        holder.cbSelected.setChecked(entry.selected);
        holder.tvName.setText(entry.name);
        holder.etPercentage.setText(entry.percentage > 0 ? String.valueOf(entry.percentage) : "");
        holder.etPercentage.setEnabled(entry.selected);

        holder.cbSelected.setOnCheckedChangeListener((btn, checked) -> {
            entry.selected = checked;
            holder.etPercentage.setEnabled(checked);
            if (!checked) {
                entry.percentage = 0;
                holder.etPercentage.setText("");
            }
            onChanged.run();
        });

        holder.watcher = new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override public void afterTextChanged(Editable s) {
                try {
                    entry.percentage = Integer.parseInt(s.toString());
                } catch (NumberFormatException e) {
                    entry.percentage = 0;
                }
                onChanged.run();
            }
        };
        holder.etPercentage.addTextChangedListener(holder.watcher);
    }

    @Override
    public int getItemCount() {
        return entries.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        final CheckBox cbSelected;
        final TextView tvName;
        final EditText etPercentage;
        TextWatcher watcher;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            cbSelected = itemView.findViewById(R.id.cbParticipantSelected);
            tvName = itemView.findViewById(R.id.tvParticipantName);
            etPercentage = itemView.findViewById(R.id.etPercentage);
        }
    }
}
