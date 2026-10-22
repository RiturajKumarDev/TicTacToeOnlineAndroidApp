package com.rituraj.tictactoeonlineapp.ui.fragments;

import static android.content.Context.CLIPBOARD_SERVICE;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.rituraj.tictactoeonlineapp.databinding.FragmentInvitesBinding;

public class InvitesFragment extends Fragment {

    private FragmentInvitesBinding binding;
    private FirebaseAuth auth;
    private String userId;

    public static InvitesFragment newInstance() {
        return new InvitesFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentInvitesBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        fun();
    }

    private void fun() {
        auth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser != null) {
            userId = currentUser.getUid();
            binding.redeemCodeText.setText(userId);
        } else {
            binding.redeemCodeText.setText("CASH123");
        }
        setUpListeners();
    }

    private void setUpListeners() {
        binding.redeemCodeCopyBtn.setOnClickListener(v -> {
            String code = binding.redeemCodeText.getText().toString();
            if (getContext() != null) {
                ClipboardManager clipboard = (ClipboardManager) requireContext().getSystemService(CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("Referral Code", code);
                clipboard.setPrimaryClip(clip);
                Toast.makeText(requireContext(), "Referral Code Copied!", Toast.LENGTH_SHORT).show();
            }
        });

        binding.shareWhatappBtn.setOnClickListener(v -> {
            String message = getShareMessage();
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("text/plain");
            intent.putExtra(Intent.EXTRA_TEXT, message);
            intent.setPackage("com.whatsapp");
            try {
                startActivity(intent);
            } catch (Exception e) {
                Intent chooser = Intent.createChooser(intent, "Share via");
                startActivity(chooser);
            }
        });

        binding.shareTelegramBtn.setOnClickListener(v -> {
            String message = getShareMessage();
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("text/plain");
            intent.putExtra(Intent.EXTRA_TEXT, message);
            intent.setPackage("org.telegram.messenger");
            try {
                startActivity(intent);
            } catch (Exception e) {
                Intent chooser = Intent.createChooser(intent, "Share via");
                startActivity(chooser);
            }
        });
    }

    private String getShareMessage() {
        String code = (userId != null && !userId.isEmpty()) ? userId : "CASH123";
        String pkgName = (getActivity() != null) ? getActivity().getPackageName() : "com.rituraj.tictactoeonlineapp";
        String appLink = "https://play.google.com/store/apps/details?id=" + pkgName;

        return "🎮 *Tic Tac Toe Pro AI - Challenge & Earn!* 🏆\n\n"
                + "Hey! Join me for exciting multiplayer matches, challenge smart AI bots, and win rewards! 💰\n\n"
                + "🎁 *Use My Referral Code:* " + code + "\n"
                + "✨ Get 100 Bonus Coins instantly on sign up!\n\n"
                + "👇 *Download & Play Now:*\n"
                + appLink;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
