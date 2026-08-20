package com.rituraj.tictactoeonlineapp.ui.games;

import android.content.DialogInterface;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.rituraj.tictactoeonlineapp.MainActivity;
import com.rituraj.tictactoeonlineapp.R;
import com.rituraj.tictactoeonlineapp.ads.MyInterstitialAds;
import com.rituraj.tictactoeonlineapp.appPrefs.SaveInDevice;
import com.rituraj.tictactoeonlineapp.models.TicTacToeAI;

public class AdvanceAiActivity extends AppCompatActivity {
    private char[][] board = {
            {'_', '_', '_'},
            {'_', '_', '_'},
            {'_', '_', '_'}
    };
    private ImageView[][] buttons = new ImageView[3][3];
    private TicTacToeAI ai = new TicTacToeAI();
    private boolean gameOver = false, playerMove = true;
    private CountDownTimer countDownTimer;
    private ProgressBar playWithAiTimeline, playWithAiPlayerTimeline;
    private MyInterstitialAds myInterstitialAds;
    private AdView adView1, adView2;
    private FirebaseAuth auth;
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
        initView();
        interstitialAd();
        googleAD();
        fun();
        firstMoveSelection();
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
        String adUintId = getResources().getString(R.string.advance_ai_ad);
        myInterstitialAds = new MyInterstitialAds(AdvanceAiActivity.this, AdvanceAiActivity.this, adUintId);
    }

    private void firstMoveSelection() {
        LayoutInflater inflater = LayoutInflater.from(this);
        View view = inflater.inflate(R.layout.dialog_choice, null);

        AlertDialog.Builder builder = new AlertDialog.Builder(AdvanceAiActivity.this);
        builder.setView(view);
        AlertDialog dialog = builder.create();
        dialog.setCancelable(false);
        dialog.show();
        Button btnUser = view.findViewById(R.id.btnUser);
        Button btnAI = view.findViewById(R.id.btnAI);
        btnUser.setOnClickListener(v -> {
            playerMove = true;
            timingSetting();
            dialog.dismiss();
        });
        btnAI.setOnClickListener(v -> {
            playerMove = false;
            timingSetting();
            aiMove();
            dialog.dismiss();
        });
    }

    private void timingSetting() {
        int totalMillis = 30 * 1000;
        countDownTimer = new CountDownTimer(totalMillis, 100) {
            @Override
            public void onTick(long millisUntilFinished) {
                int progress = (int) ((millisUntilFinished * 100) / 30000);
                if (playerMove) playWithAiPlayerTimeline.setProgress(progress);
                else playWithAiTimeline.setProgress(progress);
            }

            @Override
            public void onFinish() {
                if (playerMove)
                    new Handler(Looper.getMainLooper()).postDelayed(AdvanceAiActivity.this::aiMove, 500);
                playWithAiPlayerTimeline.setProgress(0);
                playWithAiTimeline.setProgress(0);
                restartTimer();
            }
        }.start();
    }

    private void restartTimer() {
        countDownTimer.cancel();
        timingSetting();
    }

    private void initView() {
        playWithAiPlayerTimeline = findViewById(R.id.playWithAiPlayerTimeline);
        playWithAiTimeline = findViewById(R.id.playWithAiTimeline);

        buttons[0][0] = findViewById(R.id.botPlayerTap0);
        buttons[0][1] = findViewById(R.id.botPlayerTap1);
        buttons[0][2] = findViewById(R.id.botPlayerTap2);

        buttons[1][0] = findViewById(R.id.botPlayerTap3);
        buttons[1][1] = findViewById(R.id.botPlayerTap4);
        buttons[1][2] = findViewById(R.id.botPlayerTap5);

        buttons[2][0] = findViewById(R.id.botPlayerTap6);
        buttons[2][1] = findViewById(R.id.botPlayerTap7);
        buttons[2][2] = findViewById(R.id.botPlayerTap8);
    }

    private void fun() {
        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        if (user != null) {
            Button aiCurrentUsername = findViewById(R.id.aiCurrentUsername);
            aiCurrentUsername.setText(user.getDisplayName());
        }

        buttons[0][0].setOnClickListener(v -> onPlayerMove(0, 0));
        buttons[0][1].setOnClickListener(v -> onPlayerMove(0, 1));
        buttons[0][2].setOnClickListener(v -> onPlayerMove(0, 2));

        buttons[1][0].setOnClickListener(v -> onPlayerMove(1, 0));
        buttons[1][1].setOnClickListener(v -> onPlayerMove(1, 1));
        buttons[1][2].setOnClickListener(v -> onPlayerMove(1, 2));

        buttons[2][0].setOnClickListener(v -> onPlayerMove(2, 0));
        buttons[2][1].setOnClickListener(v -> onPlayerMove(2, 1));
        buttons[2][2].setOnClickListener(v -> onPlayerMove(2, 2));
    }

    private void onPlayerMove(int row, int col) {
        if (gameOver || board[row][col] != '_' || !playerMove)
            return;

        // Player move
        playerMove = false;
        board[row][col] = 'X';
        playWithAiPlayerTimeline.setProgress(0);
        buttons[row][col].setTranslationY(-1000f);
        buttons[row][col].setImageResource(R.drawable.x);
        buttons[row][col].animate().translationYBy(1000f).setDuration(300);

        if (checkWinner('X')) {
            gameOver = true;
            playerMove = true;
            Toast.makeText(this, "Player wins!", Toast.LENGTH_SHORT).show();
            showGameOverDialog("You has won🏆\nand Bot has loss!");
            return;
        }

        if (isBoardFull()) {
            gameOver = true;
            Toast.makeText(this, "Draw!", Toast.LENGTH_SHORT).show();
            showGameOverDialog("It's a Tie! 🤝 Excellent Performance! 🎉");
            return;
        }

        new Handler(Looper.getMainLooper()).postDelayed(AdvanceAiActivity.this::aiMove, 500);
    }

    private void aiMove() {
        // AI move
        if (isBoardEmpty()) {
            int row = (int) (Math.random() * 3);
            int col = (int) (Math.random() * 3);
            playerMove = true;
            playWithAiTimeline.setProgress(0);
            board[row][col] = 'O';
            buttons[row][col].setTranslationY(-1000f);
            buttons[row][col].setImageResource(R.drawable.o);
            buttons[row][col].animate().translationYBy(1000f).setDuration(300);
            return;
        }
        TicTacToeAI.Move bestMove = ai.findBestMove(board);

        if (bestMove.row != -1 && bestMove.col != -1) {
            playerMove = true;
            playWithAiTimeline.setProgress(0);
            board[bestMove.row][bestMove.col] = 'O';
            buttons[bestMove.row][bestMove.col].setTranslationY(-1000f);
            buttons[bestMove.row][bestMove.col].setImageResource(R.drawable.o);
            buttons[bestMove.row][bestMove.col].animate().translationYBy(1000f).setDuration(300);
        }

        if (checkWinner('O')) {
            gameOver = true;
            showGameOverDialog("AI Wins! 🏆\nYou — Keep Practicing! 💪");
            Toast.makeText(this, "AI wins!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (isBoardFull()) {
            gameOver = true;
            Toast.makeText(this, "Draw!", Toast.LENGTH_SHORT).show();
            showGameOverDialog("It's a Tie! 🤝 Excellent Performance! 🎉");
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
        if (board[0][2] == player && board[1][1] == player && board[2][0] == player)
            return true;
        return false;
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
        countDownTimer.cancel();
        AlertDialog.Builder builder = new AlertDialog.Builder(AdvanceAiActivity.this);
        builder.setTitle("Game Over");
        builder.setMessage(resultMessage + "\n\n" + "Do you want to start a new game?");
        builder.setPositiveButton("New Game", (dialog, which) -> {
            Toast.makeText(AdvanceAiActivity.this, "New Game", Toast.LENGTH_SHORT).show();
            dialog.dismiss();
            resetGame();
        });
        builder.setNegativeButton("Exit", (dialog, which) -> {
            dialog.dismiss();
            finish();
        });
        builder.setCancelable(false);
        builder.show();
    }

    private void resetGame() {
        if (myInterstitialAds != null) myInterstitialAds.showInterstitialAd();
        playerMove = true;
        gameOver = false;
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                board[i][j] = '_';
                buttons[i][j].setImageResource(0);
            }
        }
        restartTimer();
    }

    @Override
    public void onDestroy() {
        if (countDownTimer != null) countDownTimer.cancel();
        super.onDestroy();
    }
}