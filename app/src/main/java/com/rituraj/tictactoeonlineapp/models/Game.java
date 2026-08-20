package com.rituraj.tictactoeonlineapp.models;

public class Game {
    private String grid = "2,2,2,2,2,2,2,2,2", activeToken = "1", activeUserId, activeUserName;

    public Game() {
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
}
