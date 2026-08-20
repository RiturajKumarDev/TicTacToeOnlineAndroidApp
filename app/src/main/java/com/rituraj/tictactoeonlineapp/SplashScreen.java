package com.rituraj.tictactoeonlineapp;

import android.app.Application;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.rituraj.tictactoeonlineapp.ads.MyAppOpenAds;
import com.rituraj.tictactoeonlineapp.ui.auth.LoginPage;

public class SplashScreen extends AppCompatActivity {

    private final Handler handler = new Handler(Looper.getMainLooper());
    private boolean isNavigationDone = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
            EdgeToEdge.enable(this);
        } catch (Exception e) {
            e.printStackTrace();
        }

        setContentView(R.layout.activity_splash_screen);

        View mainView = findViewById(R.id.main);
        if (mainView != null) {
            ViewCompat.setOnApplyWindowInsetsListener(mainView, (v, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
                return insets;
            });
        }

        startMain();
    }

    private void startMain() {
        try {
            Application application = getApplication();

            if (application instanceof MyAppOpenAds) {
                ((MyAppOpenAds) application).showAdIfAvailable(
                        SplashScreen.this,
                        () -> handler.postDelayed(this::startLoginPage, 2000)
                );
            } else {
                handler.postDelayed(this::startLoginPage, 1000);
            }
        } catch (Exception e) {
            e.printStackTrace();
            startLoginPage();
        }
    }

    private void startLoginPage() {
        if (isFinishing() || isDestroyed() || isNavigationDone) return;
        isNavigationDone = true;

        try {
            Intent intent = new Intent(SplashScreen.this, LoginPage.class);
            startActivity(intent);
            finish();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onDestroy() {
        handler.removeCallbacksAndMessages(null);
        super.onDestroy();
    }
}