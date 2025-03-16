package com.ravemaster.ravechat.models;

import java.util.Date;

public class ChatMessage {
    private String senderId, receiverId, message, time;
    private Date date;

    public ChatMessage(String senderId, String receiverId, String message, String time, Date date) {
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.message = message;
        this.time = time;
        this.date = date;
    }

    public String getSenderId() {
        return senderId;
    }

    public String getReceiverId() {
        return receiverId;
    }

    public String getMessage() {
        return message;
    }

    public String getTime() {
        return time;
    }

    public Date getDate() {
        return date;
    }

}
