package com.rituraj.tictactoeonlineapp.ui.games;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.OvershootInterpolator;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.rituraj.tictactoeonlineapp.R;
import com.rituraj.tictactoeonlineapp.models.Game;
import com.rituraj.tictactoeonlineapp.models.Player;
import com.rituraj.tictactoeonlineapp.models.User;
import com.rituraj.tictactoeonlineapp.ui.auth.LoginPage;

public class OnlineModeActivity extends AppCompatActivity {
    private FirebaseDatabase database;
    private DatabaseReference gameRef;
    private FirebaseAuth auth;
    private User onlineUser = new User(), currUser = new User();

    private ProgressBar currentPlayerProgressBar, onlinePlayerProgressBar;
    private TextView currUserName, onlineUserName;
    private ImageView currUserImg, onlineUserImg;
    private View cardUser, cardOnlineUser;

    private String currUserId = "", gameId = "", opponentId = "";
    private Game game = new Game();
    private String[] grid = {"2", "2", "2", "2", "2", "2", "2", "2", "2"};
    private final ImageView[] cellViews = new ImageView[9];
    private boolean gameActive = true;
    private boolean isOpponentLeftDialogShown = false;
    private AlertDialog currentGameOverDialog;
    private CountDownTimer countDownTimer;
    private final int[][] winPositions = {
            {0, 1, 2}, {3, 4, 5}, {6, 7, 8},
            {0, 3, 6}, {1, 4, 7}, {2, 5, 8},
            {0, 4, 8}, {2, 4, 6}
    };
    private ValueEventListener gameListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_online_mode);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        database = FirebaseDatabase.getInstance();
        auth = FirebaseAuth.getInstance();

        gameId = getIntent().getStringExtra("GameId");
        if (gameId == null || gameId.isEmpty()) {
            gameId = getIntent().getStringExtra("UserID");
        }

        View btnBack = findViewById(R.id.btnBack);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> {
                if (gameActive) {
                    showLeaveConfirmationDialog();
                } else {
                    notifyOpponentILeft();
                    finish();
                }
            });
        }

        initViews();
        fun();
        timingSetting();
        startEntranceAnimations();
    }

    private void startEntranceAnimations() {
        try {
            Animation slideAnim = AnimationUtils.loadAnimation(this, R.anim.splash_text_slide);
            View header = findViewById(R.id.playersHeaderSection);
            View gameBoard = findViewById(R.id.playWithAiBoard);
            if (header != null) header.startAnimation(slideAnim);
            if (gameBoard != null) gameBoard.startAnimation(slideAnim);
        } catch (Exception ignored) {
        }
    }

    private void initViews() {
        currUserImg = findViewById(R.id.currUserImg);
        onlineUserImg = findViewById(R.id.onlineUserImg);
        currUserName = findViewById(R.id.currUserName);
        onlineUserName = findViewById(R.id.onlineUserName);
        currentPlayerProgressBar = findViewById(R.id.currentPlayerProgressBar);
        onlinePlayerProgressBar = findViewById(R.id.onlinePlayerProgressBar);

        cardUser = findViewById(R.id.cardUser);
        cardOnlineUser = findViewById(R.id.cardOnlineUser);

        cellViews[0] = findViewById(R.id.imageView0);
        cellViews[1] = findViewById(R.id.imageView1);
        cellViews[2] = findViewById(R.id.imageView2);
        cellViews[3] = findViewById(R.id.imageView3);
        cellViews[4] = findViewById(R.id.imageView4);
        cellViews[5] = findViewById(R.id.imageView5);
        cellViews[6] = findViewById(R.id.imageView6);
        cellViews[7] = findViewById(R.id.imageView7);
        cellViews[8] = findViewById(R.id.imageView8);

        for (int i = 0; i < cellViews.length; i++) {
            if (cellViews[i] != null) {
                final int index = i;
                cellViews[i].setOnClickListener(v -> onlinePlay(index));
            }
        }
    }

    public void fun() {
        FirebaseUser firebaseUser = auth.getCurrentUser();
        if (firebaseUser == null) {
            startActivity(new Intent(OnlineModeActivity.this, LoginPage.class));
            finish();
            return;
        }

        currUserId = firebaseUser.getUid();
        currUser.setUserId(firebaseUser.getUid());
        currUser.setEmail(firebaseUser.getEmail());
        currUser.setUserName(firebaseUser.getDisplayName() != null ? firebaseUser.getDisplayName() : "Player");
        if (firebaseUser.getPhotoUrl() != null)
            currUser.setProfileUrl(firebaseUser.getPhotoUrl().toString());
        else currUser.setProfileUrl("");

        if (currUserName != null) currUserName.setText(currUser.getUserName());
        if (currUserImg != null) {
            Glide.with(this).load(currUser.getProfileUrl()).placeholder(R.drawable.user).error(R.drawable.user).circleCrop().into(currUserImg);
        }

        if (gameId == null || gameId.isEmpty()) {
            Toast.makeText(this, "Match details not found!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        setupOnDisconnect();
        fetchGameData();
    }

    private void setupOnDisconnect() {
        if (gameId != null && !gameId.isEmpty() && currUserId != null) {
            try {
                DatabaseReference ref = database.getReference("Games").child(gameId);
                ref.child("status").onDisconnect().setValue("LEFT");
                ref.child("leftPlayerId").onDisconnect().setValue(currUserId);
            } catch (Exception ignored) {
            }
        }
    }

    public void fetchGameData() {
        gameRef = database.getReference().child("Games").child(gameId);
        gameRef.keepSynced(true);
        gameListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()) {
                    if (!gameActive) {
                        if (currentGameOverDialog != null && currentGameOverDialog.isShowing()) {
                            try { currentGameOverDialog.dismiss(); } catch (Exception ignored) {}
                        }
                        Toast.makeText(OnlineModeActivity.this, "Opponent exited the match.", Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        showOpponentLeftDialog();
                    }
                    return;
                }
                game = snapshot.getValue(Game.class);
                if (game == null) {
                    if (!gameActive) {
                        if (currentGameOverDialog != null && currentGameOverDialog.isShowing()) {
                            try { currentGameOverDialog.dismiss(); } catch (Exception ignored) {}
                        }
                        Toast.makeText(OnlineModeActivity.this, "Opponent exited the match.", Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        showOpponentLeftDialog();
                    }
                    return;
                }

                // Check if opponent left or disconnected
                if ("LEFT".equals(game.getStatus()) || (game.getLeftPlayerId() != null && !game.getLeftPlayerId().isEmpty() && !game.getLeftPlayerId().equals(currUserId))) {
                    if (!gameActive) {
                        if (currentGameOverDialog != null && currentGameOverDialog.isShowing()) {
                            try { currentGameOverDialog.dismiss(); } catch (Exception ignored) {}
                        }
                        Toast.makeText(OnlineModeActivity.this, "Opponent exited the match.", Toast.LENGTH_SHORT).show();
                        finish();
                        return;
                    }
                    if (currentGameOverDialog != null && currentGameOverDialog.isShowing()) {
                        try { currentGameOverDialog.dismiss(); } catch (Exception ignored) {}
                    }
                    showOpponentLeftDialog();
                    return;
                }

                if (currUserId.equals(game.getPlayerId1())) {
                    opponentId = game.getPlayerId2();
                } else {
                    opponentId = game.getPlayerId1();
                    database.getReference("Notifications").child(currUserId).child("Accepted").setValue(true);
                }

                if (opponentId != null && !opponentId.isEmpty()) {
                    fetchOpponentProfile(opponentId);
                }

                grid = game.getGrid().split(",");

                // Check if Rematch triggered (grid reset to all 2s)
                if ("2,2,2,2,2,2,2,2,2".equals(game.getGrid()) && !gameActive) {
                    gameActive = true;
                    if (currentGameOverDialog != null && currentGameOverDialog.isShowing()) {
                        currentGameOverDialog.dismiss();
                    }
                    resetBoardForNewGame();
                    Toast.makeText(OnlineModeActivity.this, "New Rematch Started! 🎮", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Check if Opponent requested Rematch while dialog is open
                if (game.getRematchRequest() != null && opponentId != null && game.getRematchRequest().equals(opponentId)) {
                    if (currentGameOverDialog != null && currentGameOverDialog.isShowing()) {
                        MaterialButton btnPlayAgain = currentGameOverDialog.findViewById(R.id.btnDialogPlayAgain);
                        if (btnPlayAgain != null) {
                            btnPlayAgain.setText("ACCEPT REMATCH 🔄");
                        }
                    }
                }

                if (gameActive) {
                    updateGameUI();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(OnlineModeActivity.this, "Game connection error!", Toast.LENGTH_SHORT).show();
            }
        };
        gameRef.addValueEventListener(gameListener);
    }

    private void resetBoardForNewGame() {
        if (cellViews != null) {
            for (ImageView cell : cellViews) {
                if (cell != null) {
                    cell.setImageResource(0);
                    cell.setScaleX(1.0f);
                    cell.setScaleY(1.0f);
                    cell.setAlpha(1.0f);
                }
            }
        }
        updateTurnHighlight();
        restartTimer();
    }

    private void showOpponentLeftDialog() {
        if (isOpponentLeftDialogShown || isFinishing() || isDestroyed()) return;
        isOpponentLeftDialogShown = true;
        gameActive = false;

        if (countDownTimer != null) {
            countDownTimer.cancel();
        }

        updatePlayerDataInFirebase(true);

        try {
            View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_game_over, null);
            AlertDialog dialog = new AlertDialog.Builder(this)
                    .setView(dialogView)
                    .setCancelable(false)
                    .create();

            if (dialog.getWindow() != null) {
                dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
                dialog.getWindow().setDimAmount(0.15f);
            }

            TextView tvTitle = dialogView.findViewById(R.id.dialogTitle);
            TextView tvMessage = dialogView.findViewById(R.id.dialogResultMessage);
            View btnPlayAgain = dialogView.findViewById(R.id.btnDialogPlayAgain);
            View btnExit = dialogView.findViewById(R.id.btnDialogExit);

            if (tvTitle != null) {
                tvTitle.setText("OPPONENT LEFT 🚪");
            }

            String oppName = (onlineUser != null && onlineUser.getUserName() != null && !onlineUser.getUserName().isEmpty())
                    ? onlineUser.getUserName() : "Opponent";

            if (tvMessage != null) {
                tvMessage.setText(oppName + " has left the match.\n\nYou win by forfeit! 🏆");
            }

            if (btnPlayAgain != null) {
                btnPlayAgain.setVisibility(View.GONE);
            }

            if (btnExit != null) {
                btnExit.setOnClickListener(v -> {
                    dialog.dismiss();
                    notifyOpponentILeft();
                    finish();
                });
            }

            try {
                Animation popAnim = AnimationUtils.loadAnimation(this, R.anim.splash_logo_pop);
                dialogView.startAnimation(popAnim);
            } catch (Exception ignored) {
            }

            currentGameOverDialog = dialog;
            dialog.show();
        } catch (Exception e) {
            e.printStackTrace();
            finish();
        }
    }

    @Override
    public void onBackPressed() {
        if (gameActive) {
            showLeaveConfirmationDialog();
        } else {
            notifyOpponentILeft();
            super.onBackPressed();
        }
    }

    private void showLeaveConfirmationDialog() {
        if (isFinishing() || isDestroyed()) return;

        try {
            View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_leave_match, null);
            AlertDialog dialog = new AlertDialog.Builder(this)
                    .setView(dialogView)
                    .setCancelable(true)
                    .create();

            if (dialog.getWindow() != null) {
                dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
                dialog.getWindow().setDimAmount(0.20f);
            }

            View btnStay = dialogView.findViewById(R.id.btnStayInMatch);
            View btnLeave = dialogView.findViewById(R.id.btnForfeitLeave);

            if (btnStay != null) {
                btnStay.setOnClickListener(v -> dialog.dismiss());
            }

            if (btnLeave != null) {
                btnLeave.setOnClickListener(v -> {
                    dialog.dismiss();
                    notifyOpponentILeft();
                    finish();
                });
            }

            try {
                Animation popAnim = AnimationUtils.loadAnimation(this, R.anim.splash_logo_pop);
                dialogView.startAnimation(popAnim);
            } catch (Exception ignored) {
            }

            dialog.show();
        } catch (Exception e) {
            e.printStackTrace();
            notifyOpponentILeft();
            finish();
        }
    }

    private void notifyOpponentILeft() {
        if (gameId != null && !gameId.isEmpty() && currUserId != null) {
            try {
                DatabaseReference ref = database.getReference("Games").child(gameId);
                ref.child("status").setValue("LEFT");
                ref.child("leftPlayerId").setValue(currUserId);
            } catch (Exception ignored) {
            }
        }
    }

    private void fetchOpponentProfile(String opId) {
        database.getReference().child("Users").child(opId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            onlineUser = snapshot.getValue(User.class);
                            if (onlineUser != null) {
                                onlineUser.setUserId(snapshot.getKey());
                                if (onlineUserName != null) {
                                    onlineUserName.setText(onlineUser.getUserName() != null ? onlineUser.getUserName() : "Opponent");
                                }
                                if (onlineUserImg != null && onlineUser.getProfileUrl() != null && !onlineUser.getProfileUrl().isEmpty()) {
                                    Glide.with(OnlineModeActivity.this)
                                            .load(onlineUser.getProfileUrl())
                                            .placeholder(R.drawable.user)
                                            .error(R.drawable.user)
                                            .circleCrop()
                                            .into(onlineUserImg);
                                }
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                    }
                });
    }

    public void onlinePlay(int currIndex) {
        if (game == null) return;
        if (gameActive && currUserId.equals(game.getActiveUserId())) {
            if (currIndex >= 0 && currIndex < grid.length && grid[currIndex].equals("2")) {
                if (currentPlayerProgressBar != null) currentPlayerProgressBar.setProgress(0);
                if (onlinePlayerProgressBar != null) onlinePlayerProgressBar.setProgress(0);

                grid[currIndex] = game.getActiveToken();
                game.setActiveToken(game.getActiveToken().equals("1") ? "0" : "1");
                game.setActiveUserId(currUserId.equals(game.getPlayerId1()) ? game.getPlayerId2() : game.getPlayerId1());
                game.setActiveUserName(currUserId.equals(game.getPlayerId1()) ? (onlineUser.getUserName() != null ? onlineUser.getUserName() : "Opponent") : currUser.getUserName());
                game.setGrid(String.join(",", grid));

                database.getReference().child("Games").child(gameId).setValue(game);
            }
        }
    }

    public void updateGameUI() {
        restartTimer();
        updateTurnHighlight();

        for (int i = 0; i < 9 && i < cellViews.length; i++) {
            ImageView imageView = cellViews[i];
            if (imageView != null) {
                if (grid[i].equals("1")) {
                    animateCellPlacement(imageView, R.drawable.x);
                } else if (grid[i].equals("0")) {
                    animateCellPlacement(imageView, R.drawable.o);
                } else {
                    imageView.setImageResource(0);
                }
            }
        }
        winCondition();
    }

    private void updateTurnHighlight() {
        if (game == null) return;
        boolean isMyTurn = currUserId.equals(game.getActiveUserId());

        if (isMyTurn) {
            if (cardUser instanceof CardView) {
                ((CardView) cardUser).setCardElevation(16f);
                cardUser.setAlpha(1.0f);
            }
            if (cardOnlineUser instanceof CardView) {
                ((CardView) cardOnlineUser).setCardElevation(4f);
                cardOnlineUser.setAlpha(0.65f);
            }
        } else {
            if (cardOnlineUser instanceof CardView) {
                ((CardView) cardOnlineUser).setCardElevation(16f);
                cardOnlineUser.setAlpha(1.0f);
            }
            if (cardUser instanceof CardView) {
                ((CardView) cardUser).setCardElevation(4f);
                cardUser.setAlpha(0.65f);
            }
        }
    }

    private void animateCellPlacement(ImageView img, int drawableRes) {
        if (img == null) return;

        img.setImageResource(drawableRes);
        img.setScaleX(0.2f);
        img.setScaleY(0.2f);
        img.setAlpha(0.0f);
        img.setTranslationY(-150f);

        img.animate()
                .scaleX(1.0f)
                .scaleY(1.0f)
                .alpha(1.0f)
                .translationY(0f)
                .setDuration(350)
                .setInterpolator(new OvershootInterpolator())
                .start();
    }

    private void highlightWinningLine(int[] winPos) {
        if (cellViews == null || winPos == null) return;

        for (int pos : winPos) {
            if (pos >= 0 && pos < cellViews.length && cellViews[pos] != null) {
                ImageView winCell = cellViews[pos];
                winCell.animate()
                        .scaleX(1.30f)
                        .scaleY(1.30f)
                        .setDuration(400)
                        .setInterpolator(new OvershootInterpolator())
                        .start();
            }
        }
    }

    public void winCondition() {
        if (game == null) return;

        for (int[] winPosition : winPositions) {
            if (grid[winPosition[0]].equals(grid[winPosition[1]])
                    && grid[winPosition[1]].equals(grid[winPosition[2]])
                    && !grid[winPosition[2]].equals("2")) {

                String winningToken = grid[winPosition[0]];
                boolean isIWin = (winningToken.equals("1") && currUserId.equals(game.getPlayerId1()))
                        || (winningToken.equals("0") && currUserId.equals(game.getPlayerId2()));

                highlightWinningLine(winPosition);

                new Handler(Looper.getMainLooper()).postDelayed(() -> {
                    updatePlayerDataInFirebase(isIWin);
                    if (isIWin) {
                        showGameOverDialog("You Have Won! 🏆\nOpponent Defeated!");
                    } else {
                        showGameOverDialog((onlineUser.getUserName() != null ? onlineUser.getUserName() : "Opponent") + " Has Won! 🏆\nBetter luck next match!");
                    }
                }, 1800);

                gameActive = false;
                return;
            }
        }

        String currGrid = String.join(",", grid);
        if (!currGrid.contains("2")) {
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                updatePlayerDataInFirebase(false);
                showGameOverDialog("It's a Tie! 🤝 Excellent Performance! 🎉");
            }, 1200);
            gameActive = false;
        }
    }

    private void updatePlayerDataInFirebase(boolean isPlayerWin) {
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser == null || database == null) return;

        String uid = currentUser.getUid();
        DatabaseReference playerRef = database.getReference("Players").child(uid);

        playerRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Player player = snapshot.getValue(Player.class);
                if (player == null) {
                    player = new Player(uid, currentUser.getEmail(), currentUser.getDisplayName(), "100");
                }

                // Overall Stats
                int games = player.getGameCount() + 1;
                int wins = player.getWinCount() + (isPlayerWin ? 1 : 0);
                double totalWinRate = (games > 0) ? ((double) wins / games) : 0.0;

                // Online Multiplayer Specific Stats
                int onlineGames = player.getOnlineGameCount() + 1;
                int onlineWins = player.getOnlineWinCount() + (isPlayerWin ? 1 : 0);
                double onlineWinRate = (onlineGames > 0) ? ((double) onlineWins / onlineGames) : 0.0;

                int level = games / 10;
                int rank = wins * 10;
                String lastScore = isPlayerWin ? "1" : "0";

                player.setPlayerId(uid);
                if (currentUser.getDisplayName() != null && !currentUser.getDisplayName().isEmpty()) {
                    player.setName(currentUser.getDisplayName());
                }
                if (currentUser.getEmail() != null && !currentUser.getEmail().isEmpty()) {
                    player.setEmail(currentUser.getEmail());
                }

                // Overall Attributes
                player.setGameCount(games);
                player.setWinCount(wins);
                player.setWinRate(totalWinRate);
                player.setLastScore(lastScore);
                player.setLevel(level);
                player.setRank(rank);

                // Online Attributes
                player.setOnlineGameCount(onlineGames);
                player.setOnlineWinCount(onlineWins);
                player.setOnlineWinRate(onlineWinRate);

                // Save to Firebase Realtime Database Players/{uid}
                playerRef.setValue(player);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    private void showGameOverDialog(String resultMessage) {
        if (isFinishing() || isDestroyed()) return;

        if (countDownTimer != null) countDownTimer.cancel();

        try {
            View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_game_over, null);
            AlertDialog dialog = new AlertDialog.Builder(this)
                    .setView(dialogView)
                    .setCancelable(false)
                    .create();

            if (dialog.getWindow() != null) {
                dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
                dialog.getWindow().setDimAmount(0.15f);
            }

            TextView tvTitle = dialogView.findViewById(R.id.dialogTitle);
            TextView tvMessage = dialogView.findViewById(R.id.dialogResultMessage);
            MaterialButton btnPlayAgain = dialogView.findViewById(R.id.btnDialogPlayAgain);
            MaterialButton btnExit = dialogView.findViewById(R.id.btnDialogExit);

            if (tvMessage != null) {
                tvMessage.setText(resultMessage);
            }

            if (resultMessage.contains("Tie")) {
                if (tvTitle != null) {
                    tvTitle.setText("MATCH TIED! 🤝");
                }
            }

            if (btnPlayAgain != null) {
                btnPlayAgain.setVisibility(View.VISIBLE);
                btnPlayAgain.setText("REMATCH 🔄");
                btnPlayAgain.setOnClickListener(v -> handleRematchRequest(btnPlayAgain));
            }

            if (btnExit != null) {
                btnExit.setOnClickListener(v -> {
                    dialog.dismiss();
                    notifyOpponentILeft();
                    finish();
                });
            }

            try {
                Animation popAnim = AnimationUtils.loadAnimation(this, R.anim.splash_logo_pop);
                dialogView.startAnimation(popAnim);
            } catch (Exception ignored) {
            }

            currentGameOverDialog = dialog;
            dialog.show();
        } catch (Exception e) {
            e.printStackTrace();
            finish();
        }
    }

    private void handleRematchRequest(MaterialButton btnPlayAgain) {
        if (game == null || gameId == null || gameId.isEmpty()) return;

        String opponentRematch = game.getRematchRequest();
        if (opponentRematch != null && !opponentRematch.isEmpty() && !opponentRematch.equals(currUserId)) {
            // Opponent already requested rematch -> Start Rematch!
            game.setGrid("2,2,2,2,2,2,2,2,2");
            game.setActiveToken("1");
            game.setRematchRequest("");
            game.setStatus("PLAYING");
            database.getReference().child("Games").child(gameId).setValue(game);
        } else {
            // Send Rematch Request
            database.getReference().child("Games").child(gameId).child("rematchRequest").setValue(currUserId);
            if (btnPlayAgain != null) {
                btnPlayAgain.setText("WAITING FOR OPPONENT... ⏳");
                btnPlayAgain.setEnabled(false);
            }
            Toast.makeText(this, "Rematch request sent to opponent!", Toast.LENGTH_SHORT).show();
        }
    }

    private void timingSetting() {
        int totalMillis = 30 * 1000;
        countDownTimer = new CountDownTimer(totalMillis, 100) {
            @Override
            public void onTick(long millisUntilFinished) {
                int progress = (int) ((millisUntilFinished * 100) / 30000);
                if (game != null && opponentId != null && !opponentId.isEmpty()) {
                    if (game.getActiveUserId() != null && game.getActiveUserId().equals(opponentId)) {
                        if (currentPlayerProgressBar != null)
                            currentPlayerProgressBar.setProgress(0);
                        if (onlinePlayerProgressBar != null)
                            onlinePlayerProgressBar.setProgress(progress);
                    } else {
                        if (currentPlayerProgressBar != null)
                            currentPlayerProgressBar.setProgress(progress);
                        if (onlinePlayerProgressBar != null) onlinePlayerProgressBar.setProgress(0);
                    }
                }
            }

            @Override
            public void onFinish() {
                if (currentPlayerProgressBar != null) currentPlayerProgressBar.setProgress(0);
                if (onlinePlayerProgressBar != null) onlinePlayerProgressBar.setProgress(0);
                if (game != null && currUserId != null && opponentId != null && !opponentId.isEmpty()) {
                    game.setActiveToken(game.getActiveToken().equals("1") ? "0" : "1");
                    game.setActiveUserId(game.getActiveUserId().equals(currUserId) ? opponentId : currUserId);
                    game.setActiveUserName(game.getActiveUserId().equals(currUserId) ? currUser.getUserName() : (onlineUser.getUserName() != null ? onlineUser.getUserName() : "Opponent"));
                    if (gameId != null && !gameId.isEmpty()) {
                        database.getReference().child("Games").child(gameId).setValue(game);
                    }
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
            if (gameListener != null && gameRef != null) gameRef.removeEventListener(gameListener);
            notifyOpponentILeft();
        } catch (Exception ignored) {
        }
        super.onDestroy();
    }
}
