package com.bhola.livevideochat4.Models;

public class Chats_Modelclass {

    String message;
    String messageType;
    String extraMsg;
    String chatType;
    String profileUrl;
    String timeStamp;
    int viewType;//viewType 1 is sender 2 is receiver

    public Chats_Modelclass() {
    }

    public Chats_Modelclass(String message, String messageType, String extraMsg, String chatType, String profileUrl, String timeStamp, int viewType) {
        this.message = message;
        this.messageType = messageType;
        this.extraMsg = extraMsg;
        this.chatType = chatType;
        this.profileUrl = profileUrl;
        this.timeStamp = timeStamp;
        this.viewType = viewType;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getMessageType() {
        return messageType;
    }

    public void setMessageType(String messageType) {
        this.messageType = messageType;
    }

    public String getExtraMsg() {
        return extraMsg;
    }

    public void setExtraMsg(String extraMsg) {
        this.extraMsg = extraMsg;
    }

    public String getChatType() {
        return chatType;
    }

    public void setChatType(String chatType) {
        this.chatType = chatType;
    }

    public String getProfileUrl() {
        return profileUrl;
    }

    public void setProfileUrl(String profileUrl) {
        this.profileUrl = profileUrl;
    }

    public String getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(String timeStamp) {
        this.timeStamp = timeStamp;
    }

    public int getViewType() {
        return viewType;
    }

    public void setViewType(int viewType) {
        this.viewType = viewType;
    }
}

