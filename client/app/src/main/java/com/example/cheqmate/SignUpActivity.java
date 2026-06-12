package com.example.cheqmate;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.cheqmate.dto.LoginRequest;
import com.example.cheqmate.network.ApiService;
import com.example.cheqmate.network.NetworkClient;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SignUpActivity extends AppCompatActivity {

    private TextInputEditText etLogin;
    private TextInputEditText etPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        etLogin = findViewById(R.id.etLogin);
        etPassword = findViewById(R.id.etPassword);
        MaterialButton btnSignUp = findViewById(R.id.btnSignUp);
        TextView tvFooter = findViewById(R.id.tvAlreadyHaveAccount);

        btnSignUp.setOnClickListener(v -> performRegister());

        setupFooter(tvFooter);
    }

    private void performRegister() {
        String login = etLogin.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (login.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Заполните все поля", Toast.LENGTH_SHORT).show();
            return;
        }

        ApiService apiService = NetworkClient.getApiService();
        LoginRequest request = new LoginRequest(login, password);

        apiService.register(request).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(SignUpActivity.this, "Регистрация успешна! Теперь войдите", Toast.LENGTH_LONG).show();
                    startActivity(new Intent(SignUpActivity.this, LoginActivity.class));
                    finish();
                } else {
                    // Если имя занято, сервер вернет ошибку (например, 400 или 500)
                    Toast.makeText(SignUpActivity.this, "Ошибка: имя пользователя уже занято или некорректно", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                Toast.makeText(SignUpActivity.this, "Ошибка сети: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupFooter(TextView tvFooter) {
        String leading = "Уже есть аккаунт? ";
        String link = "Войти";
        SpannableString span = new SpannableString(leading + link);
        int linkStart = leading.length();
        int linkEnd = linkStart + link.length();
        span.setSpan(new ClickableSpan() {
            @Override
            public void onClick(@NonNull View widget) {
                startActivity(new Intent(SignUpActivity.this, LoginActivity.class));
                finish();
            }

            @Override
            public void updateDrawState(@NonNull TextPaint ds) {
                super.updateDrawState(ds);
                ds.setColor(ContextCompat.getColor(SignUpActivity.this, R.color.black));
                ds.setUnderlineText(true);
            }
        }, linkStart, linkEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        tvFooter.setText(span);
        tvFooter.setMovementMethod(LinkMovementMethod.getInstance());
        tvFooter.setHighlightColor(Color.TRANSPARENT);
    }
}
