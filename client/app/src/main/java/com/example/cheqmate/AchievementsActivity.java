package com.example.cheqmate;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cheqmate.adapter.AchievementAdapter;
import com.example.cheqmate.dto.AchievementResponse;
import com.example.cheqmate.network.NetworkClient;
import com.example.cheqmate.network.SessionManager;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AchievementsActivity extends AppCompatActivity {

    private final List<AchievementResponse> achievements = new ArrayList<>();
    private AchievementAdapter adapter;
    private TextView tvCount, tvEmpty;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_achievements);

        tvCount = findViewById(R.id.tvCount);
        tvEmpty = findViewById(R.id.tvEmpty);
        RecyclerView rv = findViewById(R.id.rvAchievements);
        rv.setLayoutManager(new LinearLayoutManager(this));
        adapter = new AchievementAdapter(achievements);
        rv.setAdapter(adapter);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        loadAchievements();
    }

    private void loadAchievements() {
        String token = "Bearer " + new SessionManager(this).fetchAuthToken();
        NetworkClient.getApiService().getMyAchievements(token).enqueue(new Callback<List<AchievementResponse>>() {
            @Override
            public void onResponse(Call<List<AchievementResponse>> call, Response<List<AchievementResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<AchievementResponse> data = response.body();
                    adapter.setData(data);
                    tvCount.setText(data.size() + " / 6");
                    tvEmpty.setVisibility(data.isEmpty() ? View.VISIBLE : View.GONE);
                } else {
                    Log.e("Achievements", "Error: " + response.code());
                    Toast.makeText(AchievementsActivity.this, "Ошибка загрузки", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<AchievementResponse>> call, Throwable t) {
                Log.e("Achievements", "Network error", t);
                Toast.makeText(AchievementsActivity.this, "Ошибка сети", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
