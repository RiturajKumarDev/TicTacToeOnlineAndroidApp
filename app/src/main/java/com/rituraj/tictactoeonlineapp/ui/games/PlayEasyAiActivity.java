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
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
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
import com.rituraj.tictactoeonlineapp.databinding.ActivityPlayWithAiBinding;
import com.rituraj.tictactoeonlineapp.models.Player;

import java.util.Arrays;

public class PlayEasyAiActivity extends AppCompatActivity {
    private ActivityPlayWithAiBinding binding;
    private int activePlayer = 1; // 1 = You (X), 0 = Bot (O)
    private Boolean gameActive = true, you = true;
    private CountDownTimer countDownTimer;
    private final int[] grid = {2, 2, 2, 2, 2, 2, 2, 2, 2};
    private final int[][] winPositions = {
            {0, 1, 2}, {3, 4, 5}, {6, 7, 8},
            {0, 3, 6}, {1, 4, 7}, {2, 5, 8},
            {0, 4, 8}, {2, 4, 6}
    };
    private FirebaseAuth auth;
    private FirebaseDatabase database;
    private ImageView[] cellViews;

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

        if (binding.btnBack != null) {
            binding.btnBack.setOnClickListener(v -> finish());
        }

        if (binding.btnRestartGame != null) {
            binding.btnRestartGame.setOnClickListener(v -> startNewGame());
        }

