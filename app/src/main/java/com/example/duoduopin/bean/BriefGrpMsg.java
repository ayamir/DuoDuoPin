package com.example.duoduopin.bean;

public class BriefGrpMsg {
    final String grpTitle;
    final String grpMsgContent;
    final String grpMsgTime;

    public BriefGrpMsg(String grpTitle, String grpMsgContent, String grpMsgTime) {
        this.grpTitle = grpTitle;
        this.grpMsgContent = grpMsgContent;
        this.grpMsgTime = grpMsgTime;
    }

    public String getGrpTitle() {
        return grpTitle;
    }

    public String getGrpMsgContent() {
        return grpMsgContent;
    }

    public String getGrpMsgTime() {
        return grpMsgTime;
    }
}
