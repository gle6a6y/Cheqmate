package com.example.cheqmate;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;

public class StartActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

        MaterialButton btnCreateAccount = findViewById(R.id.btnCreateAccount);
        MaterialButton btnSignIn = findViewById(R.id.btnSignIn);
        MaterialButton btnTestDirect = findViewById(R.id.btnTestDirect);

        btnCreateAccount.setOnClickListener(v ->
                startActivity(new Intent(this, SignUpActivity.class)));

        btnSignIn.setOnClickListener(v ->
                startActivity(new Intent(this, LoginActivity.class)));
        
        btnTestDirect.setOnClickListener(v ->
                startActivity(new Intent(this, MainActivity.class)));
    }
}
