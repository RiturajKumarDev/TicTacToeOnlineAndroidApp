package com.rituraj.tictactoeonlineapp.models;

public class Player {
    private String playerId, name, email;
    private String lastScore, coins;
    private int level;
    private int rank;
    private int winCount, gameCount;
    private Double winRate;

    public Player() {
    }

    public Player(String playerId, String email, String name, String coins) {
        this.playerId = playerId;
        this.name = name;
        this.email = email;
        this.coins = coins;
        this.level = 0;
        this.lastScore = "0";
        this.rank = 0;
        this.winCount = 0;
        this.gameCount = 0;
        this.winRate = 0.0;
    }

    public int getWinCount() {
        return winCount;
    }

    public void setWinCount(int winCount) {
        this.winCount = winCount;
    }

    public int getGameCount() {
        return gameCount;
    }

    public void setGameCount(int gameCount) {
        this.gameCount = gameCount;
    }

    public double getWinRate() {
        if (gameCount == 0) return 0;
        return (double) winCount / gameCount;
    }

    public String getPlayerId() {
        return playerId;
    }

    public void setPlayerId(String playerId) {
        this.playerId = playerId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getCoins() {
        return coins;
    }

    public void setCoins(String coins) {
        this.coins = coins;
    }

    public String getLastScore() {
        if (winCount != 0) return "1";
        return lastScore;
    }

    public void setLastScore(String lastScore) {
        this.lastScore = lastScore;
    }

    public int getLevel() {
        return gameCount / 10;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public int getRank() {
        return rank / 100;
    }

    public void setRank(int rank) {
        this.rank = rank;
    }
}
