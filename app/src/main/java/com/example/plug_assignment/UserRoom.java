package com.example.plug_assignment;

public class UserRoom {
    String fullName,email,photoUrl,user_id;
    String incoming_user="",isAvailable="false",status="true";

    public UserRoom(){}

    public UserRoom(String fullName, String email, String photoUrl, String user_id, String incoming_user, String isAvailable, String status) {
        this.fullName = fullName;
        this.email = email;
        this.photoUrl = photoUrl;
        this.user_id = user_id;
        this.incoming_user = incoming_user;
        this.isAvailable = isAvailable;
        this.status = status;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public String getIncoming_user() {
        return incoming_user;
    }

    public void setIncoming_user(String incoming_user) {
        this.incoming_user = incoming_user;
    }

    public String getIsAvailable() {
        return isAvailable;
    }

    public void setIsAvailable(String isAvailable) {
        this.isAvailable = isAvailable;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "UserRoom{" +
                "fullName='" + fullName + '\'' +
                ", email='" + email + '\'' +
                ", photoUrl='" + photoUrl + '\'' +
                ", user_id='" + user_id + '\'' +
                ", incoming_user='" + incoming_user + '\'' +
                ", isAvailable='" + isAvailable + '\'' +
                ", status='" + status + '\'' +
                '}';
    }
}
