package com.example.cheqmate.notification;

import android.content.Context;
import android.view.View;
import android.widget.TextView;

import com.example.cheqmate.network.NetworkClient;
import com.example.cheqmate.network.SessionManager;

import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public final class NotificationBadge {

    private NotificationBadge() {
    }

    public static void refresh(Context context, TextView badgeView) {
        if (badgeView == null) {
            return;
        }
        String token = new SessionManager(context).fetchAuthToken();
        if (token == null) {
            return;
        }
        NetworkClient.getApiService().getUnreadCount("Bearer " + token)
                .enqueue(new Callback<Map<String, Long>>() {
                    @Override
                    public void onResponse(Call<Map<String, Long>> call, Response<Map<String, Long>> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            Long count = response.body().get("count");
                            apply(badgeView, count == null ? 0 : count);
                        }
                    }

                    @Override
                    public void onFailure(Call<Map<String, Long>> call, Throwable t) {
                    }
                });
    }

    private static void apply(TextView badgeView, long count) {
        if (count <= 0) {
            badgeView.setVisibility(View.GONE);
            return;
        }
        badgeView.setText(count > 9 ? "9+" : String.valueOf(count));
        badgeView.setVisibility(View.VISIBLE);
    }
}
