package com.rituraj.tictactoeonlineapp;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.rituraj.tictactoeonlineapp.databinding.ActivityMainBinding;
import com.rituraj.tictactoeonlineapp.models.MyNotification;
import com.rituraj.tictactoeonlineapp.models.Player;
import com.rituraj.tictactoeonlineapp.models.User;
import com.rituraj.tictactoeonlineapp.ui.auth.LoginPage;
import com.rituraj.tictactoeonlineapp.ui.games.OnlineModeActivity;

public class MainActivity extends AppCompatActivity {
    private ActivityMainBinding binding;
    private FirebaseAuth auth;
    private FirebaseDatabase database;
    private DatabaseReference reference;
    private FirebaseUser user;
    public static Player player;
    private static final int NOTIFICATION_PERMISSION_CODE = 100;
    private static final String CHANNEL_ID = "game_invitation_channel";
    private static final String CHANNEL_NAME = "Game Invitations";
    private static final int NOTIFICATION_ID = 1001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

        Toolbar toolbar = findViewById(R.id.main_top_toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.splash_background_start));
        }

        BottomNavigationView navView = findViewById(R.id.nav_view);
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(R.id.navigation_home, R.id.navigation_invites, R.id.navigation_multiplayer, R.id.navigation_profile).build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_main);
        NavigationUI.setupWithNavController(binding.navView, navController);

        ViewCompat.setOnApplyWindowInsetsListener(toolbar, (v, insets) -> {
            Insets statusBar = insets.getInsets(WindowInsetsCompat.Type.statusBars());
            v.setPadding(0, statusBar.top, 0, 0);
            return insets;
        });

        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        init();
        handleNotificationAction(getIntent());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        handleNotificationAction(intent);
    }

    private void handleNotificationAction(Intent intent) {
        if (intent == null || intent.getAction() == null) return;

        if ("ACTION_DECLINE_INVITATION".equals(intent.getAction())) {
            String senderId = intent.getStringExtra("SENDER_ID");
            int notifId = intent.getIntExtra("NOTIFICATION_ID", NOTIFICATION_ID);

            NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            if (manager != null) {
                manager.cancel(notifId);
            }

            if (user != null && database != null) {
                database.getReference("Notifications").child(user.getUid()).removeValue();
                database.getReference("FriendsInvitations").child(user.getUid()).removeValue();
                if (senderId != null) {
                    database.getReference("FriendsInvitations").child(senderId).removeValue();
                }
            }

            Toast.makeText(this, "Match Challenge Declined ❌", Toast.LENGTH_SHORT).show();
        }
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

    private void fun() {
        askNotificationPermission();
        getFriendsInvitations();
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
                Toast.makeText(MainActivity.this, "Permission Granted", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(MainActivity.this, "Notification Permission Denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void setOnline() {
        if (user != null && database != null) {
            DatabaseReference userStatusRef =
                    database.getReference("Users")
                            .child(user.getUid())
                            .child("isOnline");
            userStatusRef.setValue(true);
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
        if (user != null && database != null) {
            database.getReference("Users").child(user.getUid()).child("isOnline").setValue(false);
        }
        super.onDestroy();
    }

    private void getFriendsInvitations() {
        if (user == null || database == null) return;
        reference = database.getReference("Notifications").child(user.getUid());
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()) return;
                MyNotification myNotification = snapshot.getValue(MyNotification.class);
                if (myNotification != null) {
                    notificationSetting(myNotification);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    public void notificationSetting(MyNotification myNotification) {
        if (myNotification == null || myNotification.getSenderId() == null) return;

        NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        if (manager == null) return;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription("Notifications for incoming online Tic Tac Toe game challenges");
            channel.enableVibration(true);
            channel.setShowBadge(true);
            manager.createNotificationChannel(channel);
        }

        Bitmap largeIcon = null;
        try {
            Drawable drawable = ResourcesCompat.getDrawable(getResources(), R.drawable.game_app_logo, null);
            if (drawable instanceof BitmapDrawable) {
                largeIcon = ((BitmapDrawable) drawable).getBitmap();
            }
        } catch (Exception ignored) {
        }

        String inviterName = myNotification.getSenderName() != null ? myNotification.getSenderName() : "A Player";

        // Accept Action -> Opens OnlineModeActivity
        Intent acceptIntent = new Intent(this, OnlineModeActivity.class);
        acceptIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        acceptIntent.putExtra("GameId", myNotification.getGameId());
        PendingIntent acceptPendingIntent = PendingIntent.getActivity(
                this,
                101,
                acceptIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        // Decline Action -> Handled by MainActivity
        Intent declineIntent = new Intent(this, MainActivity.class);
        declineIntent.setAction("ACTION_DECLINE_INVITATION");
        declineIntent.putExtra("SENDER_ID", myNotification.getSenderId());
        declineIntent.putExtra("NOTIFICATION_ID", NOTIFICATION_ID);
        PendingIntent declinePendingIntent = PendingIntent.getActivity(
                this,
                102,
                declineIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.game_app_logo_cir)
                .setLargeIcon(largeIcon)
                .setContentTitle("⚔️ Match Request From " + inviterName)
                .setContentText("Accept to start a live Tic Tac Toe challenge!")
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText(inviterName + " has challenged you to a live online match! Tap Accept to play now."))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_CALL)
                .setAutoCancel(true)
                .setDefaults(Notification.DEFAULT_ALL)
                .setContentIntent(acceptPendingIntent)
                .addAction(R.drawable.ic_classic_game, "ACCEPT ⚔️", acceptPendingIntent)
                .addAction(android.R.drawable.ic_menu_close_clear_cancel, "DECLINE ❌", declinePendingIntent);

        manager.notify(NOTIFICATION_ID, builder.build());
    }
}
