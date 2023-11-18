package com.bhola.livevideochat4.Models;

import java.util.ArrayList;


public class ChatItem_ModelClass {

    private String name;
    private String username;
    private String profileImage;
    private ArrayList<String> contentImages;
    private boolean containsQuestion;
    private String recommendationType;
    private ArrayList<UserBotMsg> userBotMsg;
    private UserQuestionWithAns questionWithAns;

    public ChatItem_ModelClass() {
    }

    public ChatItem_ModelClass(String name, String username, String profileImage, ArrayList<String> contentImages, boolean containsQuestion, String recommendationType, ArrayList<UserBotMsg> userBotMsg, UserQuestionWithAns questionWithAns) {
        this.name = name;
        this.username = username;
        this.profileImage = profileImage;
        this.contentImages = contentImages;
        this.containsQuestion = containsQuestion;
        this.recommendationType = recommendationType;
        this.userBotMsg = userBotMsg;
        this.questionWithAns = questionWithAns;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getProfileImage() {
        return profileImage;
    }

    public void setProfileImage(String profileImage) {
        this.profileImage = profileImage;
    }

    public ArrayList<String> getContentImages() {
        return contentImages;
    }

    public void setContentImages(ArrayList<String> contentImages) {
        this.contentImages = contentImages;
    }

    public boolean isContainsQuestion() {
        return containsQuestion;
    }

    public void setContainsQuestion(boolean containsQuestion) {
        this.containsQuestion = containsQuestion;
    }

    public String getRecommendationType() {
        return recommendationType;
    }

    public void setRecommendationType(String recommendationType) {
        this.recommendationType = recommendationType;
    }

    public ArrayList<UserBotMsg> getUserBotMsg() {
        return userBotMsg;
    }

    public void setUserBotMsg(ArrayList<UserBotMsg> userBotMsg) {
        this.userBotMsg = userBotMsg;
    }

    public UserQuestionWithAns getQuestionWithAns() {
        return questionWithAns;
    }

    public void setQuestionWithAns(UserQuestionWithAns questionWithAns) {
        this.questionWithAns = questionWithAns;
    }
}

