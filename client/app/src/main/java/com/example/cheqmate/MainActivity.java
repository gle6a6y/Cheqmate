package com.example.cheqmate;

import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
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
                openAnalytics(groupId);
            });
            recyclerView.setAdapter(adapter);
        });

        btnGroups.setOnClickListener(v -> {
            Toast.makeText(this, "Группы", Toast.LENGTH_SHORT).show();
        });

        btnAnalytics.setOnClickListener(v -> {
            Toast.makeText(this, "Аналитика", Toast.LENGTH_SHORT).show();
        });

        btnHome.setOnClickListener(v -> {
            Toast.makeText(this, "Домой", Toast.LENGTH_SHORT).show();
        });

        btnAdd.setOnClickListener(v -> {
            Toast.makeText(this, "Добавить группу", Toast.LENGTH_SHORT).show();
        });
    }

    private void openAnalytics(int groupId) {
        for (Group group : groups) {
            if (group.getId() == groupId) {
                Intent intent = new Intent(MainActivity.this, AnalyticsActivity.class);
                intent.putExtra("group", (Parcelable) group);
                startActivity(intent);
                break;
            }
        }
    }
}