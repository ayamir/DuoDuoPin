package com.example.duoduopin.bean;

public class HomeContent {
    private final String nickname;
    private final String id;
    private final String title;
    private final String description;
    private final String currentNumber;

    public HomeContent(String nickname, String id, String title, String description, String currentNumber) {
        this.nickname = nickname;
        this.id = id;
        this.title = title;
        this.description = description;
        this.currentNumber = currentNumber;
    }

    public String getNickname() {
        return nickname;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getCurrentNumber() {
        return currentNumber;
    }

    public String getId() {
        return id;
    }

}
