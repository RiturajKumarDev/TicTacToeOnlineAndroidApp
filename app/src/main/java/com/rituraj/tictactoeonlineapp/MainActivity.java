package com.rituraj.tictactoeonlineapp;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.games.PlayGames;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.rituraj.tictactoeonlineapp.databinding.ActivityMainBinding;
import com.rituraj.tictactoeonlineapp.models.Player;
import com.rituraj.tictactoeonlineapp.models.User;
import com.rituraj.tictactoeonlineapp.ui.auth.LoginPage;
import com.rituraj.tictactoeonlineapp.ui.games.OnlineModeActivity;

public class MainActivity extends AppCompatActivity {
    private ActivityMainBinding binding;
    private Menu topMenu;
    private FirebaseAuth auth;
    private FirebaseDatabase database;
    private DatabaseReference reference, coinReference;
    private ValueEventListener coinsListener;
    private FirebaseUser user;
    public static Player player;
    private AdView adView;
    private static final int NOTIFICATION_PERMISSION_CODE = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

        Toolbar toolbar = findViewById(R.id.main_top_toolbar);
        setSupportActionBar(toolbar);

        BottomNavigationView navView = findViewById(R.id.nav_view);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(R.id.navigation_home, R.id.navigation_invites, R.id.navigation_multiplayer, R.id.navigation_profile).build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_main);
        NavigationUI.setupWithNavController(binding.navView, navController);

        ViewCompat.setOnApplyWindowInsetsListener(toolbar, (v, insets) -> {
            Insets statusBar = insets.getInsets(WindowInsetsCompat.Type.statusBars());
            v.setPadding(0, statusBar.top, 0, 0);
            return insets;
        });

        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        TopToolBarSetting();
    }

    private void init() {
        database = FirebaseDatabase.getInstance();
        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        if (user == null) {
            startActivity(new Intent(MainActivity.this, LoginPage.class));
            finish();
            return;
        }
        setOnline();
        fun();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.top_nav_menu, menu);
        topMenu = menu;

        MenuItem item = menu.findItem(R.id.top_no_of_coins);
        View actionView = item.getActionView();

        TextView coinsText = actionView.findViewById(R.id.coins_text);
        setCoins(coinsText);

        return true;
    }

    private void getPlayer(TextView coinsText) {
        reference = database.getReference("Players").child(user.getUid());
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()) {
                    binding.redeemLayoutBox.setVisibility(View.VISIBLE);
                    createNewPlayer();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(MainActivity.this, "Something went wrong!", Toast.LENGTH_SHORT).show();
            }
        });

        coinReference = database.getReference("Players").child(user.getUid());
        coinsListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                player = snapshot.getValue(Player.class);
                if (player != null) {
                    player.setPlayerId(snapshot.getKey());
                    coinsText.setText(String.valueOf(player.getCoins()));
                    int coins = Integer.parseInt(player.getCoins());
                    submitLeaderboardCoins(coins);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(MainActivity.this, "Something went wrong!", Toast.LENGTH_SHORT).show();
            }
        };
        coinReference.addValueEventListener(coinsListener);
    }

    private void submitLeaderboardCoins(long score) {
        PlayGames.getGamesSignInClient(this)
                .isAuthenticated()
                .addOnCompleteListener(task -> {
                    boolean authenticated = task.isSuccessful() && task.getResult() != null && task.getResult().isAuthenticated();
                    if (authenticated) {
                        PlayGames.getLeaderboardsClient(this)
                                .submitScore("CgkIyqmG5tAVEAIQBA", score);
                    } else {
                        Toast.makeText(this, "Play Games not logged in", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void setCoins(TextView coinsText) {
        init();
        if (user != null) getPlayer(coinsText);
    }

    private void createNewPlayer() {
        database.getReference("Players").child(user.getUid()).setValue(new Player(user.getUid(), user.getDisplayName(), user.getEmail(), "100"));
    }

    private void TopToolBarSetting() {
        Toolbar mainToolBar = findViewById(R.id.main_top_toolbar);
        setSupportActionBar(mainToolBar);

        if (getSupportActionBar() != null) {
//            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
//            getSupportActionBar().setHomeAsUpIndicator(R.mipmap.ic_coins_foreground);
        }
    }

    private void fun() {
        googleAD();
        askNotificationPermission();
        getFriendsInvitations();
        redeemSetting();
    }

    private void googleAD() {
        adView = findViewById(R.id.adView_main);

        if (adView != null) {
            MobileAds.initialize(this, initializationStatus -> {
            });
            AdRequest adRequest = new AdRequest.Builder().build();
            adView.loadAd(adRequest);
        }
    }

    private void redeemSetting() {
        binding.vbSubmitRedeemBtn.setOnClickListener((v) -> {
            String redeemCode = binding.etRedeem.getText().toString();
            if (redeemCode.isEmpty()) {
                Toast.makeText(MainActivity.this, "Enter a valid code!", Toast.LENGTH_SHORT).show();
            } else {
                binding.etRedeem.setText("");
                submitRedeemCode(redeemCode);
            }
        });
        binding.viCancleRedeemBox.setOnClickListener((v) -> {
            binding.redeemLayoutBox.setVisibility(View.GONE);
        });
    }

    private void submitRedeemCode(String redeemCode) {
        ProgressDialog progressDialog = new ProgressDialog(MainActivity.this);
        progressDialog.setMessage("Validating code...");
        progressDialog.setCancelable(false);
        progressDialog.show();
        reference = database.getReference("Players").child(redeemCode).child("coins");
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                progressDialog.dismiss();
                if (!snapshot.exists()) {
                    Toast.makeText(MainActivity.this, "Invalid code!", Toast.LENGTH_SHORT).show();
                    return;
                }
                String coins = snapshot.getValue(String.class);
                if (coins == null) {
                    Toast.makeText(MainActivity.this, "Invalid code!", Toast.LENGTH_SHORT).show();
                    return;
                }
                Toast.makeText(MainActivity.this, "Code submitted!", Toast.LENGTH_SHORT).show();
                int coinValue = 0;
                try {
                    coinValue = Integer.parseInt(coins);
                } catch (Exception ignored) {
                }
                String totalCoins = String.valueOf(coinValue + 100);
                database.getReference("Players").child(redeemCode).child("coins").setValue(totalCoins);
                binding.redeemLayoutBox.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                progressDialog.dismiss();
                Toast.makeText(MainActivity.this, "Something went wrong!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void askNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.POST_NOTIFICATIONS}, NOTIFICATION_PERMISSION_CODE);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == NOTIFICATION_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(MainActivity.this, "PERMISSION GRANTED", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(MainActivity.this, "PERMISSION Deny ", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void setOnline() {
        if (user != null) {

            DatabaseReference userStatusRef =
                    database.getReference("Users")
                            .child(user.getUid())
                            .child("isOnline");

            // User online
            userStatusRef.setValue(true);

            // App close / internet disconnect par auto offline
            userStatusRef.onDisconnect().setValue(false);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        setOnline();
    }

    @Override
    protected void onDestroy() {
        if (coinsListener != null && coinReference != null) {
            coinReference.removeEventListener(coinsListener);
        }
        if (user != null)
            database.getReference("Users").child(user.getUid()).child("isOnline").setValue(false);
        super.onDestroy();
    }

    private void getFriendsInvitations() {
        reference = database.getReference("FriendsInvitations").child(user.getUid());
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()) return;
                User user = snapshot.getValue(User.class);
                if (user != null) {
                    notificationSetting(user);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(MainActivity.this, "Something went wrong!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void notificationSetting(User user) {
        final String CHANNEL_ID = "New Invitation";
        final String CHANNEL_NAME = "New Invitation Notifications";
        final int REQUEST_CODE = 100;
        Drawable drawable = ResourcesCompat.getDrawable(getResources(), R.drawable.game_app_logo, null);
        assert drawable != null;
        Bitmap largeIcon = ((BitmapDrawable) drawable).getBitmap();

        Intent intent = new Intent(MainActivity.this, OnlineModeActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra("UserID", user.getUserId());
        PendingIntent pendingIntent = PendingIntent.getActivity(this, REQUEST_CODE, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        Notification newProjectNotification;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            newProjectNotification = new Notification.Builder(this).setSmallIcon(R.drawable.game_app_logo) // app icon
                    .setLargeIcon(largeIcon) // notification icon
                    .setContentTitle("Your Friend has invited you!\nAccept to play.").setContentText(user.getUserName() + ", " + user.getEmail()).setChannelId(CHANNEL_ID).setContentIntent(pendingIntent).setAutoCancel(true).setDefaults(Notification.DEFAULT_ALL).build();
            manager.createNotificationChannel(new NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH));
        } else {
            newProjectNotification = new Notification.Builder(this).setSmallIcon(R.drawable.game_app_logo) // app icon
                    .setLargeIcon(largeIcon).setContentTitle("Your Friend has invited you!\nAccept to play.").setContentText(user.getUserName() + ", " + user.getEmail()).setContentIntent(pendingIntent).setAutoCancel(true).setDefaults(Notification.DEFAULT_ALL).build();
        }
        manager.notify((int) System.currentTimeMillis(), newProjectNotification);
    }
}
