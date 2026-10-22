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
import com.rituraj.tictactoeonlineapp.databinding.ActivityClassicModeBinding;
import com.rituraj.tictactoeonlineapp.models.Player;

import java.util.Arrays;

public class ClassicModeActivity extends AppCompatActivity {
    private ActivityClassicModeBinding binding;
    private int activePlayer = 0; // 0 = Player 1 (O), 1 = Player 2 (X)
    private Boolean gameActive = true;
    private CountDownTimer countDownTimer;
    private final int[] grid = {2, 2, 2, 2, 2, 2, 2, 2, 2};
    private final int[][] winPositions = {
            {0, 1, 2}, {3, 4, 5}, {6, 7, 8},
            {0, 3, 6}, {1, 4, 7}, {2, 5, 8},
            {0, 4, 8}, {2, 4, 6}
    };
    private long scoreP1 = 0, scoreP2 = 0, noGame = 0;
    private ImageView[] cellViews;

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
                    binding.offlinePlayer1Timeline.setProgress(progress);
                    binding.offlinePlayer2Timeline.setProgress(0);
                } else {
                    binding.offlinePlayer2Timeline.setProgress(progress);
                    binding.offlinePlayer1Timeline.setProgress(0);
                }
            }

            @Override
            public void onFinish() {
                if (!gameActive) return;
                activePlayer = activePlayer == 1 ? 0 : 1;
                updateScoreBoard();
                restartTimer();
            }
        }.start();
    }

    private void restartTimer() {
        if (countDownTimer != null) countDownTimer.cancel();
        timingSetting();
    }

    public void fun() {
        updateScoreBoard();

        cellViews = new ImageView[]{
                binding.playerTap0, binding.playerTap1, binding.playerTap2,
                binding.playerTap3, binding.playerTap4, binding.playerTap5,
                binding.playerTap6, binding.playerTap7, binding.playerTap8
        };

        View.OnClickListener listener = this::playerTap;
        for (ImageView cell : cellViews) {
            if (cell != null) {
                cell.setOnClickListener(listener);
            }
        }
    }

    private void updateScoreBoard() {
        if (binding.tvPlayer1Wins != null) {
            binding.tvPlayer1Wins.setText("Wins: " + scoreP1);
        }
        if (binding.tvPlayer2Wins != null) {
            binding.tvPlayer2Wins.setText("Wins: " + scoreP2);
        }

        if (activePlayer == 0) {
            if (binding.cardPlayer1 != null) {
                binding.cardPlayer1.setCardElevation(16f);
                binding.cardPlayer1.setAlpha(1.0f);
            }
            if (binding.cardPlayer2 != null) {
                binding.cardPlayer2.setCardElevation(4f);
                binding.cardPlayer2.setAlpha(0.65f);
            }
        } else {
            if (binding.cardPlayer2 != null) {
                binding.cardPlayer2.setCardElevation(16f);
                binding.cardPlayer2.setAlpha(1.0f);
            }
            if (binding.cardPlayer1 != null) {
                binding.cardPlayer1.setCardElevation(4f);
                binding.cardPlayer1.setAlpha(0.65f);
            }
        }
    }

    private void startNewGame() {
        gameActive = true;
        activePlayer = 0;
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

        binding.offlinePlayer1Timeline.setProgress(0);
        binding.offlinePlayer2Timeline.setProgress(0);

        updateScoreBoard();
        restartTimer();
        Toast.makeText(this, "Match Restarted!", Toast.LENGTH_SHORT).show();
    }

    private void updatePlayerDataInFirebase(boolean isPlayerWin) {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) return;

        String uid = currentUser.getUid();
        DatabaseReference playerRef = FirebaseDatabase.getInstance().getReference("Players").child(uid);

        playerRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Player player = snapshot.getValue(Player.class);
                if (player == null) {
                    player = new Player(uid, currentUser.getEmail(), currentUser.getDisplayName(), "100");
                }

                int games = player.getGameCount() + 1;
                int wins = player.getWinCount() + (isPlayerWin ? 1 : 0);
                int level = games / 10;
                int rank = wins * 10;

                player.setPlayerId(uid);
                if (currentUser.getDisplayName() != null && !currentUser.getDisplayName().isEmpty()) {
                    player.setName(currentUser.getDisplayName());
                }
                if (currentUser.getEmail() != null && !currentUser.getEmail().isEmpty()) {
                    player.setEmail(currentUser.getEmail());
                }

                player.setGameCount(games);
                player.setWinCount(wins);
                player.setLevel(level);
                player.setRank(rank);

                playerRef.setValue(player);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    private void showGameOverDialog(String resultMessage) {
        if (isFinishing() || isDestroyed()) return;

        noGame++;
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
                        scoreP1++;
                        updateScoreBoard();
                        updatePlayerDataInFirebase(true);
                        showGameOverDialog("Player 1 (O) Has Won! 🏆\nPlayer 2 (X) Defeated.");
                    } else {
                        scoreP2++;
                        updateScoreBoard();
                        updatePlayerDataInFirebase(false);
                        showGameOverDialog("Player 2 (X) Has Won! 🏆\nPlayer 1 (O) Defeated.");
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
                showGameOverDialog("It's a Tie! 🤝 Excellent Match! 🎉");
            }, 1200);
            gameActive = false;
            return;
        }

        updateScoreBoard();
        restartTimer();
    }

    public void playerTap(View v) {
        if (gameActive && v instanceof ImageView) {
            ImageView img = (ImageView) v;
            if (img.getTag() == null) return;

            int tappedImg;
            try {
                tappedImg = Integer.parseInt(img.getTag().toString());
            } catch (NumberFormatException e) {
                return;
            }

            if (grid[tappedImg] == 2) {
                grid[tappedImg] = activePlayer;

                if (activePlayer == 1) {
                    animateCellPlacement(img, R.drawable.x);
                    activePlayer = 0;
                } else {
                    animateCellPlacement(img, R.drawable.o);
                    activePlayer = 1;
                }

                winCondition();
            }
        }
    }

    @Override
    public void onDestroy() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
        super.onDestroy();
    }
}
