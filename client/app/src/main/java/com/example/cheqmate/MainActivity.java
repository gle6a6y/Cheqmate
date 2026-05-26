package com.example.cheqmate;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cheqmate.adapter.GroupAdapter;
import com.example.cheqmate.model.Group;
import com.example.cheqmate.viewmodel.GroupsViewModel;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private GroupsViewModel viewModel;
    private List<Group> groups;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_groups);

        recyclerView = findViewById(R.id.recyclerView);
        ImageView btnHome = findViewById(R.id.btnHome);
        ImageView btnAdd = findViewById(R.id.btnAdd);
        TextView btnGroups = findViewById(R.id.btnGroups);
        TextView btnAnalytics = findViewById(R.id.btnAnalytics);

        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 2);
        recyclerView.setLayoutManager(gridLayoutManager);

        viewModel = new ViewModelProvider(this).get(GroupsViewModel.class);

        viewModel.getGroups().observe(this, groupList -> {
            groups = groupList;
            GroupAdapter adapter = new GroupAdapter(groupList, groupId -> {
                openGroupDetails(groupId);
            });
            recyclerView.setAdapter(adapter);
        });

        btnGroups.setOnClickListener(v -> {
            Toast.makeText(this, "Группы", Toast.LENGTH_SHORT).show();
        });

        btnAnalytics.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, AnalyticsActivity.class);
            startActivity(intent);
        });

        btnHome.setOnClickListener(v -> {
            Toast.makeText(this, "Домой", Toast.LENGTH_SHORT).show();
        });

        btnAdd.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, CreateGroupActivity.class);
            startActivity(intent);
        });

        TextView btnLogout = findViewById(R.id.btnLogout);
        btnLogout.setOnClickListener(v -> {
            com.example.cheqmate.network.SessionManager sessionManager = new com.example.cheqmate.network.SessionManager(this);
            sessionManager.clearData();
            Intent intent = new Intent(MainActivity.this, StartActivity.class);
            startActivity(intent);
            finish();
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (viewModel != null) {
            viewModel.loadGroups();
        }
    }

    private void openGroupDetails(int groupId) {
        for (Group group : groups) {
            if (group.getId() == groupId) {
                Intent intent = new Intent(MainActivity.this, GroupDetailsActivity.class);
                intent.putExtra("GROUP_ID", group.getId());
                intent.putExtra("GROUP_NAME", group.getName());
                // В реальном приложении здесь нужно передавать список участников
                // Например: intent.putStringArrayListExtra("MEMBERS", group.getMemberNames());
                startActivity(intent);
                break;
            }
        }
    }
}
