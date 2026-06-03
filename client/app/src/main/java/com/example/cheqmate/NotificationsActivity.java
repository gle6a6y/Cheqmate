package com.example.cheqmate;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cheqmate.adapter.NotificationsAdapter;
import com.example.cheqmate.dto.NotificationResponse;
import com.example.cheqmate.network.ApiService;
import com.example.cheqmate.network.NetworkClient;
import com.example.cheqmate.network.SessionManager;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class NotificationsActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private TextView tvEmpty;
    private NotificationsAdapter adapter;
    private final List<NotificationResponse> notifications = new ArrayList<>();
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notifications);

        sessionManager = new SessionManager(this);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        recyclerView = findViewById(R.id.rvNotifications);
        tvEmpty = findViewById(R.id.tvEmpty);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        adapter = new NotificationsAdapter(notifications, this::onNotificationClicked);
        recyclerView.setAdapter(adapter);

        loadNotifications();
    }

    private void loadNotifications() {
        String token = sessionManager.fetchAuthToken();
        if (token == null) {
            return;
        }
        NetworkClient.getApiService().getNotifications("Bearer " + token)
                .enqueue(new Callback<List<NotificationResponse>>() {
                    @Override
                    public void onResponse(@NonNull Call<List<NotificationResponse>> call,
                                           @NonNull Response<List<NotificationResponse>> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            notifications.clear();
                            notifications.addAll(response.body());
                            adapter.notifyDataSetChanged();
                            updateEmptyState();
                        } else {
                            Toast.makeText(NotificationsActivity.this,
                                    "Не удалось загрузить уведомления", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<List<NotificationResponse>> call, @NonNull Throwable t) {
                        Toast.makeText(NotificationsActivity.this,
                                "Ошибка сети: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void onNotificationClicked(NotificationResponse notification, int position) {
        if (notification.isRead() || position == RecyclerView.NO_POSITION) {
            return;
        }
        String token = sessionManager.fetchAuthToken();
        if (token == null) {
            return;
        }
        notification.setRead(true);
        adapter.notifyItemChanged(position);

        NetworkClient.getApiService()
                .markNotificationRead("Bearer " + token, notification.getId())
                .enqueue(new Callback<Void>() {
                    @Override
                    public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                    }

                    @Override
                    public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                        notification.setRead(false);
                        adapter.notifyItemChanged(position);
                    }
                });
    }

    private void updateEmptyState() {
        boolean empty = notifications.isEmpty();
        tvEmpty.setVisibility(empty ? View.VISIBLE : View.GONE);
        recyclerView.setVisibility(empty ? View.GONE : View.VISIBLE);
    }
}
