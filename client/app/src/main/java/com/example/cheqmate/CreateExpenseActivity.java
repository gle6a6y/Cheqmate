package com.example.cheqmate;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CreateExpenseActivity extends AppCompatActivity {

    private final List<String> participants = Arrays.asList("Катя", "Иван", "Олег", "Алина");

    private TextInputLayout tilExpenseName;
    private TextInputLayout tilAmount;
    private TextInputLayout tilPayer;
    private TextInputEditText etExpenseName;
    private TextInputEditText etAmount;
    private AutoCompleteTextView actPayer;
    private ChipGroup cgSplitBetween;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_expense);

        initViews();
        setupPayerDropdown();
        setupSplitBetweenChips();
        setupActions();
    }

    private void initViews() {
        tilExpenseName = findViewById(R.id.tilExpenseName);
        tilAmount = findViewById(R.id.tilAmount);
        tilPayer = findViewById(R.id.tilPayer);

        etExpenseName = findViewById(R.id.etExpenseName);
        etAmount = findViewById(R.id.etAmount);
        actPayer = findViewById(R.id.actPayer);
        cgSplitBetween = findViewById(R.id.cgSplitBetween);
    }

    private void setupPayerDropdown() {
        ArrayAdapter<String> payerAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_dropdown_item_1line,
                participants
        );
        actPayer.setAdapter(payerAdapter);
        actPayer.setText(participants.get(0), false);
    }

    private void setupSplitBetweenChips() {
        for (String participant : participants) {
            Chip chip = new Chip(this);
            chip.setText(participant);
            chip.setCheckable(true);
            chip.setChecked(true);
            chip.setChipBackgroundColorResource(R.color.button_secondary_background);
            chip.setShapeAppearanceModel(chip.getShapeAppearanceModel().withCornerSize(8f));
            cgSplitBetween.addView(chip);
        }
    }

    private void setupActions() {
        ImageButton btnBack = findViewById(R.id.btnBack);
        MaterialButton btnAddExpense = findViewById(R.id.btnAddExpense);

        btnBack.setOnClickListener(v -> finish());
        btnAddExpense.setOnClickListener(v -> submitExpense());
    }

    private void submitExpense() {
        tilExpenseName.setError(null);
        tilAmount.setError(null);
        tilPayer.setError(null);

        String expenseName = etExpenseName.getText() != null ? etExpenseName.getText().toString().trim() : "";
        String amount = etAmount.getText() != null ? etAmount.getText().toString().trim() : "";
        String payer = actPayer.getText() != null ? actPayer.getText().toString().trim() : "";
        List<String> selectedParticipants = getSelectedParticipants();

        boolean hasError = false;
        if (TextUtils.isEmpty(expenseName)) {
            tilExpenseName.setError("Введите название");
            hasError = true;
        }
        if (TextUtils.isEmpty(amount)) {
            tilAmount.setError("Введите сумму");
            hasError = true;
        }
        if (TextUtils.isEmpty(payer)) {
            tilPayer.setError("Выберите плательщика");
            hasError = true;
        }
        if (selectedParticipants.isEmpty()) {
            Toast.makeText(this, "Выберите участников для разделения", Toast.LENGTH_SHORT).show();
            hasError = true;
        }

        if (hasError) {
            return;
        }

        Toast.makeText(this, "Расход добавлен", Toast.LENGTH_SHORT).show();
        finish();
    }

    private List<String> getSelectedParticipants() {
        List<String> selected = new ArrayList<>();
        for (int i = 0; i < cgSplitBetween.getChildCount(); i++) {
            if (!(cgSplitBetween.getChildAt(i) instanceof Chip)) {
                continue;
            }

            Chip chip = (Chip) cgSplitBetween.getChildAt(i);
            if (chip.isChecked()) {
                selected.add(chip.getText().toString());
            }
        }
        return selected;
    }
}
