package com.example.cheqmate.network;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import com.example.cheqmate.dto.RegisterDeviceRequest;
import com.google.firebase.messaging.FirebaseMessaging;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public final class PushTokenManager {

    private static final String TAG = "PushTokenManager";

    private PushTokenManager() {
    }

    public static void syncToken(Context context) {
        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(task -> {
                    if (!task.isSuccessful() || task.getResult() == null) {
                        Log.w(TAG, "Failed to fetch FCM token", task.getException());
                        return;
                    }
                    registerToken(context, task.getResult());
                });
    }

    public static void registerToken(Context context, String fcmToken) {
        String jwt = new SessionManager(context).fetchAuthToken();
        if (jwt == null || fcmToken == null) {
            return;
        }
        NetworkClient.getApiService()
                .registerDevice("Bearer " + jwt, new RegisterDeviceRequest(fcmToken, "android"))
                .enqueue(logCallback("register"));
    }

    public static void unregisterCurrentToken(Context context) {
        final String jwt = new SessionManager(context).fetchAuthToken();
        if (jwt == null) {
            return;
        }
        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(task -> {
                    if (!task.isSuccessful() || task.getResult() == null) {
                        return;
                    }
                    NetworkClient.getApiService()
                            .unregisterDevice("Bearer " + jwt, task.getResult())
                            .enqueue(logCallback("unregister"));
                });
    }

    private static Callback<Void> logCallback(String op) {
        return new Callback<Void>() {
            @Override
            public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                Log.d(TAG, "device " + op + " -> " + response.code());
            }

            @Override
            public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                Log.w(TAG, "device " + op + " failed", t);
            }
        };
    }
}
