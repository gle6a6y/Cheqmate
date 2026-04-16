package com.example.cheqmate.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.cheqmate.model.Group;

import java.util.ArrayList;
import java.util.List;

public class GroupsViewModel extends AndroidViewModel {

    private MutableLiveData<List<Group>> groups = new MutableLiveData<>();

    public GroupsViewModel(@NonNull Application application) {
        super(application);
        loadMockData();
    }

    private void loadMockData() {
        List<Group> mockGroups = new ArrayList<>();
        mockGroups.add(new Group(1, "Все для похода", "12.07", 4, 500, 320, "ВП"));
        mockGroups.add(new Group(2, "Ресторан", "12.07", 4, 1200, 0, "РЕ"));
        mockGroups.add(new Group(3, "День рождения", "12.07", 3, 700, 340, "ДР"));
        mockGroups.add(new Group(4, "Путешествие", "11.07", 5, 0, 1500, "ПУ"));
        mockGroups.add(new Group(5, "Продукты", "10.07", 2, 300, 0, "ПР"));
        groups.setValue(mockGroups);
    }

    public LiveData<List<Group>> getGroups() {
        return groups;
    }
}