package com.rituraj.tictactoeonlineapp.ui.games;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.rewarded.RewardedAd;
import com.google.android.gms.games.PlayGames;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.rituraj.tictactoeonlineapp.MainActivity;
import com.rituraj.tictactoeonlineapp.R;
import com.rituraj.tictactoeonlineapp.ads.MyInterstitialAds;
import com.rituraj.tictactoeonlineapp.ads.MyRewardedAds;
import com.rituraj.tictactoeonlineapp.appPrefs.SaveInDevice;
import com.rituraj.tictactoeonlineapp.databinding.ActivityPlayWithAiBinding;

import java.util.Arrays;

public class PlayWithAiActivity extends AppCompatActivity {
    private ActivityPlayWithAiBinding binding;
    private int activePlayer = 1;
    private Boolean gameActive = true, you = true;
    private CountDownTimer countDownTimer;
    int[] grid = {2, 2, 2, 2, 2, 2, 2, 2, 2};
    int[][] winPositions = {{0, 1, 2}, {3, 4, 5}, {6, 7, 8}, {0, 3, 6}, {1, 4, 7}, {2, 5, 8}, {0, 4, 8}, {2, 4, 6}};
    private FirebaseAuth auth;
    private MyInterstitialAds myInterstitialAds;
    private FirebaseDatabase database;
    private AdView adView1, adView2;
    private long score = 0, noGame = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityPlayWithAiBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        fun();
        timingSetting();
    }

    private void timingSetting() {
        int totalMillis = 30 * 1000;
        countDownTimer = new CountDownTimer(totalMillis, 100) {
            @Override
            public void onTick(long millisUntilFinished) {
                int progress = (int) ((millisUntilFinished * 100) / 30000);
                if (activePlayer == 1) binding.playWithAiPlayerTimeline.setProgress(progress);
                else binding.playWithAiTimeline.setProgress(progress);
            }

            @Override
            public void onFinish() {
                if (activePlayer == 1)
                    new Handler(Looper.getMainLooper()).postDelayed(PlayWithAiActivity.this::botPlay, 500);
                activePlayer = activePlayer == 1 ? 0 : 1;
                if (countDownTimer != null) countDownTimer.cancel();
                timingSetting();
            }
        }.start();
    }

    public void fun() {
        database = FirebaseDatabase.getInstance();
        auth = FirebaseAuth.getInstance();
        score = SaveInDevice.getData(PlayWithAiActivity.this, "AI_SCORE");
        noGame = SaveInDevice.getData(PlayWithAiActivity.this, "NO_GAME");
        Button aiCurrentUsername = findViewById(R.id.aiCurrentUsername);
        aiCurrentUsername.setText(auth.getCurrentUser().getDisplayName());

        ImageView playerTap0 = findViewById(R.id.botPlayerTap0);
        ImageView playerTap1 = findViewById(R.id.botPlayerTap1);
        ImageView playerTap2 = findViewById(R.id.botPlayerTap2);
        ImageView playerTap3 = findViewById(R.id.botPlayerTap3);
        ImageView playerTap4 = findViewById(R.id.botPlayerTap4);
        ImageView playerTap5 = findViewById(R.id.botPlayerTap5);
        ImageView playerTap6 = findViewById(R.id.botPlayerTap6);
        ImageView playerTap7 = findViewById(R.id.botPlayerTap7);
        ImageView playerTap8 = findViewById(R.id.botPlayerTap8);

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
        interstitialAd();
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

    private void interstitialAd() {
        String adUintId = getResources().getString(R.string.classic_end_game_interstitial_unit_id);
        myInterstitialAds = new MyInterstitialAds(PlayWithAiActivity.this, PlayWithAiActivity.this, adUintId);
    }

    private void startNewGame() {
        you = gameActive = true;
        activePlayer = 1;
        for (int i = 0; i < 9; i++)
            grid[i] = 2;

        ((ImageView) findViewById(R.id.botPlayerTap0)).setImageResource(0);
        ((ImageView) findViewById(R.id.botPlayerTap1)).setImageResource(0);
        ((ImageView) findViewById(R.id.botPlayerTap2)).setImageResource(0);
        ((ImageView) findViewById(R.id.botPlayerTap3)).setImageResource(0);
        ((ImageView) findViewById(R.id.botPlayerTap4)).setImageResource(0);
        ((ImageView) findViewById(R.id.botPlayerTap5)).setImageResource(0);
        ((ImageView) findViewById(R.id.botPlayerTap6)).setImageResource(0);
        ((ImageView) findViewById(R.id.botPlayerTap7)).setImageResource(0);
        ((ImageView) findViewById(R.id.botPlayerTap8)).setImageResource(0);
        binding.playWithAiPlayerTimeline.setProgress(0);
        binding.playWithAiTimeline.setProgress(0);
        countDownTimer.cancel();
        if (myInterstitialAds != null) myInterstitialAds.showInterstitialAd();
        timingSetting();
    }

    private void showGameOverDialog(String resultMessage) {
        SaveInDevice.saveData(PlayWithAiActivity.this, "NO_GAME", ++noGame);
        AlertDialog.Builder builder = new AlertDialog.Builder(PlayWithAiActivity.this);
        builder.setTitle("Game Over");
        builder.setMessage(resultMessage + "\n\n" + "Do you want to start a new game?");
        builder.setPositiveButton("New Game", (dialog, which) -> {
            Toast.makeText(PlayWithAiActivity.this, "New Game", Toast.LENGTH_SHORT).show();
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
                    showGameOverDialog("AI Wins! 🏆\nYou — Keep Practicing! 💪");
                } else {
                    showGameOverDialog("You has won🏆\nand Bot has loss!");
                    submitLeaderboardScore(++score);
                    SaveInDevice.saveData(PlayWithAiActivity.this, "AI_SCORE", score);
                }
                gameActive = false;
                return;
            }
        }
        String[] gridStr = Arrays.stream(grid).mapToObj(String::valueOf).toArray(String[]::new);
        String currGrid = String.join(",", gridStr);
        if (!currGrid.contains("2")) {
            showGameOverDialog("It's a Tie! 🤝 Excellent Performance! 🎉");
            gameActive = false;
        }
        countDownTimer.cancel();
        timingSetting();
    }

    private void submitLeaderboardScore(long score) {
        PlayGames.getGamesSignInClient(this)
                .isAuthenticated()
                .addOnCompleteListener(task -> {
                    boolean authenticated = task.isSuccessful() && task.getResult() != null && task.getResult().isAuthenticated();
                    if (authenticated) {
                        PlayGames.getLeaderboardsClient(this)
                                .submitScore("CgkIyqmG5tAVEAIQAQ", score);
                    } else {
                        Toast.makeText(this, "Play Games not logged in", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    public void playerTap(View v) {
        if (you && gameActive) {
            ImageView img = (ImageView) v;
            int tappedImg = Integer.parseInt(img.getTag().toString());
            if (grid[tappedImg] == 2) {
                you = false;
                grid[tappedImg] = activePlayer;
                img.setTranslationY(-1000f);
                img.setImageResource(R.drawable.x);
                activePlayer = 0;
                binding.playWithAiPlayerTimeline.setProgress(0);
                img.animate().translationYBy(1000f).setDuration(300);
                winCondition();
                new Handler(Looper.getMainLooper()).postDelayed(this::botPlay, 500);
            }
        }
    }

    public void botPlay() {
        if (gameActive) {
            int index = findIndex();
            String buttonID = "botPlayerTap" + index;
            int resID = getResources().getIdentifier(buttonID, "id", PlayWithAiActivity.this.getPackageName());
            ImageView img = findViewById(resID);
            if (grid[index] == 2) {
                grid[index] = activePlayer;
                img.setTranslationY(-1000f);
                img.setImageResource(R.drawable.o);
                activePlayer = 1;
                binding.playWithAiTimeline.setProgress(0);
                img.animate().translationYBy(1000f).setDuration(300);
                winCondition();
            }
            you = true;
        }
    }

    int findIndex() {
        int index;
        do {
            index = (int) (Math.random() * 9);
        } while (grid[index] != 2);
        return algo(index);
    }

    public int algo(int index) {
        for (int[] winPosition : winPositions) {
            if (grid[winPosition[0]] == grid[winPosition[2]] && grid[winPosition[0]] == 0 && grid[winPosition[1]] == 2) {
                return winPosition[1];
            } else if (grid[winPosition[0]] == grid[winPosition[1]] && grid[winPosition[1]] == 0 && grid[winPosition[2]] == 2) {
                return winPosition[2];
            } else if (grid[winPosition[1]] == grid[winPosition[2]] && grid[winPosition[1]] == 0 && grid[winPosition[0]] == 2) {
                return winPosition[0];
            }
        }
        for (int[] winPosition : winPositions) {
            if (grid[winPosition[0]] == grid[winPosition[2]] && grid[winPosition[0]] != 2 && grid[winPosition[1]] == 2) {
                return winPosition[1];
            } else if (grid[winPosition[0]] == grid[winPosition[1]] && grid[winPosition[1]] != 2 && grid[winPosition[2]] == 2) {
                return winPosition[2];
            } else if (grid[winPosition[1]] == grid[winPosition[2]] && grid[winPosition[1]] != 2 && grid[winPosition[0]] == 2) {
                return winPosition[0];
            }
        }
        for (int[] winPosition : winPositions) {
            if (grid[winPosition[0]] == 0 && grid[winPosition[1]] == 2 && grid[winPosition[2]] == 2) {
                return winPosition[1];
            } else if (grid[winPosition[0]] == 2 && grid[winPosition[1]] == 0 && grid[winPosition[2]] == 2) {
                return winPosition[2];
            } else if (grid[winPosition[0]] == 2 && grid[winPosition[1]] == 2 && grid[winPosition[2]] == 0) {
                return winPosition[0];
            }
        }
        return index;
    }

    @Override
    public void onDestroy() {
        if (countDownTimer != null) countDownTimer.cancel();
        PlayGames.getLeaderboardsClient(this)
                .submitScore("CgkIyqmG5tAVEAIQAQ", score);
//        if (myInterstitialAds != null) myInterstitialAds.showInterstitialAd();
        super.onDestroy();
    }
}