        fun();
        timingSetting();
        startEntranceAnimations();
    }

    private void startEntranceAnimations() {
        try {
            Animation slideAnim = AnimationUtils.loadAnimation(this, R.anim.splash_text_slide);
            if (binding.playersHeaderSection != null) {
                binding.playersHeaderSection.startAnimation(slideAnim);
            }
            if (binding.playWithAiBoard != null) {
                binding.playWithAiBoard.startAnimation(slideAnim);
            }
        } catch (Exception ignored) {
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
                if (activePlayer == 1) {
                    binding.playWithAiPlayerTimeline.setProgress(progress);
                    binding.playWithAiTimeline.setProgress(0);
                } else {
                    binding.playWithAiTimeline.setProgress(progress);
                    binding.playWithAiPlayerTimeline.setProgress(0);
                }
            }

            @Override
            public void onFinish() {
                if (!gameActive) return;
                if (activePlayer == 1) {
                    triggerBotPlayWithAnimation();
                }
                activePlayer = activePlayer == 1 ? 0 : 1;
                updateTurnHighlight();
                restartTimer();
            }
        }.start();
    }

    private void restartTimer() {
        if (countDownTimer != null) countDownTimer.cancel();
        timingSetting();
    }

    public void fun() {
        database = FirebaseDatabase.getInstance();
        auth = FirebaseAuth.getInstance();

        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser != null && currentUser.getDisplayName() != null && !currentUser.getDisplayName().isEmpty()) {
            binding.aiCurrentUsername.setText(currentUser.getDisplayName());
        } else {
            binding.aiCurrentUsername.setText("You");
        }

        cellViews = new ImageView[]{
                binding.botPlayerTap0, binding.botPlayerTap1, binding.botPlayerTap2,
                binding.botPlayerTap3, binding.botPlayerTap4, binding.botPlayerTap5,
                binding.botPlayerTap6, binding.botPlayerTap7, binding.botPlayerTap8
        };

        View.OnClickListener listener = this::playerTap;
        for (ImageView cell : cellViews) {
            if (cell != null) {
                cell.setOnClickListener(listener);
            }
        }

        updateTurnHighlight();
    }

    private void updateTurnHighlight() {
        if (activePlayer == 1) {
            if (binding.cardUser != null) {
                binding.cardUser.setCardElevation(16f);
                binding.cardUser.setAlpha(1.0f);
            }
            if (binding.cardAi != null) {
                binding.cardAi.setCardElevation(4f);
                binding.cardAi.setAlpha(0.65f);
            }
        } else {
            if (binding.cardAi != null) {
                binding.cardAi.setCardElevation(16f);
                binding.cardAi.setAlpha(1.0f);
            }
            if (binding.cardUser != null) {
                binding.cardUser.setCardElevation(4f);
                binding.cardUser.setAlpha(0.65f);
            }
        }
    }

    private void startNewGame() {
        you = gameActive = true;
        activePlayer = 1;
        Arrays.fill(grid, 2);

        if (cellViews != null) {
            for (ImageView cell : cellViews) {
                if (cell != null) {
                    cell.setImageResource(0);
                    cell.setAlpha(1.0f);
                    cell.setScaleX(1.0f);
                    cell.setScaleY(1.0f);
                }
            }
        }

        binding.playWithAiPlayerTimeline.setProgress(0);
        binding.playWithAiTimeline.setProgress(0);

        updateTurnHighlight();
        restartTimer();
        Toast.makeText(this, "Match Restarted!", Toast.LENGTH_SHORT).show();
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

                // Easy AI Specific Attributes
                int easyGames = player.getEasyAiGameCount() + 1;
                int easyWins = player.getEasyAiWinCount() + (isPlayerWin ? 1 : 0);
                double easyWinRate = (easyGames > 0) ? ((double) easyWins / easyGames) : 0.0;

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

                // Easy AI Specific Fields
                player.setEasyAiGameCount(easyGames);
                player.setEasyAiWinCount(easyWins);
                player.setEasyAiWinRate(easyWinRate);

                // Save to Firebase Realtime Database
                playerRef.setValue(player);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
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
                    startNewGame();
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
            startNewGame();
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
        for (int[] winPosition : winPositions) {
            if (grid[winPosition[0]] == grid[winPosition[1]]
                    && grid[winPosition[1]] == grid[winPosition[2]]
                    && grid[winPosition[2]] != 2) {

                highlightWinningLine(winPosition);

                // 1.8 second delay so user clearly sees the winning line on the board before the bottom dialog sheet pops up!
                new Handler(Looper.getMainLooper()).postDelayed(() -> {
                    if (grid[winPosition[0]] == 0) {
                        updatePlayerDataInFirebase(false);
                        showGameOverDialog("AI Bot Has Won! 🤖\nKeep practicing!");
                    } else {
                        updatePlayerDataInFirebase(true);
                        showGameOverDialog("You Have Won! 🏆\nBot Defeated!");
                    }
                }, 1800);

                gameActive = false;
                return;
            }
        }

        boolean isTie = true;
        for (int cell : grid) {
            if (cell == 2) {
                isTie = false;
                break;
            }
        }

        if (isTie) {
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                updatePlayerDataInFirebase(false);
                showGameOverDialog("It's a Tie! 🤝 Excellent Performance! 🎉");
            }, 1200);
            gameActive = false;
            return;
        }

        updateTurnHighlight();
        restartTimer();
    }

    public void playerTap(View v) {
        if (you && gameActive && v instanceof ImageView) {
            ImageView img = (ImageView) v;
            if (img.getTag() == null) return;

            int tappedImg;
            try {
                tappedImg = Integer.parseInt(img.getTag().toString());
            } catch (NumberFormatException e) {
                return;
            }

            if (grid[tappedImg] == 2) {
                you = false;
                grid[tappedImg] = activePlayer;
                animateCellPlacement(img, R.drawable.x);
                activePlayer = 0;
                updateTurnHighlight();
                binding.playWithAiPlayerTimeline.setProgress(0);
                winCondition();
                if (gameActive) {
                    triggerBotPlayWithAnimation();
                }
            }
        }
    }

    private void triggerBotPlayWithAnimation() {
        new Handler(Looper.getMainLooper()).postDelayed(this::botPlay, 500);
    }

    public void botPlay() {
        if (gameActive) {
            int index = findIndex();
            if (index < 0 || index >= cellViews.length) return;

            ImageView img = cellViews[index];
            if (img != null && grid[index] == 2) {
                grid[index] = activePlayer;
                animateCellPlacement(img, R.drawable.o);
                activePlayer = 1;
                updateTurnHighlight();
                binding.playWithAiTimeline.setProgress(0);
                winCondition();
            }
            you = true;
        }
    }

    int findIndex() {
        int index;
        int count = 0;
        do {
            index = (int) (Math.random() * 9);
            count++;
            if (count > 50) break;
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
            } else if (grid[winPosition[0]] == 2 && grid[winPosition[1]] == 2 && grid[winPosition[0]] == 0) {
                return winPosition[0];
            }
        }
        return index;
    }

    @Override
    public void onDestroy() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
        super.onDestroy();
    }
}
