package com.example.cheqmate;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cheqmate.adapter.YourDebtsAdapter;
import com.example.cheqmate.network.SessionManager;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;

public class GroupDetailsActivity extends AppCompatActivity {

    private MaterialButton btnPay;
    private String groupName;
    private int groupId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_details);

        groupName = getIntent().getStringExtra("GROUP_NAME");
        groupId = getIntent().getIntExtra("GROUP_ID", -1);

        initHeaderAndActions();
        setupDebtsList();
        loadData();
    }

    private void initHeaderAndActions() {
        ImageButton btnBack = findViewById(R.id.btnBack);
        ImageButton btnAddExpense = findViewById(R.id.btnAddExpense);
        ImageButton btnAddCheck = findViewById(R.id.btnAddCheck);
        btnPay = findViewById(R.id.btnPay);

        btnBack.setOnClickListener(v -> finish());

        btnAddExpense.setOnClickListener(v -> {
            Intent intent = new Intent(GroupDetailsActivity.this, CreateExpenseActivity.class);
            intent.putExtra("GROUP_NAME", groupName);
            
            // Передаем участников. В идеале они должны быть в модели Group.
            // Пока добавим текущего пользователя и "alex" для теста, чтобы избежать ошибки 500 на бэкенде
            ArrayList<String> members = new ArrayList<>();
            String currentUser = new SessionManager(this).fetchUserName();
            if (currentUser != null) members.add(currentUser);
            members.add("alex"); 
            
            intent.putStringArrayListExtra("MEMBERS", members);
            startActivity(intent);
        });

        btnAddCheck.setOnClickListener(v ->
                Toast.makeText(this, "Скоро будет", Toast.LENGTH_SHORT).show()
        );

        btnPay.setOnClickListener(v ->
                Toast.makeText(this, "Оплата будет доступна скоро", Toast.LENGTH_SHORT).show()
        );
    }

    private void setupDebtsList() {
        RecyclerView rvYourDebts = findViewById(R.id.rvYourDebts);
        rvYourDebts.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));

        List<DebtPerson> debtPeople = new ArrayList<>();
        // Здесь пока заглушки, в будущем данные должны приходить из API долгов группы
        debtPeople.add(new DebtPerson(R.drawable.ic_home, "Катя", "700 ₽"));
        debtPeople.add(new DebtPerson(R.drawable.ic_plane, "Иван", "300 ₽"));

        YourDebtsAdapter adapter = new YourDebtsAdapter(debtPeople, this::onDebtSelected);
        rvYourDebts.setAdapter(adapter);
    }

    private void loadData() {
        TextView tvGroupName = findViewById(R.id.tvGroupName);
        TextView tvOwedToYou = findViewById(R.id.tvOwedToYou);
        TextView tvYouOwe = findViewById(R.id.tvYouOwe);

        tvGroupName.setText(groupName != null ? groupName : "Группа");
        // Эти данные тоже должны загружаться через API
        tvOwedToYou.setText("0 ₽");
        tvYouOwe.setText("0 ₽");
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
