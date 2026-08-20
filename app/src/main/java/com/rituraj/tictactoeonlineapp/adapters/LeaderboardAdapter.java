package com.rituraj.tictactoeonlineapp.adapters;

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

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class LeaderboardAdapter extends RecyclerView.Adapter<LeaderboardAdapter.LeaderboardViewHolder> {

    private List<Player> players;
    private List<Player> originalList;
    private OnPlayerClickListener listener;
    private DecimalFormat decimalFormat = new DecimalFormat("#.##");

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
        if (query.isEmpty()) {
            players = new ArrayList<>(originalList);
        } else {
            List<Player> filtered = new ArrayList<>();
            for (Player player : originalList) {
                if (player.getName().toLowerCase().contains(query.toLowerCase()) ||
                        player.getEmail().toLowerCase().contains(query.toLowerCase())) {
                    filtered.add(player);
                }
            }
            players = filtered;
        }
        notifyDataSetChanged();
    }

    public void sortBy(String sortType) {
        List<Player> sorted = new ArrayList<>(players);
        switch (sortType) {
            case "Rank":
                // Already sorted by rank
                break;
            case "Win Rate":
                sorted.sort((p1, p2) -> Double.compare(
                        p2.getWinRate(), p1.getWinRate()));
                break;
            case "Coins":
                sorted.sort((p1, p2) -> Integer.compare(
                        Integer.parseInt(p2.getCoins()),
                        Integer.parseInt(p1.getCoins())));
                break;
            case "Level":
                sorted.sort((p1, p2) -> Integer.compare(p2.getLevel(), p1.getLevel()));
                break;
        }
        players = sorted;
        notifyDataSetChanged();
    }

    class LeaderboardViewHolder extends RecyclerView.ViewHolder {
        private TextView tvRank, tvPlayerName, tvPlayerEmail, tvWinRate, tvLevel;
        private TextView tvCoins, tvWinCount, tvGameCount, tvLastScore;
        private ImageView ivMedal;
        private CardView cardView;

        public LeaderboardViewHolder(@NonNull View itemView) {
            super(itemView);
            tvRank = itemView.findViewById(R.id.tvRank);
            tvPlayerName = itemView.findViewById(R.id.tvPlayerName);
            tvPlayerEmail = itemView.findViewById(R.id.tvPlayerEmail);
            tvWinRate = itemView.findViewById(R.id.tvWinRate);
            tvLevel = itemView.findViewById(R.id.tvLevel);
            tvCoins = itemView.findViewById(R.id.tvCoins);
            tvWinCount = itemView.findViewById(R.id.tvWinCount);
            tvGameCount = itemView.findViewById(R.id.tvGameCount);
            tvLastScore = itemView.findViewById(R.id.tvLastScore);
            ivMedal = itemView.findViewById(R.id.ivMedal);
            cardView = (CardView) itemView;
        }

        void bind(Player player, int rank) {
            tvRank.setText(String.valueOf(rank));
            tvPlayerName.setText(player.getName());
            tvPlayerEmail.setText(player.getEmail());

            // Win rate
            double winRate = player.getWinRate() * 100;
            tvWinRate.setText(String.format("🏆 %.1f%%", winRate));

            // Level
            tvLevel.setText(String.format("⭐ Lvl %d", player.getLevel()));

            // Coins
            tvCoins.setText(formatCoins(player.getCoins()));

            // Win/Loss stats
            tvWinCount.setText(String.format("W: %d", player.getWinCount()));
            tvGameCount.setText(String.format("G: %d", player.getGameCount()));

            // Last score
            tvLastScore.setText(String.format("Last: %s", player.getLastScore()));

            // Medal for top 3
            if (rank == 1) {
                ivMedal.setVisibility(View.VISIBLE);
                ivMedal.setImageResource(R.drawable.ic_gold_medal);
                tvRank.setBackgroundResource(R.drawable.rank_circle_gold);
            } else if (rank == 2) {
                ivMedal.setVisibility(View.VISIBLE);
                ivMedal.setImageResource(R.drawable.ic_silver_medal);
                tvRank.setBackgroundResource(R.drawable.rank_circle_silver);
            } else if (rank == 3) {
                ivMedal.setVisibility(View.VISIBLE);
                ivMedal.setImageResource(R.drawable.ic_bronze_medal);
                tvRank.setBackgroundResource(R.drawable.rank_circle_bronze);
            } else {
                ivMedal.setVisibility(View.GONE);
                tvRank.setBackgroundResource(R.drawable.rank_circle_bg);
            }

            // Click listener
            cardView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onPlayerClick(player);
                }
            });
        }

        private String formatCoins(String coins) {
            try {
                int coinValue = Integer.parseInt(coins);
                if (coinValue >= 1000) {
                    return String.format("%.1fK", coinValue / 1000.0);
                }
                return String.valueOf(coinValue);
            } catch (NumberFormatException e) {
                return coins;
            }
        }
    }
}
