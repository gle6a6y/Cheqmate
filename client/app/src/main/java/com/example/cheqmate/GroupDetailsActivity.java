package com.example.cheqmate;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import java.io.IOException;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cheqmate.adapter.GroupChequesAdapter;
import com.example.cheqmate.adapter.YourDebtsAdapter;
import com.example.cheqmate.dto.ChequeResponse;
import com.example.cheqmate.dto.DebtResponse;
import com.example.cheqmate.dto.PayDebtRequest;
import com.example.cheqmate.network.NetworkClient;
import com.example.cheqmate.network.SessionManager;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
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
    private BottomSheetDialog payBottomSheet;
    private DebtPerson selectedDebt;
    private String groupName;
    private int groupId;

    private ArrayList<String> realGroupMembers = new ArrayList<>();
    private final List<DebtPerson> debtPeople = new ArrayList<>();
    private final List<ChequeResponse> groupCheques = new ArrayList<>();
    private YourDebtsAdapter adapter;
    private GroupChequesAdapter chequesAdapter;
    private TextView tvChequesEmpty;
    private TextView tvDebtsEmpty;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_details);

        groupName = getIntent().getStringExtra("GROUP_NAME");
        groupId = getIntent().getIntExtra("GROUP_ID", -1);
        realGroupMembers = getIntent().getStringArrayListExtra("MEMBERS");
        if (realGroupMembers == null) {
            realGroupMembers = new ArrayList<>();
        }

        for (String u : realGroupMembers) {
            Log.d("MEMBERS", u);
        }

        initHeaderAndActions();
        setupChequesList();
        setupDebtsList();
        loadData();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadCheques();
    }

    private void initHeaderAndActions() {
        ImageButton btnBack = findViewById(R.id.btnBack);
        ImageButton btnAddCheck = findViewById(R.id.btnAddCheck);
        btnPay = findViewById(R.id.btnPay);

        btnBack.setOnClickListener(v -> finish());

        btnAddCheck.setOnClickListener(v -> openCreateCheque());

        btnPay.setOnClickListener(v -> openPayPanel());
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

    private void setupChequesList() {
        RecyclerView rvCheques = findViewById(R.id.rvCheques);
        tvChequesEmpty = findViewById(R.id.tvChequesEmpty);
        rvCheques.setLayoutManager(new LinearLayoutManager(this));
        chequesAdapter = new GroupChequesAdapter(groupCheques);
        rvCheques.setAdapter(chequesAdapter);
    }

    private void setupDebtsList() {
        RecyclerView rvYourDebts = findViewById(R.id.rvYourDebts);
        tvDebtsEmpty = findViewById(R.id.tvDebtsEmpty);
        rvYourDebts.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));

        adapter = new YourDebtsAdapter(debtPeople, this::onDebtSelected);
        rvYourDebts.setAdapter(adapter);
    }

    private void loadData() {
        TextView tvGroupName = findViewById(R.id.tvGroupName);
        tvGroupName.setText(groupName != null ? groupName : "Группа");

        SessionManager sessionManager = new SessionManager(this);
        String token = "Bearer " + sessionManager.fetchAuthToken();

        loadCheques();

        NetworkClient.getApiService().getGroupFullInfo(token, groupId).enqueue(new Callback<com.google.gson.JsonObject>() {
            @Override
            public void onResponse(Call<com.google.gson.JsonObject> call, Response<com.google.gson.JsonObject> response) {
                if (response.isSuccessful() && response.body() != null) {
                    try {
                        com.google.gson.JsonArray members = response.body().getAsJsonArray("members");
                        if (members != null) {
                            realGroupMembers.clear();
                            for (com.google.gson.JsonElement el : members) {
                                realGroupMembers.add(el.getAsJsonObject().get("name").getAsString());
                            }
                        }
                    } catch (Exception ignored) {}
                }
            }

            @Override
            public void onFailure(Call<com.google.gson.JsonObject> call, Throwable t) {}
        });

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

    private void loadCheques() {
        if (groupId < 0) {
            Toast.makeText(this, "Нет id группы — откройте группу с главного экрана", Toast.LENGTH_SHORT).show();
            return;
        }

        String authToken = new SessionManager(this).fetchAuthToken();
        if (authToken == null) {
            Toast.makeText(this, "Войдите снова", Toast.LENGTH_SHORT).show();
            return;
        }

        String token = "Bearer " + authToken;
        NetworkClient.getApiService().getGroupCheques(token, groupId).enqueue(new Callback<List<ChequeResponse>>() {
            @Override
            public void onResponse(Call<List<ChequeResponse>> call, Response<List<ChequeResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    displayCheques(response.body());
                } else {
                    String details = readErrorBody(response);
                    Log.e("CHEQUES", "HTTP " + response.code() + " " + details);
                    Toast.makeText(GroupDetailsActivity.this,
                            "Не удалось загрузить чеки (" + response.code() + ")", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<ChequeResponse>> call, Throwable t) {
                Toast.makeText(GroupDetailsActivity.this,
                        "Ошибка сети при загрузке чеков", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private static String readErrorBody(Response<?> response) {
        if (response.errorBody() == null) {
            return "";
        }
        try {
            return response.errorBody().string();
        } catch (IOException e) {
            return e.getMessage();
        }
    }

    private void displayCheques(List<ChequeResponse> cheques) {
        groupCheques.clear();
        if (cheques != null) {
            groupCheques.addAll(cheques);
        }
        chequesAdapter.notifyDataSetChanged();

        boolean empty = groupCheques.isEmpty();
        tvChequesEmpty.setVisibility(empty ? View.VISIBLE : View.GONE);
    }

    private void parseAndDisplayDebts(DebtResponse response) {
        TextView tvOwedToYou = findViewById(R.id.tvOwedToYou);
        TextView tvYouOwe = findViewById(R.id.tvYouOwe);

        debtPeople.clear();

        if (response.getDebtors() != null) {
            for (DebtResponse.DebtItem d : response.getDebtors()) {
                String name = d.getName();
                double amount = d.getAmount();
                debtPeople.add(new DebtPerson(
                        R.drawable.ic_photo_user, name, amount,
                        String.format("+%.2f ₽", amount),
                        DebtDirection.OWES_ME));
            }
        }

        if (response.getCreditors() != null) {
            for (DebtResponse.DebtItem c : response.getCreditors()) {
                String name = c.getName();
                double amount = c.getAmount();
                debtPeople.add(new DebtPerson(
                        R.drawable.ic_photo_user, name, amount,
                        String.format("-%.2f ₽", amount),
                        DebtDirection.I_OWE));
            }
        }

        refreshDebtSummary();

        adapter.clearSelection();
        adapter.notifyDataSetChanged();
        hidePayPanel();
        btnPay.setVisibility(View.GONE);
        selectedDebt = null;

        updateDebtsEmptyState();
    }

    private void onDebtSelected(DebtPerson selectedPerson) {
        selectedDebt = selectedPerson;
        hidePayPanel();

        if (selectedPerson != null && selectedPerson.canPay()) {
            btnPay.setVisibility(View.VISIBLE);
        } else {
            btnPay.setVisibility(View.GONE);
            if (selectedPerson != null && selectedPerson.owesMe()) {
                Toast.makeText(this, selectedPerson.getName() + " должен вам — оплата не нужна",
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void openPayPanel() {
        if (selectedDebt == null || !selectedDebt.canPay()) {
            return;
        }

        hidePayPanel();

        payBottomSheet = new BottomSheetDialog(this);
        View sheetView = getLayoutInflater().inflate(R.layout.dialog_pay, null);
        payBottomSheet.setContentView(sheetView);

        TextView tvPayTitle = sheetView.findViewById(R.id.tvPayTitle);
        TextInputEditText etPayAmount = sheetView.findViewById(R.id.etPayAmount);
        MaterialButton btnTransfer = sheetView.findViewById(R.id.btnTransfer);

        if (selectedDebt.owesMe()) {
            tvPayTitle.setText(selectedDebt.getName() + " вернул долг?");
        } else {
            tvPayTitle.setText("Перевод для " + selectedDebt.getName());
        }
        btnTransfer.setOnClickListener(v -> performTransfer(etPayAmount));

        payBottomSheet.show();
    }

    private void performTransfer(TextInputEditText etPayAmount) {
        if (selectedDebt == null || !selectedDebt.canPay()) {
            return;
        }

        String raw = etPayAmount.getText() != null
                ? etPayAmount.getText().toString().trim().replace(',', '.')
                : "";
        if (raw.isEmpty()) {
            Toast.makeText(this, "Введите сумму", Toast.LENGTH_SHORT).show();
            return;
        }

        double paid;
        try {
            paid = Double.parseDouble(raw);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Некорректная сумма", Toast.LENGTH_SHORT).show();
            return;
        }

        if (paid <= 0) {
            Toast.makeText(this, "Сумма должна быть больше нуля", Toast.LENGTH_SHORT).show();
            return;
        }

        if (paid > selectedDebt.getAmountValue() + 1e-6) {
            Toast.makeText(this, "Сумма больше долга", Toast.LENGTH_SHORT).show();
            return;
        }

        SessionManager sessionManager = new SessionManager(this);
        String token = sessionManager.fetchAuthToken();
        String myName = sessionManager.fetchUserName();
        if (token == null) {
            Toast.makeText(this, "Войдите снова", Toast.LENGTH_SHORT).show();
            return;
        }

        PayDebtRequest request = new PayDebtRequest();
        if (selectedDebt.owesMe()) {
            request.setCreditorUsername(myName);
            request.setDebtorUsername(selectedDebt.getName());
        } else {
            request.setCreditorUsername(selectedDebt.getName());
        }
        request.setAmount(paid);

        NetworkClient.getApiService()
                .payDebtInGroup("Bearer " + token, groupId, request)
                .enqueue(new Callback<DebtResponse>() {
                    @Override
                    public void onResponse(Call<DebtResponse> call, Response<DebtResponse> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            hidePayPanel();
                            parseAndDisplayDebts(response.body());
                            Toast.makeText(GroupDetailsActivity.this,
                                    "Перевод учтён", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(GroupDetailsActivity.this,
                                    "Не удалось провести перевод (" + response.code() + ")",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<DebtResponse> call, Throwable t) {
                        Toast.makeText(GroupDetailsActivity.this,
                                "Ошибка сети: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void refreshDebtSummary() {
        TextView tvOwedToYou = findViewById(R.id.tvOwedToYou);
        TextView tvYouOwe = findViewById(R.id.tvYouOwe);

        double totalOwedToMe = 0;
        double totalIOwe = 0;

        for (DebtPerson person : debtPeople) {
            if (person.owesMe()) {
                totalOwedToMe += person.getAmountValue();
            } else {
                totalIOwe += person.getAmountValue();
            }
        }

        tvOwedToYou.setText(String.format("%.2f ₽", totalOwedToMe));
        tvYouOwe.setText(String.format("%.2f ₽", totalIOwe));

        tvOwedToYou.setTextColor(getColor(totalOwedToMe > 0
                ? R.color.money_positive : R.color.money_gray));
        tvYouOwe.setTextColor(getColor(totalIOwe > 0
                ? R.color.money_black : R.color.money_gray));
    }

    private void updateDebtsEmptyState() {
        tvDebtsEmpty.setVisibility(debtPeople.isEmpty() ? View.VISIBLE : View.GONE);
    }

    private void hidePayPanel() {
        if (payBottomSheet != null && payBottomSheet.isShowing()) {
            payBottomSheet.dismiss();
        }
        payBottomSheet = null;
    }

    public enum DebtDirection {
        OWES_ME,
        I_OWE
    }

    public static class DebtPerson {
        private final int iconResId;
        private final String name;
        private final DebtDirection direction;
        private double amountValue;
        private String amount;

        public DebtPerson(int iconResId, String name, double amountValue, String amount,
                          DebtDirection direction) {
            this.iconResId = iconResId;
            this.name = name;
            this.amountValue = amountValue;
            this.amount = amount;
            this.direction = direction;
        }

        public int getIconResId() { return iconResId; }
        public String getName() { return name; }
        public String getAmount() { return amount; }
        public double getAmountValue() { return amountValue; }
        public DebtDirection getDirection() { return direction; }
        public boolean owesMe() { return direction == DebtDirection.OWES_ME; }
        public boolean canPay() { return true; }
    }
}