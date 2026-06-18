package com.nachiket.library_management.model;

public class User {

    private int userId;
    private String name;
    private String uniqueIdCard;

    public User() {
    }

    public User(int userId, String name, String uniqueIdCard) {
        this.userId = userId;
        this.name = name;
        this.uniqueIdCard = uniqueIdCard;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUniqueIdCard() {
        return uniqueIdCard;
    }

    public void setUniqueIdCard(String uniqueIdCard) {
        this.uniqueIdCard = uniqueIdCard;
    }
}