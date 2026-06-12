package com.example.cheqmate.adapter;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cheqmate.R;
import com.example.cheqmate.dto.ChequeItemRequest;
import com.google.android.material.textfield.TextInputEditText;

import java.util.Collections;
import java.util.List;

public class ChequeItemsAdapter extends RecyclerView.Adapter<ChequeItemsAdapter.ViewHolder> {

    private final List<ChequeItemRequest> items;
    private final List<String> participants;
    private final Runnable onDataChanged;

    public ChequeItemsAdapter(List<ChequeItemRequest> items, List<String> participants, Runnable onDataChanged) {
        this.items = items;
        this.participants = participants;
        this.onDataChanged = onDataChanged;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_cheque_position, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ChequeItemRequest item = items.get(position);

        holder.etName.removeTextChangedListener(holder.nameWatcher);
        holder.etPrice.removeTextChangedListener(holder.priceWatcher);

        holder.etName.setText(item.getName());
        holder.etPrice.setText(item.getPrice() > 0 ? String.valueOf(item.getPrice()) : "");

        ArrayAdapter<String> adapter = new ArrayAdapter<>(holder.itemView.getContext(),
                android.R.layout.simple_dropdown_item_1line, participants);
        holder.actParticipant.setAdapter(adapter);

        if (item.getParticipantNames() != null && !item.getParticipantNames().isEmpty()) {
            holder.actParticipant.setText(item.getParticipantNames().get(0), false);
        }

        holder.nameWatcher = new SimpleTextWatcher(s -> {
            item.setName(s);
            onDataChanged.run();
        });
        holder.priceWatcher = new SimpleTextWatcher(s -> {
            try {
                item.setPrice(Double.parseDouble(s));
            } catch (NumberFormatException e) {
                item.setPrice(0);
            }
            onDataChanged.run();
        });

        holder.etName.addTextChangedListener(holder.nameWatcher);
        holder.etPrice.addTextChangedListener(holder.priceWatcher);

        holder.actParticipant.setOnItemClickListener((parent, view, pos, id) -> {
            item.setParticipantNames(Collections.singletonList(participants.get(pos)));
            onDataChanged.run();
        });

        holder.btnRemove.setOnClickListener(v -> {
            int currentPos = holder.getAdapterPosition();
            if (currentPos != RecyclerView.NO_POSITION) {
                items.remove(currentPos);
                notifyItemRemoved(currentPos);
                onDataChanged.run();
            }
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public void setAllParticipants(String name) {
        for (ChequeItemRequest item : items) {
            item.setParticipantNames(Collections.singletonList(name));
        }
        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextInputEditText etName, etPrice;
        AutoCompleteTextView actParticipant;
        ImageButton btnRemove;
        TextWatcher nameWatcher;
        TextWatcher priceWatcher;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            etName = itemView.findViewById(R.id.etItemName);
            etPrice = itemView.findViewById(R.id.etItemPrice);
            actParticipant = itemView.findViewById(R.id.actItemParticipant);
            btnRemove = itemView.findViewById(R.id.btnRemoveItem);
        }
    }

    private static class SimpleTextWatcher implements TextWatcher {
        private final java.util.function.Consumer<String> consumer;
        SimpleTextWatcher(java.util.function.Consumer<String> consumer) { this.consumer = consumer; }
        @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
        @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
        @Override public void afterTextChanged(Editable s) { consumer.accept(s.toString()); }
    }
}
