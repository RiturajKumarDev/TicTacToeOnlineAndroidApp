package com.rituraj.tictactoeonlineapp.ui.games;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.games.PlayGames;
import com.rituraj.tictactoeonlineapp.R;
import com.rituraj.tictactoeonlineapp.ads.MyInterstitialAds;
import com.rituraj.tictactoeonlineapp.appPrefs.SaveInDevice;
import com.rituraj.tictactoeonlineapp.databinding.ActivityClassicModeBinding;

import java.util.Arrays;

public class ClassicModeActivity extends AppCompatActivity {
    private ActivityClassicModeBinding binding;
    int activePlayer = 0;
    Boolean gameActive = true;
    private CountDownTimer countDownTimer;
    int[] grid = {2, 2, 2, 2, 2, 2, 2, 2, 2};
    int[][] winPositions = {{0, 1, 2}, {3, 4, 5}, {6, 7, 8}, {0, 3, 6}, {1, 4, 7}, {2, 5, 8}, {0, 4, 8}, {2, 4, 6},};
    private MyInterstitialAds myInterstitialAds;
    private AdView adView1, adView2;
    private long score = 0, noGame = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityClassicModeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        fun();
        interstitialAd();
        timingSetting();
    }

    private void timingSetting() {
        int totalMillis = 30 * 1000;
        countDownTimer = new CountDownTimer(totalMillis, 100) {
            @Override
            public void onTick(long millisUntilFinished) {
                int progress = (int) ((millisUntilFinished * 100) / 30000);
                if (activePlayer == 1) binding.offlinePlayer1Timeline.setProgress(progress);
                else binding.offlinePlayer2Timeline.setProgress(progress);
            }

            @Override
            public void onFinish() {
                activePlayer = activePlayer == 1 ? 0 : 1;
                if (countDownTimer != null) countDownTimer.cancel();
                timingSetting();
            }
        }.start();
    }

    public void fun() {
        score = SaveInDevice.getData(ClassicModeActivity.this, "OFFLINE_SCORE");
        noGame = SaveInDevice.getData(ClassicModeActivity.this, "NO_GAME");
        ImageView playerTap0 = findViewById(R.id.playerTap0);
        ImageView playerTap1 = findViewById(R.id.playerTap1);
        ImageView playerTap2 = findViewById(R.id.playerTap2);
        ImageView playerTap3 = findViewById(R.id.playerTap3);
        ImageView playerTap4 = findViewById(R.id.playerTap4);
        ImageView playerTap5 = findViewById(R.id.playerTap5);
        ImageView playerTap6 = findViewById(R.id.playerTap6);
        ImageView playerTap7 = findViewById(R.id.playerTap7);
        ImageView playerTap8 = findViewById(R.id.playerTap8);

        View.OnClickListener listener = this::playerTap;

        playerTap0.setOnClickListener(listener);
        playerTap1.setOnClickListener(listener);
        playerTap2.setOnClickListener(listener);
        playerTap3.setOnClickListener(listener);
        playerTap4.setOnClickListener(listener);
        playerTap5.setOnClickListener(listener);
        playerTap6.setOnClickListener(listener);
        playerTap7.setOnClickListener(listener);
        playerTap8.setOnClickListener(listener);
        googleAD();
    }

    private void googleAD() {
        adView1 = findViewById(R.id.adView_game1);
        if (adView1 != null) {
            MobileAds.initialize(this);
            AdRequest adRequest = new AdRequest.Builder().build();
            adView1.loadAd(adRequest);
        }

        adView2 = findViewById(R.id.adView_game2);
        if (adView2 != null) {
            MobileAds.initialize(this);
            AdRequest adRequest = new AdRequest.Builder().build();
            adView2.loadAd(adRequest);
        }
    }

    private void startNewGame() {
        gameActive = true;
        activePlayer = 0;
        for (int i = 0; i < 9; i++)
            grid[i] = 2;

        ((ImageView) findViewById(R.id.playerTap0)).setImageResource(0);
        ((ImageView) findViewById(R.id.playerTap1)).setImageResource(0);
        ((ImageView) findViewById(R.id.playerTap2)).setImageResource(0);
        ((ImageView) findViewById(R.id.playerTap3)).setImageResource(0);
        ((ImageView) findViewById(R.id.playerTap4)).setImageResource(0);
        ((ImageView) findViewById(R.id.playerTap5)).setImageResource(0);
        ((ImageView) findViewById(R.id.playerTap6)).setImageResource(0);
        ((ImageView) findViewById(R.id.playerTap7)).setImageResource(0);
        ((ImageView) findViewById(R.id.playerTap8)).setImageResource(0);
        binding.offlinePlayer1Timeline.setProgress(0);
        binding.offlinePlayer2Timeline.setProgress(0);
        countDownTimer.cancel();
        timingSetting();
        if (myInterstitialAds != null) myInterstitialAds.showInterstitialAd();
    }

    private void interstitialAd() {
        String adUintId = getResources().getString(R.string.classic_end_game_interstitial_unit_id);
        myInterstitialAds = new MyInterstitialAds(ClassicModeActivity.this, ClassicModeActivity.this, adUintId);
    }

    private void showGameOverDialog(String resultMessage) {
        SaveInDevice.saveData(ClassicModeActivity.this, "NO_GAME", ++noGame);
        AlertDialog.Builder builder = new AlertDialog.Builder(ClassicModeActivity.this);
        builder.setTitle("Game Over");
        builder.setMessage(resultMessage + "\n\n" + "Do you want to start a new game?");
        builder.setPositiveButton("New Game", (dialog, which) -> {
            Toast.makeText(ClassicModeActivity.this, "New Game", Toast.LENGTH_SHORT).show();
            startNewGame();
        });
        builder.setNegativeButton("Exit", (dialog, which) -> {
            dialog.dismiss();
            finish();
        });
        builder.setCancelable(false);
        builder.show();
    }

    public void winCondition() {
        for (int[] winPosition : winPositions) {
            if (grid[winPosition[0]] == grid[winPosition[1]] && grid[winPosition[1]] == grid[winPosition[2]] && grid[winPosition[2]] != 2) {
                if (grid[winPosition[0]] == 0) {
                    showGameOverDialog("Player 1 (O) has won🏆\nPlayer 2 (X) has loss!");
                    submitLeaderboardScore(++score);
                    SaveInDevice.saveData(ClassicModeActivity.this, "OFFLINE_SCORE", score);
                } else showGameOverDialog("Player 2 (X) has won🏆\nPlayer 1 (O) has loss!");
                gameActive = false;
                return;
            }
        }
        String[] gridStr = Arrays.stream(grid).mapToObj(String::valueOf).toArray(String[]::new);
        String currGrid = String.join(",", gridStr);
        if (!currGrid.contains("2")) showGameOverDialog("It's a Tie! 🤝 Excellent Performance! 🎉");
        countDownTimer.cancel();
        timingSetting();
    }

    public void playerTap(View v) {
        if (gameActive) {
            ImageView img = (ImageView) v;
            int tappedImg = Integer.parseInt(img.getTag().toString());
            if (grid[tappedImg] == 2) {
                grid[tappedImg] = activePlayer;
                img.setTranslationY(-1000f);
                if (activePlayer == 1) {
                    img.setImageResource(R.drawable.x);
                    activePlayer = 0;
                    binding.offlinePlayer1Timeline.setProgress(0);
                } else {
                    img.setImageResource(R.drawable.o);
                    activePlayer = 1;
                    binding.offlinePlayer2Timeline.setProgress(0);
                }
                img.animate().translationYBy(1000f).setDuration(300);
                winCondition();
            }
        }
    }

    private void submitLeaderboardScore(long score) {
        PlayGames.getGamesSignInClient(this).isAuthenticated().addOnCompleteListener(task -> {
            boolean authenticated = task.isSuccessful() && task.getResult() != null && task.getResult().isAuthenticated();
            if (authenticated) {
                PlayGames.getLeaderboardsClient(this).submitScore("CgkIyqmG5tAVEAIQAg", score);
                Toast.makeText(this, "Score submitted: " + score, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Play Games not logged in", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onDestroy() {
        if (countDownTimer != null) countDownTimer.cancel();
//        if (myInterstitialAds != null) myInterstitialAds.showInterstitialAd();
        super.onDestroy();
    }
}
