package com.example.cheqmate;

import android.animation.ObjectAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.Random;

public class RouletteActivity extends AppCompatActivity {

    public static final String EXTRA_MEMBERS = "MEMBERS";
    public static final String EXTRA_LOSER = "LOSER";

    private RouletteWheelView wheelView;
    private Button btnSpin;
    private Button btnConfirmLoser;
    private TextView tvWinner;

    private ArrayList<String> participants;
    private String currentLoser;
    private boolean isSpinning = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_roulette);

        participants = getIntent().getStringArrayListExtra(EXTRA_MEMBERS);
        if (participants == null) participants = new ArrayList<>();

        wheelView = findViewById(R.id.rouletteWheelView);
        btnSpin = findViewById(R.id.btnSpin);
        btnConfirmLoser = findViewById(R.id.btnConfirmLoser);
        tvWinner = findViewById(R.id.tvWinner);

        wheelView.setParticipants(participants);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        btnSpin.setOnClickListener(v -> spinWheel());

        btnConfirmLoser.setOnClickListener(v -> {
            Intent result = new Intent();
            result.putExtra(EXTRA_LOSER, currentLoser);
            setResult(RESULT_OK, result);
            finish();
        });
    }

    private void spinWheel() {
        if (isSpinning || participants.isEmpty()) return;

        isSpinning = true;
        btnSpin.setEnabled(false);
        tvWinner.setVisibility(View.GONE);
        btnConfirmLoser.setVisibility(View.GONE);

        int winnerIndex = new Random().nextInt(participants.size());
        int extraRotations = 6 + new Random().nextInt(4);
        float finalAngle = wheelView.calcFinalAngle(winnerIndex, extraRotations);

        ObjectAnimator animator = ObjectAnimator.ofFloat(wheelView, "wheelAngle", wheelView.getWheelAngle(), finalAngle);
        animator.setDuration(4500);
        animator.setInterpolator(new DecelerateInterpolator(3f));
        animator.addListener(new android.animation.AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(android.animation.Animator animation) {
                isSpinning = false;
                btnSpin.setEnabled(true);
                currentLoser = participants.get(winnerIndex);
                tvWinner.setText("Проигравший: " + currentLoser);
                tvWinner.setVisibility(View.VISIBLE);
                btnConfirmLoser.setVisibility(View.VISIBLE);
            }
        });
        animator.start();
    }
}
