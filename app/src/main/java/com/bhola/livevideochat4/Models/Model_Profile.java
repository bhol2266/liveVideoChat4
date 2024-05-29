package com.bhola.livevideochat4.Models;

import java.util.List;
import java.util.Map;

public class Model_Profile {

    private String username;
    private String name;
    private String from;
    private String languages;
    private String age;
    private String interested;
    private String bodyType;
    private String specifics;
    private String ethnicity;
    private String hair;
    private String eyeColor;
    private String subculture;
    private String profilePhoto;
    private String coverPhoto;
    private List<Map<String, String>> interestArrayList;
    private List<String> images;
    private List<Map<String, String>> videos;
    private int censored;
    private int like;
    private int selectedBot;


    public Model_Profile() {
    }

    public Model_Profile(String username, String name, String from, String languages, String age, String interested, String bodyType, String specifics, String ethnicity, String hair, String eyeColor, String subculture, String profilePhoto, String coverPhoto, List<Map<String, String>> interestArrayList, List<String> images, List<Map<String, String>> videos, int censored, int like, int selectedBot) {
        this.username = username;
        this.name = name;
        this.from = from;
        this.languages = languages;
        this.age = age;
        this.interested = interested;
        this.bodyType = bodyType;
        this.specifics = specifics;
        this.ethnicity = ethnicity;
        this.hair = hair;
        this.eyeColor = eyeColor;
        this.subculture = subculture;
        this.profilePhoto = profilePhoto;
        this.coverPhoto = coverPhoto;
        this.interestArrayList = interestArrayList;
        this.images = images;
        this.videos = videos;
        this.censored = censored;
        this.like = like;
        this.selectedBot = selectedBot;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getLanguages() {
        return languages;
    }

    public void setLanguages(String languages) {
        this.languages = languages;
    }

    public String getAge() {
        return age;
    }

    public void setAge(String age) {
        this.age = age;
    }

    public String getInterested() {
        return interested;
    }

    public void setInterested(String interested) {
        this.interested = interested;
    }

    public String getBodyType() {
        return bodyType;
    }

    public void setBodyType(String bodyType) {
        this.bodyType = bodyType;
    }

    public String getSpecifics() {
        return specifics;
    }

    public void setSpecifics(String specifics) {
        this.specifics = specifics;
    }

    public String getEthnicity() {
        return ethnicity;
    }

    public void setEthnicity(String ethnicity) {
        this.ethnicity = ethnicity;
    }

    public String getHair() {
        return hair;
    }

    public void setHair(String hair) {
        this.hair = hair;
    }

    public String getEyeColor() {
        return eyeColor;
    }

    public void setEyeColor(String eyeColor) {
        this.eyeColor = eyeColor;
    }

    public String getSubculture() {
        return subculture;
    }

    public void setSubculture(String subculture) {
        this.subculture = subculture;
    }

    public String getProfilePhoto() {
        return profilePhoto;
    }

    public void setProfilePhoto(String profilePhoto) {
        this.profilePhoto = profilePhoto;
    }

    public String getCoverPhoto() {
        return coverPhoto;
    }

    public void setCoverPhoto(String coverPhoto) {
        this.coverPhoto = coverPhoto;
    }

    public List<Map<String, String>> getInterestArrayList() {
        return interestArrayList;
    }

    public void setInterestArrayList(List<Map<String, String>> interestArrayList) {
        this.interestArrayList = interestArrayList;
    }

    public List<String> getImages() {
        return images;
    }

    public void setImages(List<String> images) {
        this.images = images;
    }

    public List<Map<String, String>> getVideos() {
        return videos;
    }

    public void setVideos(List<Map<String, String>> videos) {
        this.videos = videos;
    }

    public int getCensored() {
        return censored;
    }

    public void setCensored(int censored) {
        this.censored = censored;
    }

    public int getLike() {
        return like;
    }

    public void setLike(int like) {
        this.like = like;
    }

    public int getSelectedBot() {
        return selectedBot;
    }

    public void setSelectedBot(int selectedBot) {
        this.selectedBot = selectedBot;
    }
}
