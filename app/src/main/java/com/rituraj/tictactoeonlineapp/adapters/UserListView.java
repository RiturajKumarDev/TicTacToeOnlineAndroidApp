package com.rituraj.tictactoeonlineapp.adapters;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.rituraj.tictactoeonlineapp.R;
import com.rituraj.tictactoeonlineapp.models.Game;
import com.rituraj.tictactoeonlineapp.models.MyNotification;
import com.rituraj.tictactoeonlineapp.models.User;
import com.rituraj.tictactoeonlineapp.ui.games.OnlineModeActivity;

import java.util.ArrayList;

public class UserListView extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private final Context context;
    private final ArrayList<User> users;
    private final FirebaseDatabase database;
    private DialogInterface myDialog;
    private final User currUser;
    private ValueEventListener listener;

    public UserListView(Context context, ArrayList<User> users, User currUser) {
        this.context = context;
        this.users = users;
        this.database = FirebaseDatabase.getInstance();
        this.currUser = currUser;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.user_card_list, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        User user = users.get(position);
        ViewHolder vh = (ViewHolder) holder;

        if (user.getProfileUrl() != null && !user.getProfileUrl().isEmpty()) {
            Glide.with(context).load(user.getProfileUrl()).placeholder(R.drawable.user).error(R.drawable.user).circleCrop().into(vh.onlineUserProfileImgList);
        } else {
            vh.onlineUserProfileImgList.setImageResource(R.drawable.user);
        }

        vh.userId = user.getUserId();
        vh.onlineUserNameList.setText(user.getUserName() != null ? user.getUserName() : "Player");

        if (user.isOnline()) {
            vh.greenLed.setVisibility(View.VISIBLE);
            vh.onlineUserStatusSub.setText("Online & Ready");
            vh.onlineUserStatusSub.setTextColor(context.getResources().getColor(R.color.success, null));
        } else {
            vh.greenLed.setVisibility(View.GONE);
            vh.onlineUserStatusSub.setText("Offline");
            vh.onlineUserStatusSub.setTextColor(context.getResources().getColor(R.color.gray_600, null));
        }

        vh.userCardPlayBtn.setOnClickListener(v -> {
            showAlertDialog(user);
        });
    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public String userId;
        public ImageView onlineUserProfileImgList;
        public TextView onlineUserNameList;
        public TextView onlineUserStatusSub;
        public MaterialButton userCardPlayBtn;
        public View greenLed;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            onlineUserProfileImgList = itemView.findViewById(R.id.onlineUserProfileImgList);
            onlineUserNameList = itemView.findViewById(R.id.onlineUserNameList);
            onlineUserStatusSub = itemView.findViewById(R.id.onlineUserStatusSub);
            userCardPlayBtn = itemView.findViewById(R.id.userCardPlayBtn);
            greenLed = itemView.findViewById(R.id.greenLed);
        }
    }

    private void showAlertDialog(User targetUser) {
        if (currUser == null || currUser.getUserId() == null || targetUser == null) return;
        DatabaseReference gameRef = database.getReference().child("Games").push();
        String gameId = gameRef.getKey();
        Game game = new Game();
        game.setGameId(gameId);
        game.setPlayerId1(currUser.getUserId());
        game.setPlayerId2(targetUser.getUserId());
        game.setActiveToken("1");
        game.setActiveUserId(currUser.getUserId());
        game.setActiveUserName(currUser.getUserName());
        game.setGrid("2,2,2,2,2,2,2,2,2");
        gameRef.setValue(game);

        String targetUserUserId = targetUser.getUserId();
        MyNotification myNotification = new MyNotification();
        myNotification.setSenderId(currUser.getUserId());
        myNotification.setSenderName(currUser.getUserName());
        myNotification.setReceiverId(targetUserUserId);
        myNotification.setAccepted(false);
        myNotification.setGameId(gameId);
        database.getReference("Notifications").child(targetUserUserId).setValue(myNotification);

        try {
            View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_challenge_sent, null);
            AlertDialog dialog = new AlertDialog.Builder(context).setView(dialogView).setCancelable(false).create();

            if (dialog.getWindow() != null) {
                dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
                dialog.getWindow().setDimAmount(0.20f);
            }

            TextView tvName = dialogView.findViewById(R.id.tvOpponentName);
            ImageView imgAvatar = dialogView.findViewById(R.id.imgOpponentAvatar);
            View btnCancel = dialogView.findViewById(R.id.btnCancelChallenge);

            if (tvName != null) {
                tvName.setText(targetUser.getUserName() != null ? targetUser.getUserName() : "Player");
            }

            if (imgAvatar != null && targetUser.getProfileUrl() != null && !targetUser.getProfileUrl().isEmpty()) {
                Glide.with(context).load(targetUser.getProfileUrl()).placeholder(R.drawable.user).error(R.drawable.user).circleCrop().into(imgAvatar);
            }

            if (btnCancel != null) {
                btnCancel.setOnClickListener(v -> {
                    database.getReference("Notifications").child(targetUserUserId).removeValue();
                    dialog.dismiss();
                });
            }

            myDialog = dialog;

            // Animate dialog pop-in
            try {
                Animation popAnim = AnimationUtils.loadAnimation(context, R.anim.splash_logo_pop);
                dialogView.startAnimation(popAnim);
            } catch (Exception ignored) {
            }

            dialog.show();
            startGame(targetUserUserId, gameId);
        } catch (Exception e) {
            e.printStackTrace();
            startGame(targetUserUserId, gameId);
        }
    }

    private void startGame(String targetUserUserId, String gameId) {
        if (currUser == null || currUser.getUserId() == null) return;

        DatabaseReference reference = database.getReference("Notifications").child(targetUserUserId).child("Accepted");
        listener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    Boolean accepted = snapshot.getValue(Boolean.class);
                    if (accepted != null && accepted) {
                        if (listener != null) {
                            reference.removeEventListener(listener);
                        }
                        if (myDialog != null) {
                            myDialog.dismiss();
                        }
                        Toast.makeText(context, "Match Accepted!", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(context, OnlineModeActivity.class);
                        intent.putExtra("GameId", gameId);
                        context.startActivity(intent);
                        database.getReference("Notifications").child(targetUserUserId).removeValue();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        };
        reference.addValueEventListener(listener);
    }
}
