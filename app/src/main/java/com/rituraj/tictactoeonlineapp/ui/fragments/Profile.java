package com.rituraj.tictactoeonlineapp.ui.fragments;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.rituraj.tictactoeonlineapp.R;
import com.rituraj.tictactoeonlineapp.models.User;
import com.rituraj.tictactoeonlineapp.ui.auth.LoginPage;

public class Profile extends Fragment {

    private FirebaseAuth auth;
    private FirebaseDatabase database;
    private DatabaseReference userRef;
    private ValueEventListener userListener;
    private GoogleSignInClient googleSignInClient;

    // Model Fields
    private String userId;
    private String userName;
    private String email;
    private String profileUrl;
    private boolean isOnline = false;

    // Views
    private ImageView profileImg;
    private ImageView statusIndicator;
    private ImageView statusDot;
    private TextView profileUserName;
    private TextView profileEmail;
    private TextView profileUserId;
    private TextView profileStatusText;
    private MaterialButton signOutBtn;
    private LinearLayout btnCopyUserId;

    public static Profile newInstance() {
        return new Profile();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);
        initializeViews(view);
        setupAuthAndDatabase();
        return view;
    }

    private void initializeViews(View view) {
        signOutBtn = view.findViewById(R.id.signOutBtn);
        profileImg = view.findViewById(R.id.profileImg);
        statusIndicator = view.findViewById(R.id.statusIndicator);
        statusDot = view.findViewById(R.id.statusDot);
        profileUserName = view.findViewById(R.id.profileUserName);
        profileEmail = view.findViewById(R.id.profileEmail);
        profileUserId = view.findViewById(R.id.profileUserId);
        profileStatusText = view.findViewById(R.id.profileStatusText);
        btnCopyUserId = view.findViewById(R.id.btnCopyUserId);

        if (btnCopyUserId != null) {
            btnCopyUserId.setOnClickListener(v -> copyUserIdToClipboard());
        }
        if (signOutBtn != null) {
            signOutBtn.setOnClickListener(v -> confirmAndSignOut());
        }
    }

    private void setupAuthAndDatabase() {
        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();

        try {
            GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestIdToken(getString(R.string.default_web_client_id))
                    .requestEmail()
                    .build();
            googleSignInClient = GoogleSignIn.getClient(requireContext(), gso);
        } catch (Exception e) {
            e.printStackTrace();
        }

        FirebaseUser firebaseUser = auth.getCurrentUser();
        if (firebaseUser != null) {
            userId = firebaseUser.getUid();
            userName = firebaseUser.getDisplayName() != null ? firebaseUser.getDisplayName() : "Guest Player";
            email = firebaseUser.getEmail() != null ? firebaseUser.getEmail() : "No email linked";
            profileUrl = firebaseUser.getPhotoUrl() != null ? firebaseUser.getPhotoUrl().toString() : null;
            isOnline = true;

            updateUI(userId, userName, email, profileUrl, isOnline);

            userRef = database.getReference("Users").child(userId);
            userListener = new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (isAdded() && snapshot.exists()) {
                        User userModel = snapshot.getValue(User.class);
                        if (userModel != null) {
                            userId = userModel.getUserId() != null ? userModel.getUserId() : userId;
                            userName = userModel.getUserName() != null ? userModel.getUserName() : userName;
                            email = userModel.getEmail() != null ? userModel.getEmail() : email;
                            profileUrl = userModel.getProfileUrl() != null ? userModel.getProfileUrl() : profileUrl;
                            isOnline = userModel.isOnline();

                            updateUI(userId, userName, email, profileUrl, isOnline);
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    // Ignored gracefully
                }
            };
            userRef.addValueEventListener(userListener);

        } else {
            isOnline = false;
            updateUI("---", "Guest", "Not logged in", null, false);
        }
    }

    private void updateUI(String id, String name, String mail, String url, boolean online) {
        if (!isAdded()) return;

        if (profileUserName != null) profileUserName.setText(name);
        if (profileEmail != null) profileEmail.setText(mail);
        if (profileUserId != null) profileUserId.setText(id);

        if (profileStatusText != null) {
            if (online) {
                profileStatusText.setText("Online & Ready");
                profileStatusText.setTextColor(getResources().getColor(R.color.success, null));
                if (statusIndicator != null) statusIndicator.setVisibility(View.VISIBLE);
                if (statusDot != null) statusDot.setVisibility(View.VISIBLE);
            } else {
                profileStatusText.setText("Offline");
                profileStatusText.setTextColor(getResources().getColor(R.color.gray_600, null));
                if (statusIndicator != null) statusIndicator.setVisibility(View.GONE);
                if (statusDot != null) statusDot.setVisibility(View.GONE);
            }
        }

        if (profileImg != null) {
            if (url != null && !url.isEmpty()) {
                Glide.with(requireContext())
                        .load(url)
                        .placeholder(R.drawable.user)
                        .error(R.drawable.user)
                        .circleCrop()
                        .into(profileImg);
            } else {
                profileImg.setImageResource(R.drawable.user);
            }
        }
    }

    private void copyUserIdToClipboard() {
        if (userId != null && !userId.isEmpty() && !userId.equals("---") && getContext() != null) {
            ClipboardManager clipboard = (ClipboardManager) requireContext().getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText("Player ID", userId);
            if (clipboard != null) {
                clipboard.setPrimaryClip(clip);
                Toast.makeText(requireContext(), "Player ID copied to clipboard!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void confirmAndSignOut() {
        Context context = getContext() != null ? getContext() : (getActivity() != null ? getActivity() : null);
        if (context == null) {
            executeSignOut();
            return;
        }

        try {
            View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_confirm_logout, null);
            AlertDialog dialog = new AlertDialog.Builder(context)
                    .setView(dialogView)
                    .setCancelable(true)
                    .create();

            if (dialog.getWindow() != null) {
                dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
            }

            View btnConfirm = dialogView.findViewById(R.id.btnConfirmSignOut);
            View btnCancel = dialogView.findViewById(R.id.btnCancelSignOut);

            if (btnConfirm != null) {
                btnConfirm.setOnClickListener(v -> {
                    dialog.dismiss();
                    executeSignOut();
                });
            } else {
                executeSignOut();
            }

            if (btnCancel != null) {
                btnCancel.setOnClickListener(v -> dialog.dismiss());
            }

            dialog.show();
        } catch (Exception e) {
            e.printStackTrace();
            executeSignOut();
        }
    }

    private void executeSignOut() {
        Context context = getContext() != null ? getContext() : (getActivity() != null ? getActivity() : null);

        // 1. Update online status in Realtime DB if logged in
        try {
            if (userRef != null) {
                userRef.child("isOnline").setValue(false);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // 2. Sign out Firebase Auth
        try {
            FirebaseAuth.getInstance().signOut();
        } catch (Exception e) {
            e.printStackTrace();
        }

        // 3. Sign out Google Sign-In Client
        try {
            if (context != null) {
                GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                        .requestIdToken(getString(R.string.default_web_client_id))
                        .requestEmail()
                        .build();
                GoogleSignIn.getClient(context, gso).signOut().addOnCompleteListener(task -> {
                    performNavigationToLogin();
                });
            } else {
                performNavigationToLogin();
            }
        } catch (Exception e) {
            e.printStackTrace();
            performNavigationToLogin();
        }
    }

    private void performNavigationToLogin() {
        if (getActivity() != null) {
            Toast.makeText(getActivity(), "Logged out successfully", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(getActivity(), LoginPage.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            getActivity().finish();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (userRef != null && userListener != null) {
            userRef.removeEventListener(userListener);
        }
    }

    // Getters and Setters
    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getProfileUrl() {
        return profileUrl;
    }

    public void setProfileUrl(String profileUrl) {
        this.profileUrl = profileUrl;
    }

    public boolean isOnline() {
        return isOnline;
    }

    public void setOnline(boolean online) {
        isOnline = online;
    }
}
