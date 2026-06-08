package com.example.cheqmate.network;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.cheqmate.dto.NotificationResponse;
import com.google.gson.Gson;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.sse.EventSource;
import okhttp3.sse.EventSourceListener;
import okhttp3.sse.EventSources;

public class SseClient {

    private static final String TAG = "SseClient";

    public interface Listener {
        void onNotification(NotificationResponse notification);
    }

    private final OkHttpClient client = new OkHttpClient.Builder()
            .readTimeout(0, TimeUnit.MILLISECONDS)
            .retryOnConnectionFailure(true)
            .build();

    private final Gson gson = new Gson();
    private EventSource eventSource;

    public void connect(String jwt, Listener listener) {
        if (eventSource != null) {
            return;
        }
        Request request = new Request.Builder()
                .url(NetworkClient.getBaseUrl() + "api/notifications/subscribe?token=" + jwt)
                .build();

        eventSource = EventSources.createFactory(client).newEventSource(request, new EventSourceListener() {
            @Override
            public void onEvent(@NonNull EventSource es, @Nullable String id,
                                @Nullable String type, @NonNull String data) {
                if ("NOTIFICATION".equals(type)) {
                    try {
                        NotificationResponse n = gson.fromJson(data, NotificationResponse.class);
                        if (n != null) {
                            listener.onNotification(n);
                        }
                    } catch (Exception e) {
                        Log.w(TAG, "Bad SSE payload: " + data, e);
                    }
                }
            }

            @Override
            public void onFailure(@NonNull EventSource es, @Nullable Throwable t, @Nullable Response response) {
                Log.w(TAG, "SSE connection failed", t);
                eventSource = null;
            }
        });
    }

    public void disconnect() {
        if (eventSource != null) {
            eventSource.cancel();
            eventSource = null;
        }
    }
}
