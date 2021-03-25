package com.example.duoduopin.bean;

public class GrpMsgContent {
    private final String userId;
    private final String billId;
    private final String billTitle;
    private final String nickname;
    private final String type;
    private final String time;
    private final String content;

    public GrpMsgContent(String userId, String billId, String billTitle, String nickname, String type, String time, String content) {
        this.userId = userId;
        this.billId = billId;
        this.billTitle = billTitle;
        this.nickname = nickname;
        this.type = type;
        this.time = time;
        this.content = content;
    }

    public String getNickname() {
        return nickname;
    }

    public String getBillId() {
        return billId;
    }

    public String getType() {
        return type;
    }

    public String getTime() {
        return time;
    }

    public String getContent() {
        return content;
    }

    public String getUserId() {
        return userId;
    }


    public String getBillTitle() {
        return billTitle;
    }

}
