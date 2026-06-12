package com.example.cheqmate;

import android.app.Dialog;
import android.os.Bundle;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cheqmate.adapter.PersonalExpenseAdapter;
import com.example.cheqmate.dto.PersonalExpenseRequest;
import com.example.cheqmate.dto.PersonalExpenseResponse;
import com.example.cheqmate.network.NetworkClient;
import com.example.cheqmate.network.SessionManager;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PersonalExpensesActivity extends AppCompatActivity {

    private static final String[] CATEGORIES = {
            "Коммуналка", "Продукты", "Транспорт", "Здоровье", "Развлечения", "Другое"
    };

    private final List<PersonalExpenseResponse> expenses = new ArrayList<>();
    private PersonalExpenseAdapter adapter;
    private TextView tvTotal;
    private String token;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_personal_expenses);

        token = "Bearer " + new SessionManager(this).fetchAuthToken();

        tvTotal = findViewById(R.id.tvTotal);
        RecyclerView rvExpenses = findViewById(R.id.rvExpenses);
        rvExpenses.setLayoutManager(new LinearLayoutManager(this));
        adapter = new PersonalExpenseAdapter(expenses, this::deleteExpense);
        rvExpenses.setAdapter(adapter);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        findViewById(R.id.btnAddExpense).setOnClickListener(v -> showAddDialog());

        loadExpenses();
    }

    private void loadExpenses() {
        NetworkClient.getApiService().getPersonalExpenses(token).enqueue(new Callback<List<PersonalExpenseResponse>>() {
            @Override
            public void onResponse(Call<List<PersonalExpenseResponse>> call, Response<List<PersonalExpenseResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    adapter.setData(response.body());
                    updateTotal(response.body());
                }
            }

            @Override
            public void onFailure(Call<List<PersonalExpenseResponse>> call, Throwable t) {
                Toast.makeText(PersonalExpensesActivity.this, "Ошибка сети", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateTotal(List<PersonalExpenseResponse> list) {
        double total = 0;
        for (PersonalExpenseResponse e : list) total += e.getAmount();
        tvTotal.setText(String.format("%.0f ₽", total));
    }

    private void showAddDialog() {
        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_add_expense);
        dialog.getWindow().setLayout(
                (int) (getResources().getDisplayMetrics().widthPixels * 0.9),
                android.view.ViewGroup.LayoutParams.WRAP_CONTENT
        );

        AutoCompleteTextView actCategory = dialog.findViewById(R.id.actCategory);
        TextInputEditText etAmount = dialog.findViewById(R.id.etAmount);
        TextInputEditText etDescription = dialog.findViewById(R.id.etDescription);

        ArrayAdapter<String> catAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, CATEGORIES);
        actCategory.setAdapter(catAdapter);
        actCategory.setText(CATEGORIES[0], false);

        dialog.findViewById(R.id.btnSave).setOnClickListener(v -> {
            String category = actCategory.getText().toString().trim();
            String amountStr = etAmount.getText() != null ? etAmount.getText().toString().trim() : "";
            String description = etDescription.getText() != null ? etDescription.getText().toString().trim() : "";

            if (category.isEmpty() || amountStr.isEmpty()) {
                Toast.makeText(this, "Заполните категорию и сумму", Toast.LENGTH_SHORT).show();
                return;
            }

            double amount;
            try {
                amount = Double.parseDouble(amountStr);
            } catch (NumberFormatException e) {
                Toast.makeText(this, "Некорректная сумма", Toast.LENGTH_SHORT).show();
                return;
            }

            PersonalExpenseRequest request = new PersonalExpenseRequest(category, amount, description);
            NetworkClient.getApiService().createPersonalExpense(token, request).enqueue(new Callback<PersonalExpenseResponse>() {
                @Override
                public void onResponse(Call<PersonalExpenseResponse> call, Response<PersonalExpenseResponse> response) {
                    if (response.isSuccessful()) {
                        dialog.dismiss();
                        loadExpenses();
                    } else {
                        Toast.makeText(PersonalExpensesActivity.this, "Ошибка сохранения", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<PersonalExpenseResponse> call, Throwable t) {
                    Toast.makeText(PersonalExpensesActivity.this, "Ошибка сети", Toast.LENGTH_SHORT).show();
                }
            });
        });

        dialog.show();
    }

    private void deleteExpense(int id) {
        NetworkClient.getApiService().deletePersonalExpense(token, id).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) loadExpenses();
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(PersonalExpensesActivity.this, "Ошибка сети", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
