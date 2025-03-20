package com.ravemaster.ravechat.api.models;

public class FCMMessage {
    private Message message;

    public FCMMessage(Message message) {
        this.message = message;
    }

    public Message getMessage() {
        return message;
    }

    public void setMessage(Message message) {
        this.message = message;
    }
}
