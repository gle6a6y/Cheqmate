package com.example.cheqmate;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cheqmate.adapter.GroupAdapter;
import com.example.cheqmate.model.Group;
import com.example.cheqmate.network.PushTokenManager;
import com.example.cheqmate.network.SessionManager;
import com.example.cheqmate.network.SseClient;
import com.example.cheqmate.notification.Notifier;
import com.example.cheqmate.viewmodel.GroupsViewModel;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private GroupsViewModel viewModel;
    private List<Group> groups;

    private final SseClient sseClient = new SseClient();

    private final ActivityResultLauncher<String> notificationPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), granted -> {
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_groups);

        Notifier.ensureChannel(this);
        requestNotificationPermissionIfNeeded();
        PushTokenManager.syncToken(this);

        recyclerView = findViewById(R.id.recyclerView);
        ImageView btnAdd = findViewById(R.id.btnAdd);
        TextView btnGroups = findViewById(R.id.btnGroups);
        TextView btnAnalytics = findViewById(R.id.btnAnalytics);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));

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

        findViewById(R.id.btnNotifications).setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, NotificationsActivity.class)));

        btnAdd.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, CreateGroupActivity.class);
            startActivity(intent);
        });

        findViewById(R.id.btnLogout).setOnClickListener(v -> {
            SessionManager sessionManager = new SessionManager(this);
            PushTokenManager.unregisterCurrentToken(this);
            sseClient.disconnect();
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
        connectLiveNotifications();
    }

    @Override
    protected void onPause() {
        super.onPause();
        sseClient.disconnect();
    }

    private void connectLiveNotifications() {
        String jwt = new SessionManager(this).fetchAuthToken();
        if (jwt == null) {
            return;
        }
        sseClient.connect(jwt, notification ->
                Notifier.show(getApplicationContext(), notification.getTitle(), notification.getBody()));
    }

    private void requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
                && ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
        }
    }

    private void openGroupDetails(int groupId) {
        for (Group group : groups) {
            if (group.getId() == groupId) {
                Intent intent = new Intent(MainActivity.this, GroupDetailsActivity.class);
                intent.putExtra("GROUP_ID", group.getId());
                intent.putExtra("GROUP_NAME", group.getName());
                intent.putStringArrayListExtra("MEMBERS", group.getParticipants());
                startActivity(intent);
                break;
            }
        }
    }
}
