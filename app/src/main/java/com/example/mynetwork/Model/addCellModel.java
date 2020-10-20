package com.example.mynetwork.Model;

import com.google.gson.annotations.SerializedName;

public class addCellModel {
    @SerializedName("message")
    private String message;
    @SerializedName("success")
    private boolean success;

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }
}
