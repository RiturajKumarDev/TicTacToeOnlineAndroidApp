package com.rituraj.tictactoeonlineapp.ads;

import android.app.Activity;
import android.content.Context;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;

public class MyInterstitialAds {
    private InterstitialAd mInterstitialAd;
    private AdRequest adRequest;
    private Context context;
    private Activity activity;
    private String adUintId;

    public MyInterstitialAds(Context context, Activity activity, String adUintId) {
        this.context = context;
        this.activity = activity;
        this.adUintId = adUintId;
        interstitialAd();
    }

    private void interstitialAd() {
        adRequest = new AdRequest.Builder().build();
        InterstitialAd.load(context, adUintId, adRequest, new InterstitialAdLoadCallback() {
            @Override
            public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                super.onAdFailedToLoad(loadAdError);
                mInterstitialAd = null;
                Toast.makeText(context, "Ad Failed To Load", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onAdLoaded(@NonNull InterstitialAd interstitialAd) {
                super.onAdLoaded(interstitialAd);
                mInterstitialAd = interstitialAd;
                interstitialAd.setFullScreenContentCallback(new FullScreenContentCallback() {
                    @Override
                    public void onAdClicked() {
                        super.onAdClicked();
                    }

                    @Override
                    public void onAdDismissedFullScreenContent() {
                        super.onAdDismissedFullScreenContent();
                        // Ad Dismiss By user
                        interstitialAd();
                    }

                    @Override
                    public void onAdFailedToShowFullScreenContent(@NonNull AdError adError) {
                        super.onAdFailedToShowFullScreenContent(adError);
                    }

                    @Override
                    public void onAdImpression() {
                        super.onAdImpression();
                    }

                    @Override
                    public void onAdShowedFullScreenContent() {
                        super.onAdShowedFullScreenContent();
                        mInterstitialAd = null;
                    }
                });
            }
        });
    }

    public void showInterstitialAd() {
        if (mInterstitialAd != null) {
            mInterstitialAd.show(activity);
        } else {
            interstitialAd();
        }
    }
}
