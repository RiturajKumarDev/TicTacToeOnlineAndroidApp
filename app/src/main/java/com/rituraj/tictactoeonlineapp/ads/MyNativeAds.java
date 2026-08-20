package com.rituraj.tictactoeonlineapp.ads;

import android.content.Context;
import android.view.View;

import androidx.annotation.NonNull;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdLoader;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.nativead.NativeAdOptions;

public class MyNativeAds {
    private AdLoader adLoader;
    private Context context;
    private String nativeAdUnitID;
    private TemplateView templateView;

    public MyNativeAds(Context context, String nativeAdUnitID, TemplateView templateView) {
        this.context = context;
        this.nativeAdUnitID = nativeAdUnitID;
        this.templateView = templateView;
        loadInitializeAds();
    }

    private void loadInitializeAds() {
        templateView.setVisibility(View.GONE);

        new Thread(() -> {
            MobileAds.initialize(context, initializationStatus -> {
                loadingNativeAds();
            });
        }).start();
    }

    private void loadingNativeAds() {

        // The native ad loaded successfully. You can show the ad.
        adLoader = new AdLoader.Builder(context, nativeAdUnitID)
                .forNativeAd(
                        nativeAd -> {
//                            if (isDetached()) {
//                                nativeAd.destroy();
//                                return;
//                            }
                            if (!adLoader.isLoading()) {
                                NativeTemplateStyle styles = new NativeTemplateStyle.Builder().build();
                                templateView.setStyles(styles);
                                templateView.setVisibility(View.VISIBLE);
                                templateView.setNativeAd(nativeAd);
                            }
                        })
                .withAdListener(
                        new AdListener() {
                            @Override
                            // The native ad load failed. Check the adError message for failure
                            // reasons.
                            public void onAdFailedToLoad(@NonNull LoadAdError adError) {
                                if (templateView.getVisibility() == View.VISIBLE)
                                    templateView.setVisibility(View.GONE);
                                loadingNativeAds();
                            }
                        })
                .withNativeAdOptions(new NativeAdOptions.Builder().build())
                .build();

        // Load three native ads.
        adLoader.loadAd(new AdRequest.Builder().build());
    }
}
