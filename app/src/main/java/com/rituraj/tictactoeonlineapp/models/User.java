package com.rituraj.tictactoeonlineapp.models;

import androidx.annotation.NonNull;

public class User {
    private String userId, userName, email, profileUrl;
    private boolean isOnline;

    public User() {
    }

    public User(String userName, String email, String profileUrl) {
        this.userName = userName;
        this.email = email;
        this.profileUrl = profileUrl;
    }

    public User(String userId, String userName, String email, String profileUrl) {
        this.userId = userId;
        this.userName = userName;
        this.email = email;
        this.profileUrl = profileUrl;
    }

    public boolean isOnline() {
        return isOnline;
    }

    public void setOnline(boolean online) {
        isOnline = online;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getProfileUrl() {
        return profileUrl;
    }

    public void setProfileUrl(String profileUrl) {
        this.profileUrl = profileUrl;
    }

    @NonNull
    public String toString() {
        return userName + "\n" + userId;
    }
}
