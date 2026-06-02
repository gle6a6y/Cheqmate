package com.example.cheqmate;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;
import java.util.List;

public class CreateChequeActivity extends AppCompatActivity {

    private LinearLayout itemsContainer;
    private TextInputLayout tilChequeName;
    private TextInputEditText etChequeName;
    private List<ChequeItem> chequeItems = new ArrayList<>();
    private int itemCounter = 1;
    private String payer = "";

    private static final int FORTUNE_WHEEL_REQUEST = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_cheque);

        initViews();
        setupActions();
        addNewItem();
    }

    private void initViews() {
        tilChequeName = findViewById(R.id.tilChequeName);
        etChequeName = findViewById(R.id.etChequeName);
        itemsContainer = findViewById(R.id.itemsContainer);
    }

    private void setupActions() {
        ImageButton btnBack = findViewById(R.id.btnBack);
        MaterialButton btnAddItem = findViewById(R.id.btnAddItem);
        MaterialButton btnCreateCheque = findViewById(R.id.btnCreateCheque);
        MaterialButton btnFortuneWheel = findViewById(R.id.btnFortuneWheel);

        btnBack.setOnClickListener(v -> finish());
        btnAddItem.setOnClickListener(v -> addNewItem());
        btnCreateCheque.setOnClickListener(v -> submitCheque());
        btnFortuneWheel.setOnClickListener(v -> openFortuneWheel());
    }

    private void addNewItem() {
        View itemView = getLayoutInflater().inflate(R.layout.item_cheque_position, itemsContainer, false);
        
        TextInputEditText etItemName = itemView.findViewById(R.id.etItemName);
        TextInputEditText etItemPrice = itemView.findViewById(R.id.etItemPrice);
        Button btnRemoveItem = itemView.findViewById(R.id.btnRemoveItem);
        
        etItemName.setHint("Позиция " + itemCounter);
        
        btnRemoveItem.setOnClickListener(v -> {
            itemsContainer.removeView(itemView);
            for (int i = chequeItems.size() - 1; i >= 0; i--) {
                if (chequeItems.get(i).view == itemView) {
                    chequeItems.remove(i);
                    break;
                }
            }
        });
        
       ChequeItem chequeItem = new ChequeItem(itemView, etItemName, etItemPrice);
        chequeItems.add(chequeItem);
        itemsContainer.addView(itemView);
        itemCounter++;
    }

    private void openFortuneWheel() {
        Intent intent = new Intent(this, FortuneWheelActivity.class);
        startActivityForResult(intent, FORTUNE_WHEEL_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == FORTUNE_WHEEL_REQUEST && resultCode == RESULT_OK && data != null) {
            payer = data.getStringExtra("loser");
            Toast.makeText(this, "Плательщик: " + payer, Toast.LENGTH_SHORT).show();
        }
    }

    private void submitCheque() {
        tilChequeName.setError(null);
        
        String chequeName = etChequeName.getText() != null ? etChequeName.getText().toString().trim() : "";
        
        if (TextUtils.isEmpty(chequeName)) {
            tilChequeName.setError("Введите название чека");
            return;
        }
        
        List<ItemData> itemsData = new ArrayList<>();
        double total = 0.0;
        
        for (ChequeItem chequeItem : chequeItems) {
            String itemName = chequeItem.etName.getText() != null ? 
                chequeItem.etName.getText().toString().trim() : "";
            String itemPriceStr = chequeItem.etPrice.getText() != null ? 
                chequeItem.etPrice.getText().toString().trim() : "";
            
            if (TextUtils.isEmpty(itemName)) {
                itemName = "Позиция";
            }
            
            if (!TextUtils.isEmpty(itemPriceStr)) {
                try {
                    double price = Double.parseDouble(itemPriceStr);
                    itemsData.add(new ItemData(itemName, price));
                    total += price;
                } catch (NumberFormatException e) {
                }
            }
        }
        
        if (itemsData.isEmpty()) {
            Toast.makeText(this, "Добавьте хотя бы одну позицию с ценой", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Если плательщик выбран через рулетку, назначаем его на все позиции
        String payerName = payer.isEmpty() ? "Не выбран" : payer;
        
        // Отправляем данные в бэкенд с информацией о плательщике
        sendChequeToBackend(chequeName, total, itemsData, payerName);
        
        Toast.makeText(this, 
            String.format("Чек создан: %s, позиций: %d, сумма: %.2f, плательщик: %s", 
                chequeName, itemsData.size(), total, payerName), 
            Toast.LENGTH_LONG).show();
        finish();
    }
    
    private void sendChequeToBackend(String chequeName, double total, List<ItemData> itemsData, String payerName) {
        // TODO: Реализовать отправку в бэкенд через API
        // В DTO CreateChequeRequest есть поля:
        // - chequeName
        // - total
        // - whoPaidName (плательщик)
        // - ownerName (владелец чека)
        // - proportions (распределение позиций)
        
        // Пока просто логируем
        android.util.Log.d("CreateCheque", "Создание чека: " + chequeName + 
            ", сумма: " + total + ", плательщик: " + payerName + 
            ", позиций: " + itemsData.size());
    }

    private static class ChequeItem {
        View view;
        TextInputEditText etName;
        TextInputEditText etPrice;
        
        ChequeItem(View view, TextInputEditText etName, TextInputEditText etPrice) {
            this.view = view;
            this.etName = etName;
            this.etPrice = etPrice;
        }
    }
    
    private static class ItemData {
        String name;
        double price;
        
        ItemData(String name, double price) {
            this.name = name;
            this.price = price;
        }
    }
}
