package com.rituraj.tictactoeonlineapp.ui.invite;

import static android.content.Context.CLIPBOARD_SERVICE;

import androidx.lifecycle.ViewModelProvider;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.rituraj.tictactoeonlineapp.R;

public class InvitesFragment extends Fragment {
    private View view;
    private InvitesViewModel mViewModel;
    private FirebaseAuth auth;
    private String userId;

    public static InvitesFragment newInstance() {
        return new InvitesFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_invites, container, false);
        fun();
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = new ViewModelProvider(this).get(InvitesViewModel.class);
        // TODO: Use the ViewModel
    }

    private void fun() {
        auth = FirebaseAuth.getInstance();
        userId = auth.getCurrentUser().getUid();
        redeemCodeCopyBtn(userId);
    }

    private void redeemCodeCopyBtn(String userId) {
        TextView redeemCodeText = view.findViewById(R.id.redeemCodeText);
        Button redeemCodeCopyBtn = view.findViewById(R.id.redeemCodeCopyBtn);
        Button shareWhatappBtn = view.findViewById(R.id.shareWhatappBtn);
        Button shareTelegramBtn = view.findViewById(R.id.shareTelegramBtn);
        Button withdrawCoinsBtn = view.findViewById(R.id.withdrawCoinsBtn);
        redeemCodeText.setText(userId);
        redeemCodeCopyBtn.setOnClickListener((v) -> {
            String upiId = redeemCodeText.getText().toString();
            ClipboardManager clipboard = (ClipboardManager) requireContext().getSystemService(CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText("label", upiId);
            clipboard.setPrimaryClip(clip);
            Toast.makeText(requireContext(), upiId, Toast.LENGTH_SHORT).show();
        });
        shareWhatappBtn.setOnClickListener(v -> {
            String message = "🔥 Join me on Tic Tac Toe Online!\n"
                    + "Play with friends, have fun, and earn rewards. 💸\n\n"
                    + "Use my referral code: " + userId + "\n\n"
                    + "Download the game now:\nhttps://play.google.com/store/apps/details?id="
                    + requireActivity().getPackageName();
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("text/plain");
            intent.putExtra(Intent.EXTRA_TEXT, message);
            intent.setPackage("com.whatsapp");
            try {
                startActivity(intent);
            } catch (Exception e) {
                Toast.makeText(requireContext(), "WhatsApp not installed", Toast.LENGTH_SHORT).show();
            }
        });
        shareTelegramBtn.setOnClickListener(v -> {
            String message = "🔥 Join me on Tic Tac Toe Online!\n"
                    + "Play with friends, have fun, and earn rewards. 💸\n\n"
                    + "Use my referral code: " + userId + "\n\n"
                    + "Download the game now:\nhttps://play.google.com/store/apps/details?id="
                    + requireActivity().getPackageName();
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("text/plain");
            intent.putExtra(Intent.EXTRA_TEXT, message);
            intent.setPackage("org.telegram.messenger");
            try {
                startActivity(intent);
            } catch (Exception e) {
                Toast.makeText(requireContext(), "Telegram not installed", Toast.LENGTH_SHORT).show();
            }
        });
        withdrawCoinsBtn.setOnClickListener((v) -> {
            Toast.makeText(requireContext(), "Minimum 5000 coins required to withdraw!!", Toast.LENGTH_SHORT).show();
        });
    }


}