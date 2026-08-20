package com.rituraj.tictactoeonlineapp.ui.leaderboard;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.rituraj.tictactoeonlineapp.R;
import com.rituraj.tictactoeonlineapp.adapters.LeaderboardAdapter;
import com.rituraj.tictactoeonlineapp.models.Player;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class LeaderboardActivity extends AppCompatActivity {
    private FirebaseDatabase database;
    private DatabaseReference reference;
    private RecyclerView rvLeaderboard;
    private LeaderboardAdapter adapter;
    private EditText etSearch;
    private Spinner spinnerSort;
    private TextView tvTotalPlayers, tvAvgWinRate, tvTopScore;
    private List<Player> allPlayers;
    private View emptyState;
    private DecimalFormat decimalFormat = new DecimalFormat("#.##");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_leaderboard);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        fun();
    }

    private void fun() {
        initViews();
        setupRecyclerView();
        setupSearch();
        setupSortSpinner();
        loadPlayerData();
    }

    private void initViews() {
        rvLeaderboard = findViewById(R.id.rvLeaderboard);
        etSearch = findViewById(R.id.etSearch);
        spinnerSort = findViewById(R.id.spinnerSort);
        tvTotalPlayers = findViewById(R.id.tvTotalPlayers);
        tvAvgWinRate = findViewById(R.id.tvAvgWinRate);
        tvTopScore = findViewById(R.id.tvTopScore);
        emptyState = findViewById(R.id.emptyState);

        allPlayers = new ArrayList<>();
    }

    private void setupRecyclerView() {
        adapter = new LeaderboardAdapter(player -> {
            android.widget.Toast.makeText(LeaderboardActivity.this, "Selected: " + player.getName(), android.widget.Toast.LENGTH_SHORT).show();
        });

        rvLeaderboard.setLayoutManager(new LinearLayoutManager(this));
        rvLeaderboard.setAdapter(adapter);
        rvLeaderboard.setHasFixedSize(true);
    }

    private void setupSearch() {
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                adapter.filter(s.toString());
                updateEmptyState();
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    private void setupSortSpinner() {
        spinnerSort.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String sortBy = parent.getItemAtPosition(position).toString();
                adapter.sortBy(sortBy);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    private void updateEmptyState() {
        if (adapter.getItemCount() == 0) {
            emptyState.setVisibility(View.VISIBLE);
            rvLeaderboard.setVisibility(View.GONE);
        } else {
            emptyState.setVisibility(View.GONE);
            rvLeaderboard.setVisibility(View.VISIBLE);
        }
    }

    private void loadPlayerData() {
        List<Player> players = new ArrayList<>();
        database = FirebaseDatabase.getInstance();
        reference = database.getReference().child("Players");
        reference.keepSynced(true);
        reference.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                if (!snapshot.exists()) return;
                Player player = snapshot.getValue(Player.class);
                if (player != null) {
                    players.add(player);
                    allPlayers = players;
                    adapter.setPlayers(players);

                    adapter.sortBy("Win Rate");
                    updateStats();
                    updateEmptyState();
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void updateStats() {
        tvTotalPlayers.setText(String.valueOf(allPlayers.size()));

        double avgWinRate = 0;
        int maxScore = 0;

        for (Player player : allPlayers) {
            avgWinRate += player.getWinRate();
            try {
                int lastScore = Integer.parseInt(player.getLastScore());
                if (lastScore > maxScore) {
                    maxScore = lastScore;
                }
            } catch (NumberFormatException e) {
                // Handle if lastScore is not a number
            }
        }

        if (allPlayers.size() > 0) {
            avgWinRate = (avgWinRate / allPlayers.size()) * 100;
            tvAvgWinRate.setText(String.format("%.1f%%", avgWinRate));
        }

        tvTopScore.setText(String.valueOf(maxScore));
    }
}