package com.rituraj.tictactoeonlineapp.ui.games;

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

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.rituraj.tictactoeonlineapp.R;
import com.rituraj.tictactoeonlineapp.models.Player;
import com.rituraj.tictactoeonlineapp.models.TicTacToeAI;

public class PlayHardAiActivity extends AppCompatActivity {
    private final char[][] board = {
            {'_', '_', '_'},
            {'_', '_', '_'},
            {'_', '_', '_'}
    };
    private final ImageView[][] buttons = new ImageView[3][3];
    private final ImageView[] cellViews = new ImageView[9];
    private final TicTacToeAI ai = new TicTacToeAI();
    private boolean gameOver = false, playerMove = true;
    private CountDownTimer countDownTimer;
    private ProgressBar playWithAiTimeline, playWithAiPlayerTimeline;
    private View cardAi, cardUser;
    private FirebaseAuth auth;
    private FirebaseDatabase database;
    private FirebaseUser user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_advance_ai);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        database = FirebaseDatabase.getInstance();
        auth = FirebaseAuth.getInstance();

        View btnBack = findViewById(R.id.btnBack);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        View btnRestartGame = findViewById(R.id.btnRestartGame);
        if (btnRestartGame != null) {
            btnRestartGame.setOnClickListener(v -> resetGame());
        }

        initView();
        fun();
        firstMoveSelection();
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

    private void firstMoveSelection() {
        try {
            View view = LayoutInflater.from(this).inflate(R.layout.dialog_choice, null);

            AlertDialog dialog = new AlertDialog.Builder(this)
                    .setView(view)
                    .setCancelable(false)
                    .create();

            if (dialog.getWindow() != null) {
                dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
            }

            View btnUser = view.findViewById(R.id.btnUser);
            View btnAI = view.findViewById(R.id.btnAI);

            if (btnUser != null) {
                btnUser.setOnClickListener(v -> {
                    playerMove = true;
                    updateTurnHighlight();
                    timingSetting();
                    dialog.dismiss();
                });
            }

            if (btnAI != null) {
                btnAI.setOnClickListener(v -> {
                    playerMove = false;
                    updateTurnHighlight();
                    timingSetting();
                    new Handler(Looper.getMainLooper()).postDelayed(this::aiMove, 500);
                    dialog.dismiss();
                });
            }

            dialog.show();
        } catch (Exception e) {
            playerMove = true;
            updateTurnHighlight();
            timingSetting();
        }
    }

    private void timingSetting() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }

        int totalMillis = 30 * 1000;
        countDownTimer = new CountDownTimer(totalMillis, 100) {
            @Override
            public void onTick(long millisUntilFinished) {
                int progress = (int) ((millisUntilFinished * 100) / 30000);
                if (playerMove) {
                    if (playWithAiPlayerTimeline != null) playWithAiPlayerTimeline.setProgress(progress);
                    if (playWithAiTimeline != null) playWithAiTimeline.setProgress(0);
                } else {
                    if (playWithAiTimeline != null) playWithAiTimeline.setProgress(progress);
                    if (playWithAiPlayerTimeline != null) playWithAiPlayerTimeline.setProgress(0);
                }
            }

            @Override
            public void onFinish() {
                if (gameOver) return;
                if (playerMove) {
                    new Handler(Looper.getMainLooper()).postDelayed(PlayHardAiActivity.this::aiMove, 500);
                }
                playerMove = !playerMove;
                updateTurnHighlight();
                restartTimer();
            }
        }.start();
    }

    private void restartTimer() {
        if (countDownTimer != null) countDownTimer.cancel();
        timingSetting();
    }

    private void initView() {
        playWithAiPlayerTimeline = findViewById(R.id.playWithAiPlayerTimeline);
        playWithAiTimeline = findViewById(R.id.playWithAiTimeline);
        cardAi = findViewById(R.id.cardAi);
        cardUser = findViewById(R.id.cardUser);

        buttons[0][0] = findViewById(R.id.botPlayerTap0);
        buttons[0][1] = findViewById(R.id.botPlayerTap1);
        buttons[0][2] = findViewById(R.id.botPlayerTap2);

        buttons[1][0] = findViewById(R.id.botPlayerTap3);
        buttons[1][1] = findViewById(R.id.botPlayerTap4);
        buttons[1][2] = findViewById(R.id.botPlayerTap5);

        buttons[2][0] = findViewById(R.id.botPlayerTap6);
        buttons[2][1] = findViewById(R.id.botPlayerTap7);
        buttons[2][2] = findViewById(R.id.botPlayerTap8);

        cellViews[0] = buttons[0][0];
        cellViews[1] = buttons[0][1];
        cellViews[2] = buttons[0][2];
        cellViews[3] = buttons[1][0];
        cellViews[4] = buttons[1][1];
        cellViews[5] = buttons[1][2];
        cellViews[6] = buttons[2][0];
        cellViews[7] = buttons[2][1];
        cellViews[8] = buttons[2][2];
    }

    private void fun() {
        user = auth.getCurrentUser();
        if (user != null && user.getDisplayName() != null && !user.getDisplayName().isEmpty()) {
            TextView aiCurrentUsername = findViewById(R.id.aiCurrentUsername);
            if (aiCurrentUsername != null) {
                aiCurrentUsername.setText(user.getDisplayName());
            }
        }

        for (int r = 0; r < 3; r++) {
            for (int c = 0; c < 3; c++) {
                final int finalR = r;
                final int finalC = c;
                if (buttons[r][c] != null) {
                    buttons[r][c].setOnClickListener(v -> onPlayerMove(finalR, finalC));
                }
            }
        }
    }

    private void updateTurnHighlight() {
        if (playerMove) {
            if (cardUser instanceof CardView) {
                ((CardView) cardUser).setCardElevation(16f);
                cardUser.setAlpha(1.0f);
            }
            if (cardAi instanceof CardView) {
                ((CardView) cardAi).setCardElevation(4f);
                cardAi.setAlpha(0.65f);
            }
        } else {
            if (cardAi instanceof CardView) {
                ((CardView) cardAi).setCardElevation(16f);
                cardAi.setAlpha(1.0f);
            }
            if (cardUser instanceof CardView) {
                ((CardView) cardUser).setCardElevation(4f);
                cardUser.setAlpha(0.65f);
            }
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
                int overallGames = player.getGameCount() + 1;
                int overallWins = player.getWinCount() + (isPlayerWin ? 1 : 0);

                // Hard AI Specific Attributes
                int hardGames = player.getHardAiGameCount() + 1;
                int hardWins = player.getHardAiWinCount() + (isPlayerWin ? 1 : 0);
                double hardWinRate = (hardGames > 0) ? ((double) hardWins / hardGames) : 0.0;

                int level = overallGames / 10;
                int rank = overallWins * 10;
                String lastScore = isPlayerWin ? "1" : "0";

                player.setPlayerId(uid);
                if (currentUser.getDisplayName() != null && !currentUser.getDisplayName().isEmpty()) {
                    player.setName(currentUser.getDisplayName());
                }
                if (currentUser.getEmail() != null && !currentUser.getEmail().isEmpty()) {
                    player.setEmail(currentUser.getEmail());
                }

                // Overall Fields
                player.setGameCount(overallGames);
                player.setWinCount(overallWins);
                player.setLastScore(lastScore);
                player.setLevel(level);
                player.setRank(rank);

                // Hard AI Specific Fields
                player.setHardAiGameCount(hardGames);
                player.setHardAiWinCount(hardWins);
                player.setHardAiWinRate(hardWinRate);

                // Save to Firebase Realtime Database
                playerRef.setValue(player);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
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

    private void highlightWinningLineForPlayer(char p) {
        int[][] winLines = {
                {0, 1, 2}, {3, 4, 5}, {6, 7, 8},
                {0, 3, 6}, {1, 4, 7}, {2, 5, 8},
                {0, 4, 8}, {2, 4, 6}
        };

        for (int[] line : winLines) {
            int r0 = line[0] / 3, c0 = line[0] % 3;
            int r1 = line[1] / 3, c1 = line[1] % 3;
            int r2 = line[2] / 3, c2 = line[2] % 3;

            if (board[r0][c0] == p && board[r1][c1] == p && board[r2][c2] == p) {
                for (int pos : line) {
                    if (pos >= 0 && pos < cellViews.length && cellViews[pos] != null) {
                        cellViews[pos].animate()
                                .scaleX(1.30f)
                                .scaleY(1.30f)
                                .setDuration(400)
                                .setInterpolator(new OvershootInterpolator())
                                .start();
                    }
                }
                break;
            }
        }
    }

    private void onPlayerMove(int row, int col) {
        if (gameOver || board[row][col] != '_' || !playerMove)
            return;

        playerMove = false;
        board[row][col] = 'X';
        if (playWithAiPlayerTimeline != null) playWithAiPlayerTimeline.setProgress(0);
        animateCellPlacement(buttons[row][col], R.drawable.x);
        updateTurnHighlight();

        if (checkWinner('X')) {
            gameOver = true;
            highlightWinningLineForPlayer('X');
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                updatePlayerDataInFirebase(true);
                showGameOverDialog("You Have Won! 🏆\nPro AI Defeated!");
            }, 1800);
            return;
        }

        if (isBoardFull()) {
            gameOver = true;
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                updatePlayerDataInFirebase(false);
                showGameOverDialog("It's a Tie! 🤝 Excellent Match! 🎉");
            }, 1200);
            return;
        }

        new Handler(Looper.getMainLooper()).postDelayed(PlayHardAiActivity.this::aiMove, 550);
    }

    private void aiMove() {
        if (gameOver) return;

        int bestR = -1, bestC = -1;
        if (isBoardEmpty()) {
            bestR = (int) (Math.random() * 3);
            bestC = (int) (Math.random() * 3);
        } else {
            TicTacToeAI.Move bestMove = ai.findBestMove(board);
            bestR = bestMove.row;
            bestC = bestMove.col;
        }

        if (bestR != -1 && bestC != -1 && board[bestR][bestC] == '_') {
            playerMove = true;
            if (playWithAiTimeline != null) playWithAiTimeline.setProgress(0);
            board[bestR][bestC] = 'O';
            animateCellPlacement(buttons[bestR][bestC], R.drawable.o);
            updateTurnHighlight();
        }

        if (checkWinner('O')) {
            gameOver = true;
            highlightWinningLineForPlayer('O');
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                updatePlayerDataInFirebase(false);
                showGameOverDialog("Pro AI Bot Wins! 🤖\nTry again!");
            }, 1800);
            return;
        }

        if (isBoardFull()) {
            gameOver = true;
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                updatePlayerDataInFirebase(false);
                showGameOverDialog("It's a Tie! 🤝 Excellent Match! 🎉");
            }, 1200);
        }
    }

    private boolean checkWinner(char player) {
        restartTimer();
        for (int i = 0; i < 3; i++) {
            if (board[i][0] == player && board[i][1] == player && board[i][2] == player)
                return true;
            if (board[0][i] == player && board[1][i] == player && board[2][i] == player)
                return true;
        }
        if (board[0][0] == player && board[1][1] == player && board[2][2] == player)
            return true;
        return board[0][2] == player && board[1][1] == player && board[2][0] == player;
    }

    private boolean isBoardFull() {
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                if (board[i][j] == '_')
                    return false;
            }
        }
        return true;
    }

    private boolean isBoardEmpty() {
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                if (board[i][j] != '_')
                    return false;
            }
        }
        return true;
    }

    private void showGameOverDialog(String resultMessage) {
        if (isFinishing() || isDestroyed()) return;

        if (countDownTimer != null) {
            countDownTimer.cancel();
        }

        try {
            View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_game_over, null);
            AlertDialog dialog = new AlertDialog.Builder(this)
                    .setView(dialogView)
                    .setCancelable(false)
                    .create();

            if (dialog.getWindow() != null) {
                dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
                // Glassmorphic translucent card allows seeing board & winning line directly through dialog
                dialog.getWindow().setDimAmount(0.15f);
            }

            TextView tvTitle = dialogView.findViewById(R.id.dialogTitle);
            TextView tvMessage = dialogView.findViewById(R.id.dialogResultMessage);
            View btnPlayAgain = dialogView.findViewById(R.id.btnDialogPlayAgain);
            View btnExit = dialogView.findViewById(R.id.btnDialogExit);

            if (tvMessage != null) {
                tvMessage.setText(resultMessage);
            }

            if (resultMessage.contains("Tie")) {
                if (tvTitle != null) {
                    tvTitle.setText("MATCH TIED! 🤝");
                }
            }

            if (btnPlayAgain != null) {
                btnPlayAgain.setOnClickListener(v -> {
                    dialog.dismiss();
                    resetGame();
                });
            }

            if (btnExit != null) {
                btnExit.setOnClickListener(v -> {
                    dialog.dismiss();
                    finish();
                });
            }

            // Animate dialog pop-in
            try {
                Animation popAnim = AnimationUtils.loadAnimation(this, R.anim.splash_logo_pop);
                dialogView.startAnimation(popAnim);
            } catch (Exception ignored) {
            }

            dialog.show();
        } catch (Exception e) {
            e.printStackTrace();
            resetGame();
        }
    }

    private void resetGame() {
        playerMove = true;
        gameOver = false;
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                board[i][j] = '_';
                if (buttons[i][j] != null) {
                    buttons[i][j].setImageResource(0);
                    buttons[i][j].setScaleX(1.0f);
                    buttons[i][j].setScaleY(1.0f);
                }
            }
        }
        updateTurnHighlight();
        restartTimer();
        firstMoveSelection();
    }

    @Override
    public void onDestroy() {
        if (countDownTimer != null) countDownTimer.cancel();
        super.onDestroy();
    }
}
