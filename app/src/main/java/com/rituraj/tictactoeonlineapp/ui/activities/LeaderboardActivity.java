package com.rituraj.tictactoeonlineapp.ui.activities;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.rituraj.tictactoeonlineapp.R;
import com.rituraj.tictactoeonlineapp.adapters.LeaderboardAdapter;
import com.rituraj.tictactoeonlineapp.models.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class LeaderboardActivity extends AppCompatActivity {
    private FirebaseDatabase database;
    private DatabaseReference reference;
    private RecyclerView rvLeaderboard;
    private LeaderboardAdapter adapter;
    private EditText etSearch;
    private Spinner spinnerSort;
    private TextView tvTotalPlayers, tvAvgWinRate, tvTopScore;
    private ImageView btnBack;
    private List<Player> allPlayers;
    private View emptyState;

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
        btnBack = findViewById(R.id.btnBack);

        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        allPlayers = new ArrayList<>();
    }

    private void setupRecyclerView() {
        adapter = new LeaderboardAdapter(player -> {
            String name = player.getName() != null ? player.getName() : "Player";
            Toast.makeText(LeaderboardActivity.this, name + " • " + player.getWinCount() + " Wins", Toast.LENGTH_SHORT).show();
        });

        rvLeaderboard.setLayoutManager(new LinearLayoutManager(this));
        rvLeaderboard.setAdapter(adapter);
        rvLeaderboard.setHasFixedSize(true);
    }

    private void setupSearch() {
        if (etSearch == null) return;
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (adapter != null) {
                    adapter.filter(s.toString());
                    updateEmptyState();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    private void setupSortSpinner() {
        if (spinnerSort == null) return;
        spinnerSort.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String sortBy = parent.getItemAtPosition(position).toString();
                if (adapter != null) {
                    adapter.sortBy(sortBy);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    private void updateEmptyState() {
        if (adapter == null || emptyState == null || rvLeaderboard == null) return;

        if (adapter.getItemCount() == 0) {
            emptyState.setVisibility(View.VISIBLE);
            rvLeaderboard.setVisibility(View.GONE);
        } else {
            emptyState.setVisibility(View.GONE);
            rvLeaderboard.setVisibility(View.VISIBLE);
        }
    }

    private void loadPlayerData() {
        database = FirebaseDatabase.getInstance();
        reference = database.getReference().child("Players");
        reference.keepSynced(true);

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<Player> players = new ArrayList<>();
                for (DataSnapshot snap : snapshot.getChildren()) {
                    Player player = snap.getValue(Player.class);
                    if (player != null) {
                        player.setPlayerId(snap.getKey());
                        players.add(player);
                    }
                }

                allPlayers = players;
                if (adapter != null) {
                    adapter.setPlayers(players);
                    adapter.sortBy("Win Rate");
                }
                updateStats();
                updateEmptyState();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(LeaderboardActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateStats() {
        if (allPlayers == null || tvTotalPlayers == null) return;

        tvTotalPlayers.setText(String.valueOf(allPlayers.size()));

        double avgWinRateSum = 0.0;
        int topWins = 0;

        for (Player player : allPlayers) {
            avgWinRateSum += player.getWinRate();
            if (player.getWinCount() > topWins) {
                topWins = player.getWinCount();
            }
        }

        if (!allPlayers.isEmpty()) {
            double avgPct = (avgWinRateSum / allPlayers.size()) * 100.0;
            tvAvgWinRate.setText(String.format(Locale.getDefault(), "%.1f%%", avgPct));
        } else {
            tvAvgWinRate.setText("0%");
        }

        tvTopScore.setText(String.valueOf(topWins));
    }
}
