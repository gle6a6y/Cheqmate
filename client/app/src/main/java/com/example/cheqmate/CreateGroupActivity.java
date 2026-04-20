package com.example.cheqmate;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.textfield.TextInputEditText;

public class CreateGroupActivity extends AppCompatActivity {

    private TextInputEditText etGroupName;
    private TextInputEditText etParticipants;
    private ChipGroup cgParticipants;
    private Button btnCreateGroup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_group);

        etGroupName = findViewById(R.id.etGroupName);
        etParticipants = findViewById(R.id.etParticipants);
        cgParticipants = findViewById(R.id.cgParticipants);
        btnCreateGroup = findViewById(R.id.btnCreateGroup);

        etParticipants.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                String text = s.toString();
                if (text.endsWith(",")) {
                    String name = text.substring(0, text.length() - 1).trim();
                    if (!name.isEmpty()) {
                        addParticipantChip(name);
                    }
                    etParticipants.setText("");
                }
            }
        });

        btnCreateGroup.setOnClickListener(v -> {
            // TODO: handle group creation
            finish();
        });
    }

    private void addParticipantChip(String name) {
        Chip chip = new Chip(this);
        chip.setText(name);
        chip.setCloseIconVisible(true);
        chip.setOnCloseIconClickListener(v -> cgParticipants.removeView(chip));
        // To make it look like a "gray rectangle" as requested
        chip.setChipBackgroundColorResource(R.color.button_secondary_background);
        chip.setShapeAppearanceModel(chip.getShapeAppearanceModel().withCornerSize(8f));
        
        cgParticipants.addView(chip);
    }
}
