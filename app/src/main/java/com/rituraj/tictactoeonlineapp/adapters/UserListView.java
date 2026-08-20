package com.rituraj.tictactoeonlineapp.adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.rituraj.tictactoeonlineapp.R;
import com.rituraj.tictactoeonlineapp.models.User;
import com.rituraj.tictactoeonlineapp.ui.games.OnlineModeActivity;

import java.util.ArrayList;

public class UserListView extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private Context context;
    private ArrayList<User> users;
    private FirebaseDatabase database;
    private DialogInterface myDialog;
    private User currUser;
    private ValueEventListener listener;

    public UserListView(Context context, ArrayList<User> users, User currUser) {
        this.context = context;
        this.users = users;
        database = FirebaseDatabase.getInstance();
        this.currUser = currUser;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.user_card_list, parent, false);
        return new UserListView.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        User user = users.get(position);
        ViewHolder vh = (ViewHolder) holder;

        Glide.with(context)
                .load(user.getProfileUrl())
                .placeholder(R.drawable.user)
                .error(R.drawable.user)
                .into(vh.onlineUserProfileImgList);
        vh.userId = user.getUserId();
        vh.onlineUserNameList.setText(user.getUserName());
        vh.userCardPlayBtn.setOnClickListener((v) -> {
            showAlertDialog(vh.userId, "Friend Request Sent", "Wait for him/her to accept.");
        });
        if (user.isOnline())
            vh.greenLed.setVisibility(View.VISIBLE);
        else
            vh.greenLed.setVisibility(View.GONE);
    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public String userId;
        public ImageView onlineUserProfileImgList;
        public TextView onlineUserNameList;
        public Button userCardPlayBtn;
        public View greenLed;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);
            onlineUserProfileImgList = itemView.findViewById(R.id.onlineUserProfileImgList);
            onlineUserNameList = itemView.findViewById(R.id.onlineUserNameList);
            userCardPlayBtn = itemView.findViewById(R.id.userCardPlayBtn);
            greenLed = itemView.findViewById(R.id.greenLed);
        }

        @Override
        public void onClick(View v) {
//            Intent intent = new Intent(context, OnlineModeActivity.class);
//            intent.putExtra("UserID", userId);
//            context.startActivity(intent);
        }
    }

    private void showAlertDialog(String userId, String title, String message) {
        database.getReference("FriendsInvitations").child(userId).setValue(currUser);
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(title);
        builder.setMessage(message);
        builder.setIcon(android.R.drawable.ic_dialog_alert);
        builder.setPositiveButton("Cancel", (dialog, which) -> {
            myDialog = dialog;
            database.getReference("FriendsInvitations").child(userId).removeValue();
            dialog.dismiss();
        });
        builder.setCancelable(false);
        builder.show();
        startGame(userId);
    }

    private void startGame(String userId) {
        DatabaseReference reference = database.getReference("GameRequest").child(currUser.getUserId()).child("Accepted");
        listener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    Boolean accepted = snapshot.getValue(Boolean.class);
                    if (accepted != null && accepted) {
                        if (listener != null)
                            reference.removeEventListener(listener);
                        if (myDialog != null)
                            myDialog.dismiss();
                        database.getReference("FriendsInvitations").child(userId).removeValue();
                        Toast.makeText(context, "Request Accepted", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(context, OnlineModeActivity.class);
                        intent.putExtra("UserID", userId);
                        context.startActivity(intent);
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
