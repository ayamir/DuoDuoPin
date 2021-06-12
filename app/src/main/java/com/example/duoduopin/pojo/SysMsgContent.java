package com.example.duoduopin.pojo;

import androidx.annotation.NonNull;

import com.google.gson.annotations.Expose;

public class SysMsgContent {
    @Expose
    private final String messageId;
    @Expose
    private final String receiverId;
    @Expose
    private final String senderId;
    @Expose
    private final String billId;
    @Expose
    private final String type;
    @Expose
    private String time;
    @Expose
    private final String content;
    private boolean isRead;

    public SysMsgContent(String messageId, String senderId, String receiverId, String billId, String type, String time, String content) {
        this.messageId = messageId;
        this.receiverId = receiverId;
        this.senderId = senderId;
        this.billId = billId;
        this.type = type;
        this.time = time;
        this.content = content;
    }

    public String getMessageId() {
        return messageId;
    }

    public String getSenderId() {
        return senderId;
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

    public String getReceiverId() {
        return receiverId;
    }

    public String getContent() {
        return content;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public boolean isRead() {
        return isRead;
    }

    public void setRead(boolean read) {
        isRead = read;
    }

    @NonNull
    @Override
    public String toString() {
        return "\nmessageId = " + messageId +
                "\nreceiverId = " + receiverId +
                "\nsenderId = " + senderId +
                "\nbillId = " + billId +
                "\ntype = " + type +
                "\ntime = " + time +
                "\ncontent = " + content +
                "\nisRead = " + isRead;
    }
}