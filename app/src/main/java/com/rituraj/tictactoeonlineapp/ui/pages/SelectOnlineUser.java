package com.rituraj.tictactoeonlineapp.ui.pages;

import android.app.ProgressDialog;
import android.os.Bundle;

import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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
    private User currUser = new User();
    private ArrayList<User> users = new ArrayList<>();
    private RecyclerView recyclerView;
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

        setupRecyclerView();
        fun();
    }

    private void setupRecyclerView() {
        recyclerView = findViewById(R.id.userRecyclerView);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        userAdapter = new UserListView(this, users, currUser);
        recyclerView.setAdapter(userAdapter);
    }

    public void fun() {
        if (auth.getCurrentUser() == null) return;

        currUserEmail = auth.getCurrentUser().getEmail();

        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Loading");
        progressDialog.setMessage("Loading users ...");
        progressDialog.setCancelable(false);
        progressDialog.show();

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
                if (progressDialog != null && progressDialog.isShowing()) {
                    progressDialog.dismiss();
                }
                users.clear();
                for (DataSnapshot snap : snapshot.getChildren()) {
                    User user = snap.getValue(User.class);
                    if (user != null) {
                        user.setUserId(snap.getKey());
                        Boolean online = snap.child("isOnline").getValue(Boolean.class);
                        user.setOnline(online != null && online);

                        if (!currUserEmail.equals(user.getEmail()))
                            if (user.isOnline())
                                users.add(0, user);
                            else
                                users.add(user);
                    }
                }
                userAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                if (progressDialog != null && progressDialog.isShowing()) {
                    progressDialog.dismiss();
                }

                Toast.makeText(SelectOnlineUser.this, error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}