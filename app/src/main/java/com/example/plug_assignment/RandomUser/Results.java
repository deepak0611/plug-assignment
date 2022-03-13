package com.example.plug_assignment.RandomUser;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class Results implements Serializable
{

    @SerializedName("gender")
    @Expose
    private String gender;
    @SerializedName("name")
    @Expose
    private Name name;

    @SerializedName("email")
    @Expose
    private String email;


    @SerializedName("picture")
    @Expose
    private UserPicture picture;

    private final static long serialVersionUID = -2160475164831394260L;

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public Name getName() {
        return name;
    }
    public void setName(Name name) {
        this.name = name;
    }
    public String getEmail() {
        return email;
    }
    public void setEmail(String email) {
        this.email = email;
    }
    public UserPicture getPicture() {
        return picture;
    }

    public void setPicture(UserPicture picture) {
        this.picture = picture;
    }

    @Override
    public String toString() {
        return "Results{" +
                "gender='" + gender + '\'' +
                ", name=" + name +
                ", email='" + email + '\'' +
                ", picture=" + picture +
                '}';
    }
}
