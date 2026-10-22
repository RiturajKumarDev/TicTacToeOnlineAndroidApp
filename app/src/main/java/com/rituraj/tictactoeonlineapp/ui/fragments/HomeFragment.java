package com.rituraj.tictactoeonlineapp.ui.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.rituraj.tictactoeonlineapp.R;
import com.rituraj.tictactoeonlineapp.databinding.FragmentHomeBinding;
import com.rituraj.tictactoeonlineapp.models.Player;
import com.rituraj.tictactoeonlineapp.models.User;
import com.rituraj.tictactoeonlineapp.ui.auth.LoginPage;
import com.rituraj.tictactoeonlineapp.ui.games.PlayHardAiActivity;
import com.rituraj.tictactoeonlineapp.ui.games.ClassicModeActivity;
import com.rituraj.tictactoeonlineapp.ui.games.PlayEasyAiActivity;
import com.rituraj.tictactoeonlineapp.ui.activities.SelectOnlineUser;

import java.util.Locale;

public class HomeFragment extends Fragment {
    private FragmentHomeBinding binding;
    private FirebaseAuth auth;
    private FirebaseUser user;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        fun();
        return binding.getRoot();
    }

    public void fun() {
        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        if (user == null) {
            if (getActivity() != null) {
                startActivity(new Intent(getActivity(), LoginPage.class));
                getActivity().finish();
            }
            return;
        }
        if (!isAdded()) return;
        setUpUI();
        getPlayerData();
    }

    private void setUpUI() {
        if (user != null) {
            String name = user.getDisplayName();
            if (name == null || name.trim().isEmpty()) {
                if (user.getEmail() != null && user.getEmail().contains("@")) {
                    name = user.getEmail().split("@")[0];
                } else {
                    name = "Player";
                }
            }
            binding.welcomeTitle.setText("Welcome, " + name + "!");

            // 1. Load profile picture from Firebase Auth Photo URL first
            String photoUrl = user.getPhotoUrl() != null ? user.getPhotoUrl().toString() : null;
            loadProfileImage(photoUrl);

            // 2. Also check Firebase Realtime Database for profile picture URL
            FirebaseDatabase.getInstance().getReference("Users")
                    .child(user.getUid())
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (isAdded() && binding != null && snapshot.exists()) {
                                User userModel = snapshot.getValue(User.class);
                                if (userModel != null) {
                                    if (userModel.getUserName() != null && !userModel.getUserName().trim().isEmpty()) {
                                        binding.welcomeTitle.setText("Welcome, " + userModel.getUserName() + "!");
                                    }
                                    if (userModel.getProfileUrl() != null && !userModel.getProfileUrl().isEmpty()) {
                                        loadProfileImage(userModel.getProfileUrl());
                                    }
                                }
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                        }
                    });
        }

        binding.offlineBtn.setOnClickListener(v -> {
            if (getContext() != null) {
                startActivity(new Intent(getContext(), ClassicModeActivity.class));
            }
        });

        binding.playBotBtn.setOnClickListener(v -> {
            if (getContext() != null) {
                showGameOverDialog();
            }
        });

        binding.onlineBtn.setOnClickListener(v -> {
            if (getContext() != null) {
                startActivity(new Intent(getContext(), SelectOnlineUser.class));
            }
        });

        binding.leaderboardLink.setOnClickListener(v -> {
            if (getActivity() != null) {
                BottomNavigationView navView = getActivity().findViewById(R.id.nav_view);
                if (navView != null)
                    navView.setSelectedItemId(R.id.navigation_multiplayer);
            }
        });

        binding.homeInviteLink.setOnClickListener(v -> {
            if (getActivity() != null) {
                BottomNavigationView navView = getActivity().findViewById(R.id.nav_view);
                if (navView != null)
                    navView.setSelectedItemId(R.id.navigation_invites);
            }
        });

        binding.homeMultiplayerBtn.setOnClickListener(v -> {
            if (getActivity() != null) {
                BottomNavigationView navView = getActivity().findViewById(R.id.nav_view);
                if (navView != null)
                    navView.setSelectedItemId(R.id.navigation_multiplayer);
            }
        });

        binding.userAvatar.setOnClickListener(v -> {
            if (getActivity() != null) {
                BottomNavigationView navView = getActivity().findViewById(R.id.nav_view);
                if (navView != null)
                    navView.setSelectedItemId(R.id.navigation_profile);
            }
        });
    }

    private void loadProfileImage(String url) {
        if (!isAdded() || binding == null || getContext() == null) return;

        if (url != null && !url.trim().isEmpty()) {
            Glide.with(this)
                    .load(url)
                    .placeholder(R.drawable.user)
                    .error(R.drawable.user)
                    .circleCrop()
                    .into(binding.userAvatar);
        } else {
            binding.userAvatar.setImageResource(R.drawable.user);
        }
    }

    private void showGameOverDialog() {
        if (getContext() == null || !isAdded()) return;

        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_select_ai_difficulty, null);
        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setView(dialogView)
                .setCancelable(true)
                .create();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        View btnEasy = dialogView.findViewById(R.id.btnEasyMode);
        View btnHard = dialogView.findViewById(R.id.btnHardMode);
        View btnCancel = dialogView.findViewById(R.id.btnCancelDialog);

        if (btnEasy != null) {
            btnEasy.setOnClickListener(v -> {
                dialog.dismiss();
                startActivity(new Intent(getContext(), PlayEasyAiActivity.class));
            });
        }

        if (btnHard != null) {
            btnHard.setOnClickListener(v -> {
                dialog.dismiss();
                startActivity(new Intent(getContext(), PlayHardAiActivity.class));
            });
        }

        if (btnCancel != null) {
            btnCancel.setOnClickListener(v -> dialog.dismiss());
        }

        dialog.show();
    }

    private void getPlayerData() {
        if (user == null || binding == null || !isAdded()) return;

        DatabaseReference playerRef = FirebaseDatabase.getInstance().getReference("Players").child(user.getUid());
        playerRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!isAdded() || binding == null) return;
                Player player = snapshot.getValue(Player.class);
                if (player != null) {
                    long totalGame = player.getGameCount();
                    long totalWin = player.getWinCount();

                    binding.homeTotalGame.setText(String.valueOf(totalGame));
                    binding.homeTotalWin.setText(String.valueOf(totalWin));

                    float winRate = 0.0f;
                    if (totalGame != 0) {
                        winRate = (totalWin * 100.0f) / totalGame;
                    }
                    binding.homeWinRate.setText(String.format(Locale.getDefault(), "%.1f%%", winRate));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        if (isAdded()) {
            getPlayerData();
        }
    }

    @Override
    public void onDestroyView() {
        binding = null;
        super.onDestroyView();
    }
}
