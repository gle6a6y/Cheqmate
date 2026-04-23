package com.example.cheqmate;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cheqmate.adapter.ChequeItemsAdapter;
import com.example.cheqmate.dto.ChequeItemRequest;
import com.example.cheqmate.dto.ChequeRequest;
import com.example.cheqmate.network.NetworkClient;
import com.example.cheqmate.network.SessionManager;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CreateExpenseActivity extends AppCompatActivity {

    private List<String> participants = new ArrayList<>();

    private TextInputLayout tilExpenseName;
    private TextInputEditText etExpenseName;
    private AutoCompleteTextView actPayer;
    private RecyclerView rvPositions;
    private TextView tvTotalAmount;
    
    private List<ChequeItemRequest> itemsList = new ArrayList<>();
    private ChequeItemsAdapter adapter;
    private String groupName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_expense);

        groupName = getIntent().getStringExtra("GROUP_NAME");
        ArrayList<String> members = getIntent().getStringArrayListExtra("MEMBERS");
        
        if (groupName == null) groupName = "Общая";
        if (members != null) {
            participants.addAll(members);
        } else {
            // Фолбэк если данные не переданы (для теста)
            participants.add("ivan");
        }

        initViews();
        setupRecyclerView();
        setupPayerDropdown();
        setupActions();
        
        addNewPosition();
    }

    private void initViews() {
        tilExpenseName = findViewById(R.id.tilExpenseName);
        etExpenseName = findViewById(R.id.etExpenseName);
        actPayer = findViewById(R.id.actPayer);
        rvPositions = findViewById(R.id.rvPositions);
        tvTotalAmount = findViewById(R.id.tvTotalAmount);
    }

    private void setupRecyclerView() {
        adapter = new ChequeItemsAdapter(itemsList, participants, this::calculateTotal);
        rvPositions.setLayoutManager(new LinearLayoutManager(this));
        rvPositions.setAdapter(adapter);
    }

    private void setupPayerDropdown() {
        ArrayAdapter<String> payerAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_dropdown_item_1line,
                participants
        );
        actPayer.setAdapter(payerAdapter);
        if (!participants.isEmpty()) {
            actPayer.setText(participants.get(0), false);
        }
    }

    private void setupActions() {
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        findViewById(R.id.btnAddPosition).setOnClickListener(v -> addNewPosition());
        findViewById(R.id.btnAddExpense).setOnClickListener(v -> submitCheque());
    }

    private void addNewPosition() {
        itemsList.add(new ChequeItemRequest("", 0, 1, new ArrayList<>(Collections.singletonList(participants.get(0)))));
        adapter.notifyItemInserted(itemsList.size() - 1);
        calculateTotal();
    }

    private void calculateTotal() {
        double total = 0;
        for (ChequeItemRequest item : itemsList) {
            total += item.getPrice() * item.getQuantity();
        }
        tvTotalAmount.setText(String.format("%.2f ₽", total));
    }

    private void submitCheque() {
        String chequeName = etExpenseName.getText().toString().trim();
        String whoPaid = actPayer.getText().toString();
        
        if (TextUtils.isEmpty(chequeName)) {
            tilExpenseName.setError("Введите название чека");
            return;
        }

        if (itemsList.isEmpty()) {
            Toast.makeText(this, "Добавьте хотя бы одну позицию", Toast.LENGTH_SHORT).show();
            return;
        }

        double total = 0;
        for (ChequeItemRequest item : itemsList) {
            total += item.getPrice() * item.getQuantity();
        }

        SessionManager sessionManager = new SessionManager(this);
        String ownerName = sessionManager.fetchUserName();
        if (ownerName == null) ownerName = whoPaid;

        ChequeRequest request = new ChequeRequest(
                groupName,
                chequeName,
                total,
                ownerName,
                whoPaid,
                itemsList
        );

        sendToServer(request);
    }

    private void sendToServer(ChequeRequest request) {
        String token = new SessionManager(this).fetchAuthToken();
        NetworkClient.getApiService().createCheque("Bearer " + token, request)
                .enqueue(new Callback<Void>() {
                    @Override
                    public void onResponse(Call<Void> call, Response<Void> response) {
                        if (response.isSuccessful()) {
                            Toast.makeText(CreateExpenseActivity.this, "Чек успешно сохранен", Toast.LENGTH_SHORT).show();
                            finish();
                        } else {
                            Toast.makeText(CreateExpenseActivity.this, "Ошибка сервера: " + response.code(), Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<Void> call, Throwable t) {
                        Toast.makeText(CreateExpenseActivity.this, "Ошибка сети: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
