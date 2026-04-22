package com.example.cheqmate;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cheqmate.adapter.DebtAdapter;
import com.example.cheqmate.dto.DebtResponse;
import com.example.cheqmate.network.NetworkClient;
import com.example.cheqmate.network.SessionManager;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AnalyticsActivity extends AppCompatActivity {

    private TextView btnGroups;
    private TextView btnAnalytics;
    private TextView tvOwedToYou, tvOwedToYouLabel;
    private TextView tvYouOwe, tvYouOweLabel;
    private TextView tvMyOperations, tvPeriod;
    private TextView tvPersonalSpent, tvPersonalSpentLabel;
    private TextView tvPaidForOthers, tvPaidForOthersLabel;
    private TextView tvStats;
    private TextView tvDebtorsTitle, tvOwedToOthersTitle;

    private RecyclerView rvDebtors;
    private RecyclerView rvCreditors;

    private DebtAdapter debtorsAdapter;
    private DebtAdapter creditorsAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_analytics);

        initViews();
        setupRecyclerViews();
        loadData();
    }

    private void initViews() {
        btnGroups = findViewById(R.id.btnGroups);
        btnAnalytics = findViewById(R.id.btnAnalytics);

        btnGroups.setOnClickListener(v -> finish());
        btnAnalytics.setOnClickListener(v -> {
            Toast.makeText(this, "Вы уже здесь", Toast.LENGTH_SHORT).show();
        });

        tvOwedToYou = findViewById(R.id.tvOwedToYou);
        tvOwedToYouLabel = findViewById(R.id.tvOwedToYouLabel);
        tvYouOwe = findViewById(R.id.tvYouOwe);
        tvYouOweLabel = findViewById(R.id.tvYouOweLabel);

        tvMyOperations = findViewById(R.id.tvMyOperations);
        tvPeriod = findViewById(R.id.tvPeriod);
        tvPersonalSpent = findViewById(R.id.tvPersonalSpent);
        tvPersonalSpentLabel = findViewById(R.id.tvPersonalSpentLabel);
        tvPaidForOthers = findViewById(R.id.tvPaidForOthers);
        tvPaidForOthersLabel = findViewById(R.id.tvPaidForOthersLabel);
        tvStats = findViewById(R.id.tvStats);

        tvDebtorsTitle = findViewById(R.id.tvDebtorsTitle);
        tvOwedToOthersTitle = findViewById(R.id.tvOwedToOthersTitle);

        rvDebtors = findViewById(R.id.rvDebtors);
        rvCreditors = findViewById(R.id.rvCreditors);
    }

    private void setupRecyclerViews() {
        rvDebtors.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        debtorsAdapter = new DebtAdapter(new ArrayList<>(), false);
        rvDebtors.setAdapter(debtorsAdapter);

        rvCreditors.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        creditorsAdapter = new DebtAdapter(new ArrayList<>(), true);
        rvCreditors.setAdapter(creditorsAdapter);
    }

    private void loadData() {
        SessionManager sessionManager = new SessionManager(this);
        String token = sessionManager.fetchAuthToken();

        if (token == null) {
            Toast.makeText(this, "Вы не авторизованы", Toast.LENGTH_SHORT).show();
            return;
        }

        NetworkClient.getApiService().getMyDebts("Bearer " + token).enqueue(new Callback<DebtResponse>() {
            @Override
            public void onResponse(Call<DebtResponse> call, Response<DebtResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    updateUI(response.body());
                } else {
                    Log.e("Analytics", "Error response: " + response.code());
                    Toast.makeText(AnalyticsActivity.this, "Ошибка загрузки данных", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<DebtResponse> call, Throwable t) {
                Log.e("Analytics", "Network error", t);
                Toast.makeText(AnalyticsActivity.this, "Ошибка сети", Toast.LENGTH_SHORT).show();
            }
        });

        // Эти блоки пока не имеют API, оставляем нули
        tvPersonalSpent.setText("0 ₽");
        tvPaidForOthers.setText("0 ₽");
        tvStats.setText("Данные о расходах загружаются...");
    }

    private void updateUI(DebtResponse response) {
        double totalOwedToMe = 0;
        List<Debtor> debtorsList = new ArrayList<>();
        if (response.getDebtors() != null) {
            for (DebtResponse.DebtItem item : response.getDebtors()) {
                totalOwedToMe += item.getAmount();
                debtorsList.add(new Debtor(item.getName(), String.format("%.0f ₽", item.getAmount())));
            }
        }
        debtorsAdapter.setData(debtorsList);

        double totalIOwe = 0;
        List<Debtor> creditorsList = new ArrayList<>();
        if (response.getCreditors() != null) {
            for (DebtResponse.DebtItem item : response.getCreditors()) {
                totalIOwe += item.getAmount();
                creditorsList.add(new Debtor(item.getName(), String.format("%.0f ₽", item.getAmount())));
            }
        }
        creditorsAdapter.setData(creditorsList);

        tvOwedToYou.setText(String.format("+%.0f ₽", totalOwedToMe));
        tvYouOwe.setText(String.format("-%.0f ₽", totalIOwe));
    }

    public static class Debtor {
        private String name;
        private String amount;

        public Debtor(String name, String amount) {
            this.name = name;
            this.amount = amount;
        }

        public String getName() { return name; }
        public String getAmount() { return amount; }
    }
}
