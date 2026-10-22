package com.rituraj.tictactoeonlineapp.models;

public class Player {
    private String playerId, name, email;
    private String lastScore;
    private int level;
    private int rank;
    private int winCount, gameCount;
    private Double winRate = 0.0;

    // Separate attributes for Easy AI
    private int easyAiWinCount, easyAiGameCount;
    private Double easyAiWinRate = 0.0;

    // Separate attributes for Hard AI
    private int hardAiWinCount, hardAiGameCount;
    private Double hardAiWinRate = 0.0;

    // Separate attributes for Online Multiplayer
    private int onlineWinCount, onlineGameCount;
    private Double onlineWinRate = 0.0;

    public Player() {
    }

    public Player(String playerId, String email, String name, String coins) {
        this.playerId = playerId;
        this.name = name;
        this.email = email;
        this.level = 0;
        this.lastScore = "0";
        this.rank = 0;
        this.winCount = 0;
        this.gameCount = 0;
        this.winRate = 0.0;
        this.easyAiWinCount = 0;
        this.easyAiGameCount = 0;
        this.easyAiWinRate = 0.0;
        this.hardAiWinCount = 0;
        this.hardAiGameCount = 0;
        this.hardAiWinRate = 0.0;
        this.onlineWinCount = 0;
        this.onlineGameCount = 0;
        this.onlineWinRate = 0.0;
    }

    public int getOnlineWinCount() {
        return onlineWinCount;
    }

    public void setOnlineWinCount(int onlineWinCount) {
        this.onlineWinCount = onlineWinCount;
    }

    public int getOnlineGameCount() {
        return onlineGameCount;
    }

    public void setOnlineGameCount(int onlineGameCount) {
        this.onlineGameCount = onlineGameCount;
    }

    public double getOnlineWinRate() {
        if (onlineGameCount == 0) return 0.0;
        return (double) onlineWinCount / onlineGameCount;
    }

    public void setOnlineWinRate(Double onlineWinRate) {
        this.onlineWinRate = onlineWinRate;
    }

    public int getEasyAiWinCount() {
        return easyAiWinCount;
    }

    public void setEasyAiWinCount(int easyAiWinCount) {
        this.easyAiWinCount = easyAiWinCount;
    }

    public int getEasyAiGameCount() {
        return easyAiGameCount;
    }

    public void setEasyAiGameCount(int easyAiGameCount) {
        this.easyAiGameCount = easyAiGameCount;
    }

    public double getEasyAiWinRate() {
        if (easyAiGameCount == 0) return 0.0;
        return (double) easyAiWinCount / easyAiGameCount;
    }

    public void setEasyAiWinRate(Double easyAiWinRate) {
        this.easyAiWinRate = easyAiWinRate;
    }

    public int getHardAiWinCount() {
        return hardAiWinCount;
    }

    public void setHardAiWinCount(int hardAiWinCount) {
        this.hardAiWinCount = hardAiWinCount;
    }

    public int getHardAiGameCount() {
        return hardAiGameCount;
    }

    public void setHardAiGameCount(int hardAiGameCount) {
        this.hardAiGameCount = hardAiGameCount;
    }

    public double getHardAiWinRate() {
        if (hardAiGameCount == 0) return 0.0;
        return (double) hardAiWinCount / hardAiGameCount;
    }

    public void setHardAiWinRate(Double hardAiWinRate) {
        this.hardAiWinRate = hardAiWinRate;
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
        if (gameCount == 0) return 0.0;
        return (double) winCount / gameCount;
    }

    public void setWinRate(Double winRate) {
        this.winRate = winRate;
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
