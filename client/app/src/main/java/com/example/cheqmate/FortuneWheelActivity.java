package com.example.cheqmate;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Random;

public class FortuneWheelActivity extends AppCompatActivity {

    private ImageView wheelImage;
    private TextView resultText;
    private EditText etChequeName;
    private EditText etTotalAmount;
    private Button btnSpin;
    private Button btnBack;
    
    private final String[] participants = {"Катя", "Иван", "Олег", "Алина"};
    private boolean isSpinning = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fortune_wheel);

        initViews();
        setupActions();
    }

    private void initViews() {
        wheelImage = findViewById(R.id.wheelImage);
        resultText = findViewById(R.id.resultText);
        etChequeName = findViewById(R.id.etChequeName);
        etTotalAmount = findViewById(R.id.etTotalAmount);
        btnSpin = findViewById(R.id.btnSpin);
        btnBack = findViewById(R.id.btnBack);
    }

    private void setupActions() {
        btnBack.setOnClickListener(v -> finish());
        
        btnSpin.setOnClickListener(v -> {
            if (isSpinning) return;
            
            // Проверка ввода
            String chequeName = etChequeName.getText().toString().trim();
            String amountStr = etTotalAmount.getText().toString().trim();
            
            if (TextUtils.isEmpty(chequeName)) {
                etChequeName.setError("Введите название чека");
                return;
            }
            
            if (TextUtils.isEmpty(amountStr)) {
                etTotalAmount.setError("Введите сумму");
                return;
            }
            
            try {
                double amount = Double.parseDouble(amountStr);
                if (amount <= 0) {
                    etTotalAmount.setError("Сумма должна быть больше 0");
                    return;
                }
                
                // Запуск анимации рулетки
                spinWheel(chequeName, amount);
            } catch (NumberFormatException e) {
                etTotalAmount.setError("Введите корректное число");
                return;
            }
        });
    }

    private void spinWheel(String chequeName, double amount) {
        isSpinning = true;
        resultText.setText("Крутим рулетку...");
        
        // Анимация вращения
        Random random = new Random();
        int spinDuration = 3000; // 3 секунды
        float rotateDegrees = 360 * 5 + random.nextInt(360); // 5 полных оборотов + случайный угол
        
        RotateAnimation rotateAnimation = new RotateAnimation(
                0, rotateDegrees,
                Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF, 0.5f
        );
        
        rotateAnimation.setDuration(spinDuration);
        rotateAnimation.setFillAfter(true);
        
        rotateAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                btnSpin.setEnabled(false);
            }
            
            @Override
            public void onAnimationEnd(Animation animation) {
                // Определяем победителя
                String winner = determineWinner();
                resultText.setText(String.format("Чек \"%s\" на сумму %.2f ₽\nОплачивает: %s", 
                    chequeName, amount, winner));
                
                Toast.makeText(FortuneWheelActivity.this, 
                    "Чек добавлен с помощью рулетки", Toast.LENGTH_SHORT).show();
                
                isSpinning = false;
                btnSpin.setEnabled(true);
            }
            
            @Override
            public void onAnimationRepeat(Animation animation) {}
        });
        
        wheelImage.startAnimation(rotateAnimation);
    }

    private String determineWinner() {
        Random random = new Random();
        return participants[random.nextInt(participants.length)];
    }
}