package com.tindercatapp.myapplication.Matches;

public class MatchesObject {
    private String userId;
    private String name;
    private String profileImageUrl;
    private int age;
    private String location;

    public MatchesObject(String userId, String name, String profileImageUrl,int age,String location) {
        this.userId = userId;
        this.name = name;
        this.profileImageUrl = profileImageUrl;
        this.age=age;
        this.location=location;
    }

    public void setUserId(String userId) {
        this.userId = userId;

    }

    public String getUserId() {
        return userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getProfileImageUrl() {
        return profileImageUrl;
    }

    public void setProfileImageUrl(String profileImageUrl) {
        this.profileImageUrl = profileImageUrl;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }
}
