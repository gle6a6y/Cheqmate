package com.example.cheqmate;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;

public class GroupDetailsActivity extends AppCompatActivity {

    private MaterialButton btnPay;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_details);

        initHeaderAndActions();
        setupDebtsList();
        loadMockData();
    }

    private void initHeaderAndActions() {
        ImageButton btnBack = findViewById(R.id.btnBack);
        ImageButton btnAddExpense = findViewById(R.id.btnAddExpense);
        ImageButton btnAddCheck = findViewById(R.id.btnAddCheck);
        ImageButton btnFortuneWheel = findViewById(R.id.btnFortuneWheel);
        btnPay = findViewById(R.id.btnPay);

        btnBack.setOnClickListener(v -> finish());

        btnAddExpense.setOnClickListener(v -> {
            Intent intent = new Intent(GroupDetailsActivity.this, CreateExpenseActivity.class);
            startActivity(intent);
        });

        btnAddCheck.setOnClickListener(v ->
                Toast.makeText(this, "Скоро будет", Toast.LENGTH_SHORT).show()
        );

        btnFortuneWheel.setOnClickListener(v -> {
            Intent intent = new Intent(GroupDetailsActivity.this, FortuneWheelActivity.class);
            startActivity(intent);
        });

        btnPay.setOnClickListener(v ->
                Toast.makeText(this, "Оплата будет доступна скоро", Toast.LENGTH_SHORT).show()
        );
    }

    private void setupDebtsList() {
        RecyclerView rvYourDebts = findViewById(R.id.rvYourDebts);
        rvYourDebts.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));

        List<DebtPerson> debtPeople = new ArrayList<>();
        debtPeople.add(new DebtPerson(R.drawable.ic_home, "Катя", "700 ₽"));
        debtPeople.add(new DebtPerson(R.drawable.ic_plane, "Иван", "300 ₽"));
        debtPeople.add(new DebtPerson(R.drawable.ic_tree, "Олег", "1 100 ₽"));
        debtPeople.add(new DebtPerson(R.drawable.ic_cake, "Алина", "500 ₽"));

        YourDebtsAdapter adapter = new YourDebtsAdapter(debtPeople, this::onDebtSelected);
        rvYourDebts.setAdapter(adapter);
    }

    private void loadMockData() {
        TextView tvGroupName = findViewById(R.id.tvGroupName);
        TextView tvOwedToYou = findViewById(R.id.tvOwedToYou);
        TextView tvYouOwe = findViewById(R.id.tvYouOwe);

        tvGroupName.setText("Поездка в Сочи");
        tvOwedToYou.setText("+4 250 ₽");
        tvYouOwe.setText("-1 300 ₽");
    }

    private void onDebtSelected(DebtPerson selectedPerson) {
        if (selectedPerson == null) {
            btnPay.setVisibility(android.view.View.GONE);
        } else {
            btnPay.setVisibility(android.view.View.VISIBLE);
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

        public int getIconResId() {
            return iconResId;
        }

        public String getName() {
            return name;
        }

        public String getAmount() {
            return amount;
        }
    }
}