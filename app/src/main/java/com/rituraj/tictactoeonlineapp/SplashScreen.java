package com.rituraj.tictactoeonlineapp;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
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

        startAnimations();
        startDynamicLoadingText();
        startMain();
    }

    private void startAnimations() {
        try {
            View logoCard = findViewById(R.id.splashLogoCard);
            View glowRing = findViewById(R.id.splashGlowRing);
            View titleContainer = findViewById(R.id.titleContainer);
            View progressContainer = findViewById(R.id.progressContainer);
            View circle1 = findViewById(R.id.splashCircle1);
            View circle2 = findViewById(R.id.splashCircle2);
            View floatingX = findViewById(R.id.floatingX);
            View floatingO = findViewById(R.id.floatingO);

            Animation popAnim = AnimationUtils.loadAnimation(this, R.anim.splash_logo_pop);
            Animation ringAnim = AnimationUtils.loadAnimation(this, R.anim.splash_ring_expand);
            Animation slideAnim = AnimationUtils.loadAnimation(this, R.anim.splash_text_slide);
            Animation rotateAnim = AnimationUtils.loadAnimation(this, R.anim.splash_rotate);
            Animation floatLeft = AnimationUtils.loadAnimation(this, R.anim.float_sway_left);
            Animation floatRight = AnimationUtils.loadAnimation(this, R.anim.float_sway_right);

            if (circle1 != null) {
                circle1.startAnimation(rotateAnim);
            }

            if (circle2 != null) {
                circle2.startAnimation(rotateAnim);
            }

            if (floatingX != null) {
                floatingX.startAnimation(floatLeft);
            }

            if (floatingO != null) {
                floatingO.startAnimation(floatRight);
            }

            if (glowRing != null) {
                glowRing.startAnimation(ringAnim);
            }

            if (logoCard != null) {
                logoCard.startAnimation(popAnim);
            }

            if (titleContainer != null) {
                titleContainer.startAnimation(slideAnim);
            }

            if (progressContainer != null) {
                progressContainer.startAnimation(slideAnim);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void startDynamicLoadingText() {
        TextView loadingText = findViewById(R.id.loadingText);
        if (loadingText == null) return;

        handler.postDelayed(() -> {
            if (!isNavigationDone && !isFinishing()) {
                loadingText.setText("Connecting to game servers...");
            }
        }, 900);

        handler.postDelayed(() -> {
            if (!isNavigationDone && !isFinishing()) {
                loadingText.setText("Ready to play!");
            }
        }, 1800);
    }

    private void startMain() {
        handler.postDelayed(this::startNextScreen, 2600);
    }

    private void startNextScreen() {
        if (isFinishing() || isDestroyed() || isNavigationDone) return;
        isNavigationDone = true;

        try {
            Intent intent;
            if (FirebaseAuth.getInstance().getCurrentUser() != null) {
                intent = new Intent(SplashScreen.this, MainActivity.class);
            } else {
                intent = new Intent(SplashScreen.this, LoginPage.class);
            }
            startActivity(intent);
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            finish();
        } catch (Exception e) {
            e.printStackTrace();
            Intent intent = new Intent(SplashScreen.this, LoginPage.class);
            startActivity(intent);
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            finish();
        }
    }

    @Override
    protected void onDestroy() {
        handler.removeCallbacksAndMessages(null);
        super.onDestroy();
    }
}
