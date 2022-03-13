package com.example.plug_assignment.RetroFit;

import com.example.plug_assignment.RandomUser.RandomUser;

import retrofit2.Call;
import retrofit2.http.GET;

public interface ApiInterface {
    @GET("api/")
    Call<RandomUser> getTodos();
}
