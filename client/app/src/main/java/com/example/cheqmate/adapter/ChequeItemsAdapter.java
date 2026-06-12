package com.example.cheqmate.adapter;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cheqmate.R;
import com.example.cheqmate.dto.ChequeItemRequest;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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
        holder.btnSharesSummary.setText(getSharesSummary(item));

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

        holder.btnSharesSummary.setOnClickListener(v -> {
            showSharesDialog(v.getContext(), item, () -> {
                holder.btnSharesSummary.setText(getSharesSummary(item));
                onDataChanged.run();
            });
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
        Map<String, Double> shares = Collections.singletonMap(name, 100.0);
        for (ChequeItemRequest item : items) {
            item.setParticipantShares(new LinkedHashMap<>(shares));
        }
        notifyDataSetChanged();
    }

    private String getSharesSummary(ChequeItemRequest item) {
        Map<String, Double> shares = item.getParticipantShares();
        if (shares != null && !shares.isEmpty()) {
            StringBuilder sb = new StringBuilder();
            for (Map.Entry<String, Double> e : shares.entrySet()) {
                if (sb.length() > 0) sb.append(", ");
                sb.append(e.getKey()).append(" (").append((int) e.getValue().doubleValue()).append("%)");
            }
            return sb.toString();
        }
        List<String> names = item.getParticipantNames();
        if (names != null && !names.isEmpty()) {
            return String.join(", ", names);
        }
        return "Выбрать участников";
    }

    private void showSharesDialog(Context context, ChequeItemRequest item, Runnable onConfirm) {
        List<SplitSharesAdapter.Entry> entries = buildEntries(item);

        View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_split_shares, null);
        RecyclerView rv = dialogView.findViewById(R.id.rvShareParticipants);
        android.widget.TextView tvTotal = dialogView.findViewById(R.id.tvSharesTotal);
        MaterialButton btnEqual = dialogView.findViewById(R.id.btnEqualShares);

        Runnable updateTotal = () -> {
            int sum = entries.stream().filter(e -> e.selected).mapToInt(e -> e.percentage).sum();
            tvTotal.setText("Итого: " + sum + "%");
            tvTotal.setTextColor(sum == 100 ? 0xFF2E7D32 : 0xFFC62828);
        };

        SplitSharesAdapter sharesAdapter = new SplitSharesAdapter(entries, updateTotal);
        rv.setLayoutManager(new LinearLayoutManager(context));
        rv.setAdapter(sharesAdapter);
        updateTotal.run();

        btnEqual.setOnClickListener(v -> {
            long count = entries.stream().filter(e -> e.selected).count();
            if (count == 0) return;
            int base = (int) (100 / count);
            int remainder = (int) (100 % count);
            int i = 0;
            for (SplitSharesAdapter.Entry e : entries) {
                if (e.selected) {
                    e.percentage = base + (i < remainder ? 1 : 0);
                    i++;
                }
            }
            sharesAdapter.notifyDataSetChanged();
            updateTotal.run();
        });

        AlertDialog dialog = new MaterialAlertDialogBuilder(context)
                .setTitle("Разделить расходы")
                .setView(dialogView)
                .setPositiveButton("ОК", null)
                .setNegativeButton("Отмена", null)
                .create();

        dialog.setOnShowListener(d -> {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
                int sum = entries.stream().filter(e -> e.selected).mapToInt(e -> e.percentage).sum();
                if (sum != 100) {
                    Toast.makeText(context, "Сумма долей должна быть 100%", Toast.LENGTH_SHORT).show();
                    return;
                }
                Map<String, Double> shares = new LinkedHashMap<>();
                for (SplitSharesAdapter.Entry e : entries) {
                    if (e.selected && e.percentage > 0) {
                        shares.put(e.name, (double) e.percentage);
                    }
                }
                item.setParticipantShares(shares);
                item.setParticipantNames(new ArrayList<>(shares.keySet()));
                onConfirm.run();
                dialog.dismiss();
            });
        });

        dialog.show();
    }

    private List<SplitSharesAdapter.Entry> buildEntries(ChequeItemRequest item) {
        Map<String, Double> currentShares = item.getParticipantShares();
        List<String> currentNames = item.getParticipantNames();
        List<SplitSharesAdapter.Entry> entries = new ArrayList<>();

        for (String participant : participants) {
            boolean selected;
            int pct;
            if (currentShares != null && currentShares.containsKey(participant)) {
                selected = true;
                pct = (int) Math.round(currentShares.get(participant));
            } else if (currentShares == null && currentNames != null && currentNames.contains(participant)) {
                selected = true;
                int n = currentNames.size();
                pct = n > 0 ? 100 / n : 0;
            } else {
                selected = false;
                pct = 0;
            }
            entries.add(new SplitSharesAdapter.Entry(participant, selected, pct));
        }
        return entries;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextInputEditText etName, etPrice;
        MaterialButton btnSharesSummary;
        ImageButton btnRemove;
        TextWatcher nameWatcher;
        TextWatcher priceWatcher;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            etName = itemView.findViewById(R.id.etItemName);
            etPrice = itemView.findViewById(R.id.etItemPrice);
            btnSharesSummary = itemView.findViewById(R.id.btnSharesSummary);
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
