package com.example.duoduopin.bean;

public class GrpMsgDisplay {
    private final String content;
    private final int picId;
    private final String nickname;
    private final String time;

    public GrpMsgDisplay(String content, int picId, String nickname, String time) {
        this.content = content;
        this.picId = picId;
        this.nickname = nickname;
        this.time = time;
    }

    public int getPicId() {
        return picId;
    }

    public String getContent() {
        return content;
    }

    public String getNickname() {
        return nickname;
    }

    public String getTime() {
        return time;
    }
}
