package com.rituraj.tictactoeonlineapp.ui.auth;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
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
    private static final String TAG = "LoginPage";
    private ActivityLoginPageBinding binding;
    private FirebaseAuth auth;
    private FirebaseDatabase database;
    private ProgressDialog progressDialog;
    private GoogleSignInClient googleSignInClient;
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

        database = FirebaseDatabase.getInstance();
        auth = FirebaseAuth.getInstance();

        if (auth.getCurrentUser() != null) {
            startActivity(new Intent(LoginPage.this, MainActivity.class));
            finish();
            return;
        }

        fun();
    }

    public void fun() {
        startAnimations();

        progressDialog = new ProgressDialog(LoginPage.this);
        progressDialog.setTitle("Signing In");
        progressDialog.setMessage("Authenticating account, please wait...");
        progressDialog.setCancelable(false);

        if (binding.loginBtn != null) {
            binding.loginBtn.setOnClickListener(v -> {
                if (!isNetworkAvailable()) {
                    Toast.makeText(this, "No Internet Connection! Please check your network.", Toast.LENGTH_LONG).show();
                    return;
                }
                if (!isFinishing()) {
                    progressDialog.setMessage("Authenticating with Google...");
                    progressDialog.show();
                }
                signInSetting();
            });
        }

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        googleSignInClient = GoogleSignIn.getClient(this, gso);
    }

    private boolean isNetworkAvailable() {
        try {
            ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
            if (cm != null) {
                NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
                return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error checking network connectivity", e);
        }
        return true;
    }

    private void startAnimations() {
        try {
            Animation pulseAnim = AnimationUtils.loadAnimation(this, R.anim.logo_pulse);
            Animation floatAnim = AnimationUtils.loadAnimation(this, R.anim.logo_float);
            Animation ringAnim = AnimationUtils.loadAnimation(this, R.anim.splash_ring_expand);
            Animation rotateAnim = AnimationUtils.loadAnimation(this, R.anim.splash_rotate);
            Animation floatLeft = AnimationUtils.loadAnimation(this, R.anim.float_sway_left);
            Animation floatRight = AnimationUtils.loadAnimation(this, R.anim.float_sway_right);

            if (binding.splashGlowRing != null) {
                binding.splashGlowRing.startAnimation(ringAnim);
            }

            if (binding.decorativeCircle1 != null) {
                binding.decorativeCircle1.startAnimation(rotateAnim);
            }

            if (binding.decorativeCircle2 != null) {
                binding.decorativeCircle2.startAnimation(rotateAnim);
            }

            if (binding.floatingX != null) {
                binding.floatingX.startAnimation(floatLeft);
            }

            if (binding.floatingO != null) {
                binding.floatingO.startAnimation(floatRight);
            }

            pulseAnim.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {
                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    if (binding != null && binding.logoCard != null) {
                        binding.logoCard.startAnimation(floatAnim);
                    }
                }

                @Override
                public void onAnimationRepeat(Animation animation) {
                }
            });

            if (binding != null && binding.logoCard != null) {
                binding.logoCard.startAnimation(pulseAnim);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error animating login page views", e);
        }
    }

    public void signInSetting() {
        if (!isNetworkAvailable()) {
            dismissProgress();
            Toast.makeText(this, "No Internet Connection! Please check your network.", Toast.LENGTH_LONG).show();
            return;
        }

        if (googleSignInClient != null) {
            googleSignInClient.signOut().addOnCompleteListener(task -> {
                Intent signInIntent = googleSignInClient.getSignInIntent();
                startActivityForResult(signInIntent, RC_SIGN_IN);
            });
        } else {
            dismissProgress();
            Toast.makeText(this, "Google Sign-In not initialized", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (RC_SIGN_IN == requestCode) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                if (account != null && account.getIdToken() != null) {
                    firebaseAuthWithGoogle(account.getIdToken());
                } else {
                    dismissProgress();
                    Toast.makeText(LoginPage.this, "Sign-in cancelled", Toast.LENGTH_SHORT).show();
                }
            } catch (ApiException e) {
                dismissProgress();
                Log.e(TAG, "Google Sign-In ApiException: Code = " + e.getStatusCode(), e);
                int code = e.getStatusCode();
                if (code == 7) {
                    Toast.makeText(LoginPage.this, "Network Error (Code 7): Please check your internet connection and try again.", Toast.LENGTH_LONG).show();
                } else if (code == 12501) {
                    Toast.makeText(LoginPage.this, "Sign-in cancelled", Toast.LENGTH_SHORT).show();
                } else if (code == 12500) {
                    Toast.makeText(LoginPage.this, "Google Play Services Error (Code 12500). Please update Play Services.", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(LoginPage.this, "Sign-In Error (Code " + code + "): Please check network/Play Services.", Toast.LENGTH_LONG).show();
                }
            } catch (Exception e) {
                dismissProgress();
                Log.e(TAG, "Google Sign-In Error", e);
                Toast.makeText(LoginPage.this, "Sign-In Failed", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        auth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (!task.isSuccessful()) {
                        dismissProgress();
                        Toast.makeText(LoginPage.this, "Firebase Authentication Failed!", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    FirebaseUser user = auth.getCurrentUser();
                    if (user == null) {
                        dismissProgress();
                        Toast.makeText(LoginPage.this, "User profile not found", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    User user1 = new User();
                    user1.setUserId(user.getUid());
                    user1.setEmail(user.getEmail());
                    user1.setUserName(user.getDisplayName() != null ? user.getDisplayName() : "Player");
                    user1.setOnline(true);
                    if (user.getPhotoUrl() != null) {
                        user1.setProfileUrl(user.getPhotoUrl().toString());
                    } else {
                        user1.setProfileUrl("");
                    }

                    database.getReference().child("Users").child(user.getUid()).setValue(user1);

                    dismissProgress();
                    Toast.makeText(LoginPage.this, "Welcome " + (user.getDisplayName() != null ? user.getDisplayName() : "Player") + "!", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(LoginPage.this, MainActivity.class));
                    finish();
                });
    }

    private void dismissProgress() {
        if (!isFinishing() && progressDialog != null && progressDialog.isShowing()) {
            try {
                progressDialog.dismiss();
            } catch (Exception e) {
                Log.e(TAG, "Error dismissing progress dialog", e);
            }
        }
    }
}
