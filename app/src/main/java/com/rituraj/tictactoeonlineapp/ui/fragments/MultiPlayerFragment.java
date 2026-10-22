package com.rituraj.tictactoeonlineapp.ui.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.rituraj.tictactoeonlineapp.databinding.FragmentMultiPlayerBinding;
import com.rituraj.tictactoeonlineapp.ui.games.ClassicModeActivity;
import com.rituraj.tictactoeonlineapp.ui.activities.LeaderboardActivity;
import com.rituraj.tictactoeonlineapp.ui.activities.SelectOnlineUser;

public class MultiPlayerFragment extends Fragment {

    private FragmentMultiPlayerBinding binding;

    public static MultiPlayerFragment newInstance() {
        return new MultiPlayerFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentMultiPlayerBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setUpListeners();
    }

    private void setUpListeners() {
        binding.multiClassicModeBtn.setOnClickListener(v -> {
            if (getContext() != null) {
                startActivity(new Intent(getContext(), ClassicModeActivity.class));
            }
        });

        binding.multiOnlineModeBtn.setOnClickListener(v -> {
            if (getContext() != null) {
                startActivity(new Intent(getContext(), SelectOnlineUser.class));
            }
        });

        binding.multiLeaderboardAiLink.setOnClickListener(v -> openLeaderboard());
        binding.multiLeaderboardOfflineLink.setOnClickListener(v -> openLeaderboard());
        binding.multiLeaderboardOnlineLink.setOnClickListener(v -> openLeaderboard());
    }

    private void openLeaderboard() {
        if (getContext() != null) {
            startActivity(new Intent(getContext(), LeaderboardActivity.class));
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
