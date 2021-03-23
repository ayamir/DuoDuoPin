package com.example.duoduopin.bean;

public class MessageContentBean {
    private final String messageId;
    private final String receiverId;
    private final String senderId;
    private final String billId;
    private final String type;
    private final String time;
    private final String content;

    public MessageContentBean(String messageId, String senderId, String receiverId, String billId, String type, String time, String content) {
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
}