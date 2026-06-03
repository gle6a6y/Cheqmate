package com.example.cheqmate.viewmodel;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.cheqmate.dto.GroupResponse;
import com.example.cheqmate.model.Group;
import com.example.cheqmate.network.ApiService;
import com.example.cheqmate.network.NetworkClient;
import com.example.cheqmate.network.SessionManager;

import java.util.List;
import java.util.stream.Collectors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class GroupsViewModel extends AndroidViewModel {

    private final MutableLiveData<List<Group>> groups = new MutableLiveData<>();
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private final SessionManager sessionManager;

    public GroupsViewModel(@NonNull Application application) {
        super(application);
        sessionManager = new SessionManager(application);
        loadGroups();
    }

    public void loadGroups() {
        String token = sessionManager.fetchAuthToken();
        if (token == null) {
            errorMessage.setValue("Токен отсутствует. Войдите заново.");
            return;
        }

        ApiService apiService = NetworkClient.getApiService();
        apiService.getMyGroups("Bearer " + token).enqueue(new Callback<List<GroupResponse>>() {
            @Override
            public void onResponse(@NonNull Call<List<GroupResponse>> call, @NonNull Response<List<GroupResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Group> uiGroups = response.body().stream()
                            .map(dto -> new Group(
                                    dto.getId(),
                                    dto.getName(),
                                    "Сегодня", // для чего то lastActivity надо было, но я забыл. пусть останется
                                    dto.getParticipantsCount(),
                                    dto.getIncome(),
                                    dto.getExpense(),
                                    dto.getParticipants(),
                                    generateIcon(dto.getName())
                            ))
                            .collect(Collectors.toList());
                    groups.setValue(uiGroups);
                } else if (response.code() == 401) {
                    errorMessage.setValue("Сессия истекла. Пожалуйста, войдите снова.");
                } else {
                    errorMessage.setValue("Ошибка сервера: " + response.code());
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<GroupResponse>> call, @NonNull Throwable t) {
                errorMessage.setValue("Ошибка сети: " + t.getMessage());
                Log.e("GroupsViewModel", "Error loading groups", t);
            }
        });
    }

    private String generateIcon(String name) {
        if (name == null || name.length() < 2) return "GR";
        return name.substring(0, 2).toUpperCase();
    }

    public LiveData<List<Group>> getGroups() {
        return groups;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }
}
