package com.rituraj.tictactoeonlineapp.ui.activities;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.rituraj.tictactoeonlineapp.R;
import com.rituraj.tictactoeonlineapp.adapters.UserListView;
import com.rituraj.tictactoeonlineapp.databinding.ActivitySelectOnlineUserBinding;
import com.rituraj.tictactoeonlineapp.models.User;

import java.util.ArrayList;

public class SelectOnlineUser extends AppCompatActivity {

    private ActivitySelectOnlineUserBinding binding;
    private FirebaseDatabase database;
    private DatabaseReference reference;
    private FirebaseAuth auth;
    private ProgressDialog progressDialog;
    private String currUserEmail;
    private final User currUser = new User();
    private final ArrayList<User> users = new ArrayList<>();
    private UserListView userAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        binding = ActivitySelectOnlineUserBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        database = FirebaseDatabase.getInstance();
        auth = FirebaseAuth.getInstance();

        if (binding.btnBack != null) {
            binding.btnBack.setOnClickListener(v -> finish());
        }

        setupRecyclerView();
        fun();
    }

    private void setupRecyclerView() {
        binding.userRecyclerView.setHasFixedSize(true);
        binding.userRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        userAdapter = new UserListView(this, users, currUser);
        binding.userRecyclerView.setAdapter(userAdapter);
    }

    public void fun() {
        if (auth.getCurrentUser() == null) return;

        currUserEmail = auth.getCurrentUser().getEmail();

        if (!isFinishing()) {
            progressDialog = new ProgressDialog(this);
            progressDialog.setTitle("Loading Players");
            progressDialog.setMessage("Finding active online players...");
            progressDialog.setCancelable(false);
            progressDialog.show();
        }

        currUser.setUserId(auth.getCurrentUser().getUid());
        currUser.setProfileUrl(String.valueOf(auth.getCurrentUser().getPhotoUrl()));
        currUser.setEmail(auth.getCurrentUser().getEmail());
        currUser.setUserName(auth.getCurrentUser().getDisplayName());

        getAllUsers();
    }

    public void getAllUsers() {
        reference = database.getReference("Users");

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                dismissProgress();
                users.clear();
                for (DataSnapshot snap : snapshot.getChildren()) {
                    User user = snap.getValue(User.class);
                    if (user != null) {
                        user.setUserId(snap.getKey());

                        // Safe evaluation for isOnline from Firebase
                        Object onlineObj = snap.child("isOnline").getValue();
                        boolean isOnline = false;
                        if (onlineObj instanceof Boolean) {
                            isOnline = (Boolean) onlineObj;
                        } else if (onlineObj instanceof String) {
                            isOnline = Boolean.parseBoolean((String) onlineObj);
                        } else if (onlineObj instanceof Long) {
                            isOnline = ((Long) onlineObj) == 1;
                        }
                        user.setOnline(isOnline);

                        if (currUserEmail != null && !currUserEmail.equalsIgnoreCase(user.getEmail())) {
                            if (user.isOnline()) {
                                users.add(0, user);
                            } else {
                                users.add(user);
                            }
                        }
                    }
                }

                userAdapter.notifyDataSetChanged();
                updateEmptyState();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                dismissProgress();
                Toast.makeText(SelectOnlineUser.this, error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateEmptyState() {
        if (users.isEmpty()) {
            if (binding.emptyStateOnline != null)
                binding.emptyStateOnline.setVisibility(View.VISIBLE);
            binding.userRecyclerView.setVisibility(View.GONE);
        } else {
            if (binding.emptyStateOnline != null) binding.emptyStateOnline.setVisibility(View.GONE);
            binding.userRecyclerView.setVisibility(View.VISIBLE);
        }
    }

    private void dismissProgress() {
        if (!isFinishing() && progressDialog != null && progressDialog.isShowing()) {
            try {
                progressDialog.dismiss();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
