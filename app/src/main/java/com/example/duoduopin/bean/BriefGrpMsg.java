package com.example.duoduopin.bean;

import java.io.Serializable;

public class BriefGrpMsg implements Serializable {
    final String grpTitle;
    final String grpMsgOwnNickname;
    final String grpMsgContent;
    final String grpMsgTime;

    public BriefGrpMsg(String grpTitle, String grpMsgOwnNickname, String grpMsgContent, String grpMsgTime) {
        this.grpTitle = grpTitle;
        this.grpMsgOwnNickname = grpMsgOwnNickname;
        this.grpMsgContent = grpMsgContent;
        this.grpMsgTime = grpMsgTime;
    }

    public String getGrpTitle() {
        return grpTitle;
    }

    public String getGrpMsgOwnNickname() { return grpMsgOwnNickname; }

    public String getGrpMsgContent() {
        return grpMsgContent;
    }

    public String getGrpMsgTime() {
        return grpMsgTime;
    }
}
