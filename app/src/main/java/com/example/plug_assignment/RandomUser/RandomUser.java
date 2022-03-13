package com.example.plug_assignment.RandomUser;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class RandomUser {

    @SerializedName("results")
    @Expose
    private List<Results> results = null;

    private final static long serialVersionUID = -227557932098575252L;

    public List<Results> getResults() {
        return results;
    }

    public void setResults(List<Results> results) {
        this.results = results;
    }

    @Override
    public String toString() {
        return "RandomUser{" +
                "results=" + results +
                '}';
    }
}



