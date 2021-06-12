package com.example.duoduopin.pojo;

public class BriefOrderContent {
    private final String nickname;
    private final String title;
    private final String description;
    private final String currentNumber;

    public BriefOrderContent(String nickname, String title, String description, String currentNumber) {
        this.nickname = nickname;
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
}
