package com.rituraj.tictactoeonlineapp.models;

public class Message {
    private String messageId, msg;

    public Message() {
    }

    public Message(String messageId, String msg) {
        this.messageId = messageId;
        this.msg = msg;
    }

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }
}
