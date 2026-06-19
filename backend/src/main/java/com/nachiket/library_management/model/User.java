package com.nachiket.library_management.model;

public class User {

    private Integer userId;
    private String name;
    private String uniqueIdCard;

    public User() {
    }

    public User(Integer userId, String name, String uniqueIdCard) {
        this.userId = userId;
        this.name = name;
        this.uniqueIdCard = uniqueIdCard;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
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