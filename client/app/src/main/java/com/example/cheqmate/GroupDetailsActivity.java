package com.example.cheqmate;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cheqmate.adapter.YourDebtsAdapter;
import com.example.cheqmate.dto.DebtResponse;
import com.example.cheqmate.network.NetworkClient;
import com.example.cheqmate.network.SessionManager;
import com.google.android.material.button.MaterialButton;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class GroupDetailsActivity extends AppCompatActivity {

    private MaterialButton btnPay;
    private String groupName;
    private int groupId;

    // Переменные для хранения реальных данных
    private final ArrayList<String> realGroupMembers = new ArrayList<>();
    private final List<DebtPerson> debtPeople = new ArrayList<>();
    private YourDebtsAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_details);

        groupName = getIntent().getStringExtra("GROUP_NAME");
        groupId = getIntent().getIntExtra("GROUP_ID", -1);

        initHeaderAndActions();
        setupDebtsList();
        loadData(); // Переписанный метод загрузки
    }

    private void initHeaderAndActions() {
        ImageButton btnBack = findViewById(R.id.btnBack);
        ImageButton btnAddCheck = findViewById(R.id.btnAddCheck);
        btnPay = findViewById(R.id.btnPay);

        btnBack.setOnClickListener(v -> finish());

        btnAddCheck.setOnClickListener(v -> openCreateCheque());

        btnPay.setOnClickListener(v ->
                Toast.makeText(this, "Оплата будет доступна скоро", Toast.LENGTH_SHORT).show()
        );
    }

    private void openCreateCheque() {
        Intent intent = new Intent(GroupDetailsActivity.this, CreateExpenseActivity.class);
        intent.putExtra("GROUP_NAME", groupName);

        if (realGroupMembers.isEmpty()) {
            String currentUser = new SessionManager(this).fetchUserName();
            if (currentUser != null) {
                realGroupMembers.add(currentUser);
            }
        }

        intent.putStringArrayListExtra("MEMBERS", realGroupMembers);
        startActivity(intent);
    }

    private void setupDebtsList() {
        RecyclerView rvYourDebts = findViewById(R.id.rvYourDebts);
        rvYourDebts.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));

        // Инициализируем адаптер с пустым (пока что) списком debtPeople
        adapter = new YourDebtsAdapter(debtPeople, this::onDebtSelected);
        rvYourDebts.setAdapter(adapter);
    }

    private void loadData() {
        TextView tvGroupName = findViewById(R.id.tvGroupName);
        tvGroupName.setText(groupName != null ? groupName : "Группа");

        SessionManager sessionManager = new SessionManager(this);
        String token = "Bearer " + sessionManager.fetchAuthToken();

        NetworkClient.getApiService().getMyDebtsByGroup(token, groupId).enqueue(new Callback<DebtResponse>() {
            @Override
            public void onResponse(Call<DebtResponse> call, Response<DebtResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    parseAndDisplayDebts(response.body());
                } else {
                    Toast.makeText(GroupDetailsActivity.this, "Ошибка сервера при загрузке долгов", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<DebtResponse> call, Throwable t) {
                Toast.makeText(GroupDetailsActivity.this, "Ошибка сети: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void parseAndDisplayDebts(DebtResponse response) {
        TextView tvOwedToYou = findViewById(R.id.tvOwedToYou);
        TextView tvYouOwe = findViewById(R.id.tvYouOwe);

        double totalOwedToMe = 0;
        double totalIOwe = 0;

        debtPeople.clear();

        if (response.getDebtors() != null) {
            for (DebtResponse.DebtItem d : response.getDebtors()) {
                String name = d.getName();
                double amount = d.getAmount();
                totalOwedToMe += amount;
                debtPeople.add(new DebtPerson(R.drawable.ic_home, name, String.format("+%.2f ₽", amount)));
            }
        }

        if (response.getCreditors() != null) {
            for (DebtResponse.DebtItem c : response.getCreditors()) {
                String name = c.getName();
                double amount = c.getAmount();
                totalIOwe += amount;
                debtPeople.add(new DebtPerson(R.drawable.ic_plane, name, String.format("-%.2f ₽", amount)));
            }
        }

        tvOwedToYou.setText(String.format("%.2f ₽", totalOwedToMe));
        tvYouOwe.setText(String.format("%.2f ₽", totalIOwe));
        adapter.notifyDataSetChanged();
    }

    private void onDebtSelected(DebtPerson selectedPerson) {
        if (selectedPerson == null) {
            btnPay.setVisibility(View.GONE);
        } else {
            btnPay.setVisibility(View.VISIBLE);
        }
    }

    public static class DebtPerson {
        private final int iconResId;
        private final String name;
        private final String amount;

        public DebtPerson(int iconResId, String name, String amount) {
            this.iconResId = iconResId;
            this.name = name;
            this.amount = amount;
        }

        public int getIconResId() { return iconResId; }
        public String getName() { return name; }
        public String getAmount() { return amount; }
    }
}