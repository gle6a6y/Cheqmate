package com.example.cheqmate;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class AnalyticsActivity extends AppCompatActivity {

    private TextView tvProjectName;
    private TextView tvAnalyticsTitle;
    private TextView tvOwedToYou, tvOwedToYouLabel;
    private TextView tvYouOwe, tvYouOweLabel;
    private TextView tvMyOperations, tvPeriod;
    private TextView tvPersonalSpent, tvPersonalSpentLabel;
    private TextView tvPaidForOthers, tvPaidForOthersLabel;
    private TextView tvStats;
    private TextView tvDebtorsTitle, tvGroupsTitle;

    private RecyclerView rvDebtors;
    private RecyclerView rvGroups;

    private DebtorsAdapter debtorsAdapter;
    private GroupsAdapter groupsAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_analytics);

        initViews();
        setupRecyclerViews();
        loadData();
    }

    private void initViews() {
        // Шапка
        tvProjectName = findViewById(R.id.tvProjectName);
        tvAnalyticsTitle = findViewById(R.id.tvAnalyticsTitle);

        // Блок 1
        tvOwedToYou = findViewById(R.id.tvOwedToYou);
        tvOwedToYouLabel = findViewById(R.id.tvOwedToYouLabel);
        tvYouOwe = findViewById(R.id.tvYouOwe);
        tvYouOweLabel = findViewById(R.id.tvYouOweLabel);

        // Блок 2: Мои операции
        tvMyOperations = findViewById(R.id.tvMyOperations);
        tvPeriod = findViewById(R.id.tvPeriod);
        tvPersonalSpent = findViewById(R.id.tvPersonalSpent);
        tvPersonalSpentLabel = findViewById(R.id.tvPersonalSpentLabel);
        tvPaidForOthers = findViewById(R.id.tvPaidForOthers);
        tvPaidForOthersLabel = findViewById(R.id.tvPaidForOthersLabel);
        tvStats = findViewById(R.id.tvStats);

        // Заголовки секций
        tvDebtorsTitle = findViewById(R.id.tvDebtorsTitle);
        tvGroupsTitle = findViewById(R.id.tvGroupsTitle);

        // RecyclerViews
        rvDebtors = findViewById(R.id.rvDebtors);
        rvGroups = findViewById(R.id.rvGroups);
    }

    private void setupRecyclerViews() {
        // Должники - горизонтальный список
        rvDebtors.setLayoutManager(
                new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        );
        debtorsAdapter = new DebtorsAdapter(new ArrayList<>());
        rvDebtors.setAdapter(debtorsAdapter);

        // Группы - горизонтальный список
        rvGroups.setLayoutManager(
                new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        );
        groupsAdapter = new GroupsAdapter(new ArrayList<>());
        rvGroups.setAdapter(groupsAdapter);
    }

    private void loadData() {
        // Загрузка данных для блока 1
        tvOwedToYou.setText("+30$");
        tvYouOwe.setText("-20$");

        // Загрузка данных для блока 2
        tvPersonalSpent.setText("500$");
        tvPaidForOthers.setText("300$");
        tvStats.setText("Ты платил в 62% расходов\nВ 38% платили другие");

        // Данные для должников
        List<Debtor> debtorsList = new ArrayList<>();
        debtorsList.add(new Debtor("Иван Е", "5000 ₽"));
        debtorsList.add(new Debtor("Алла К", "3000 ₽"));
        debtorsList.add(new Debtor("Егор И", "500 ₽"));
        debtorsList.add(new Debtor("Мария С", "1200 ₽"));
        debtorsAdapter.setData(debtorsList);

        // Данные для групп
        List<Group> groupsList = new ArrayList<>();
        groupsList.add(new Group(R.drawable.ic_plane, "Поездка", "500 ₽"));
        groupsList.add(new Group(R.drawable.ic_home, "Жилье", "2500 ₽"));
        groupsList.add(new Group(R.drawable.ic_tree, "Поход", "6300 ₽"));
        groupsList.add(new Group(R.drawable.ic_cake, "Кафе", "2500 ₽"));
        groupsAdapter.setData(groupsList);
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

    public static class Group {
        private int iconResId;
        private String name;
        private String amount;

        public Group(int iconResId, String name, String amount) {
            this.iconResId = iconResId;
            this.name = name;
            this.amount = amount;
        }

        public int getIconResId() { return iconResId; }
        public String getName() { return name; }
        public String getAmount() { return amount; }
    }
}