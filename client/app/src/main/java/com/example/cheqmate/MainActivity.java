package com.example.cheqmate;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.cheqmate.network.SessionManager;
import com.google.android.material.button.MaterialButton;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        
        sessionManager = new SessionManager(this);

        MaterialButton btnCreateCheque = findViewById(R.id.btnCreateCheque);
        MaterialButton btnGroups = findViewById(R.id.btnGroups);
        MaterialButton btnLogout = findViewById(R.id.btnLogout);

        btnCreateCheque.setOnClickListener(v -> {
            android.util.Log.d("MainActivity", "Кнопка 'Создать чек' нажата");
            Intent intent = new Intent(MainActivity.this, CreateChequeActivity.class);
            startActivity(intent);
        });
                
        btnGroups.setOnClickListener(v -> {
            // TODO: Implement groups activity
            Toast.makeText(MainActivity.this, "Раздел групп в разработке", Toast.LENGTH_SHORT).show();
        });

        btnLogout.setOnClickListener(v -> {
            sessionManager.clearData();
            startActivity(new Intent(MainActivity.this, StartActivity.class));
            finish();
        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }
}