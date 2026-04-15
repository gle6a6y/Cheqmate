package com.example.cheqmate.network;

import android.content.Context;
import android.content.SharedPreferences;

public class SessionManager {
    private static final String PREF_NAME = "CheqmatePrefs";
    private static final String KEY_TOKEN = "auth_token";
    private static final String KEY_USER_NAME = "user_name";

    private final SharedPreferences prefs;

    public SessionManager(Context context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public void saveAuthToken(String token, String name) {
        prefs.edit()
            .putString(KEY_TOKEN, token)
            .putString(KEY_USER_NAME, name)
            .apply();
    }

    public String fetchAuthToken() {
        return prefs.getString(KEY_TOKEN, null);
    }

    public String fetchUserName() {
        return prefs.getString(KEY_USER_NAME, null);
    }

    public void clearData() {
        prefs.edit().clear().apply();
    }
}
