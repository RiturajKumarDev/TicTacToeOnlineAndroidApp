package com.rituraj.tictactoeonlineapp.models;

public class Game {
    private String gameId;
    private String grid = "2,2,2,2,2,2,2,2,2", activeToken = "1", activeUserId, activeUserName;
    private String playerId1, playerId2;
    private String status = "PLAYING";
    private String leftPlayerId = "";
    private String rematchRequest = "";

    public Game() {
    }

    public String getRematchRequest() {
        return rematchRequest;
    }

    public void setRematchRequest(String rematchRequest) {
        this.rematchRequest = rematchRequest;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getLeftPlayerId() {
        return leftPlayerId;
    }

    public void setLeftPlayerId(String leftPlayerId) {
        this.leftPlayerId = leftPlayerId;
    }

    public String getGameId() {
        return gameId;
    }

    public void setGameId(String gameId) {
        this.gameId = gameId;
    }

    public String getActiveToken() {
        return activeToken;
    }

    public void setActiveToken(String activeToken) {
        this.activeToken = activeToken;
    }

    public String getGrid() {
        return grid;
    }

    public void setGrid(String grid) {
        this.grid = grid;
    }

    public String getActiveUserId() {
        return activeUserId;
    }

    public void setActiveUserId(String activeUserId) {
        this.activeUserId = activeUserId;
    }

    public String getActiveUserName() {
        return activeUserName;
    }

    public void setActiveUserName(String activeUserName) {
        this.activeUserName = activeUserName;
    }

    public String getPlayerId1() {
        return playerId1;
    }

    public void setPlayerId1(String playerId1) {
        this.playerId1 = playerId1;
    }

    public String getPlayerId2() {
        return playerId2;
    }

    public void setPlayerId2(String playerId2) {
        this.playerId2 = playerId2;
    }
}
