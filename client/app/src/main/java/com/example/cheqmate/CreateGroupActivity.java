package com.example.cheqmate;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.cheqmate.dto.GroupCreateRequest;
import com.example.cheqmate.network.ApiService;
import com.example.cheqmate.network.NetworkClient;
import com.example.cheqmate.network.SessionManager;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.textfield.TextInputEditText;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CreateGroupActivity extends AppCompatActivity {

    private TextInputEditText etGroupName;
    private TextInputEditText etParticipants;
    private ChipGroup cgParticipants;
    private Button btnCreateGroup;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_group);

        sessionManager = new SessionManager(this);

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
            String groupName = etGroupName.getText().toString().trim();
            List<String> members = new ArrayList<>();
            for (int i = 0; i < cgParticipants.getChildCount(); i++) {
                Chip chip = (Chip) cgParticipants.getChildAt(i);
                members.add(chip.getText().toString());
            }

            performCreateGroup(groupName, members);
        });
    }

    private void addParticipantChip(String name) {
        Chip chip = new Chip(this);
        chip.setText(name);
        chip.setCloseIconVisible(true);
        chip.setOnCloseIconClickListener(v -> cgParticipants.removeView(chip));
        // Стиль чипов
        chip.setChipBackgroundColorResource(R.color.button_secondary_background);
        chip.setShapeAppearanceModel(chip.getShapeAppearanceModel().withCornerSize(8f));
        
        cgParticipants.addView(chip);
    }

    private void performCreateGroup(String name, List<String> members) {
        if (name.isEmpty() || members.isEmpty()) {
            Toast.makeText(this, "Название и участники обязательны", Toast.LENGTH_SHORT).show();
            return;
        }

        String token = sessionManager.fetchAuthToken();
        if (token == null) {
            Toast.makeText(this, "Ошибка авторизации", Toast.LENGTH_SHORT).show();
            return;
        }

        ApiService apiService = NetworkClient.getApiService();
        GroupCreateRequest request = new GroupCreateRequest(name, members);
        
        // Добавим логи для проверки
        android.util.Log.d("CREATE_GROUP", "Sending JSON: name=" + name + ", members=" + members);

        apiService.createGroup("Bearer " + token, request).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(CreateGroupActivity.this, "Группа создана!", Toast.LENGTH_SHORT).show();
                    finish();
                } else if (response.code() == 400) {
                    try {
                        String errorMsg = response.errorBody() != null ? response.errorBody().string() : "Ошибка запроса";
                        Toast.makeText(CreateGroupActivity.this, errorMsg, Toast.LENGTH_LONG).show();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else {
                    Toast.makeText(CreateGroupActivity.this, "Ошибка сервера: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                Toast.makeText(CreateGroupActivity.this, "Ошибка сети: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
