package com.rituraj.tictactoeonlineapp.ui.home;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.google.android.gms.ads.rewarded.RewardedAd;
import com.google.android.gms.games.PlayGames;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.rituraj.tictactoeonlineapp.MainActivity;
import com.rituraj.tictactoeonlineapp.R;
import com.rituraj.tictactoeonlineapp.ads.MyRewardedAds;
import com.rituraj.tictactoeonlineapp.appPrefs.SaveInDevice;
import com.rituraj.tictactoeonlineapp.databinding.FragmentHomeBinding;
import com.rituraj.tictactoeonlineapp.models.Player;
import com.rituraj.tictactoeonlineapp.ui.auth.LoginPage;
import com.rituraj.tictactoeonlineapp.ui.games.AdvanceAiActivity;
import com.rituraj.tictactoeonlineapp.ui.games.ClassicModeActivity;
import com.rituraj.tictactoeonlineapp.ui.games.PlayWithAiActivity;
import com.rituraj.tictactoeonlineapp.ui.leaderboard.LeaderboardActivity;
import com.rituraj.tictactoeonlineapp.ui.pages.SelectOnlineUser;

import java.util.Locale;

public class HomeFragment extends Fragment {
    private View view;
    private FragmentHomeBinding binding;
    private RewardedAd mRewardedAd;
    private FirebaseDatabase database;
    private FirebaseAuth auth;
    private FirebaseUser user;
    private long totalScore = 0, totalGame = 0;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        HomeViewModel homeViewModel = new ViewModelProvider(this).get(HomeViewModel.class);

        binding = FragmentHomeBinding.inflate(inflater, container, false);
        view = binding.getRoot();

        fun();
        return view;
    }

    public void fun() {
        database = FirebaseDatabase.getInstance();
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
        myRewardedAdSetting();
    }

    private void setUpUI() {
        binding.offlineBtn.setOnClickListener((v -> {
            if (getContext() != null) {
                startActivity(new Intent(getContext(), ClassicModeActivity.class));
            }
        }));
        binding.playBotBtn.setOnClickListener((v -> {
            if (getContext() != null) {
                showGameOverDialog();
            }
        }));
        binding.onlineBtn.setOnClickListener((v -> {
            if (getContext() != null) {
                startActivity(new Intent(getContext(), SelectOnlineUser.class));
            }
        }));
        binding.leaderboardLink.setOnClickListener((v) -> {
            if (getActivity() != null) {
                BottomNavigationView navView = getActivity().findViewById(R.id.nav_view);
                if (navView != null)
                    navView.setSelectedItemId(R.id.navigation_multiplayer);
            }
        });
        binding.homeInviteLink.setOnClickListener((v -> {
            if (getActivity() != null) {
                BottomNavigationView navView = getActivity().findViewById(R.id.nav_view);
                if (navView != null)
                    navView.setSelectedItemId(R.id.navigation_invites);
            }
        }));
        binding.homeMultiplayerBtn.setOnClickListener((v) -> {
            if (getActivity() != null) {
                BottomNavigationView navView = getActivity().findViewById(R.id.nav_view);
                if (navView != null)
                    navView.setSelectedItemId(R.id.navigation_multiplayer);
            }
        });
    }

    private void showGameOverDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Start Game");
        builder.setMessage("Do you want to start a game with AI?");
        builder.setPositiveButton("Easy", (dialog, which) -> {
            dialog.dismiss();
            startActivity(new Intent(getContext(), PlayWithAiActivity.class));
        });
        builder.setNegativeButton("Hard", (dialog, which) -> {
            dialog.dismiss();
            startActivity(new Intent(getContext(), AdvanceAiActivity.class));
        });
        builder.show();
    }

    private void getPlayerData() {
        totalScore = SaveInDevice.getData(requireActivity(), "AI_SCORE")
                + SaveInDevice.getData(requireActivity(), "OFFLINE_SCORE")
                + SaveInDevice.getData(requireActivity(), "ONLINE_SCORE");
        totalGame = SaveInDevice.getData(requireActivity(), "NO_GAME");
        binding.homeTotalGame.setText(String.valueOf(totalGame));
        binding.homeTotalWin.setText(String.valueOf(totalScore));
        float winRate = 0.0f;
        if (totalGame != 0)
            winRate = (totalScore * 100.0f) / totalGame;
        binding.homeWinRate.setText(String.format("%.2f%%", winRate));
    }

    private void myRewardedAdSetting() {
        if (!isAdded() || getActivity() == null || getContext() == null) {
            return;
        }
        String rewardedAdUnitID = getString(R.string.home_Rewarded_AdUnit_ID);
        MyRewardedAds myRewardedAds = new MyRewardedAds(getActivity(), getContext(), rewardedAdUnitID, database, MainActivity.player, ad -> {
            mRewardedAd = ad;
        });
        if (binding != null) {
            binding.homeRewardedAds.setOnClickListener((v) -> {
                if (mRewardedAd != null) myRewardedAds.showRewardedAd(mRewardedAd);
            });
        }
    }

    @Override
    public void onResume() {
        getPlayerData();
        super.onResume();
    }

    @Override
    public void onDestroyView() {
        binding = null;
        super.onDestroyView();
    }
}
