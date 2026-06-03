package com.example.cheqmate.messaging;

import androidx.annotation.NonNull;

import com.example.cheqmate.network.PushTokenManager;
import com.example.cheqmate.notification.Notifier;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class CheqmateMessagingService extends FirebaseMessagingService {

    @Override
    public void onNewToken(@NonNull String token) {
        PushTokenManager.registerToken(getApplicationContext(), token);
    }

    @Override
    public void onMessageReceived(@NonNull RemoteMessage message) {
        String title = null;
        String body = null;

        if (message.getNotification() != null) {
            title = message.getNotification().getTitle();
            body = message.getNotification().getBody();
        }
        if (body == null && !message.getData().isEmpty()) {
            title = message.getData().get("title");
            body = message.getData().get("body");
        }

        if (body != null) {
            Notifier.show(getApplicationContext(), title, body);
        }
    }
}
