package com.rituraj.tictactoeonlineapp.ui.multiplyer;

import androidx.lifecycle.ViewModelProvider;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.games.PlayGames;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.rituraj.tictactoeonlineapp.R;
import com.rituraj.tictactoeonlineapp.ui.games.ClassicModeActivity;
import com.rituraj.tictactoeonlineapp.ui.leaderboard.LeaderboardActivity;
import com.rituraj.tictactoeonlineapp.ui.pages.SelectOnlineUser;

public class MultiPlayerFragment extends Fragment {
    private View view;
    private MultiPlayerViewModel mViewModel;

    public static MultiPlayerFragment newInstance() {
        return new MultiPlayerFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_multi_player, container, false);
        fun();
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = new ViewModelProvider(this).get(MultiPlayerViewModel.class);
        // TODO: Use the ViewModel
    }

    private void fun() {
        Button multiClassicModeBtn = view.findViewById(R.id.multiClassicModeBtn);
        Button multiOnlineModeBtn = view.findViewById(R.id.multiOnlineModeBtn);
        TextView multiLeaderboardAiLink = view.findViewById(R.id.multiLeaderboardAiLink);
        TextView multiLeaderboardOfflineLink = view.findViewById(R.id.multiLeaderboardOfflineLink);
        TextView multiLeaderboardOnlineLink = view.findViewById(R.id.multiLeaderboardOnlineLink);
        TextView multiLeaderboardCoinsLink = view.findViewById(R.id.multiLeaderboardCoinsLink);

        multiClassicModeBtn.setOnClickListener((v -> {
            if (getContext() != null) {
                startActivity(new Intent(getContext(), ClassicModeActivity.class));
            }
        }));
        multiOnlineModeBtn.setOnClickListener((v -> {
            if (getContext() != null) {
                startActivity(new Intent(getContext(), SelectOnlineUser.class));
            }
        }));
        multiLeaderboardAiLink.setOnClickListener((v) -> {
            PlayGames.getGamesSignInClient(requireActivity())
                    .isAuthenticated()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful() && task.getResult().isAuthenticated()) {
                            PlayGames.getLeaderboardsClient(requireActivity())
                                    .getLeaderboardIntent("CgkIyqmG5tAVEAIQAQ")
                                    .addOnSuccessListener(intent -> {
                                        startActivityForResult(intent, 9001);
                                    }).addOnFailureListener(e -> {
                                        Toast.makeText(requireContext(), "Failed to open leaderboard", Toast.LENGTH_SHORT).show();
                                    });
                        } else {
                            Toast.makeText(requireContext(), "Please login to Play Games", Toast.LENGTH_SHORT).show();
                        }
                    });
        });
        multiLeaderboardOfflineLink.setOnClickListener((v) -> {
            PlayGames.getGamesSignInClient(requireActivity())
                    .isAuthenticated()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful() && task.getResult().isAuthenticated()) {
                            PlayGames.getLeaderboardsClient(requireActivity())
                                    .getLeaderboardIntent("CgkIyqmG5tAVEAIQAg")
                                    .addOnSuccessListener(intent -> {
                                        startActivityForResult(intent, 9001);
                                    }).addOnFailureListener(e -> {
                                        Toast.makeText(requireContext(), "Failed to open leaderboard", Toast.LENGTH_SHORT).show();
                                    });
                        } else {
                            Toast.makeText(requireContext(), "Please login to Play Games", Toast.LENGTH_SHORT).show();
                        }
                    });
        });
        multiLeaderboardOnlineLink.setOnClickListener((v) -> {
            PlayGames.getGamesSignInClient(requireActivity())
                    .isAuthenticated()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful() && task.getResult().isAuthenticated()) {
                            PlayGames.getLeaderboardsClient(requireActivity())
                                    .getLeaderboardIntent("CgkIyqmG5tAVEAIQAw")
                                    .addOnSuccessListener(intent -> {
                                        startActivityForResult(intent, 9001);
                                    }).addOnFailureListener(e -> {
                                        Toast.makeText(requireContext(), "Failed to open leaderboard", Toast.LENGTH_SHORT).show();
                                    });
                        } else {
                            Toast.makeText(requireContext(), "Please login to Play Games", Toast.LENGTH_SHORT).show();
                        }
                    });
        });
        multiLeaderboardCoinsLink.setOnClickListener((v) -> {
            PlayGames.getGamesSignInClient(requireActivity())
                    .isAuthenticated()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful() && task.getResult().isAuthenticated()) {
                            PlayGames.getLeaderboardsClient(requireActivity())
                                    .getLeaderboardIntent("CgkIyqmG5tAVEAIQBA")
                                    .addOnSuccessListener(intent -> {
                                        startActivityForResult(intent, 9001);
                                    }).addOnFailureListener(e -> {
                                        Toast.makeText(requireContext(), "Failed to open leaderboard", Toast.LENGTH_SHORT).show();
                                    });
                        } else {
                            Toast.makeText(requireContext(), "Please login to Play Games", Toast.LENGTH_SHORT).show();
                        }
                    });
        });
    }
}