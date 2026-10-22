package com.rituraj.tictactoeonlineapp.adapters;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.rituraj.tictactoeonlineapp.R;
import com.rituraj.tictactoeonlineapp.models.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class LeaderboardAdapter extends RecyclerView.Adapter<LeaderboardAdapter.LeaderboardViewHolder> {

    private List<Player> players;
    private List<Player> originalList;
    private final OnPlayerClickListener listener;

    public interface OnPlayerClickListener {
        void onPlayerClick(Player player);
    }

    public LeaderboardAdapter(OnPlayerClickListener listener) {
        this.players = new ArrayList<>();
        this.originalList = new ArrayList<>();
        this.listener = listener;
    }

    @NonNull
    @Override
    public LeaderboardViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_leaderboard_card, parent, false);
        return new LeaderboardViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull LeaderboardViewHolder holder, int position) {
        Player player = players.get(position);
        holder.bind(player, position + 1);
    }

    @Override
    public int getItemCount() {
        return players.size();
    }

    public void setPlayers(List<Player> playerList) {
        this.originalList = new ArrayList<>(playerList);
        this.players = new ArrayList<>(playerList);
        notifyDataSetChanged();
    }

    public void filter(String query) {
        if (query == null || query.isEmpty()) {
            players = new ArrayList<>(originalList);
        } else {
            List<Player> filtered = new ArrayList<>();
            String lowerQuery = query.toLowerCase(Locale.ROOT);
            for (Player player : originalList) {
                if ((player.getName() != null && player.getName().toLowerCase(Locale.ROOT).contains(lowerQuery)) ||
                        (player.getEmail() != null && player.getEmail().toLowerCase(Locale.ROOT).contains(lowerQuery))) {
                    filtered.add(player);
                }
            }
            players = filtered;
        }
        notifyDataSetChanged();
    }

    public void sortBy(String sortType) {
        List<Player> sorted = new ArrayList<>(players);
        if ("Wins".equalsIgnoreCase(sortType)) {
            sorted.sort((p1, p2) -> Integer.compare(p2.getWinCount(), p1.getWinCount()));
        } else if ("Level".equalsIgnoreCase(sortType)) {
            sorted.sort((p1, p2) -> Integer.compare(p2.getLevel(), p1.getLevel()));
        } else {
            // Default "Win Rate" or "Rank"
            sorted.sort((p1, p2) -> Double.compare(p2.getWinRate(), p1.getWinRate()));
        }
        players = sorted;
        notifyDataSetChanged();
    }

    class LeaderboardViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvRank;
        private final TextView tvPlayerName;
        private final TextView tvPlayerEmail;
        private final TextView tvWinRate;
        private final TextView tvLevel;
        private final TextView tvWinCount;
        private final TextView tvGameCount;
        private final TextView tvLastScore;
        private final ImageView ivMedal;
        private final CardView cardView;

        public LeaderboardViewHolder(@NonNull View itemView) {
            super(itemView);
            tvRank = itemView.findViewById(R.id.tvRank);
            tvPlayerName = itemView.findViewById(R.id.tvPlayerName);
            tvPlayerEmail = itemView.findViewById(R.id.tvPlayerEmail);
            tvWinRate = itemView.findViewById(R.id.tvWinRate);
            tvLevel = itemView.findViewById(R.id.tvLevel);
            tvWinCount = itemView.findViewById(R.id.tvWinCount);
            tvGameCount = itemView.findViewById(R.id.tvGameCount);
            tvLastScore = itemView.findViewById(R.id.tvLastScore);
            ivMedal = itemView.findViewById(R.id.ivMedal);
            cardView = (CardView) itemView;
        }

        void bind(Player player, int rank) {
            tvRank.setText(String.valueOf(rank));
            tvPlayerName.setText(player.getName() != null ? player.getName() : "Player");
            tvPlayerEmail.setText(player.getEmail() != null ? player.getEmail() : "");

            // Win rate
            double winRatePct = player.getWinRate() * 100;
            tvWinRate.setText(String.format(Locale.getDefault(), "🏆 %.1f%%", winRatePct));

            // Level
            tvLevel.setText(String.format(Locale.getDefault(), "⭐ Lvl %d", player.getLevel()));

            // Win/Loss stats
            tvWinCount.setText(String.format(Locale.getDefault(), "W: %d", player.getWinCount()));
            tvGameCount.setText(String.format(Locale.getDefault(), "G: %d", player.getGameCount()));

            // Last score
            tvLastScore.setText(String.format(Locale.getDefault(), "Last: %s", player.getLastScore() != null ? player.getLastScore() : "0"));

            // Medals & background highlights for top 3
            if (rank == 1) {
                ivMedal.setVisibility(View.VISIBLE);
                ivMedal.setImageResource(R.drawable.ic_gold_medal);
                tvRank.setBackgroundResource(R.drawable.rank_circle_gold);
                cardView.setCardBackgroundColor(Color.parseColor("#FFFBEB"));
            } else if (rank == 2) {
                ivMedal.setVisibility(View.VISIBLE);
                ivMedal.setImageResource(R.drawable.ic_silver_medal);
                tvRank.setBackgroundResource(R.drawable.rank_circle_silver);
                cardView.setCardBackgroundColor(Color.parseColor("#F8FAFC"));
            } else if (rank == 3) {
                ivMedal.setVisibility(View.VISIBLE);
                ivMedal.setImageResource(R.drawable.ic_bronze_medal);
                tvRank.setBackgroundResource(R.drawable.rank_circle_bronze);
                cardView.setCardBackgroundColor(Color.parseColor("#FFF7ED"));
            } else {
                ivMedal.setVisibility(View.GONE);
                tvRank.setBackgroundResource(R.drawable.rank_circle_bg);
                cardView.setCardBackgroundColor(Color.WHITE);
            }

            // Click listener
            cardView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onPlayerClick(player);
                }
            });
        }
    }
}
