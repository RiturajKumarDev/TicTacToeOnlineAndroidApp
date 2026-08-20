package com.rituraj.tictactoeonlineapp.ui.games;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.games.PlayGames;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.rituraj.tictactoeonlineapp.R;
import com.rituraj.tictactoeonlineapp.ads.MyInterstitialAds;
import com.rituraj.tictactoeonlineapp.appPrefs.SaveInDevice;
import com.rituraj.tictactoeonlineapp.databinding.ActivityOnlineModeBinding;
import com.rituraj.tictactoeonlineapp.models.Game;
import com.rituraj.tictactoeonlineapp.models.Player;
import com.rituraj.tictactoeonlineapp.models.User;
import com.rituraj.tictactoeonlineapp.ui.auth.LoginPage;

public class OnlineModeActivity extends AppCompatActivity {
    private FirebaseDatabase database;
    private DatabaseReference reference, gameRef;
    private FirebaseAuth auth;
    private User onlineUser = new User(), currUser = new User();

    private ProgressBar currentPlayerProgressBar, onlinePlayerProgressBar;
    private TextView currUserName, onlineUserName;
    private ImageView currUserImg, onlineUserImg;
    private AdView adView1, adView2;

    private String currUserId, onlineUserId;
    private String[] grid = {"2", "2", "2", "2", "2", "2", "2", "2", "2"};
    private boolean gameActive = true;
    private int[][] winPositions = {{0, 1, 2}, {3, 4, 5}, {6, 7, 8}, {0, 3, 6}, {1, 4, 7}, {2, 5, 8}, {0, 4, 8}, {2, 4, 6},};
    private String userId1UserId2, userId2UserId1, lossPlayerName, winPlayerName;
    private CountDownTimer countDownTimer;
    private Game game = new Game();
    private ValueEventListener gameListener;
    private long score = 0, noGame = 0;
    private MyInterstitialAds myInterstitialAds;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            EdgeToEdge.enable(this);
        } catch (Exception ignored) {
        }
        setContentView(R.layout.activity_online_mode);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        init();
        intiViews();
        fun();
    }

    private void init() {
        database = FirebaseDatabase.getInstance();
        auth = FirebaseAuth.getInstance();

        onlineUserId = getIntent().getStringExtra("UserID");
        if (onlineUserId == null || onlineUserId.isEmpty()) {
            Toast.makeText(OnlineModeActivity.this, "Invalid user", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void intiViews() {
        // Users
        currUserImg = findViewById(R.id.currUserImg);
        onlineUserImg = findViewById(R.id.onlineUserImg);
        currUserName = findViewById(R.id.currUserName);
        onlineUserName = findViewById(R.id.onlineUserName);
        currentPlayerProgressBar = findViewById(R.id.currentPlayerProgressBar);
        onlinePlayerProgressBar = findViewById(R.id.onlinePlayerProgressBar);

        // Ad View
        adView1 = findViewById(R.id.adView_game1);
        adView2 = findViewById(R.id.adView_game2);
    }

    public void fun() {
        interstitialAd();
        score = SaveInDevice.getData(OnlineModeActivity.this, "ONLINE_SCORE");
        noGame = SaveInDevice.getData(OnlineModeActivity.this, "NO_GAME");
        googleAD();
        getUsers();
    }

    private void interstitialAd() {
        String adUintId = getResources().getString(R.string.online_end_game_interstitial_unit_id);
        myInterstitialAds = new MyInterstitialAds(OnlineModeActivity.this, OnlineModeActivity.this, adUintId);
    }

    private void googleAD() {
        if (adView1 != null) {
            MobileAds.initialize(this);
            AdRequest adRequest = new AdRequest.Builder().build();
            adView1.loadAd(adRequest);
        }

        if (adView2 != null) {
            MobileAds.initialize(this);
            AdRequest adRequest = new AdRequest.Builder().build();
            adView2.loadAd(adRequest);
        }
    }

    private void getUsers() {
        FirebaseUser firebaseUser = auth.getCurrentUser();
        if (firebaseUser == null) {
            startActivity(new Intent(OnlineModeActivity.this, LoginPage.class));
            finish();
            return;
        }
        currUserId = firebaseUser.getUid();
        currUser.setUserId(firebaseUser.getUid());
        currUser.setEmail(firebaseUser.getEmail());
        currUser.setUserName(firebaseUser.getDisplayName());
        if (firebaseUser.getPhotoUrl() != null)
            currUser.setProfileUrl(firebaseUser.getPhotoUrl().toString());
        else currUser.setProfileUrl("");

        DatabaseReference myReference = database.getReference().child("Users").child(onlineUserId);
        myReference.keepSynced(true);
        myReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    onlineUser = snapshot.getValue(User.class);
                    if (onlineUser == null) {
                        finish();
                        return;
                    }
                    onlineUser.setUserId(snapshot.getKey());
                    setUsers();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(OnlineModeActivity.this, "Opp's Something went wrong!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setUsers() {
        userId1UserId2 = currUser.getUserId() + onlineUser.getUserId();
        userId2UserId1 = onlineUser.getUserId() + currUser.getUserId();

        currUserName.setText(currUser.getUserName());
        Glide.with(OnlineModeActivity.this).load(currUser.getProfileUrl()).placeholder(R.drawable.user).error(R.drawable.user).into(currUserImg);

        onlineUserName.setText(String.valueOf(onlineUser.getUserName()));
        Glide.with(OnlineModeActivity.this).load(onlineUser.getProfileUrl()).placeholder(R.drawable.user).error(R.drawable.user).into(onlineUserImg);
        gameRequestAccepted();
        createGameGrid();
    }

    private void gameRequestAccepted() {
        database.getReference("GameRequest").child(onlineUser.getUserId()).child("Accepted").setValue(true);
        new Handler(Looper.getMainLooper()).postDelayed(this::removeGameRequest, 500);
    }

    public void removeGameRequest() {
        database.getReference("GameRequest").child(onlineUser.getUserId()).removeValue();
    }

    public void createGameGrid() {
        game.setActiveToken("1");
        game.setActiveUserId(currUser.getUserId());
        game.setActiveUserName(currUser.getUserName());
        game.setGrid(String.join(",", grid));
        saveAndUpdateOnlineGame();
        restartTimer();
        fetchGameData();
    }

    private void saveAndUpdateOnlineGame() {
        database.getReference().child("Games").child(userId1UserId2).setValue(game);
        database.getReference().child("Games").child(userId2UserId1).setValue(game);
    }

    public void onlinePlay(View view) {
        if (game == null) return;
        if (gameActive && currUser.getUserId().equals(game.getActiveUserId())) {
            ImageView currImg = (ImageView) view;
            int currIndex = Integer.parseInt(currImg.getTag().toString());

            if (grid[currIndex].equals("2")) {
                currentPlayerProgressBar.setProgress(0);
                onlinePlayerProgressBar.setProgress(0);
                grid[currIndex] = game.getActiveToken();
                game.setActiveToken(game.getActiveToken().equals("1") ? "0" : "1");
                game.setActiveUserId(game.getActiveUserId().equals(currUser.getUserId()) ? onlineUser.getUserId() : currUser.getUserId());
                game.setActiveUserName(game.getActiveUserName().equals(currUser.getUserName()) ? onlineUser.getUserName() : currUser.getUserName());
                game.setGrid(String.join(",", grid));
                saveAndUpdateOnlineGame();
            }
        }
    }

    public void fetchGameData() {
        gameRef = database.getReference().child("Games").child(userId1UserId2);
        gameRef.keepSynced(true);
        gameListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()) {
                    finish();
                    return;
                }
                game = snapshot.getValue(Game.class);
                if (game == null) {
                    finish();
                    return;
                }
                grid = game.getGrid().split(",");
                gameActive = true;
                updateGameUI();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(OnlineModeActivity.this, "Opp's!", Toast.LENGTH_SHORT).show();
            }
        };
        gameRef.addValueEventListener(gameListener);
    }

    public void updateGameUI() {
        restartTimer();
        for (int i = 0; i < 9; i++) {
            String imgId = "imageView" + i;
            int resID = getResources().getIdentifier(imgId, "id", getPackageName());
            ImageView imageView = (ImageView) findViewById(resID);
            if (grid[i].equals("1")) imageView.setImageResource(R.drawable.x);
            else if (grid[i].equals("0")) imageView.setImageResource(R.drawable.o);
            else imageView.setImageResource(0);
        }
        winCondition();
    }

    public void winCondition() {
        lossPlayerName = game.getActiveUserName().equals(currUser.getUserName()) ? currUser.getUserName() : onlineUser.getUserName();
        winPlayerName = game.getActiveUserName().equals(currUser.getUserName()) ? onlineUser.getUserName() : currUser.getUserName();
        for (int[] winPosition : winPositions) {
            if (grid[winPosition[0]].equals(grid[winPosition[1]]) && grid[winPosition[1]].equals(grid[winPosition[2]]) && !grid[winPosition[2]].equals("2")) {
                if (winPlayerName.equals(currUser.getUserName())) {
                    score++;
                    submitLeaderboardScore(score);
                    SaveInDevice.saveData(OnlineModeActivity.this, "ONLINE_SCORE", score);
                }
                if (grid[winPosition[0]].equals("0")) {
                    showGameOverDialog(winPlayerName + " (O) won🏆\n" + "and " + lossPlayerName + "(X) has loss!");
                } else {
                    showGameOverDialog(winPlayerName + " (X) won🏆\n" + "and " + lossPlayerName + "(O) has loss!");
                }
                gameActive = false;
                return;
            }
        }
        String currGrid = String.join(",", grid);
        if (!currGrid.contains("2")) showGameOverDialog("It's a Tie! 🤝 Excellent Performance! 🎉");
    }

    private void submitLeaderboardScore(long score) {
        PlayGames.getGamesSignInClient(this).isAuthenticated().addOnCompleteListener(task -> {
            boolean authenticated = task.isSuccessful() && task.getResult() != null && task.getResult().isAuthenticated();
            if (authenticated) {
                PlayGames.getLeaderboardsClient(this).submitScore("CgkIyqmG5tAVEAIQAw", score);
            } else {
                Toast.makeText(this, "Play Games not logged in", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showGameOverDialog(String resultMessage) {
        noGame++;
        SaveInDevice.saveData(OnlineModeActivity.this, "NO_GAME", ++noGame);
        if (countDownTimer != null) countDownTimer.cancel();
        AlertDialog.Builder builder = new AlertDialog.Builder(OnlineModeActivity.this);
        builder.setTitle("Game Over!!");
        builder.setMessage(resultMessage);
        builder.setPositiveButton("Ok", (dialog, which) -> {
            dialog.dismiss();
            finish();
        });
        builder.setNegativeButton("Exit", (dialog, which) -> {
            dialog.dismiss();
            finish();
        });
        builder.setCancelable(false);
        builder.show();
    }

    private void timingSetting() {
        int totalMillis = 30 * 1000;
        countDownTimer = new CountDownTimer(totalMillis, 100) {
            @Override
            public void onTick(long millisUntilFinished) {
                int progress = (int) ((millisUntilFinished * 100) / 30000);
                if (game != null && onlineUser != null && onlineUser.getUserId() != null)
                    if (game.getActiveUserId().equals(onlineUser.getUserId())) {
                        currentPlayerProgressBar.setProgress(0);
                        onlinePlayerProgressBar.setProgress(progress);
                    } else {
                        currentPlayerProgressBar.setProgress(progress);
                        onlinePlayerProgressBar.setProgress(0);
                    }
            }

            @Override
            public void onFinish() {
                currentPlayerProgressBar.setProgress(0);
                onlinePlayerProgressBar.setProgress(0);
                if (game != null) {
                    game.setActiveToken(game.getActiveToken().equals("1") ? "0" : "1");
                    game.setActiveUserId(game.getActiveUserId().equals(currUser.getUserId()) ? onlineUser.getUserId() : currUser.getUserId());
                    game.setActiveUserName(game.getActiveUserName().equals(currUser.getUserName()) ? onlineUser.getUserName() : currUser.getUserName());
                    saveAndUpdateOnlineGame();
                }
                restartTimer();
            }
        }.start();
    }

    private void restartTimer() {
        if (countDownTimer != null) countDownTimer.cancel();
        timingSetting();
    }

    @Override
    public void onDestroy() {
        try {
            if (countDownTimer != null) countDownTimer.cancel();
            if (gameListener != null) gameRef.removeEventListener(gameListener);
            if (myInterstitialAds != null) myInterstitialAds.showInterstitialAd();
            if (userId1UserId2 != null)
                database.getReference("Games").child(userId1UserId2).removeValue();
            if (userId2UserId1 != null)
                database.getReference("Games").child(userId2UserId1).removeValue();
        } catch (Exception ignored) {
        }
        super.onDestroy();
    }
}
