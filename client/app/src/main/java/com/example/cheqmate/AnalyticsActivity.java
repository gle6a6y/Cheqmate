package com.example.cheqmate;

import android.content.Intent;
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
import com.example.cheqmate.dto.OperationsStatsResponse;
import com.example.cheqmate.dto.PersonalExpenseResponse;
import com.example.cheqmate.network.NetworkClient;
import com.example.cheqmate.network.SessionManager;
import com.example.cheqmate.notification.NotificationBadge;

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
    private TextView tvPersonalSpentTotal;
    private TextView tvStatTotalSpent, tvStatCheques, tvStatDebtsPaid;
    private TextView tvPaidForOthers, tvPaidForOthersLabel;
    private TextView tvStats;
    private TextView tvDebtorsTitle, tvOwedToOthersTitle;
    private TextView tvDebtorsEmpty, tvCreditorsEmpty;
    private TextView tvNotificationBadge;

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
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadData();
        NotificationBadge.refresh(this, tvNotificationBadge);
    }

    private void initViews() {
        btnGroups = findViewById(R.id.btnGroups);
        btnAnalytics = findViewById(R.id.btnAnalytics);

        btnGroups.setOnClickListener(v -> finish());
        btnAnalytics.setOnClickListener(v ->
                Toast.makeText(this, "Вы уже здесь", Toast.LENGTH_SHORT).show());

        tvNotificationBadge = findViewById(R.id.tvNotificationBadge);
        TextView btnNotifications = findViewById(R.id.btnNotifications);
        btnNotifications.setOnClickListener(v ->
                startActivity(new Intent(AnalyticsActivity.this, NotificationsActivity.class)));

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
        tvPersonalSpentTotal = findViewById(R.id.tvPersonalSpentTotal);
        tvStatTotalSpent = findViewById(R.id.tvStatTotalSpent);
        tvStatCheques = findViewById(R.id.tvStatCheques);
        tvStatDebtsPaid = findViewById(R.id.tvStatDebtsPaid);

        findViewById(R.id.btnGoToPersonalExpenses).setOnClickListener(v ->
                startActivity(new Intent(this, PersonalExpensesActivity.class)));

        findViewById(R.id.btnGoToAchievements).setOnClickListener(v ->
                startActivity(new Intent(this, AchievementsActivity.class)));

        tvDebtorsTitle = findViewById(R.id.tvDebtorsTitle);
        tvOwedToOthersTitle = findViewById(R.id.tvOwedToOthersTitle);

        rvDebtors = findViewById(R.id.rvDebtors);
        rvCreditors = findViewById(R.id.rvCreditors);
        tvDebtorsEmpty = findViewById(R.id.tvDebtorsEmpty);
        tvCreditorsEmpty = findViewById(R.id.tvCreditorsEmpty);
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
            Toast.makeText(this, "Ошибка сессии, войдите снова", Toast.LENGTH_SHORT).show();
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

        tvStats.setVisibility(View.VISIBLE);
        tvStats.setText("Данные о расходах загружаются...");

        NetworkClient.getApiService().getMyOperations("Bearer " + token).enqueue(new Callback<OperationsStatsResponse>() {
            @Override
            public void onResponse(Call<OperationsStatsResponse> call, Response<OperationsStatsResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    OperationsStatsResponse ops = response.body();
                    tvPersonalSpent.setText(formatMoney(ops.getPersonalSpent()));
                    tvPaidForOthers.setText(formatMoney(ops.getPaidForOthers()));
                    tvStats.setVisibility(View.GONE);
                } else {
                    tvStats.setText("Не удалось загрузить операции");
                }
            }

            @Override
            public void onFailure(Call<OperationsStatsResponse> call, Throwable t) {
                tvStats.setText("Ошибка сети при загрузке операций");
            }
        });

        NetworkClient.getApiService().getMyStats("Bearer " + token).enqueue(new retrofit2.Callback<com.example.cheqmate.dto.UserStatsResponse>() {
            @Override
            public void onResponse(retrofit2.Call<com.example.cheqmate.dto.UserStatsResponse> call, retrofit2.Response<com.example.cheqmate.dto.UserStatsResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    com.example.cheqmate.dto.UserStatsResponse stats = response.body();
                    tvStatTotalSpent.setText(String.valueOf(stats.getGroupsCount()));
                    tvStatCheques.setText(String.valueOf(stats.getChequesCount()));
                    tvStatDebtsPaid.setText(String.valueOf(stats.getDebtsPaidCount()));
                }
            }

            @Override
            public void onFailure(retrofit2.Call<com.example.cheqmate.dto.UserStatsResponse> call, Throwable t) {}
        });

        NetworkClient.getApiService().getPersonalExpenses("Bearer " + token).enqueue(new Callback<List<PersonalExpenseResponse>>() {
            @Override
            public void onResponse(Call<List<PersonalExpenseResponse>> call, Response<List<PersonalExpenseResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    double total = 0;
                    for (PersonalExpenseResponse e : response.body()) {
                        total += e.getAmount();
                    }
                    tvPersonalSpentTotal.setText(formatMoney(total));
                }
            }

            @Override
            public void onFailure(Call<List<PersonalExpenseResponse>> call, Throwable t) {}
        });
    }

    private String formatMoney(double amount) {
        return String.format("%.0f ₽", amount);
    }

    private void updateUI(DebtResponse debts) {
        double totalOwedToMe = 0;
        List<Debtor> debtorsList = new ArrayList<>();
        if (debts.getDebtors() != null) {
            for (DebtResponse.DebtItem item : debts.getDebtors()) {
                totalOwedToMe += item.getAmount();
                debtorsList.add(new Debtor(item.getName(), String.format("%.0f ₽", item.getAmount())));
            }
        }
        debtorsAdapter.setData(debtorsList);
        tvDebtorsEmpty.setVisibility(debtorsList.isEmpty() ? View.VISIBLE : View.GONE);

        double totalIOwe = 0;
        List<Debtor> creditorsList = new ArrayList<>();
        if (debts.getCreditors() != null) {
            for (DebtResponse.DebtItem item : debts.getCreditors()) {
                totalIOwe += item.getAmount();
                creditorsList.add(new Debtor(item.getName(), String.format("%.0f ₽", item.getAmount())));
            }
        }
        creditorsAdapter.setData(creditorsList);
        tvCreditorsEmpty.setVisibility(creditorsList.isEmpty() ? View.VISIBLE : View.GONE);

        tvOwedToYou.setText(String.format("+%.0f ₽", totalOwedToMe));
        tvYouOwe.setText(String.format("-%.0f ₽", totalIOwe));

        tvOwedToYou.setTextColor(getColor(totalOwedToMe > 0
                ? R.color.money_positive : R.color.money_gray));
        tvYouOwe.setTextColor(getColor(totalIOwe > 0
                ? R.color.money_black : R.color.money_gray));
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
