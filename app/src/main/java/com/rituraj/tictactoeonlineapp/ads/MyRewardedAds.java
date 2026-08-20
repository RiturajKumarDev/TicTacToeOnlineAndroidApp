package com.rituraj.tictactoeonlineapp.ads;

import android.app.Activity;
import android.content.Context;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.android.gms.ads.rewarded.RewardedAd;
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback;
import com.google.firebase.Firebase;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.rituraj.tictactoeonlineapp.models.Player;

public class MyRewardedAds {

    public interface RewardedAdListener {
        void onRewardedAdLoaded(RewardedAd ad);
    }

    private Context context;
    private Activity activity;
    private String rewardedAdUnitID;
    private RewardedAdListener listener;
    private FirebaseDatabase database;
    private Player player;

    public MyRewardedAds(Activity activity, Context context, String rewardedAdUnitID, FirebaseDatabase database, Player player, RewardedAdListener listener) {
        this.activity = activity;
        this.context = context;
        this.rewardedAdUnitID = rewardedAdUnitID;
        this.database = database;
        this.player = player;
        this.listener = listener;

        MobileAds.initialize(activity, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(@NonNull InitializationStatus initializationStatus) {
                loadRewardedAd();
            }
        });
    }

    private void loadRewardedAd() {
        AdRequest adRequest = new AdRequest.Builder().build();
        RewardedAd.load(context, rewardedAdUnitID, adRequest, new RewardedAdLoadCallback() {
            @Override
            public void onAdLoaded(@NonNull RewardedAd rewardedAd) {
                listener.onRewardedAdLoaded(rewardedAd);
                rewardedAd.setFullScreenContentCallback(new FullScreenContentCallback() {
                    @Override
                    public void onAdDismissedFullScreenContent() {
                        loadRewardedAd(); // Reload for next time
                    }

                    @Override
                    public void onAdFailedToShowFullScreenContent(@NonNull com.google.android.gms.ads.AdError adError) {
                        Toast.makeText(context, "Ad failed to show: " + adError.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                Toast.makeText(context, "Ad failed to load: " + loadAdError.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    public void showRewardedAd(RewardedAd ad) {
        if (ad != null) {
            ad.show(activity, rewardItem -> {
                int amount = rewardItem.getAmount();
                String type = rewardItem.getType();
                int totalCoins = Integer.parseInt(player.getCoins()) + 20;
                player.setCoins(String.valueOf(totalCoins));
                database.getReference("Players").child(player.getPlayerId()).child("coins").setValue(player.getCoins());
            });
        } else {
            loadRewardedAd();
        }
    }
}
