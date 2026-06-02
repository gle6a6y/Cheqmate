package com.example.cheqmate;

import android.content.Intent;
import android.os.Bundle;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Random;

public class FortuneWheelActivity extends AppCompatActivity {

    private ImageView wheelImage;
    private TextView resultText;
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
        btnSpin = findViewById(R.id.btnSpin);
        btnBack = findViewById(R.id.btnBack);
    }

    private void setupActions() {
        btnBack.setOnClickListener(v -> finish());
        
        btnSpin.setOnClickListener(v -> {
            if (isSpinning) return;
            spinWheel();
        });
    }

    private void spinWheel() {
        isSpinning = true;
        resultText.setText("Крутим рулетку...");
        
        Random random = new Random();
        int spinDuration = 3000;
        float rotateDegrees = 360 * 5 + random.nextInt(360);
        
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
                String loser = determineLoser();
                resultText.setText(String.format("Плательщик: %s", loser));
                
                Intent result = new Intent();
                result.putExtra("loser", loser);
                setResult(RESULT_OK, result);
                
                Toast.makeText(FortuneWheelActivity.this, 
                    "Выбран плательщик: " + loser, Toast.LENGTH_SHORT).show();
                
                isSpinning = false;
                btnSpin.setEnabled(true);
                finish();
            }
            
            @Override
            public void onAnimationRepeat(Animation animation) {}
        });
        
        wheelImage.startAnimation(rotateAnimation);
    }

    private String determineLoser() {
        Random random = new Random();
        return participants[random.nextInt(participants.length)];
    }
}
