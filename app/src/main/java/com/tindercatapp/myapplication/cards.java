package com.tindercatapp.myapplication;

public class cards {
    private String userId;
    private String name;
    private String profileImageUrl;
    private int age;
    private String location;

    public cards (String userId, String name, String profileImageUrl,int age, String location){
        this.userId = userId;
        this.name = name;
        this.profileImageUrl = profileImageUrl;
        this.age=age;
        this.location=location;
    }

    public String getUserId(){
        return userId;
    }
    public void setUserID(String userID){
        this.userId = userId;
    }

    public String getName(){
        return name;
    }
    public void setName(String name){
        this.name = name;
    }

    public String getProfileImageUrl(){
        return profileImageUrl;
    }

    public void setProfileImageUrl(String profileImageUrl){
        this.profileImageUrl = profileImageUrl;
    }

    public void setUserId(String userId) {
        this.userId = userId;
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

    @Override
    public String toString() {
        this.name = this.name .toUpperCase().charAt(0)+this.name .substring(1,this.name.length());
        String desc =this.name;
        if(age!=0){
            desc+=", "+age;
        }
        if(location!="" && location.length()>1){
            desc+=", "+location;
        }
        return desc;
    }
}