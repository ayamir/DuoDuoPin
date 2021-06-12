package com.example.duoduopin.pojo;

import androidx.annotation.Nullable;

public class GrpMsgDisplay {
    private final int picId;
    private final String content;
    private final String nickname;
    private final String time;
    private final boolean isMine;

    public GrpMsgDisplay(String content, int picId, String nickname, String time, boolean isMine) {
        this.picId = picId;
        this.content = content;
        this.nickname = nickname;
        this.time = time;
        this.isMine = isMine;
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

    public boolean isMine() {
        return isMine;
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (obj == this) {
            return true;
        }

        if (!(obj instanceof GrpMsgDisplay)) {
            return false;
        }
        GrpMsgDisplay other = (GrpMsgDisplay) obj;
        return (nickname.equals(other.getNickname())) && (content.equals(other.getContent())) && (time.equals(other.getTime())) && (picId == other.getPicId() && (isMine == other.isMine()));
    }
}
