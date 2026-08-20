package com.rituraj.tictactoeonlineapp.ui.auth;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AbsListView;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ListAdapter;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.games.GamesSignInClient;
import com.google.android.gms.games.PlayGames;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.FirebaseDatabase;
import com.rituraj.tictactoeonlineapp.MainActivity;
import com.rituraj.tictactoeonlineapp.R;
import com.rituraj.tictactoeonlineapp.databinding.ActivityLoginPageBinding;
import com.rituraj.tictactoeonlineapp.models.User;

public class LoginPage extends AppCompatActivity {
    private ActivityLoginPageBinding binding;
    private FirebaseAuth auth;
    private FirebaseDatabase database;
    private ProgressDialog progressDialog;
    private GoogleSignInClient googleSignInClient;
    private boolean isConnect = false;
    private final int RC_SIGN_IN = 80;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityLoginPageBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        fun();
    }

    public void fun() {
        database = FirebaseDatabase.getInstance();
        auth = FirebaseAuth.getInstance();


        progressDialog = new ProgressDialog(LoginPage.this);
        progressDialog.setTitle("Login");
        progressDialog.setMessage("Login to your Account");
        progressDialog.setCancelable(false);
        binding.checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(@NonNull CompoundButton compoundButton, boolean b) {
                isConnect = b;
                if (b)
                    Toast.makeText(LoginPage.this, "Connect", Toast.LENGTH_SHORT).show();
                else
                    Toast.makeText(LoginPage.this, "Disconnect", Toast.LENGTH_SHORT).show();
            }
        });

        binding.loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressDialog.show();
                signInSetting();
            }
        });

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        googleSignInClient = GoogleSignIn.getClient(this, gso);

        googleSignInClient.signOut();

        if (auth.getCurrentUser() != null) {
            startActivity(new Intent(LoginPage.this, MainActivity.class));
            finish();
        }
    }

    public void signInSetting() {
        Intent singnIntent = googleSignInClient.getSignInIntent();
        startActivityForResult(singnIntent, RC_SIGN_IN);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (RC_SIGN_IN == requestCode) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                if (account != null)
                    firebaseAuthWithGoogle(account.getIdToken());
            } catch (Exception e) {
                Toast.makeText(LoginPage.this, "SignIn Failed", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        auth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (!task.isSuccessful()) {
                        Toast.makeText(LoginPage.this, "Login Failed!", Toast.LENGTH_SHORT).show();
                        progressDialog.dismiss();
                        return;
                    }
                    FirebaseUser user = auth.getCurrentUser();
                    if (user == null) {
                        Toast.makeText(LoginPage.this, "User not found", Toast.LENGTH_SHORT).show();
                        progressDialog.dismiss();
                        return;
                    }
                    User user1 = new User();
                    user1.setUserId(user.getUid());
                    user1.setEmail(user.getEmail());
                    user1.setUserName(user.getDisplayName());
                    user1.setOnline(true);
                    if (user.getPhotoUrl() != null)
                        user1.setProfileUrl(user.getPhotoUrl().toString());
                    else
                        user1.setProfileUrl("");
                    database.getReference().child("Users").child(user.getUid()).setValue(user1);
                    if (isConnect) {
                        GamesSignInClient signInClient = PlayGames.getGamesSignInClient(LoginPage.this);
                        signInClient.signIn().addOnCompleteListener(taskPlay -> {
                            progressDialog.dismiss();
                            if (taskPlay.isSuccessful() && taskPlay.getResult() != null && taskPlay.getResult().isAuthenticated()) {
                                Toast.makeText(LoginPage.this, "Play Games connected", Toast.LENGTH_SHORT).show();
                                startActivity(new Intent(LoginPage.this, MainActivity.class));
                                finish();
                            } else
                                Toast.makeText(LoginPage.this, "Play Games sign-in failed", Toast.LENGTH_SHORT).show();
                        });
                    } else {
                        startActivity(new Intent(LoginPage.this, MainActivity.class));
                        finish();
                    }
                });
    }
}
